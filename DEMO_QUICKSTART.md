# DEMO QUICKSTART - PHƯƠNG PHÁP MIỀN THỰC SỰ

## ⚡ Bắt Đầu Nhanh - 5 Phút

### 1️⃣ Start Server

```bash
cd NCKHGRAPHDATABASE/complete
mvn spring-boot:run
```

Chờ tới khi thấy:
```
Started ServingWebContentApplication in X seconds (JVM running for Y seconds)
```

### 2️⃣ Test Endpoints

#### DEMO 1: Ba loại node (SAFE, SUSPICIOUS, FRAUD)
```bash
curl http://localhost:8080/api/multiregion/demo/three-regions
```

**Kết quả:**
```json
{
  "status": "SUCCESS",
  "title": "DEMO: 3 LOẠI NODE - PHƯƠNG PHÁP MIỀN THỰC SỰ",
  "results": [
    {
      "nodeType": "SAFE_NODE",
      "primaryRegion": "SAFE",
      "distances": {
        "SAFE": "0.50",
        "SUSPICIOUS": "8.24",
        "FRAUD": "12.15"
      },
      "probabilities": {
        "SAFE": "96.50%",
        "SUSPICIOUS": "2.40%",
        "FRAUD": "1.10%"
      }
    },
    ...
  ]
}
```

#### DEMO 2: Trường hợp phức tạp (ANOMALY)
```bash
curl http://localhost:8080/api/multiregion/demo/complex-case
```

**Kết quả:**
```json
{
  "status": "SUCCESS",
  "title": "DEMO: TRƯỜNG HỢP PHỨC TẠP - MẦU THUẪN",
  "anomaly": {
    "score": "0.42",
    "status": "🔴 ANOMALY DETECTED!",
    "reason": "VPN=true nhưng Blacklist=false → Mâu thuẫn trong hành vi"
  },
  "region_analysis": {
    "primary_region": "SUSPICIOUS",
    "fraud_probability": "25.30%"
  }
}
```

#### DEMO 3: So sánh Rule-Based vs Multi-Region
```bash
curl http://localhost:8080/api/multiregion/demo/rule-based-vs-region
```

**Kết quả:**
```json
{
  "status": "SUCCESS",
  "title": "DEMO: RULE-BASED vs MULTI-REGION",
  "description": "Cùng rule-based score nhưng miền khác → Multi-region tốt hơn",
  "comparison": [
    {
      "node": "A: VPN + Spam",
      "rule_based_score": "~30",
      "multi_region": {
        "primary_region": "SUSPICIOUS",
        "fraud_probability": "35.20%"
      }
    }
  ],
  "conclusion": "Multi-region phân biệt loại hành vi khác nhau"
}
```

#### DEMO 4: Định nghĩa 3 miền
```bash
curl http://localhost:8080/api/multiregion/demo/definitions
```

---

### 3️⃣ Test Phân Tích Custom Node

```bash
# POST một node custom
curl -X POST http://localhost:8080/api/multiregion/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "ipCount": 8,
    "urlCount": 12,
    "emailCount": 10,
    "domainCount": 6,
    "failedLoginCount": 2,
    "requestFrequency": 3.0,
    "vpn": true,
    "blacklist": false,
    "suspiciousUrl": true,
    "torNetwork": false,
    "spamPattern": true,
    "abnormalAccessTime": true
  }'
```

**Kết quả:**
```json
{
  "status": "SUCCESS",
  "node": {
    "ipCount": 8,
    "urlCount": 12,
    "emailCount": 10,
    ...
  },
  "primary_region": "SUSPICIOUS",
  "probabilities": {
    "SAFE": "15.20%",
    "SUSPICIOUS": "60.50%",
    "FRAUD": "24.30%"
  },
  "distances": {
    "SAFE": "8.24",
    "SUSPICIOUS": "2.10",
    "FRAUD": "4.50"
  }
}
```

### 4️⃣ Test Sample Nodes

```bash
# Sample SAFE node
curl http://localhost:8080/api/multiregion/sample-nodes/safe

# Sample SUSPICIOUS node
curl http://localhost:8080/api/multiregion/sample-nodes/suspicious

# Sample FRAUD node
curl http://localhost:8080/api/multiregion/sample-nodes/fraud
```

