# HỆ THỐNG PHÁT HIỆN GỌI LỪA HYBRID ĐẶC ĐIỂM HỎI-VÙNG MIỀN

## Tóm tắt hệ thống

Hệ thống **Hybrid Multi-Region Fraud Detection System** là một nền tảng phát hiện gọi lừa kết hợp:
- Rule-Based (nền tảng)
- KNN (phân tích hành vi)
- Multi-Region Analysis (xác định vùng hành vi)
- Statistical Probability (tính toán xác suất Bayesian)
- Consensus Engine (giảm sai số và xử lý lệch dữ liệu)

---

## I. PHƯƠNG PHÁP KNN (K-Nearest Neighbors)

### 1. Yêu cầu dữ liệu đầu vào: Feature Vector

**Định nghĩa**: Vector đặc trưng hành vi - biểu diễn toàn bộ hành vi người dùng dưới dạng vector số

**Ví dụ**:
```
Đặc trưng              Giá trị
numberOfIPs            5
numberOfURLs           10
isVPN                  1 (true)
isBlacklist            0 (false)
isSpamPattern          1 (true)
```

**Biểu diễn Vector**: `[5, 10, 1, 0, 1]`

**Triển khai**: Lớp `BehaviorFeatureVector` chứa:
- Numeric features: ipCount, urlCount, emailCount, domainCount, failedLoginCount, requestFrequency
- Boolean features: vpn, blacklist, suspiciousUrl, torNetwork, spamPattern, abnormalAccessTime

### 2. Yêu cầu dữ liệu huấn luyện

**Định nghĩa**: Danh sách node đã được phân loại trước

**Ví dụ**:
```
Node      Loại
A         SAFE
B         FRAUD
C         FRAUD
D         SUSPICIOUS
```

**Ý nghĩa**: KNN so sánh node mới với dữ liệu cũ để xác định loại

**Triển khai**: Lớp `SessionFeatureService` tải historical samples từ database

### 3. Yêu cầu giá trị K

**Định nghĩa**: K là số lượng node gần nhất được lấy để phân tích

**Giá trị mặc định**: K = 7

**Ví dụ**: K=7 => lấy 7 node gần nhất, bỏ phiếu để xác định kết quả

**Triển khai**: Constant `DEFAULT_K = 7` trong `KNNEnhancedAnalysisService`

### 4. Yêu cầu thuật toán khoảng cách

**Ba thuật toán được triển khai**:

#### a) Euclidean Distance (dữ liệu số)
```
d(x,y) = √(Σ(xi - yi)²)
```
- **Lớp**: `EuclideanDistance`
- **Dùng cho**: Số IP, số URL, số email
- **Ví dụ**: Khoảng cách giữa vector `[5,10]` và `[3,8]` = √((5-3)² + (10-8)²) = √8 ≈ 2.83

#### b) Minkowski Distance (dữ liệu nhiều chiều)
```
d(x,y) = (Σ|xi - yi|^p)^(1/p)
```
- **Lớp**: `MinkowskiDistance`
- **Tham số**: p = 2.0 (mặc định = Euclidean)
- **Dùng cho**: Dữ liệu đa chiều

#### c) Hamming Distance (dữ liệu boolean)
```
d(x,y) = Σ[xi ≠ yi]
```
- **Lớp**: `HammingDistance`
- **Dùng cho**: VPN, blacklist, TOR, spam pattern
- **Ví dụ**: Khoảng cách giữa `[1,0,1]` và `[1,1,1]` = 1 (một vị trị khác nhau)

### 5. Yêu cầu chuẩn hóa dữ liệu

**Vấn đề**: Các đặc trưng có giá trị khác nhau rất lớn
```
Đặc trưng        Giá trị
requestCount     1000
VPN              1
```
Nếu không chuẩn hóa, requestCount sẽ áp đảo toàn bộ dữ liệu

**Giải pháp**: Min-Max Normalization
```
x' = (x - min) / (max - min)
```

**Kết quả**: Tất cả giá trị nằm trong phạm vi [0, 1]

**Triển khai**: Lớp `FeatureNormalizationUtility`
- Phương thức: `normalizeNumericFeatures()`
- Phương thức: `normalizeFullVector()`
- Hỗ trợ cập nhật giới hạn động

### 6. Yêu cầu Voting (Bỏ phiếu)

**Định nghĩa**: Sau khi tìm K node gần nhất, mỗi node bỏ một phiếu

**Ví dụ**:
```
K=7 neighbors:
SAFE      FRAUD
1         6

=> Kết luận: Node gần hành vi gian lận (85.7% votes)
```

