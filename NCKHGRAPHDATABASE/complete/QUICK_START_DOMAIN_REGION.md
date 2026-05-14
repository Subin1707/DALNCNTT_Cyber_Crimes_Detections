# 🚀 Quick Start Guide - Domain Region Visualization

## Bắt đầu nhanh trong 5 phút

### Bước 1: Khởi chạy ứng dụng

```bash
cd e:\DALNCNTT_Cyber_Crimes_Detections\NCKHGRAPHDATABASE\complete
mvn clean spring-boot:run
```

Hoặc sử dụng Gradle:

```bash
./gradlew bootRun
```

### Bước 2: Mở trình duyệt

```
http://localhost:8080/visualization
```

### Bước 3: Tạo Demo

1. Nhấp vào dropdown **"Chọn Distance Metric"**
2. Chọn một thuật toán (mặc định: Euclidean)
3. Nhấp nút **"🎯 Tạo Demo"**
4. Đợi visualization tải

---

## 📊 Giao diện chính

### Legend (Huyền thoại)

```
🟢 SAFE REGION          🟠 SUSPICIOUS REGION     🔴 FRAUD REGION
Xanh lá nhạt            Vàng nhạt                Đỏ nhạt
Xanh lá đậm (viền)      Cam (viền)               Đỏ đậm (viền)
Xanh dương (nodes)      Cam (nodes)              Đỏ đậm (nodes)
```

### Metrics

Hiển thị thống kê:
- **Tổng Nodes:** Số lượng đối tượng dữ liệu
- **Distance Metric:** Thuật toán khoảng cách hiện tại
- **Avg Risk Score:** Mức độ nguy hiểm trung bình
- **Regions:** Số miền (luôn = 3)

---

## 🎨 Các Distance Metrics

### Euclidean (Mặc định)

Phù hợp cho: Dữ liệu liên tục, khoảng cách thực

```
Formula: √(x₁² + y₁² + ... + xₙ²)
Ví dụ: [0,0] → [3,4] = 5
```

### Manhattan

Phù hợp cho: Grid-based, urban distances

```
Formula: |x₁| + |y₁| + ... + |xₙ|
Ví dụ: [0,0] → [3,4] = 7
```

### Minkowski

Phù hợp cho: Dữ liệu nhiều chiều

```
Formula: (|x₁|ᵖ + |y₁|ᵖ + ... + |xₙ|ᵖ)^(1/p)
Trường hợp: p=1 (Manhattan), p=2 (Euclidean)
```

### Hamming

Phù hợp cho: Dữ liệu boolean

```
Formula: Số vị trí khác nhau
Ví dụ: [1,0,1] vs [1,1,0] = 2
```

### Cosine

Phù hợp cho: Vector cao chiều, NLP

```
Formula: 1 - (u·v)/(‖u‖·‖v‖)
Ứng dụng: So sánh hướng vector
```

---

## 📈 Các Demo Sẵn

### Demo 1: Safe Users

```
SAFE REGION có:
- UserA (IP ổn định, không spam)
- UserB (Request bình thường)
- UserC (Lịch sử sạch)

Màu: Xanh lá, viền xanh dương
KNN: 3 users gần nhau
```

### Demo 2: Suspicious Activity

```
SUSPICIOUS REGION có:
- UserX (Nhiều thẻ tín dụng)
- UserY (VPN được phát hiện)
- UserZ (Mua sắm bất thường)

Màu: Vàng nhạt, viền cam
KNN: Mixed pattern detection
```

### Demo 3: Fraud Patterns

```
FRAUD REGION có:
- BotA (Tự động spam)
- BotB (Chargeback scam)
- MalwareX (C&C bot)
- SpamY (Phishing)

Màu: Đỏ nhạt, viền đỏ đậm
KNN: Botnet cluster detected
```

---

## 🔧 Cấu hình

### Đặt Distance Metric mặc định

```java
@Configuration
public class VisualizationConfig {
    
    @Bean
    public DomainRegionVisualizationService visualizationService() {
        DomainRegionVisualizationService service = 
            new DomainRegionVisualizationService();
        service.setDistanceMetric("euclidean");
        return service;
    }
}
```

### Tuning KNN

