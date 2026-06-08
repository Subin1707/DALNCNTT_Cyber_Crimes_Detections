package com.example.servingwebcontent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PacketCaptureService {

    private static final String SYSTEM_CAPTURE_SESSION_ID = "NETWORK_CAPTURE_SYSTEM";
    private static final String SYSTEM_CAPTURE_OWNER = "system@network-capture.local";

    private final FraudAnalysisService fraudAnalysisService;
    private final String tsharkPath;
    private final String captureInterface;
    private final AtomicLong totalLines = new AtomicLong(0);
    private final AtomicLong totalRecords = new AtomicLong(0);
    private final AtomicLong totalSaved = new AtomicLong(0);
    private volatile Instant lastSavedAt = null;
    private volatile String activeSessionId = null;
    private volatile String activeCreatedBy = null;
    private final Deque<Map<String, Object>> recentEvents = new ConcurrentLinkedDeque<>();

    public PacketCaptureService(FraudAnalysisService fraudAnalysisService,
                                @Value("${packet.capture.tshark-path:E:\\Program Files (x86)\\tshark.exe}") String tsharkPath,
                                @Value("${packet.capture.interface:5}") String captureInterface) {
        this.fraudAnalysisService = fraudAnalysisService;
        this.tsharkPath = tsharkPath;
        this.captureInterface = captureInterface;
    }

    public void activateCustomerCapture(String customerEmail, String httpSessionId) {
        if (customerEmail == null || customerEmail.isBlank()
                || httpSessionId == null || httpSessionId.isBlank()) {
            return;
        }
        this.activeCreatedBy = customerEmail.trim().toLowerCase();
        this.activeSessionId = "NETWORK_CAPTURE_" + httpSessionId.trim();
    }

    public void deactivateCustomerCapture(String httpSessionId) {
        if (httpSessionId == null || httpSessionId.isBlank()) {
            return;
        }
        String expectedSessionId = "NETWORK_CAPTURE_" + httpSessionId.trim();
        if (expectedSessionId.equals(activeSessionId)) {
            this.activeSessionId = null;
            this.activeCreatedBy = null;
        }
    }

    public void startCapture() {
        try {

            ProcessBuilder pb = new ProcessBuilder(
                    tsharkPath,
                    "-l",
                    "-i", captureInterface,
                    "-n",
                    "-Y", "dns.qry.name || tls.handshake.extensions_server_name || http.request",
                    "-T", "fields",
                    "-e", "ip.src",
                    "-e", "ipv6.src",
                    "-e", "dns.qry.name",
                    "-e", "tls.handshake.extensions_server_name",
                    "-e", "http.host",
                    "-e", "http.request.uri",
                    "-e", "http.request.full_uri"
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            System.out.println("[PacketCapture] tshark started on interface " + captureInterface
                    + ", waiting for DNS / TLS SNI / HTTP requests...");

            while ((line = reader.readLine()) != null) {
                totalLines.incrementAndGet();

                String[] parts = line.split("\\t", -1);
                if (parts.length < 2) continue;

                String ipv4 = parts.length > 0 && parts[0] != null ? parts[0].trim() : "";
                String ipv6 = parts.length > 1 && parts[1] != null ? parts[1].trim() : "";
                String dnsNames = parts.length > 2 && parts[2] != null ? parts[2].trim() : "";
                String sniNames = parts.length > 3 && parts[3] != null ? parts[3].trim() : "";
                String httpHost = parts.length > 4 && parts[4] != null ? parts[4].trim() : "";
                String httpUri = parts.length > 5 && parts[5] != null ? parts[5].trim() : "";
                String httpFullUri = parts.length > 6 && parts[6] != null ? parts[6].trim() : "";

                String ip = !ipv4.isBlank() ? ipv4 : ipv6;
                if (ip.isBlank()) continue;

                // DNS domains
                if (!dnsNames.isBlank()) {
                    for (String domain : dnsNames.split(",")) {
                        String d = domain.trim();
                        if (d.isEmpty()) continue;

                        totalRecords.incrementAndGet();
                        try {
                            saveNetworkConnection("DNS", ip, d);
                        } catch (Exception ex) {
                            System.err.println("[PacketCapture] save DNS failed: " + ex.getMessage());
                        }
                    }
                }

                // TLS SNI domains (HTTPS)
                if (!sniNames.isBlank()) {
                    for (String domain : sniNames.split(",")) {
                        String d = domain.trim();
                        if (d.isEmpty()) continue;

                        totalRecords.incrementAndGet();
                        try {
                            saveNetworkConnection("SNI", ip, d);
                        } catch (Exception ex) {
                            System.err.println("[PacketCapture] save SNI failed: " + ex.getMessage());
                        }
                    }
                }

                // HTTP requests (port 80 only; HTTPS does not expose path)
                String bestUrl = null;
                if (!httpFullUri.isBlank()) {
                    bestUrl = httpFullUri;
                } else if (!httpHost.isBlank()) {
                    String uri = httpUri == null ? "" : httpUri.trim();
                    if (uri.isBlank()) uri = "/";
                    bestUrl = "http://" + httpHost + uri;
                }

                if (bestUrl != null && !bestUrl.isBlank()) {
                    totalRecords.incrementAndGet();
                    try {
                        saveNetworkUrlVisit(ip, bestUrl);
                    } catch (Exception ex) {
                        System.err.println("[PacketCapture] save HTTP failed: " + ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getTotalLines() {
        return totalLines.get();
    }

    public long getTotalRecords() {
        return totalRecords.get();
    }

    public long getTotalSaved() {
        return totalSaved.get();
    }

    public String getLastSavedAt() {
        return lastSavedAt == null ? null : lastSavedAt.toString();
    }

    public List<Map<String, Object>> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }

    private void saveNetworkConnection(String kind, String ip, String domain) {
        int savedTargets = 0;
        for (CaptureTarget target : captureTargets()) {
            fraudAnalysisService.addNetworkConnection(ip, domain, target.sessionId(), target.createdBy());
            savedTargets++;
        }
        totalSaved.addAndGet(savedTargets);
        lastSavedAt = Instant.now();
        addRecentEvent(kind, ip, domain, savedTargets);
    }

    private void saveNetworkUrlVisit(String ip, String url) {
        int savedTargets = 0;
        for (CaptureTarget target : captureTargets()) {
            fraudAnalysisService.addNetworkUrlVisit(ip, url, target.sessionId(), target.createdBy());
            savedTargets++;
        }
        totalSaved.addAndGet(savedTargets);
        lastSavedAt = Instant.now();
        addRecentEvent("HTTP", ip, url, savedTargets);
    }

    private List<CaptureTarget> captureTargets() {
        List<CaptureTarget> targets = new ArrayList<>();
        targets.add(new CaptureTarget(SYSTEM_CAPTURE_SESSION_ID, SYSTEM_CAPTURE_OWNER));

        String sessionId = activeSessionId;
        String createdBy = activeCreatedBy;
        if (sessionId != null && !sessionId.isBlank()
                && createdBy != null && !createdBy.isBlank()
                && !SYSTEM_CAPTURE_SESSION_ID.equals(sessionId)) {
            targets.add(new CaptureTarget(sessionId, createdBy));
        }

        return targets;
    }

    private void addRecentEvent(String kind, String ip, String value, int savedTargets) {
        recentEvents.addLast(Map.of(
                "ts", Instant.now().toString(),
                "kind", kind,
                "ip", ip,
                "value", value,
                "savedTargets", savedTargets
        ));
        while (recentEvents.size() > 50) {
            recentEvents.pollFirst();
        }
    }

    private record CaptureTarget(String sessionId, String createdBy) {
    }
}
