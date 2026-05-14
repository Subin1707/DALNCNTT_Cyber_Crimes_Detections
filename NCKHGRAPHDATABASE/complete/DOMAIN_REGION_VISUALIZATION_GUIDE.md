# 🔍 Domain Region Visualization System - Hệ thống Trực quan hóa Miền Hành vi

## 📚 Tài liệu toàn diện

---

## I. Giới thiệu hệ thống

### 1.1 Khái niệm cơ bản

Hệ thống **Domain Region Visualization** là một giải pháp trực quan hóa AI cho phát hiện gian lận dựa trên mô hình **Miền Hành vi** (Behavioral Regions).

**Các thành phần chính:**

| Thành phần | Hiển thị | Ý nghĩa |
|-----------|---------|--------|
| **MIỀN** | Hình tròn lớn | Vùng hành vi |
| **NODE** | Hình tròn nhỏ | Đối tượng dữ liệu |
| **CENTER VECTOR** | Dấu ★ | Đặc trưng trung tâm |
| **KHOẢNG CÁCH** | Đường nối | Tương đồng giữa nodes |
| **KNN** | Node lân cận | Láng giềng gần nhất |
| **MÀU SẮC** | Gradient RGB | Mức độ nguy hiểm |

---

## II. Cấu trúc hiển thị đúng

### 2.1 Ba miền chuẩn

```
        ┌─────────────────────┐
        │     FRAUD REGION    │
        │                     │
        │ 🔴 A   🔴 B   🔴 C  │
        │ 🔴 D   🔴 E         │
        │                     │
        └─────────────────────┘


   ┌─────────────────────┐
   │  SUSPICIOUS REGION  │
   │                     │
   │ 🟠 F   🟠 G   🟠 H  │
   │                     │
   └─────────────────────┘


┌─────────────────────────┐
│      SAFE REGION        │
│                         │
│ 🔵 X  🔵 Y  🔵 Z  🔵 T  │
│ 🔵 M  🔵 N              │
│                         │
└─────────────────────────┘
```

### 2.2 Phân biệt màu

**QUAN TRỌNG:** Miền và Node phải khác màu và khác độ đậm

#### SAFE REGION (Vùng an toàn)
- **Miền:** Xanh lá nhạt (`#90EE90`)
- **Viền:** Xanh lá đậm (`#228B22`)
- **Node:** Xanh dương (`#1E90FF`)
- **Ý nghĩa:** An toàn, ổn định, tin cậy

#### SUSPICIOUS REGION (Vùng nghi ngờ)
- **Miền:** Vàng nhạt (`#FFFFE0`)
- **Viền:** Cam (`#FF8C00`)
- **Node:** Cam (`#FF8C00`)
- **Ý nghĩa:** Bất thường, cần theo dõi

#### FRAUD REGION (Vùng gian lận)
- **Miền:** Đỏ nhạt (`#FFB6C1`)
- **Viền:** Đỏ đậm (`#DC143C`)
- **Node:** Đỏ đậm (`#8B0000`)
- **Ý nghĩa:** Nguy hiểm, gian lận, vi phạm

---

## III. Ý nghĩa của Node trong Miền

### 3.1 Node trong SAFE REGION

```
Node        | Đặc điểm
------------|------------------
UserA       | IP ổn định
UserB       | Không spam
UserC       | Request bình thường
```

**Vì sao nằm trong SAFE?**
- Có vector đặc trưng gần miền an toàn
- Tất cả đặc trưng đều có giá trị thấp
- Không có flag nguy hiểm

### 3.2 Node gần biên (Borderline nodes)

```
SAFE REGION        SUSPICIOUS REGION

🔵 🔵 🔵      🟠 Node X      🟠 🟠 🟠
```

**Ý nghĩa:**
- Node X chưa đủ nguy hiểm
- Nhưng có hành vi bất thường
- Cần theo dõi sát

### 3.3 Node bị "kéo" về FRAUD REGION

