# 🎨 Domain Region Visualization - Visual Reference Guide

## 📊 Biểu đồ Hệ thống

### Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────┐
│                     WEB BROWSER                              │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          domain-region-visualization.html            │  │
│  │  - Legend (3 colors)                                 │  │
│  │  - Controls (metric selector, demo button)           │  │
│  │  - Visualization frame (SVG)                         │  │
│  │  - Metrics dashboard                                 │  │
│  │  - Reports viewer                                    │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP/REST
                     ▼
┌─────────────────────────────────────────────────────────────┐
│            SPRING BOOT APPLICATION (Java)                   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  DomainRegionVisualizationController                 │  │
│  │  - /visualization (GET) - Web page                   │  │
│  │  - /visualization/demo (POST) - Create demo          │  │
│  │  - /visualization/html (GET) - HTML output           │  │
│  │  - /visualization/report (GET) - Text report         │  │
│  │  - /visualization/metrics (GET) - List metrics       │  │
│  └──────────────────────────────────────────────────────┘  │
│                     ▲                                        │
│                     │                                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  DomainRegionVisualizationService                    │  │
│  │  - initializeStandardRegions()                       │  │
│  │  - createNode()                                      │  │
│  │  - addNodeToRegion()                                 │  │
│  │  - computeKNNForAllNodes()                           │  │
│  │  - generateVisualizationHTML()                       │  │
│  │  - generateDetailedReport()                          │  │
│  └──────────────────────────────────────────────────────┘  │
│                     ▲                                        │
│                     │                                        │
│  ┌────────────┬─────────────┬──────────────────────────┐   │
│  │            │             │                          │   │
│  ▼            ▼             ▼                          ▼   │
│ RegionViz   NodeViz    DistanceMetric           RegionType  │
│ (Miền)      (Node)     (Khoảng cách)           (ENUM)      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Mô hình dữ liệu

### RegionVisualization

```
┌────────────────────────────────────────┐
│      RegionVisualization                │
├────────────────────────────────────────┤
│ Properties:                             │
│  • regionId: String                     │
│  • regionType: SAFE|SUSP|FRAUD         │
│  • colorScheme: RegionColorScheme      │
│  • centerVector: double[]               │
│  • nodes: List<NodeVisualization>      │
│  • radius: double                       │
│  • centerX, centerY: double             │
│  • nodeCount: int                       │
│  • averageRiskScore: double             │
│                                         │
│ Methods:                                │
│  • addNode(NodeVisualization)          │
│  • removeNode(NodeVisualization)       │
│  • toSVG(width, height): String        │
│  • getTextualDescription(): String     │
├────────────────────────────────────────┤
│ RegionColorScheme ENUM:                │
│  • SAFE: (bg:#90EE90, border:#228B22) │
│  • SUSPICIOUS: (bg:#FFFFE0, border:...) │
│  • FRAUD: (bg:#FFB6C1, border:#DC143C)│
└────────────────────────────────────────┘
```

### NodeVisualization

```
┌────────────────────────────────────────┐
│      NodeVisualization                  │
├────────────────────────────────────────┤
│ Properties:                             │
│  • nodeId: String                       │
│  • nodeLabel: String                    │
│  • featureVector: double[]              │
│  • riskScore: double (0.0-1.0)         │
│  • regionType: RegionType               │
│  • x, y: double (2D coordinates)       │
│  • knnNeighbors: List<NodeVisualization>│
│  • distanceToCenterVector: double       │
│  • weight: double                       │
│  • status: NORMAL|WARNING|...          │
│                                         │
│ Methods:                                │
│  • computeKNNNeighbors(...)            │
│  • computeDistanceToCenterVector(...)  │
│  • toSVG(color): String                │
│  • getDetailedDescription(): String    │
├────────────────────────────────────────┤
│ NodeStatus ENUM:                       │
│  • NORMAL (Risk < 0.33)                │
│  • WARNING (0.33 <= Risk < 0.67)      │
│  • ANALYZING                            │
│  • DANGEROUS (Risk >= 0.67)            │
└────────────────────────────────────────┘
```

---

## 🎨 Mô hình hiển thị visual

### Region Layout (3D view)

```
                    FRAUD REGION (Đỏ)
                    ┌─────────────────┐
                    │ 🔴 🔴 🔴 🔴    │
                    │ 🔴 ★ 🔴 🔴    │
                    │ 🔴 🔴 🔴       │
                    └─────────────────┘
                            △
                            │
         SUSPICIOUS REGION (Vàng)
         ┌──────────────────┐
         │ 🟠 🟠 ★ 🟠 🟠   │
         │ 🟠 🟠 🟠        │
         └──────────────────┘
                △
                │
    SAFE REGION (Xanh)
    ┌──────────────────────┐
    │ 🔵 🔵 🔵 🔵 🔵     │
    │ 🔵 🔵 ★ 🔵 🔵     │
    │ 🔵 🔵 🔵 🔵       │
    └──────────────────────┘
```

