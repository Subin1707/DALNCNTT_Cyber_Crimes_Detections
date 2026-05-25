package com.example.servingwebcontent;

import com.example.servingwebcontent.dto.GraphLinkDTO;
import com.example.servingwebcontent.dto.GraphNodeDTO;
import com.example.servingwebcontent.service.MultiEntityGraphAnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multi-Entity Graph Analysis Tests")
class MultiEntityGraphAnalysisServiceTest {

    private final MultiEntityGraphAnalysisService service = new MultiEntityGraphAnalysisService();

    @Test
    @DisplayName("Should keep unsupported node type outside regions")
    void shouldKeepUnsupportedNodeTypeOutsideRegions() {
        GraphNodeDTO sessionNode = new GraphNodeDTO(
                "session-1", "session-1", "AnalysisSession", "session-1",
                "valid", "low", 10, "AN TOAN", List.of("created_from_import")
        );
        GraphNodeDTO emailNode = new GraphNodeDTO(
                "email-1", "session-1", "Email", "user@example.com",
                "valid", "low", 10, "AN TOAN", List.of("normal")
        );

        service.enrich(
                new ArrayList<>(List.of(sessionNode, emailNode)),
                List.of(new GraphLinkDTO("session-1", "email-1", "HAS_EMAIL"))
        );

        assertEquals("OUTSIDE", sessionNode.getMembershipStatus());
        assertEquals("collect_more_data", sessionNode.getRecommendedAction());
        assertTrue(sessionNode.getGraphReasons().stream()
                .anyMatch(reason -> reason.contains("chua duoc ho tro")));
    }

    @Test
    @DisplayName("Should classify isolated clean low-risk node by its closest safe domain")
    void shouldClassifyIsolatedCleanLowRiskNodeByClosestSafeDomain() {
        GraphNodeDTO node = new GraphNodeDTO(
                "email-2", "session-1", "Email", "isolated@example.com",
                "valid", "low", 0, "AN TOAN", List.of("normal")
        );

        service.enrich(List.of(node), List.of());

        assertEquals(0, node.getDegree());
        assertEquals("IN_REGION", node.getMembershipStatus());
        assertEquals("safe", node.getDomainDistances().entrySet().stream()
                .min((left, right) -> Double.compare(left.getValue(), right.getValue()))
                .orElseThrow()
                .getKey());
        assertTrue(node.getGraphReasons().stream()
                .anyMatch(reason -> reason.contains("khong ep OUTSIDE")));
    }

    @Test
    @DisplayName("Should classify isolated high-risk node as boundary by domain features")
    void shouldClassifyIsolatedHighRiskNodeAsBoundaryByDomainFeatures() {
        GraphNodeDTO node = new GraphNodeDTO(
                "email-risky-isolated", "session-1", "Email", "risk@example.com",
                "suspicious", "high", 75, "DANG NGHI", List.of("suspicious")
        );

        service.enrich(List.of(node), List.of());

        assertEquals(0, node.getDegree());
        assertEquals("BOUNDARY", node.getMembershipStatus());
        assertEquals("manual_review", node.getRecommendedAction());
    }

    @Test
    @DisplayName("Should keep weak context-only artifact outside regions")
    void shouldKeepWeakContextOnlyArtifactOutsideRegions() {
        GraphNodeDTO emailNode = new GraphNodeDTO(
                "email-3", "session-1", "Email", "user@example.com",
                "valid", "low", 10, "AN TOAN", List.of("normal")
        );
        GraphNodeDTO ipNode = new GraphNodeDTO(
                "ip-1", "session-1", "IPAddress", "203.0.113.10",
                "valid", "low", 10, "AN TOAN", List.of()
        );

        service.enrich(
                new ArrayList<>(List.of(emailNode, ipNode)),
                List.of(new GraphLinkDTO("email-3", "ip-1", "RELATED"))
        );

        assertEquals(1, ipNode.getDegree());
        assertNotEquals("OUTSIDE", ipNode.getMembershipStatus());
        assertEquals("safe", ipNode.getDomainDistances().entrySet().stream()
                .min((left, right) -> Double.compare(left.getValue(), right.getValue()))
                .orElseThrow()
                .getKey());
    }

    @Test
    @DisplayName("Should not create overlap only because nodes share capture session")
    void shouldNotCreateOverlapOnlyBecauseNodesShareCaptureSession() {
        GraphNodeDTO session = new GraphNodeDTO(
                "capture-session", "NETWORK_CAPTURE", "AnalysisSession", "NETWORK_CAPTURE",
                "valid", "low", 0, "AN TOAN", List.of("network_capture")
        );
        GraphNodeDTO ip = new GraphNodeDTO(
                "capture-ip", "NETWORK_CAPTURE", "IPAddress", "203.0.113.8",
                "valid", "low", 5, "AN TOAN", List.of()
        );
        GraphNodeDTO url = new GraphNodeDTO(
                "capture-url", "NETWORK_CAPTURE", "URL", "https://example.test",
                "valid", "low", 5, "AN TOAN", List.of()
        );
        List<GraphLinkDTO> links = new ArrayList<>(List.of(
                new GraphLinkDTO("capture-session", "capture-ip", "HAS_IP"),
                new GraphLinkDTO("capture-session", "capture-url", "HAS_URL")
        ));

        service.enrich(new ArrayList<>(List.of(session, ip, url)), links);

        assertEquals(0, ip.getOverlapScore());
        assertEquals(0, url.getOverlapScore());
        assertTrue(links.stream().noneMatch(link -> "OVERLAP".equals(link.getType())));
    }