**Ví dụ:**
```
Node có:
- 9 đặc điểm an toàn (weight = 1)
- 1 đặc điểm blacklist (weight = 10)

=> Node bị kéo gần miền đỏ do trọng số cao
```

---

## IV. Distance Metrics (Thuật toán Khoảng cách)

### 4.1 Euclidean Distance (L2 norm)

**Formula:** $\sqrt{\sum(x_i - y_i)^2}$

```
[0, 0] vs [3, 4]
=> sqrt(3² + 4²) = 5
```

**Ứng dụng:**
- Dữ liệu liên tục
- Vector đặc trưng số thực
- Khoảng cách Euclid trong không gian n-chiều

### 4.2 Manhattan Distance (L1 norm)

**Formula:** $\sum|x_i - y_i|$

```
[0, 0] vs [3, 4]
=> |3| + |4| = 7
```

**Ứng dụng:**
- Grid-based movement
- Urban distance (taxicab geometry)
- Đường đi dọc theo lưới

### 4.3 Minkowski Distance (Lp norm)

**Formula:** $(\sum|x_i - y_i|^p)^{1/p}$

**Trường hợp đặc biệt:**
- $p=1$: Manhattan distance
- $p=2$: Euclidean distance
- $p=\infty$: Chebyshev distance

**Ứng dụng:**
- Dữ liệu nhiều chiều
- Điều chỉnh linh hoạt theo p

### 4.4 Hamming Distance

**Formula:** Số vị trí khác nhau

```
[1,0,1,0] vs [1,1,1,0]
=> 1 (khác tại vị trí 1)
```

**Ứng dụng:**
- Dữ liệu boolean
- So sánh bit
- Dữ liệu nhị phân

### 4.5 Cosine Distance

**Formula:** $1 - \frac{u \cdot v}{||u|| \times ||v||}$

**Ứng dụng:**
- Vector cao chiều
- So sánh hướng của vector
- Phân tích văn bản (NLP)

---

## V. KNN (K-Nearest Neighbors)

### 5.1 Cách hoạt động

```
Cho Node X:
1. Tính khoảng cách từ X đến tất cả nodes khác
2. Sắp xếp theo khoảng cách
3. Lấy K neighbors gần nhất

Mặc định K = 3
```

### 5.2 Ứng dụng trong hệ thống

```
FRAUD REGION:
🔴 BotA       <- Neighbors: BotB, MalwareX, SpamNodeY
🔴 BotB       <- Vì vector giống nhau
🔴 MalwareX   <- Hành vi giống nhau
🔴 SpamNodeY  <- Cùng pattern gian lận
```

**Ý nghĩa:**
- Nodes gần nhau = hành vi tương tự
- Dùng để phân cụm (clustering)
- Phát hiện các botnet, malware group

---

## VI. Mô hình chuẩn cho báo cáo

```
             🔴 FRAUD REGION
        (Miền hành vi vi phạm)

           🔴 🔴 🔴 🔴
         🔴 🔴 ★ 🔴 🔴
             🔴 🔴


      🟠 SUSPICIOUS REGION
       (Miền hành vi nghi ngờ)

           🟠 🟠 ★ 🟠
             🟠 🟠


         🔵 SAFE REGION
        (Miền hành vi an toàn)

       🔵 🔵 🔵 🔵 🔵
         🔵 ★ 🔵 🔵
```

**Biểu tượng:**
- `★` = Center Vector (đặc trưng trung tâm)
- `●` = Node (nhỏ = an toàn, lớn = nguy hiểm)
- `━` = Đường KNN (nối các node lân cận)

---

## VII. Cấu trúc code Java

### 7.1 RegionVisualization.java

Đại diện cho một miền hành vi

