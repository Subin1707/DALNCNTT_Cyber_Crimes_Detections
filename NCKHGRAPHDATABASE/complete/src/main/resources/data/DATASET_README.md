# Algorithm Evaluation Dataset

Trang `/algorithm-comparison` đọc dữ liệu đánh giá thuật toán từ:

```text
src/main/resources/data/algorithm-evaluation-samples.csv
```

Dataset này được thiết kế theo dạng gần với file Excel mà hệ thống đang import. File chỉ chứa dữ liệu thô và nhãn thật, không khai báo sẵn các feature như `blacklist`, `vpn`, `suspicious_url`, `request_frequency` hay khoảng cách tới từng vùng. Các feature đó phải do hệ thống tự trích xuất trong lúc đánh giá.

## Schema CSV

CSV bắt buộc có các cột:

```text
node_id,ground_truth_label,split,evidence_source,email,ip,url,domain,file_node,file_hash,victim_account
```

Ý nghĩa từng cột:

- `node_id`: mã mẫu dùng để truy vết kết quả dự đoán.
- `ground_truth_label`: nhãn thật dùng để đối chiếu sau khi thuật toán đã dự đoán; chỉ nhận `SAFE`, `SUSPICIOUS`, `FRAUD`.
- `split`: phân chia dữ liệu, gồm `train` hoặc `test`.
- `evidence_source`: nguồn xác minh của mẫu, ví dụ `PhishTank`, `URLhaus`, `CIC`, `EnronSpam`, file import hoặc mã nguồn kiểm chứng nội bộ.
- `email`, `ip`, `url`, `domain`, `file_node`, `file_hash`, `victim_account`: dữ liệu thô giống các cột Excel người dùng nạp vào hệ thống.

## Cách Chia Dữ Liệu

- Các dòng `split=train` được dùng làm tập huấn luyện cho KNN.
- Các dòng `split=test` được dùng làm tập kiểm thử cho toàn bộ thuật toán.
- Rule-based scoring và Multi-Region không dùng `split=train` để học mô hình, nhưng vẫn được đánh giá trên cùng tập `test` để kết quả so sánh công bằng.
- `ground_truth_label` tuyệt đối không được truyền vào classifier. Nhãn này chỉ được dùng sau khi có dự đoán để tính chỉ số đánh giá.

## Luồng Đánh Giá

1. Hệ thống đọc từng dòng CSV theo schema ở trên.
2. Dữ liệu thô được đưa vào `FraudInputDTO`.
3. `FraudAnalysisService.extractBehaviorFeatures(...)` tự trích xuất `BehaviorFeatureVector` từ email, IP, URL, domain, file, hash và victim account.
4. Mỗi thuật toán nhận cùng một vector đặc trưng để dự đoán vùng rủi ro.
5. Sau khi dự đoán xong, hệ thống mới so sánh kết quả dự đoán với `ground_truth_label`.
6. Từ kết quả đối chiếu, hệ thống tính Accuracy, Precision, Recall, F1-score và confusion matrix.

Các thuật toán đang được so sánh:

- `KNN EUCLIDEAN`
- `KNN MANHATTAN`
- `KNN MINKOWSKI`
- `KNN HAMMING`
- `Rule-based scoring`
- `Multi-Region`

## Quy Trình 30 Lần So Sánh

Trang `/algorithm-comparison` không đánh giá một lần duy nhất rồi cố định ma trận. Tham số `Số lần test` quyết định số lượt so sánh độc lập cần chạy.

Ví dụ nhập `30`:

- Hệ thống chạy 30 lần test.
- Mỗi lần test tạo một tập mẫu kiểm thử riêng từ cùng `split=test`.
- Mỗi lần lấy mẫu theo từng nhãn `SAFE`, `SUSPICIOUS`, `FRAUD` để giữ tương đối cân bằng.
- Mỗi thuật toán đều được chạy trên cùng tập mẫu của lần đó.
- Sau mỗi lần chạy, từng thuật toán có một confusion matrix riêng.
- Vì vậy giao diện phải hiển thị 30 cụm: `Lần so sánh 1` đến `Lần so sánh 30`.

Trong mỗi cụm `Lần so sánh N`, người đọc có thể so sánh trực tiếp confusion matrix của các thuật toán với nhau. Đây là phần quan trọng: nếu có 30 lần test thì phải có 30 lần đối chiếu, không chỉ một ma trận tổng cộng.