**Hai loại voting**:

#### a) Simple Voting (bỏ phiếu đơn)
- Mỗi neighbor bỏ 1 phiếu
- Winner = class với phiếu nhiều nhất
- Confidence = tỷ lệ phiếu của winner

#### b) Weighted Voting (bỏ phiếu có trọng số)
- Neighbors gần hơn (khoảng cách nhỏ hơn) có trọng số cao hơn
- Weight = 1 / distance
- Winner = class có trọng số cao nhất

**Triển khai**: Lớp `KNNVotingAndRecallService`
- Phương thức: `performVoting()` - bỏ phiếu đơn
- Phương thức: `performWeightedVoting()` - bỏ phiếu có trọng số

### 7. Yêu cầu độ tin cậy

#### a) P-value < 0.05
**Ý nghĩa**: Có ý nghĩa thống kê

**Tính toán**: Chi-square test
- χ² = Σ((O-E)²/E)
- p-value ≈ erfc(√(χ²/2))

#### b) Recall ≥ 80%
**Ý nghĩa**: Phát hiện tốt

**Công thức**:
```
Recall = TP / (TP + FN) × 100%

TP = True Positive (phát hiện đúng fraud)
FN = False Negative (bỏ lỡ fraud)
```

**Ví dụ**:
- TP = 80 cases (phát hiện đúng fraud)
- FN = 20 cases (bỏ lỡ fraud)
- Recall = 80/(80+20) × 100% = 80% ✓ (đạt yêu cầu)

**Triển khai**: Lớp `KNNVotingAndRecallService`
- Phương thức: `calculateRecall()`
- Trả về: `RecallMetrics` (bao gồm recall, precision, F-score)

### 8. Yêu cầu tích hợp Rule-Based

**QUAN TRỌNG**: KNN KHÔNG thay thế Rule-Based

**KNN chỉ hỗ trợ**:
- Phân tích hành vi (behavioral analysis)
- Phát hiện pattern mới
- Tăng độ chính xác

**Rule-Based vẫn là nền tảng**: Các luật kiểm tra chứng thực

---

## II. PHƯƠNG PHÁP PHÂN TÍCH THEO MIỀN (MULTI-REGION ANALYSIS)

### 1. Yêu cầu phân chia miền

**Hệ thống cần 3 miền**:

```
Miền          Vai trò                    Ví dụ
SAFE          Đặc trưng an toàn         [1, 2, 0, 0, 0]
SUSPICIOUS    Đặc trưng nghi ngờ        [5, 8, 3, 1, 2]
FRAUD         Đặc trưng vi phạm         [20, 30, 8, 5, 7]
```

**QUAN TRỌNG**:
- Miền KHÔNG phải loại người dùng
- Miền là tập hợp đặc trưng hành vi

**Triển khai**: Enum `RegionType` với 3 giá trị: SAFE, SUSPICIOUS, FRAUD

### 2. Yêu cầu vector trung tâm miền

**Mỗi miền cần Center Vector** - đặc trưng điển hình

**Triển khai** (`MultiRegionAnalysisService`):
```java
// SAFE region
[ipCount=1, urlCount=2, emailCount=5, domainCount=3, 
 failedLogins=0, frequency=1.0]

// SUSPICIOUS region
[ipCount=5, urlCount=8, emailCount=10, domainCount=6,
 failedLogins=3, frequency=3.5]

// FRAUD region
[ipCount=15, urlCount=20, emailCount=25, domainCount=18,
 failedLogins=8, frequency=10.0]
```

### 3. Yêu cầu đo khoảng cách tới miền

**Hệ thống phải tính**:
- distanceToSafe
- distanceToSuspicious
- distanceToFraud

**Ý nghĩa**: Khoảng cách càng nhỏ, càng giống miền đó

**Triển khai** (`MultiRegionAnalysisService`):
- Sử dụng cả 3 thuật toán: Euclidean, Minkowski, Hamming
- Kết quả: Average distance từ 3 thuật toán
- Convert sang probability: `prob = e^(-distance * k)`

### 4. Yêu cầu trọng số đặc trưng

**Vấn đề**: Không phải đặc trưng nào cũng quan trọng như nhau

**Ví dụ**:
```
Đặc trưng      Trọng số
VPN            5
blacklist      10
TOR            12
```

**Ý nghĩa**: Một đặc trưng nguy hiểm mạnh có thể kéo node về miền vi phạm