```java
public class RegionVisualization {
    // Thuộc tính
    private RegionType regionType;           // SAFE, SUSPICIOUS, FRAUD
    private RegionColorScheme colorScheme;   // Màu sắc
    private double[] centerVector;           // Đặc trưng trung tâm
    private List<NodeVisualization> nodes;   // Danh sách nodes
    private double radius;                   // Bán kính miền
    private double centerX, centerY;         // Tọa độ tâm (2D)
    
    // Phương thức chính
    public void addNode(NodeVisualization node);
    public String toSVG(int width, int height);
    public String getTextualDescription();
    public String getStandardReportModel();
}
```

### 7.2 NodeVisualization.java

Đại diện cho một node (đối tượng dữ liệu)

```java
public class NodeVisualization {
    // Thuộc tính
    private String nodeId;                   // ID node
    private double[] featureVector;          // Vector đặc trưng
    private double riskScore;                // Mức độ nguy hiểm (0-1)
    private RegionType regionType;           // Region chứa node
    private double x, y;                     // Tọa độ hiển thị
    private List<NodeVisualization> knnNeighbors;  // KNN neighbors
    
    // Phương thức chính
    public void computeKNNNeighbors(List<NodeVisualization> candidates, DistanceMetric metric);
    public void computeDistanceToCenterVector(double[] center, DistanceMetric metric);
    public String toSVG(String nodeColor);
    public String getDetailedDescription();
}
```

### 7.3 DistanceMetric.java

Abstract class cho các thuật toán khoảng cách

```java
public abstract class DistanceMetric {
    // Các implementation
    public static class EuclideanDistance extends DistanceMetric { ... }
    public static class ManhattanDistance extends DistanceMetric { ... }
    public static class MinkowskiDistance extends DistanceMetric { ... }
    public static class HammingDistance extends DistanceMetric { ... }
    public static class CosineDistance extends DistanceMetric { ... }
}
```

### 7.4 DomainRegionVisualizationService.java

Service quản lý toàn bộ hệ thống

```java
@Service
public class DomainRegionVisualizationService {
    // Phương thức chính
    public void initializeStandardRegions();
    public NodeVisualization createNode(...);
    public void addNodeToAppropriateRegion(NodeVisualization node);
    public void computeKNNForAllNodes();
    public String generateVisualizationHTML();
    public String generateDetailedReport();
}
```

### 7.5 DomainRegionVisualizationController.java

REST API endpoints

```java
@Controller
@RequestMapping("/visualization")
public class DomainRegionVisualizationController {
    @PostMapping("/demo")
    public Map<String, Object> createDemo(@RequestParam String metric);
    
    @GetMapping("/html")
    public String getVisualizationHTML();
    
    @GetMapping("/report")
    public String getDetailedReport();
}
```

---

## VIII. Endpoints REST API

### 8.1 Tạo Demo

```
POST /visualization/demo?metric=euclidean
```

**Response:**
```json
{
  "status": "success",
  "message": "Demo created successfully",
  "totalNodes": 10,
  "regions": 3,
  "distanceMetric": "EUCLIDEAN"
}
```

### 8.2 Lấy HTML Visualization

```
GET /visualization/html
```

**Response:** HTML page chứa SVG visualization

### 8.3 Lấy Báo cáo chi tiết

```
GET /visualization/report
```

**Response:** Báo cáo văn bản chi tiết

### 8.4 Lấy danh sách Metrics

```
GET /visualization/metrics
```

**Response:**
```json
{
  "euclidean": "Khoảng cách Euclid (L2 norm)",
  "manhattan": "Khoảng cách Manhattan (L1 norm)",
  "minkowski": "Khoảng cách Minkowski (Lp norm)",
  "hamming": "Khoảng cách Hamming (dữ liệu boolean)",
  "cosine": "Khoảng cách Cosine (so sánh hướng)"
}
```

---

## IX. Integration Tests

### 9.1 Test khởi tạo Regions

```java
@Test
public void testInitializeStandardRegions() {
    assertEquals(3, visualizationService.getRegions().size());
    assertNotNull(visualizationService.getRegion(RegionType.SAFE));
    assertNotNull(visualizationService.getRegion(RegionType.SUSPICIOUS));
    assertNotNull(visualizationService.getRegion(RegionType.FRAUD));
}
```