---

## 📊 Hiểu Kết Quả

### Cách Đọc Output

```json
{
  "primary_region": "SUSPICIOUS",        ← Miền gần nhất
  "distances": {
    "SAFE": "8.24",                       ← Khoảng cách lớn = xa
    "SUSPICIOUS": "2.10",                 ← Khoảng cách nhỏ = gần ✓
    "FRAUD": "4.50"
  },
  "probabilities": {
    "SAFE": "15.20%",                     ← Xác suất nhỏ
    "SUSPICIOUS": "60.50%",               ← Xác suất cao ✓
    "FRAUD": "24.30%"
  }
}
```

### Ý Nghĩa

- **primary_region**: Miền mà node gần nhất
- **distances**: Khoảng cách từ node tới tâm mỗi miền (nhỏ = gần)
- **probabilities**: Xác suất node thuộc miền nào (tổng = 100%)
- **anomalyScore**: Mâu thuẫn trong hành vi (> 0.4 = anomaly)

---

## 🎯 Ví Dụ Chính Xác Từ Yêu Cầu

### Input
```json
{
  SAFE REGION:   VPN=false, Blacklist=false, Spam=false
  FRAUD REGION:  VPN=true, Blacklist=true, TOR=true
  
  CURRENT NODE:  VPN=true, Blacklist=true, Spam=false
}
```

### Output
```json
{
  "distances": {
    "SAFE": "80",         ← Xa SAFE
    "SUSPICIOUS": "25",   ← Xa SUSPICIOUS
    "FRAUD": "10"         ← GẦN FRAUD ✓ (vì 2/3 flags khớp)
  },
  "probabilities": {
    "SAFE": "3%",
    "SUSPICIOUS": "8%",
    "FRAUD": "89%"        ← Xác suất cao vì gần FRAUD region
  }
}
```

**KẾT LUẬN: Node gần FRAUD region nhất vì có VPN + Blacklist!**

---

## 📋 Tất Cả Endpoints

| Endpoint | Method | Mô Tả |
|----------|--------|--------|
| `/api/multiregion/demo/three-regions` | GET | Demo 3 loại node |
| `/api/multiregion/demo/complex-case` | GET | Demo trường hợp mâu thuẫn |
| `/api/multiregion/demo/rule-based-vs-region` | GET | So sánh Rule-Based vs Region |
| `/api/multiregion/demo/definitions` | GET | Định nghĩa 3 miền |
| `/api/multiregion/analyze` | POST | Phân tích custom node |
| `/api/multiregion/sample-nodes/safe` | GET | Sample SAFE node |
| `/api/multiregion/sample-nodes/suspicious` | GET | Sample SUSPICIOUS node |
| `/api/multiregion/sample-nodes/fraud` | GET | Sample FRAUD node |
| `/api/multiregion/regions` | GET | Thông tin miền |

---

## 🚀 Chạy Tests

```bash
mvn test -Dtest=MultiRegionAnalysisUnitTest
```

**Kết quả:**
```
Tests run: 10, Failures: 0, Errors: 0
BUILD SUCCESS
```

---

## 📖 Tài Liệu Chi Tiết

- [PHƯƠNG_PHÁP_MIỀN_KIỂM_CHỨNG.md](./PHƯƠNG_PHÁP_MIỀN_KIỂM_CHỨNG.md) - Chứng minh hệ thống
- [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md) - Cách dùng trong code
- [MULTIREGION_SYSTEM_STATUS.md](./MULTIREGION_SYSTEM_STATUS.md) - Status báo cáo

---

## 🐛 Troubleshoot

**Q: Port 8080 already in use?**
```bash
netstat -ano | findstr :8080
# Kill process: taskkill /PID <PID> /F
```

**Q: Neo4j connection error?**
- Kiểm tra `application.properties`
- Đảm bảo database neo4j server đang chạy

**Q: Build failed?**
```bash
mvn clean compile -DskipTests
```

---

**Demo hoàn thiện! ✅ Hệ thống sẵn sàng sử dụng!**