**Triển khai**: Lớp `FeatureWeightsService`
- Phương thức: `calculateWeightedDistance()`
- Phương thức: `calculateRiskScoreByWeights()`
- Trọng số có thể cấu hình động

### 5. Yêu cầu xác suất thống kê

**Sau khi tính khoảng cách, tính: Probability Of Region**

**Ví dụ**:
```
Miền         Xác suất
SAFE         20%
FRAUD        90%
```

**Ý nghĩa**: Đánh giá độ tin cậy node thuộc miền nào

**Tính toán**: 
```
prob(region) = e^(-distance * sensitivity_factor)
```

**Chuẩn hóa**: Σ prob(all regions) = 1.0

**Triển khai**: `MultiRegionAnalysisService.analyzeAgainstRegions()`

### 6. Yêu cầu tích hợp KNN

**Phương pháp miền cần**: KNN Multi Distance

**Để**:
- Đo khoảng cách chính xác hơn
- Tăng độ tin cậy phân tích

**Triển khai**: `MultiRegionAnalysisService` sử dụng cả 3 distance metrics

### 7. Yêu cầu Consensus Engine

**Hệ thống cần**: Consensus Logic

**Để**:
- So sánh kết quả
- Giảm sai số
- Xử lý trường hợp lệch dữ liệu

#### Ví dụ 1: Độ tin cậy cao (High Convergence)
```
Thuật toán   Kết quả
Euclidean    90%
Minkowski    88%
Hamming      95%
=> Độ tin cậy cao (Convergence = 85%)
```

#### Ví dụ 2: Độ tin cậy thấp (Low Convergence)
```
Thuật toán   Kết quả
Euclidean    10%
Minkowski    15%
Hamming      95%
=> Tăng mức nghi ngờ và giám sát (Convergence = 20%)
```

**Triển khai**: Lớp `EnhancedConsensusEngineService`
- `ConvergenceAnalysis` - phân tích sự hội tụ của các thuật toán
- `MethodAgreementAnalysis` - phân tích thỏa thuận giữa các phương pháp
- Phương thức: `produceEnhancedConsensus()`

### 8. Yêu cầu tích hợp Rule-Based

**QUAN TRỌNG NHẤT**: Phương pháp miền KHÔNG thay thế Rule-Based

**Rule-Based vẫn là lõi chính vì**:
- Đã kiểm chứng
- Ổn định
- Đáng tin cậy

---

## III. KẾT QUẢ CUỐI CÙNG - HYBRID SYSTEM

### Sau khi tích hợp, hệ thống sẽ có:

```
Thành phần              Vai trò
Rule-Based              Nền tảng phát hiện
KNN                     Tìm hành vi tương tự
Multi Distance          Tăng độ chính xác
Multi Region            Xác định vùng hành vi
Probability             Tăng độ tin cậy
Consensus               Giảm sai số
Neo4j                   Phân tích quan hệ
```

### Quy trình phân tích hoàn chỉnh:

```
1. INPUT: Behavior Feature Vector
   ↓
2. Rule-Based Analysis (40% weight)
   - Kiểm tra các luật
   - Output: ruleScore
   ↓
3. Multi-Region Analysis (20% weight)
   - Tính distance tới 3 miền
   - Tính probability
   - Output: regionScore
   ↓
4. KNN Analysis (25% weight)
   - Tìm K=7 nearest neighbors
   - Euclidean, Minkowski, Hamming distances
   - Convergence analysis
   - Weighted voting
   - Output: knnScore
   ↓
5. Statistical Probability (15% weight)
   - Bayesian analysis
   - P-value calculation
   - Output: probabilityScore
   ↓
6. Consensus Engine
   - Kết hợp 4 scores
   - Phát hiện anomaly
   - Tính confidence
   ↓
7. OUTPUT: Final Risk Assessment
   - Risk Level (SAFE / SUSPICIOUS / CRITICAL)
   - Confidence Score
   - Detailed Analysis
```

---

## IV. CÁC DỊCH VỤ CHÍNH

### 1. FeatureNormalizationUtility
**Chức năng**: Chuẩn hóa dữ liệu
- `normalizeNumericFeatures()` - chuẩn hóa dữ liệu số
- `normalizeBooleanFeatures()` - chuyển boolean sang [0,1]
- `normalizeFullVector()` - chuẩn hóa vector đầy đủ
- `calculateStatistics()` - tính thống kê cho các vector
- `updateBounds()` - cập nhật giới hạn động