    @Test
    @DisplayName("Should detect domain community from strong overlap")
    void shouldDetectDomainCommunityFromStrongOverlap() {
        GraphNodeDTO session = new GraphNodeDTO(
                "session-strong", "session-strong", "AnalysisSession", "session-strong",
                "valid", "low", 10, "AN TOAN", List.of("created_from_import")
        );
        GraphNodeDTO email = new GraphNodeDTO(
                "email-strong", "session-strong", "Email", "fraud@example.com",
                "suspicious", "medium", 55, "DANG NGHI", List.of("same_device", "same_ip")
        );
        GraphNodeDTO account = new GraphNodeDTO(
                "account-strong", "session-strong", "VictimAccount", "acct-001",
                "suspicious", "medium", 55, "DANG NGHI", List.of("same_device", "same_ip")
        );
        GraphNodeDTO device = new GraphNodeDTO(
                "device-1", "session-strong", "Device", "device-001",
                "valid", "low", 10, "AN TOAN", List.of("device_fingerprint")
        );
        GraphNodeDTO ip = new GraphNodeDTO(
                "ip-strong", "session-strong", "IPAddress", "198.51.100.10",
                "suspicious", "medium", 45, "DANG NGHI", List.of("suspicious")
        );

        List<GraphLinkDTO> links = new ArrayList<>(List.of(
                new GraphLinkDTO("email-strong", "device-1", "RELATED"),
                new GraphLinkDTO("account-strong", "device-1", "RELATED"),
                new GraphLinkDTO("email-strong", "ip-strong", "SENT_FROM_IP"),
                new GraphLinkDTO("account-strong", "ip-strong", "HAS_IP")
        ));

        service.enrich(
                new ArrayList<>(List.of(session, email, account, device, ip)),
                links
        );

        assertEquals("IN_REGION", email.getMembershipStatus());
        assertEquals("IN_REGION", account.getMembershipStatus());
        assertNotNull(email.getCommunityId());
        assertEquals(email.getCommunityId(), account.getCommunityId());
        assertTrue(email.getAdjustedOverlapScore() >= 0.30);
        assertTrue(links.stream().anyMatch(link -> "OVERLAP".equals(link.getType()) && link.getOverlapScore() > 0));
        assertTrue(email.getGraphReasons().stream().anyMatch(reason -> reason.contains("Overlap raw")));
    }

    @Test
    @DisplayName("Should mark multi-domain overlap without changing base membership")
    void shouldMarkMultiDomainOverlapWithoutChangingBaseMembership() {
        GraphNodeDTO email = new GraphNodeDTO(
                "email-boundary", "session-overlap", "Email", "user@example.com",
                "suspicious", "medium", 55, "DANG NGHI", List.of("same_ip")
        );
        GraphNodeDTO account = new GraphNodeDTO(
                "account-fraud", "session-overlap", "VictimAccount", "acct-fraud",
                "fake", "high", 90, "GIAN LAN", List.of("fraud_account")
        );
        GraphNodeDTO ip = new GraphNodeDTO(
                "ip-shared", "session-overlap", "IPAddress", "198.51.100.77",
                "suspicious", "medium", 45, "DANG NGHI", List.of("shared_ip")
        );

        List<GraphLinkDTO> links = new ArrayList<>(List.of(
                new GraphLinkDTO("email-boundary", "ip-shared", "HAS_IP"),
                new GraphLinkDTO("account-fraud", "ip-shared", "HAS_IP")
        ));

        service.enrich(new ArrayList<>(List.of(email, account, ip)), links);

        assertNotEquals("OUTSIDE", email.getMembershipStatus());
        assertTrue(email.isMultiDomainOverlap());
        assertTrue(email.isBridgeNode());
        assertEquals("BRIDGE", email.getInfluenceZone());
        assertTrue(email.getDomainDistances().containsKey("safe"));
        assertTrue(email.getDomainDistances().containsKey("suspicious"));
        assertTrue(email.getDomainDistances().containsKey("fraud"));
        assertTrue(email.getSoftMemberships().containsKey("suspicious"));
        assertTrue(email.getDomainInfluence().containsKey("fraud"));
        assertTrue(email.getDomainInfluence().get("fraud") > 0.0);
        assertTrue(email.getFeatureVector().containsKey("overlap"));
    }

    @Test
    @DisplayName("Should normalize known super node overlap")
    void shouldNormalizeKnownSuperNodeOverlap() {
        List<GraphNodeDTO> nodes = new ArrayList<>();
        List<GraphLinkDTO> links = new ArrayList<>();
        GraphNodeDTO domain = new GraphNodeDTO(
                "domain-google", "session-super", "Domain", "google.com",
                "valid", "low", 10, "AN TOAN", List.of("popular_domain")
        );
        nodes.add(domain);

        for (int i = 0; i < 40; i++) {
            GraphNodeDTO email = new GraphNodeDTO(
                    "email-super-" + i, "session-super", "Email", "user" + i + "@example.com",
                    "valid", "low", 10, "AN TOAN", List.of("normal")
            );
            nodes.add(email);
            links.add(new GraphLinkDTO(email.getId(), "domain-google", "HOSTED_ON"));
        }

        service.enrich(nodes, links);

        assertTrue(domain.isSuperNode());
        assertEquals("sampling", domain.getRecommendedAction());
        assertTrue(domain.getGraphReasons().stream()
                .anyMatch(reason -> reason.contains("Super node")));
    }
}