## Confusion Matrix

Confusion matrix dùng hàng là nhãn thật và cột là nhãn dự đoán:

```text
Actual \ Pred | SAFE | SUSPICIOUS | FRAUD
SAFE          | ...  | ...        | ...
SUSPICIOUS    | ...  | ...        | ...
FRAUD         | ...  | ...        | ...
```

Cách đọc:

- Ô đường chéo chính là dự đoán đúng.
- Ô ngoài đường chéo là dự đoán nhầm.
- Mỗi `Lần so sánh N` có ma trận riêng cho từng thuật toán.
- Ma trận tổng cộng vẫn có thể dùng để tính tổng số đúng/sai, nhưng không thay thế phần hiển thị từng lần test.

Ví dụ nếu nhập `30`, giao diện cần thể hiện theo cấu trúc:

```text
Lần so sánh 1
- KNN HAMMING: confusion matrix của lần 1
- KNN MANHATTAN: confusion matrix của lần 1
- KNN EUCLIDEAN: confusion matrix của lần 1
- KNN MINKOWSKI: confusion matrix của lần 1
- Rule-based scoring: confusion matrix của lần 1
- Multi-Region: confusion matrix của lần 1

Lần so sánh 2
- KNN HAMMING: confusion matrix của lần 2
- ...

...

Lần so sánh 30
- KNN HAMMING: confusion matrix của lần 30
- ...
```

## Chỉ Số Tổng Hợp

Bảng tổng quan phía trên confusion matrix vẫn hiển thị số liệu tổng hợp qua nhiều lần chạy:

- `Accuracy`: trung bình Accuracy qua tất cả lần test.
- `Precision`: trung bình macro Precision qua tất cả lần test.
- `Recall`: trung bình macro Recall qua tất cả lần test.
- `F1-score`: trung bình macro F1 qua tất cả lần test.
- `Accuracy min-max`: Accuracy thấp nhất và cao nhất trong các lần test.
- `Độ lệch chuẩn`: mức dao động Accuracy giữa các lần test.
- `Đúng/Tổng`: tổng số dự đoán đúng trên tổng số dự đoán qua tất cả lần test.

Như vậy bảng tổng quan dùng để nhìn hiệu năng trung bình, còn confusion matrix theo từng lần dùng để kiểm tra chi tiết từng lượt so sánh.

## API Và Dữ Liệu Trả Về

Endpoint:

```text
GET /api/algorithm-comparison?k=5&repeatCount=30
```

Ý nghĩa tham số:

- `k`: số láng giềng gần nhất của KNN, được giới hạn trong khoảng 1 đến 15.
- `repeatCount`: số lần test, tối thiểu 30 và tối đa 200.

Mỗi thuật toán trong response có:

- `confusionMatrix`: ma trận cộng dồn qua tất cả lần test, dùng cho tổng hợp.
- `runMetrics`: danh sách kết quả từng lần test.
- `runMetrics[n].confusionMatrix`: confusion matrix riêng của lần test thứ `n`.
- `runMetrics[n].accuracy`, `precision`, `recall`, `f1Score`: chỉ số riêng của lần test đó.
- `runMetrics[n].correctPredictions`, `totalPredictions`: số mẫu đúng và tổng số mẫu của lần test đó.

UI hiện dùng `runMetrics[n].confusionMatrix` để vẽ từng cụm `Lần so sánh N`.

## Dataset Hiện Tại

Dataset hiện tại là CSV nội bộ dạng Excel-like để hệ thống chạy được, gồm:

- 24 mẫu `train`.
- 78 mẫu `test`.
- 3 nhãn: `SAFE`, `SUSPICIOUS`, `FRAUD`.

Để báo cáo nghiêm ngặt hơn, nên thay dữ liệu nội bộ bằng dữ liệu được trích xuất từ nguồn thật. Khi thay dữ liệu, vẫn giữ nguyên schema CSV ở trên, nhưng cần đảm bảo:

- `evidence_source` ghi rõ nguồn xác minh.
- `ground_truth_label` là nhãn thật được xác nhận độc lập với thuật toán.
- Không đưa feature đã xử lý sẵn vào CSV nếu muốn đánh giá đúng luồng import Excel của hệ thống.