### Color Palette

```
SAFE REGION
Background: #90EE90 ██████ (Light Green)
Border:     #228B22 ██████ (Dark Green)
Node:       #1E90FF ██████ (Dodger Blue)

SUSPICIOUS REGION
Background: #FFFFE0 ██████ (Light Yellow)
Border:     #FF8C00 ██████ (Dark Orange)
Node:       #FF8C00 ██████ (Dark Orange)

FRAUD REGION
Background: #FFB6C1 ██████ (Light Pink)
Border:     #DC143C ██████ (Crimson)
Node:       #8B0000 ██████ (Dark Red)
```

---

## 📐 Distance Metrics Comparison

### 1. Euclidean vs Manhattan

```
Điểm: A(0,0), B(3,4)

Euclidean:           Manhattan:
    (0,4)                (0,4)
      │ \                  │
      │  \         vs      ├────
      │   \        vs      │
   (0,0)──(3,0) (3,4)  (0,0)────(3,0)
                                │
                                (3,4)

Euclidean: √(3² + 4²) = 5     Manhattan: 3 + 4 = 7
```

### 2. Minkowski với p khác nhau

```
p=1 (Manhattan):        p=2 (Euclidean):    p=∞ (Chebyshev):
Σ|xi - yi|             √Σ(xi - yi)²        max|xi - yi|

Formula:               Formula:             Formula:
3 + 4 = 7             √(9 + 16) = 5        max(3,4) = 4
```

### 3. Hamming Distance

```
Vector 1: [1, 0, 1, 0, 1]
Vector 2: [1, 1, 1, 0, 1]
           │ ✗ │ │ │

Hamming Distance = 1 (1 vị trí khác nhau)
```

### 4. Cosine Similarity

```
Vector u = [1, 1]
Vector v = [1, 0]

u·v = 1·1 + 1·0 = 1
||u|| = √(1² + 1²) = √2
||v|| = √(1² + 0²) = 1

Cosine Similarity = 1 / (√2 · 1) = 0.707
Cosine Distance = 1 - 0.707 = 0.293
```

---

## 🔀 KNN Visualization

### K=3 Neighbors

```
Query Node: Q
Other Nodes: A, B, C, D, E, F

Distance ranking:
1. A: distance = 2.5  ◄─── Select
2. B: distance = 3.2  ◄─── Select
3. C: distance = 4.1  ◄─── Select
4. D: distance = 5.3
5. E: distance = 6.7
6. F: distance = 8.2

KNN Result: Q's neighbors = {A, B, C}
```

### KNN Connections Visualization

```
In FRAUD REGION:

        🔴 Bot-B
         /  \
        /    \
    🔴 Bot-A  🔴 Bot-C
       \      /
        \    /
    🔴 Bot-D

Bot-A KNN = {Bot-B, Bot-C, Bot-D}
=> Tất cả gần nhau => Cùng botnet
=> Pattern tương tự
=> Nguy hiểm cao
```

---

## 📊 Risk Score Visualization

### Risk Score vs Node Size

```
Risk Score   Node Size    Status       Color
0.0-0.10     ● (4px)      NORMAL       Xanh dương
0.10-0.33    ●● (7px)     NORMAL       Xanh dương
0.33-0.50    ●●● (12px)   WARNING      Cam
0.50-0.67    ●●●● (15px)  WARNING      Cam
0.67-0.85    ●●●●● (18px) DANGEROUS    Đỏ đậm
0.85-1.00    ●●●●●● (21px) DANGEROUS   Đỏ đậm
```

### Node appearance:

```
NORMAL (Safe):          WARNING (Suspicious):    DANGEROUS (Fraud):
     🔵                      🟠                      🔴
   Small circle           Medium circle          Large circle
   Blue color            Orange color           Dark red color
   Solid fill            Solid fill             Glow effect
```

---

## 🔗 Flow Diagram

### Demo Creation Flow

```
User clicks "Tạo Demo"
        ▼
Choose Distance Metric
        ▼
POST /visualization/demo?metric=euclidean
        ▼
Controller:
  1. Initialize 3 regions (SAFE, SUSPICIOUS, FRAUD)
  2. Set distance metric
  3. Create 10 sample nodes
  4. Add nodes to appropriate regions
  5. Compute KNN for all nodes
        ▼
Response JSON:
{
  "status": "success",
  "totalNodes": 10,
  "regions": 3,
  "distanceMetric": "EUCLIDEAN"
}
        ▼
JavaScript updates:
  1. Update metrics display
  2. Fetch HTML visualization
  3. Render SVG
  4. Show completed
```