```java
// Trong NodeVisualization
node.setK(5);  // Tăng số neighbors từ 3 lên 5
```

### Tùy chỉnh Màu

```java
// Trong RegionVisualization.java
RegionColorScheme.SAFE.getBackgroundColor()  // "#90EE90"
```

---

## 🧪 Chạy Tests

### Chạy tất cả tests

```bash
mvn test
```

### Chạy specific test

```bash
mvn test -Dtest=DomainRegionVisualizationTest
```

### Test reports

```bash
# Sau khi chạy tests
open target/surefire-reports/index.html
```

---

## 🌐 REST API Quick Reference

### Tạo Demo

```bash
curl -X POST "http://localhost:8080/visualization/demo?metric=euclidean"
```

### Lấy Visualization HTML

```bash
curl "http://localhost:8080/visualization/html" > viz.html
```

### Lấy Báo cáo

```bash
curl "http://localhost:8080/visualization/report" > report.txt
```

### Lấy Danh sách Metrics

```bash
curl "http://localhost:8080/visualization/metrics"
```

---

## 📚 Tệp chính

| File | Chức năng |
|------|----------|
| `RegionVisualization.java` | Mô hình miền hành vi |
| `NodeVisualization.java` | Mô hình node/đối tượng |
| `DistanceMetric.java` | Các thuật toán khoảng cách |
| `DomainRegionVisualizationService.java` | Business logic chính |
| `DomainRegionVisualizationController.java` | REST API endpoints |
| `DomainRegionVisualizationTest.java` | Integration tests |
| `domain-region-visualization.html` | Web UI |

---

## 🐛 Troubleshooting

### Vấn đề: Visualization không tải

**Giải pháp:**
1. Kiểm tra console xem có error không
2. Kiểm tra port 8080 có sẵn không
3. Refresh page hoặc xóa cache

### Vấn đề: Distance Metrics không thay đổi

**Giải pháp:**
1. Nhấp "Tạo Demo" sau khi thay đổi metric
2. Kiểm tra browser console (F12) xem có error không

### Vấn đề: Nodes không hiển thị

**Giải pháp:**
1. Kiểm tra các sample nodes có được tạo không
2. Kiểm tra SVG render engine
3. Thử metric khác

---

## 💡 Tips & Tricks

### 1. So sánh các Distance Metrics

```
Cùng dataset, thay đổi metric:
- Euclidean: Khoảng cách 5
- Manhattan: Khoảng cách 7
- Hamming: Khoảng cách 2
```

### 2. Phân tích Node gần biên

```
Nodes ở biên SAFE/SUSPICIOUS
=> Có hành vi nghi ngờ nhưng chưa là fraud
=> Cần follow-up monitoring
```

### 3. Phát hiện Cluster

```
Multiple nodes gần nhau trong FRAUD
=> Cùng pattern gian lận
=> Cùng nhóm bot/attacker
=> Nguy hiểm cao
```

---

## 📖 Tài liệu thêm

- **Full Documentation:** [DOMAIN_REGION_VISUALIZATION_GUIDE.md](DOMAIN_REGION_VISUALIZATION_GUIDE.md)
- **Integration Tests:** [DomainRegionVisualizationTest.java](src/test/java/com/example/servingwebcontent/DomainRegionVisualizationTest.java)
- **Source Code:** [src/main/java/com/example/servingwebcontent/](src/main/java/com/example/servingwebcontent/)

---

## 🎓 Khái niệm cơ bản

### Miền (Region)

Vùng hành vi trong không gian dữ liệu, chứa các node có đặc điểm tương tự.

```
SAFE (xanh) <- SUSPICIOUS (vàng) <- FRAUD (đỏ)
```

### Node

Đối tượng dữ liệu (User, IP, Domain, etc.) có vector đặc trưng và mức độ nguy hiểm.

### Center Vector

Đặc trưng trung tâm của miền, đại diện cho hành vi điển hình.

### KNN

K-Nearest Neighbors - tìm k node lân cận gần nhất.

---

**Hệ thống sẵn sàng! Bắt đầu trực quan hóa ngay.** 🎉

---

**Version:** 1.0 | **Last Updated:** 2024