### 9.2 Test màu sắc

```java
@Test
public void testColorSchemes() {
    RegionVisualization safeRegion = visualizationService.getRegion(RegionType.SAFE);
    assertEquals("#90EE90", safeRegion.getColorScheme().getBackgroundColor());
    assertEquals("#228B22", safeRegion.getColorScheme().getBorderColor());
    assertEquals("#1E90FF", safeRegion.getColorScheme().getNodeColor());
}
```

### 9.3 Test Distance Metrics

```java
@Test
public void testEuclideanDistance() {
    DistanceMetric metric = new DistanceMetric.EuclideanDistance();
    double[] v1 = {0.0, 0.0};
    double[] v2 = {3.0, 4.0};
    assertEquals(5.0, metric.calculate(v1, v2), 0.001);
}
```

### 9.4 Test KNN

```java
@Test
public void testComputeKNNNeighbors() {
    visualizationService.computeKNNForAllNodes();
    NodeVisualization node = /* ... */;
    assertEquals(3, node.getKnnNeighbors().size());
}
```

---

## X. Ứng dụng thực tế

### 10.1 Phát hiện Botnet

```
FRAUD REGION:
🔴 Bot-1  (IP: 192.168.1.100, Spam: 1000/day)
🔴 Bot-2  (IP: 192.168.1.101, Spam: 950/day)
🔴 Bot-3  (IP: 192.168.1.102, Spam: 1050/day)

KNN Analysis:
=> 3 bots gần nhau => Cùng botnet
=> Pattern tương tự => Cùng C&C server
=> Thời gian tương đồng => Tấn công phối hợp
```

### 10.2 Phát hiện Fraudster

```
SUSPICIOUS -> FRAUD:
User-A (Day 1): SAFE (normal purchase)
User-A (Day 5): SUSPICIOUS (multiple cards)
User-A (Day 10): FRAUD (chargeback pattern detected)

Vector evolution:
[low, low, low] -> [med, med, med] -> [high, high, high]

Distance to FRAUD center: 0.01 (rất gần)
```

### 10.3 Phát hiện Phishing Domain

```
SUSPICIOUS REGION:
🟠 Domain-X (tương tự domain ngân hàng)
🟠 Domain-Y (SSL cert kém)
🟠 Domain-Z (redirect suspicious)

KNN: Tất cả gần nhau => Phishing ring
=> Alert: Potential phishing campaign
```

---

## XI. Hướng phát triển tương lai

### 11.1 Tính năng mở rộng

1. **3D Visualization**
   - Hiển thị nodes trong không gian 3D
   - Thêm chiều temporal (thời gian)

2. **Real-time Updates**
   - WebSocket cho live updates
   - Animated transitions

3. **Interactive Analysis**
   - Click-to-explore nodes
   - Filter by features
   - Custom region definitions

4. **Machine Learning Integration**
   - Auto-clustering
   - Anomaly detection
   - Predictive analytics

### 11.2 Performance Optimization

1. **Big Data Support**
   - Hiệu ứng lấy mẫu cho 1M+ nodes
   - GPU-accelerated distance calculations

2. **Caching**
   - KNN neighbor cache
   - Distance metric precomputation

3. **Scalability**
   - Distributed processing
   - Multi-region analysis

---

## XII. Tham khảo và tài liệu thêm

- **Euclidean Distance:** https://en.wikipedia.org/wiki/Euclidean_distance
- **KNN Algorithm:** https://en.wikipedia.org/wiki/K-nearest_neighbors_algorithm
- **Color Theory:** https://en.wikipedia.org/wiki/Color_theory
- **SVG Visualization:** https://www.w3.org/TR/SVG2/

---

**Tài liệu này là phần của Domain Region Visualization System**
**Phiên bản: 1.0 | Cập nhật: 2024**