---

## 🗂️ File Dependencies

```
DomainRegionVisualization (Web)
    │
    └── HTML/CSS/JS
        │
        └── REST API calls
            │
            └── DomainRegionVisualizationController
                │
                ├── GET /visualization
                ├── POST /visualization/demo
                ├── GET /visualization/html
                ├── GET /visualization/report
                └── GET /visualization/metrics
                    │
                    └── DomainRegionVisualizationService
                        │
                        ├── RegionVisualization
                        ├── NodeVisualization
                        └── DistanceMetric
```

---

## 📝 Sample Data Structure

### SAFE Region with 3 nodes

```json
{
  "regionId": "region-safe-001",
  "regionType": "SAFE",
  "colorScheme": {
    "backgroundColor": "#90EE90",
    "borderColor": "#228B22",
    "nodeColor": "#1E90FF"
  },
  "centerVector": [1.0, 2.0, 3.0, 2.0, 0.5, 0.5],
  "radius": 150.0,
  "centerX": 150.0,
  "centerY": 350.0,
  "nodes": [
    {
      "nodeId": "user_safe_1",
      "nodeLabel": "UserA",
      "featureVector": [1.0, 2.0, 3.0, 2.0, 0.0, 0.5],
      "riskScore": 0.1,
      "x": 100.0,
      "y": 320.0,
      "knnNeighbors": 3,
      "status": "NORMAL"
    },
    // ... more nodes
  ],
  "nodeCount": 3,
  "averageRiskScore": 0.1,
  "dominanceProbability": 1.0
}
```

---

## 🎯 Use Cases

### Use Case 1: Phát hiện Botnet

```
Hành động:
1. Hệ thống tạo demo với 10 nodes
2. Phân tích vào FRAUD region
3. Compute KNN
4. Phát hiện 4 bots gần nhau

Kết quả:
🔴 Bot-A (IP: 192.168.1.100)
🔴 Bot-B (IP: 192.168.1.101)  ─ KNN neighbors
🔴 Bot-C (IP: 192.168.1.102)  ─ Cùng pattern
🔴 Bot-D (IP: 192.168.1.103)  ─ Cùng C&C

Recommendation: BLOCK ALL 4 IPs
```

### Use Case 2: Suspicious User Monitoring

```
Timeline:
Day 1:  User SAFE (normal purchase)
        🔵 Low risk: 0.1

Day 5:  User SUSPICIOUS (multiple cards)
        🟠 Medium risk: 0.45

Day 10: User FRAUD (chargeback detected)
        🔴 High risk: 0.85

Action: Progressive monitoring → Block
```

---

## 🔍 SVG Example

```xml
<svg width="600" height="500">
  <!-- Define glow effect -->
  <defs>
    <filter id="glow">
      <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
      <feMerge>
        <feMergeNode in="coloredBlur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  </defs>
  
  <!-- SAFE Region (circle) -->
  <circle cx="150" cy="350" r="150" 
          fill="#90EE90" stroke="#228B22" stroke-width="2" opacity="0.7"/>
  
  <!-- Center Vector -->
  <text x="150" y="350" font-size="24" text-anchor="middle" 
        fill="#228B22" font-weight="bold">★</text>
  
  <!-- Nodes in SAFE region -->
  <circle cx="100" cy="320" r="7" fill="#1E90FF" opacity="0.8"/>
  <circle cx="150" cy="380" r="8" fill="#1E90FF" opacity="0.8"/>
  <circle cx="200" cy="340" r="6" fill="#1E90FF" opacity="0.8"/>
  
  <!-- KNN Connections -->
  <line x1="100" y1="320" x2="150" y2="380" 
        stroke="#228B22" stroke-width="1" opacity="0.5"/>
  <line x1="100" y1="320" x2="200" y2="340" 
        stroke="#228B22" stroke-width="1" opacity="0.5"/>
</svg>
```

---

## 📚 References

- **Euclidean Distance:** Standard L2 norm distance metric
- **KNN Algorithm:** Find K closest neighbors in feature space
- **SVG:** Scalable Vector Graphics for web rendering
- **Color Theory:** Use of color to convey meaning and risk levels

---

**Hướng dẫn này cung cấp các biểu đồ visual để hiểu rõ hệ thống.**

**Version:** 1.0 | **Date:** 2024
