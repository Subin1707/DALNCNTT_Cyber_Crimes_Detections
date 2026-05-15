# Thuyết minh chuẩn đồ án: Rule Base, KNN và Multi-Region

Tài liệu này dùng để giải thích rõ kiến trúc phân loại gian lận trong đồ án, tránh hiểu nhầm rằng hệ thống cũ bị loại bỏ khi bổ sung KNN và phương pháp miền.

## 1. Rule Base không bị bỏ

Rule Base vẫn được giữ lại trong hệ thống. Vai trò của Rule Base không còn là tầng quyết định duy nhất, mà là:

- Tầng explainability: giải thích nguyên nhân node bị đánh dấu.
- Baseline: làm mốc so sánh với KNN và Multi-Region.
- Hỗ trợ feature engineering: các rule giúp xác định đặc trưng nào quan trọng để đưa vào feature vector.

Ví dụ:

```text
IF VPN = true AND TOR = true THEN risk = high
IF blacklist = true THEN risk = high
IF failed_login > threshold THEN suspicious
```

Các rule này giúp hệ thống trả lời câu hỏi: "Vì sao node bị nghi ngờ?". KNN và Region giúp trả lời câu hỏi tiếp theo: "Node này giống nhóm nào nhất trong không gian đặc trưng?".

## 2. KNN hoạt động trên feature vector

KNN không so sánh node bằng chuỗi text hay ID. Mỗi node phải được chuyển thành vector đặc trưng số.

Ví dụ node A:

```text
IP_COUNT        = 10
URL_COUNT       = 20
FAILED_LOGIN    = 8
VPN             = 1
TOR             = 1
BLACKLIST       = 0
```

Feature vector:

```text
A = [10, 20, 8, 1, 1, 0]
```

Sau đó hệ thống thực hiện:

```text
raw node
  -> feature extraction
  -> feature normalization
  -> feature vectorization
  -> distance calculation
  -> KNN classification
```

Đây mới là cách triển khai đúng bản chất machine learning: mọi node được biểu diễn trong cùng một không gian vector.

## 3. Feature Normalization là bắt buộc

Các đặc trưng có range khác nhau. Nếu không normalize, đặc trưng có range lớn sẽ dominate toàn bộ khoảng cách.

Ví dụ:

| Feature | Range |
|---|---:|
| failed_login | 0-10 |
| request_frequency | 0-1000 |
| VPN | 0-1 |
| TOR | 0-1 |

Nếu dùng trực tiếp dữ liệu thô, `request_frequency` sẽ lớn hơn nhiều và làm sai lệch khoảng cách.

Hệ thống dùng Min-Max Normalization:

```text
x' = (x - min(x)) / (max(x) - min(x))
```

Sau normalization, mọi feature nằm trong khoảng `[0, 1]`, giúp các thuật toán distance hoạt động công bằng hơn.

Ví dụ:

```text
failed_login = 8, min = 0, max = 10
x' = (8 - 0) / (10 - 0) = 0.8

request_frequency = 200, min = 0, max = 1000
x' = (200 - 0) / (1000 - 0) = 0.2
```

## 4. Mỗi miền có center vector

Phương pháp miền không chỉ tô màu node. Mỗi miền được định nghĩa bằng một vector trung tâm đại diện cho hành vi điển hình.

Ví dụ đơn giản:

```text
SAFE_CENTER        = [1,  2,  0, 0, 0]
SUSPICIOUS_CENTER  = [5,  8,  1, 0, 1]
FRAUD_CENTER       = [15, 20, 1, 1, 1]
```

Node mới được vector hóa rồi tính khoảng cách tới từng center:

```text
d_safe        = distance(node, SAFE_CENTER)
d_suspicious  = distance(node, SUSPICIOUS_CENTER)
d_fraud       = distance(node, FRAUD_CENTER)
```

Node thuộc miền có khoảng cách nhỏ nhất hoặc xác suất lớn nhất sau khi chuyển distance thành probability.

## 5. KNN và Region phải liên kết với nhau

KNN và Region không phải hai khối tách rời. Region là vùng hành vi trong không gian đặc trưng; KNN tìm láng giềng gần nhất trong chính không gian đó.

```text
SAFE REGION
  chứa nhiều SAFE labeled nodes

SUSPICIOUS REGION
  chứa nhiều SUSPICIOUS labeled nodes

FRAUD REGION
  chứa nhiều FRAUD labeled nodes
```

