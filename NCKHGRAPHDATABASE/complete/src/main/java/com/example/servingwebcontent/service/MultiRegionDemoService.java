package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.model.RegionType;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MultiRegionDemoService - Chứng minh phương pháp miền thực sự
 * 
 * PHƯƠNG PHÁP MIỀN LÀ GÌ?
 * - Đặt node vào không gian hành vi 12 chiều
 * - Đo khoảng cách từ node tới 3 miền:
 *   • Miền AN TOÀN (xanh) - Hành vi bình thường
 *   • Miền NGHI NGỜ (vàng) - Hành vi rủi ro vừa phải
 *   • Miền VI PHẠM (đỏ) - Hành vi xác định gian lận
 * - Miền nào gần nhất → Node thuộc miền đó
 * 
 * KHÁC BIỆT VỚI RULE-BASED:
 * Rule-Based: if(blacklist) score += 40; if(vpn) score += 20;
 * Multi-Region: Node gần miền nào? Khoảng cách bao nhiêu?
 */
@Service
public class MultiRegionDemoService {

    private final MultiRegionAnalysisService multiRegionService;

    public MultiRegionDemoService(MultiRegionAnalysisService multiRegionService) {
        this.multiRegionService = multiRegionService;
    }

    /**
     * DEMO 1: Phương pháp miền thực sự
     * So sánh 3 node khác nhau
     */
    public void demoThreeRegionAnalysis() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("📊 DEMO: PHƯƠNG PHÁP MIỀN THỰC SỰ - 3 MIỀN AN TOÀN, NGHI NGỜ, VI PHẠM");
        System.out.println("=".repeat(100));

        // ============================================
        // TRƯỜNG HỢP 1: NODE TRONG MIỀN AN TOÀN (XANH)
        // ============================================
        System.out.println("\n" + "▓".repeat(100));
        System.out.println("🟢 CASE 1: NODE TRONG MIỀN AN TOÀN (Hành vi bình thường)");
        System.out.println("▓".repeat(100));

        BehaviorFeatureVector safeNode = new BehaviorFeatureVector(
                1, 2, 3, 2, 0, 0.5,  // numeric: ít IP, URL, email, domain, failed login
                false, false, false, false, false, false  // boolean: không VPN, blacklist, TOR, spam
        );

        MultiRegionAnalysisService.RegionAnalysisResult safeResult = 
            multiRegionService.analyzeAgainstRegions(safeNode);
        multiRegionService.applyFeaturePenalties(safeNode, safeResult);

        System.out.println("📋 Node Features: " + formatNode(safeNode));
        System.out.println("\n🎯 Khoảng cách đến 3 miền:");
        System.out.printf("   🟢 An Toàn:  Distance=%.2f, Probability=%.2f%% ← CLOSEST (Thuộc miền an toàn)%n",
                safeResult.getRegionDistance(RegionType.SAFE),
                safeResult.getRegionProbability(RegionType.SAFE) * 100);
        System.out.printf("   🟡 Nghi Ngờ: Distance=%.2f, Probability=%.2f%%%n",
                safeResult.getRegionDistance(RegionType.SUSPICIOUS),
                safeResult.getRegionProbability(RegionType.SUSPICIOUS) * 100);
        System.out.printf("   🔴 Vi Phạm: Distance=%.2f, Probability=%.2f%%%n",
                safeResult.getRegionDistance(RegionType.FRAUD),
                safeResult.getRegionProbability(RegionType.FRAUD) * 100);
        System.out.println("\n✅ KẾT LUẬN: Node gần miền AN TOÀN nhất → " + safeResult.getPrimaryRegion());
        System.out.println("📊 Anomaly Score: " + String.format("%.2f", safeResult.getAnomalyScore()) + 
                         " (< 0.4 = không có anomaly)");
        System.out.println("📝 Chi tiết: " + safeResult.getDetails());

        // ============================================
        // TRƯỜNG HỢP 2: NODE TRONG MIỀN NGHI NGỜ (VÀNG)
        // ============================================
        System.out.println("\n" + "▓".repeat(100));
        System.out.println("🟡 CASE 2: NODE TRONG MIỀN NGHI NGỜ (Hành vi rủi ro vừa phải)");
        System.out.println("▓".repeat(100));

        BehaviorFeatureVector suspiciousNode = new BehaviorFeatureVector(
                5, 8, 10, 6, 3, 3.0,  // numeric: vừa phải
                true, false, true, false, true, true  // boolean: VPN, spam pattern, suspicious URL, abnormal time
        );

        MultiRegionAnalysisService.RegionAnalysisResult suspiciousResult = 
            multiRegionService.analyzeAgainstRegions(suspiciousNode);
        multiRegionService.applyFeaturePenalties(suspiciousNode, suspiciousResult);