### 2. FeatureWeightsService
**Chức năng**: Quản lý trọng số đặc trưng
- TOR Network: 12.0 (cao nhất)
- Blacklist: 10.0
- Spam Pattern: 8.0
- Suspicious URL: 7.0
- Failed Login Count: 6.0
- VPN: 5.0
- Phương thức: `calculateWeightedDistance()`, `calculateRiskScoreByWeights()`

### 3. KNNVotingAndRecallService
**Chức năng**: Bỏ phiếu và tính recall
- `performVoting()` - bỏ phiếu đơn
- `performWeightedVoting()` - bỏ phiếu có trọng số
- `calculateRecall()` - tính recall metrics
- `calculatePValue()` - tính p-value

### 4. EnhancedConsensusEngineService
**Chức năng**: Tổng hợp kết quả
- `ConvergenceAnalysis` - phân tích hội tụ (0-1)
- `MethodAgreementAnalysis` - phân tích thỏa thuận
- `produceEnhancedConsensus()` - tạo consensus

---

## V. THAM SỐ HỆ THỐNG

### Trọng số Consensus
```
Rule-Based Weight:      0.40 (40%)
KNN Weight:            0.25 (25%)
Multi-Region Weight:   0.20 (20%)
Probability Weight:    0.15 (15%)
```

### Ngưỡng Xác suất (Probability Thresholds)
```
SAFE Risk:           < 0.33 (33%)
SUSPICIOUS Risk:     0.33 - 0.67
CRITICAL Risk:       > 0.67 (67%)
```

### Yêu cầu Độ tin cậy
```
P-value:             < 0.05 (Statistically Significant)
Recall:              ≥ 80% (Acceptable Detection Rate)
Convergence:         > 0.7 (High Agreement)
```

### Liabilities
```
K-value:             7 (số lượng nearest neighbors)
Minkowski P:         2.0 (tương đương Euclidean khi p=2)
```

---

## VI. YÍ NGHĨA HỌC THUẬT

### Mô hình của hệ thống:
**Hybrid Multi-Region Fraud Detection System**

### Kết hợp:
1. **Rule-Based Detection** - Hệ thống chuyên gia truyền thống
2. **Behavioral Analysis** - Phân tích hành vi thông qua KNN
3. **Machine Learning** - KNN là một thuật toán ML cơ bản
4. **Statistical Analysis** - Phân tích Bayesian, p-value, recall
5. **Graph Analysis** - Neo4j cho phân tích quan hệ

### Ưu điểm:
- ✓ Kết hợp nhiều phương pháp = độ tin cậy cao
- ✓ Consensus mechanism = giảm sai số
- ✓ Rule-Based nền tảng = ổn định, đáng tin cậy
- ✓ KNN + Multi-Region = linh hoạt, phát hiện pattern mới
- ✓ Statistical validation = có cơ sở toán học

---

## VII. HƯỚNG DẪN TRIỂN KHAI

### 1. Chuẩn bị dữ liệu
```java
BehaviorFeatureVector node = new BehaviorFeatureVector(
    ipCount: 8,
    urlCount: 15,
    emailCount: 5,
    domainCount: 4,
    failedLoginCount: 2,
    requestFrequency: 5.5,
    vpn: true,
    blacklist: false,
    suspiciousUrl: true,
    torNetwork: false,
    spamPattern: false,
    abnormalAccessTime: true
);
```

### 2. Chạy phân tích Hybrid
```java
HybridFraudDetectionResult result = hybridFraudDetectionService.analyzeNode(
    nodeFeatures,
    historicalSamples
);
```

### 3. Kết quả
```
Rule-Based Analysis:        75%
KNN Analysis:              82%
Multi-Region Analysis:     78%
Probability Analysis:      80%

Final Risk Score:          78.75%
Risk Level:                SUSPICIOUS
Confidence:                85%

Anomalies Detected:        None
Statistical Significance:  p-value = 0.0023 ✓
```

---

## VIII. KIỂM THỬ VÀ XÁC MINH

### Test Cases
1. **SAFE Cases**: Tất cả điểm < 30%
2. **SUSPICIOUS Cases**: Điểm 30-70%
3. **FRAUD Cases**: Điểm > 70%
4. **Edge Cases**: Hành vi mập mờ, disagreement giữa các phương pháp

### Metrics theo dõi
- Recall ≥ 80%
- Precision ≥ 75%
- F-Score ≥ 0.77
- Convergence ≥ 0.7 cho high-confidence cases

---

**Phiên bản**: 1.0  
**Ngày cập nhật**: 2026-05-14  
**Trạng thái**: Production Ready