KNN tìm `K` node gần nhất trong toàn bộ không gian vector, sau đó vote theo nhãn của các neighbor:

```text
K nearest neighbors:
1. fraud node       distance = 0.12
2. suspicious node  distance = 0.18
3. fraud node       distance = 0.21
4. safe node        distance = 0.40
5. suspicious node  distance = 0.43
```

Kết quả có thể là:

```text
Fraud probability       = 40%
Suspicious probability  = 40%
Safe probability        = 20%
```

Nếu dùng distance-weighted voting, neighbor gần hơn có trọng số lớn hơn.

## 6. Training dataset cho KNN

KNN cần tập dữ liệu đã gán nhãn. Đây là điểm quan trọng để phân biệt KNN với rule-based scoring.

Dataset gồm các node lịch sử:

```text
Sample 1: vector = [...], label = SAFE
Sample 2: vector = [...], label = SUSPICIOUS
Sample 3: vector = [...], label = FRAUD
...
```

Khi có node mới:

```text
new_node_vector
  -> tính khoảng cách tới mọi sample đã gán nhãn
  -> chọn K sample gần nhất
  -> vote nhãn / tính xác suất
```

Vì vậy, KNN "học" từ tập node lịch sử đã được gán nhãn, không chỉ dựa vào rule cứng.

## 7. Evaluation chuyên nghiệp

Không chỉ dùng accuracy. Đồ án nên đánh giá thêm Precision, Recall và F1-score.

Accuracy:

```text
Accuracy = (TP + TN) / (TP + TN + FP + FN)
```

Precision:

```text
Precision = TP / (TP + FP)
```

Ý nghĩa: trong các node bị dự đoán là gian lận, bao nhiêu node thật sự gian lận.

Recall:

```text
Recall = TP / (TP + FN)
```

Ý nghĩa: trong các node gian lận thật, hệ thống phát hiện được bao nhiêu.

F1-score:

```text
F1 = 2 * (Precision * Recall) / (Precision + Recall)
```

Ý nghĩa: cân bằng giữa Precision và Recall.

## 8. Confusion Matrix

Confusion Matrix giúp trình bày kết quả đúng kiểu nghiên cứu.

| Actual / Predict | Safe | Suspicious | Fraud |
|---|---:|---:|---:|
| Safe | 10 | 1 | 0 |
| Suspicious | 2 | 8 | 1 |
| Fraud | 0 | 1 | 7 |

Từ ma trận này có thể tính Accuracy, Precision, Recall và F1-score cho từng lớp.

## 9. Vì sao KNN tốt hơn Rule Base trong phân lớp mềm

Rule Base là luật cứng:

```text
IF VPN = true AND TOR = true THEN FRAUD
```

Cách này dễ giải thích nhưng thiếu linh hoạt. Trong thực tế không phải mọi node có VPN đều gian lận.

KNN mềm hơn vì dựa trên mức độ giống nhau với các node lịch sử:

```text
VPN = true
TOR = false
BLACKLIST = false
FAILED_LOGIN = 7
```

Kết quả có thể là:

```text
Suspicious = 40%
Fraud      = 60%
```

Ý nghĩa nghiên cứu: không có gì tuyệt đối trong phân lớp hành vi. Node có thể nằm giữa hai miền và cần được biểu diễn bằng xác suất.

## 10. Kiến trúc chuẩn cuối cùng

```text
Input Node
    ↓
Feature Extraction
    ↓
Feature Normalization
    ↓
Rule Base Scoring
    ↓
Feature Vectorization
    ↓
Distance Calculation
(Euclidean / Manhattan / Minkowski / Hamming)
    ↓
KNN Classification
    ↓
Probability Calculation
    ↓
Region Determination
    ↓
Evaluation
(Accuracy / Precision / Recall / F1 / Confusion Matrix)
```

## 11. Cách trình bày khi bảo vệ

Có thể giải thích ngắn gọn như sau:

> Hệ thống không bỏ Rule Base. Rule Base được giữ lại như tầng giải thích, baseline và hỗ trợ chọn đặc trưng. KNN được bổ sung để phân lớp mềm dựa trên feature vector đã normalization. Mỗi node được biểu diễn trong không gian đặc trưng, so sánh với các node lịch sử đã gán nhãn và với center vector của từng miền SAFE, SUSPICIOUS, FRAUD. Kết quả cuối cùng không chỉ là nhãn, mà còn có xác suất thuộc miền, khoảng cách tới center và các chỉ số đánh giá như Precision, Recall, F1-score, Confusion Matrix.