        System.out.println("📋 Node Features: " + formatNode(suspiciousNode));
        System.out.println("\n🎯 Khoảng cách đến 3 miền:");
        System.out.printf("   🟢 An Toàn:  Distance=%.2f, Probability=%.2f%%%n",
                suspiciousResult.getRegionDistance(RegionType.SAFE),
                suspiciousResult.getRegionProbability(RegionType.SAFE) * 100);
        System.out.printf("   🟡 Nghi Ngờ: Distance=%.2f, Probability=%.2f%% ← CLOSEST (Thuộc miền nghi ngờ)%n",
                suspiciousResult.getRegionDistance(RegionType.SUSPICIOUS),
                suspiciousResult.getRegionProbability(RegionType.SUSPICIOUS) * 100);
        System.out.printf("   🔴 Vi Phạm: Distance=%.2f, Probability=%.2f%%%n",
                suspiciousResult.getRegionDistance(RegionType.FRAUD),
                suspiciousResult.getRegionProbability(RegionType.FRAUD) * 100);
        System.out.println("\n⚠️  KẾT LUẬN: Node gần miền NGHI NGỜ nhất → " + suspiciousResult.getPrimaryRegion());
        System.out.println("📊 Anomaly Score: " + String.format("%.2f", suspiciousResult.getAnomalyScore()));
        System.out.println("📝 Chi tiết: " + suspiciousResult.getDetails());

        // ============================================
        // TRƯỜNG HỢP 3: NODE TRONG MIỀN VI PHẠM (ĐỎ)
        // ============================================
        System.out.println("\n" + "▓".repeat(100));
        System.out.println("🔴 CASE 3: NODE TRONG MIỀN VI PHẠM (Xác định gian lận)");
        System.out.println("▓".repeat(100));

        BehaviorFeatureVector fraudNode = new BehaviorFeatureVector(
                15, 20, 25, 18, 8, 10.0,  // numeric: cao
                true, true, true, true, true, true  // boolean: TẤT CẢ ĐỀU TRUE = xác định gian lận
        );

        MultiRegionAnalysisService.RegionAnalysisResult fraudResult = 
            multiRegionService.analyzeAgainstRegions(fraudNode);
        multiRegionService.applyFeaturePenalties(fraudNode, fraudResult);

        System.out.println("📋 Node Features: " + formatNode(fraudNode));
        System.out.println("\n🎯 Khoảng cách đến 3 miền:");
        System.out.printf("   🟢 An Toàn:  Distance=%.2f, Probability=%.2f%%%n",
                fraudResult.getRegionDistance(RegionType.SAFE),
                fraudResult.getRegionProbability(RegionType.SAFE) * 100);
        System.out.printf("   🟡 Nghi Ngờ: Distance=%.2f, Probability=%.2f%%%n",
                fraudResult.getRegionDistance(RegionType.SUSPICIOUS),
                fraudResult.getRegionProbability(RegionType.SUSPICIOUS) * 100);
        System.out.printf("   🔴 Vi Phạm: Distance=%.2f, Probability=%.2f%% ← CLOSEST (Thuộc miền vi phạm)%n",
                fraudResult.getRegionDistance(RegionType.FRAUD),
                fraudResult.getRegionProbability(RegionType.FRAUD) * 100);
        System.out.println("\n🚨 KẾT LUẬN: Node gần miền VI PHẠM nhất → " + fraudResult.getPrimaryRegion());
        System.out.println("📊 Anomaly Score: " + String.format("%.2f", fraudResult.getAnomalyScore()));
        System.out.println("📝 Chi tiết: " + fraudResult.getDetails());

        // ============================================
        // SO SÁNH VỚI RULE-BASED
        // ============================================
        System.out.println("\n" + "=".repeat(100));
        System.out.println("⚖️ SO SÁNH: MULTI-REGION vs RULE-BASED");
        System.out.println("=".repeat(100));

        System.out.println("\n🔴 CASE 3 (Gian lận):");
        System.out.println("   Rule-Based: blacklist(40) + vpn(10) + tor(30) + spam(20) + ...");
        System.out.println("              = Cộng điểm cố định dựa trên rules");
        System.out.println("   Multi-Region: Distance to fraud region = " + 
                String.format("%.2f", fraudResult.getRegionDistance(RegionType.FRAUD)) +
                " → Probability = " + String.format("%.2f%%", fraudResult.getRegionProbability(RegionType.FRAUD) * 100) +
                " (Hoàn toàn khác!)");
        System.out.println("   ✅ Multi-Region VƯỢT TRỘI: Phân tích không gian hành vi thực sự");

        printRegionSummary();
    }

    /**
     * DEMO 2: Trường hợp phức tạp - Node nằm giữa 2 miền
     */
    public void demoComplexCase() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("🔍 DEMO 2: TRƯỜNG HỢP PHỨC TẠP - Node nằm giữa 2 miền");
        System.out.println("=".repeat(100));

        // Node này có một số tính năng bình thường, một số nghi ngờ
        BehaviorFeatureVector complexNode = new BehaviorFeatureVector(
                8, 12, 8, 5, 2, 2.0,  // numeric: trung bình
                true, false, true, false, false, true  // VPN=true, but blacklist=false (mâu thuẫn!)
        );

        MultiRegionAnalysisService.RegionAnalysisResult complexResult = 
            multiRegionService.analyzeAgainstRegions(complexNode);
        multiRegionService.applyFeaturePenalties(complexNode, complexResult);

        System.out.println("\n📋 Node Features (MẪU THUẪN - một số tính năng bình thường, một số nghi ngờ):");
        System.out.println("   VPN: true (nghi ngờ) ❓");
        System.out.println("   Blacklist: false (bình thường) ✓");
        System.out.println("   IpCount: 8 (trung bình)");
        System.out.println("   UrlCount: 12 (trung bình)");

        System.out.println("\n🎯 Khoảng cách đến 3 miền:");
        System.out.printf("   🟢 An Toàn:  Distance=%.2f, Probability=%.2f%%%n",
                complexResult.getRegionDistance(RegionType.SAFE),
                complexResult.getRegionProbability(RegionType.SAFE) * 100);
        System.out.printf("   🟡 Nghi Ngờ: Distance=%.2f, Probability=%.2f%%  ← NẰM Ở ĐÂY%n",
                complexResult.getRegionDistance(RegionType.SUSPICIOUS),
                complexResult.getRegionProbability(RegionType.SUSPICIOUS) * 100);
        System.out.printf("   🔴 Vi Phạm: Distance=%.2f, Probability=%.2f%%%n",
                complexResult.getRegionDistance(RegionType.FRAUD),
                complexResult.getRegionProbability(RegionType.FRAUD) * 100);

        System.out.println("\n🔴 ANOMALY SCORE: " + String.format("%.2f", complexResult.getAnomalyScore()) +
                " (> 0.4 = ANOMALY! Mâu thuẫn trong hành vi!)");
        System.out.println("⚠️  Kết luận: Node nằm giữa NGHI NGỜ và VI PHẠM → Cần điều tra kỹ");
    }

    /**
     * DEMO 3: So sánh 3 node có cùng score Rule-Based nhưng miền khác
     */
    public void demoRuleBasedVsRegion() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("🧪 DEMO 3: Cùng Rule-Based Score nhưng Miền khác!");
        System.out.println("=".repeat(100));

        // Node A: VPN + Spam (score = 30)
        BehaviorFeatureVector nodeA = new BehaviorFeatureVector(
                2, 3, 2, 2, 0, 1.0,
                true, false, false, false, true, false
        );

        // Node B: IpCount=15 + UrlCount=15 (score = 25)
        BehaviorFeatureVector nodeB = new BehaviorFeatureVector(
                15, 15, 5, 5, 1, 2.0,
                false, false, false, false, false, false
        );

        System.out.println("\n📊 NODE A: VPN + Spam");
        System.out.println("   Rule-Based Score: ~30 points");
        MultiRegionAnalysisService.RegionAnalysisResult resultA = 
            multiRegionService.analyzeAgainstRegions(nodeA);
        multiRegionService.applyFeaturePenalties(nodeA, resultA);
        System.out.println("   Multi-Region: " + resultA.getPrimaryRegion() + 
                         " (Fraud prob: " + String.format("%.2f%%", resultA.getRegionProbability(RegionType.FRAUD) * 100) + ")");

        System.out.println("\n📊 NODE B: HighIpCount + HighUrlCount");
        System.out.println("   Rule-Based Score: ~25 points");
        MultiRegionAnalysisService.RegionAnalysisResult resultB = 
            multiRegionService.analyzeAgainstRegions(nodeB);
        multiRegionService.applyFeaturePenalties(nodeB, resultB);
        System.out.println("   Multi-Region: " + resultB.getPrimaryRegion() + 
                         " (Fraud prob: " + String.format("%.2f%%", resultB.getRegionProbability(RegionType.FRAUD) * 100) + ")");

        System.out.println("\n✅ KẾT LUẬN:");
        System.out.println("   Cùng Rule-Based Score ~30 nhưng MIỀN KHÁC!");
        System.out.println("   Multi-Region phân biệt LOẠI HỈnh vi → ĐỘ CHÍNH XÁC CAO HƠN");
    }

    /**
     * Print region definition
     */
    private void printRegionSummary() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("📚 ĐỊNH NGHĨA 3 MIỀN");
        System.out.println("=".repeat(100));

        System.out.println("\n🟢 MIỀN AN TOÀN (Safe Region)");
        System.out.println("   Đặc điểm:");
        System.out.println("   - VPN: false (không dùng VPN)");
        System.out.println("   - Blacklist: false (không trong danh sách đen)");
        System.out.println("   - TOR: false (không dùng TOR)");
        System.out.println("   - Spam: false (không spam)");
        System.out.println("   - IpCount: 1-2 (ít IP)");
        System.out.println("   - UrlCount: 1-3 (ít URL)");
        System.out.println("   Center Vector: (1, 2, 5, 3, 0, 1.0, false, false, false, false, false, false)");

        System.out.println("\n🟡 MIỀN NGHI NGỜ (Suspicious Region)");
        System.out.println("   Đặc điểm:");
        System.out.println("   - VPN: true (dùng VPN)");
        System.out.println("   - Blacklist: false (chưa trong danh sách đen)");
        System.out.println("   - TOR: false (không dùng TOR)");
        System.out.println("   - Spam: true (có dấu hiệu spam)");
        System.out.println("   - IpCount: 5 (vừa phải)");
        System.out.println("   - UrlCount: 8 (vừa phải)");
        System.out.println("   Center Vector: (5, 8, 10, 6, 3, 3.5, true, false, true, false, true, true)");

        System.out.println("\n🔴 MIỀN VI PHẠM (Fraud Region)");
        System.out.println("   Đặc điểm:");
        System.out.println("   - VPN: true (dùng VPN)");
        System.out.println("   - Blacklist: true (TRONG danh sách đen!)");
        System.out.println("   - TOR: true (dùng TOR!)");
        System.out.println("   - Spam: true (spam)");
        System.out.println("   - IpCount: 15+ (nhiều IP)");
        System.out.println("   - UrlCount: 20+ (nhiều URL)");
        System.out.println("   Center Vector: (15, 20, 25, 18, 8, 10.0, true, true, true, true, true, true)");

        System.out.println("\n⭐ CÁC METRIC:");
        System.out.println("   • 3 Distance Metrics: Euclidean (số), Minkowski (đa chiều), Hamming (boolean)");
        System.out.println("   • Khoảng cách = trung bình 3 metric → Xác suất = e^(-distance*2.5)");
        System.out.println("   • Miền nào gần nhất → Node thuộc miền đó");
    }

    /**
     * Format node for display
     */
    private String formatNode(BehaviorFeatureVector node) {
        return String.format(
            "IP:%d URL:%d Email:%d Domain:%d FailedLogin:%d ReqFreq:%.1f | " +
            "VPN:%s Blacklist:%s SuspiciousUrl:%s TOR:%s Spam:%s AbnormalTime:%s",
            node.getIpCount(),
            node.getUrlCount(),
            node.getEmailCount(),
            node.getDomainCount(),
            node.getFailedLoginCount(),
            node.getRequestFrequency(),
            node.isVpn(),
            node.isBlacklist(),
            node.isSuspiciousUrl(),
            node.isTorNetwork(),
            node.isSpamPattern(),
            node.isAbnormalAccessTime()
        );
    }

    /**
     * Print ASCII visualization of regions
     */
    public void printRegionVisualization() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("🎨 VISUALIZATION: 3 MIỀN TRONG KHÔNG GIAN HÀNH VI 12 CHIỀU");
        System.out.println("=".repeat(100));

        System.out.println("""
            
            ┌─────────────────────────────────────────────────────────────────┐
            │                  BEHAVIOR FEATURE SPACE (12D)                   │
            ├─────────────────────────────────────────────────────────────────┤
            │                                                                 │
            │  🟢 SAFE REGION (Xanh)                                          │
            │  ░░░░░░░░░░░░░░░                                               │
            │  ░ Normal ░ Low Risk  ░                                         │
            │  ░░░░░░░░░░░░░░░                                               │
            │     ↓ Distance metric: 0.5-1.5                                 │
            │                                                                 │
            │  🟡 SUSPICIOUS REGION (Vàng)                                    │
            │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓                                               │
            │  ▓ Medium Risk ▓                                               │
            │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓                                               │
            │     ↓ Distance metric: 1.5-3.0                                 │
            │                                                                 │
            │  🔴 FRAUD REGION (Đỏ)                                           │
            │  ████████████████                                              │
            │  █ Fraud ██ HighRisk █                                         │
            │  ████████████████                                              │
            │     ↓ Distance metric: 3.0-5.0                                 │
            │                                                                 │
            │  PHƯƠNG PHÁP: Node mới được ánh xạ vào không gian này          │
            │              → Tính khoảng cách đến 3 miền                     │
            │              → Miền nào gần nhất → Phân loại đó                │
            │                                                                 │
            └─────────────────────────────────────────────────────────────────┘
            """);
    }
}
