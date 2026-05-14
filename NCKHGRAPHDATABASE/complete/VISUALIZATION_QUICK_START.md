# 🚀 Visualization Quick Start

## ⚡ Bắt Đầu Nhanh (5 phút)

### Bước 1: Build Project
```bash
cd NCKHGRAPHDATABASE/complete
mvn clean package
```

### Bước 2: Chạy Application
```bash
# Phương pháp 1: Java CLI
java -jar target/neo4j-auth-0.0.1-SNAPSHOT.jar

# Phương pháp 2: Maven Spring Boot
mvn spring-boot:run
```

**Output:**
```
Started Application in 8.234 seconds
Tomcat started on port(s): 8080
```

### Bước 3: Truy Cập Visualization
```
http://localhost:8080/visualization
```

### Bước 4: Load Dữ Liệu Ví Dụ
- Nhấn nút **"📋 Ví dụ"** trong sidebar
- Chờ 2-3 giây để load dữ liệu

---

## 🎯 Các Tính Năng Chính

### 1. Xem Đồ Thị Liên Kết
```
SAFE (Blue) ← → SUSPICIOUS (Orange) ← → FRAUD (Red)
    ↓
  Users grouped by risk score
```

### 2. Tương Tác
- **Di chuyển**: Click + Drag node
- **Phóng to**: Scroll up
- **Thu nhỏ**: Scroll down
- **Pan**: Click + Drag canvas

### 3. Xem Chi Tiết
- Hover qua node → Tooltip hiển thị:
  - User ID
  - Region
  - Risk Score %
  - Vị trí (x, y)

### 4. Thống Kê Thời Gian Thực
```
SAFE:       5 users
SUSPICIOUS: 5 users
FRAUD:      5 users
Total:      15 users
```

---

## 📡 API Endpoints

### Get Example Data
```bash
curl http://localhost:8080/api/visualization/example
```

### Get Region Definition
```bash
curl http://localhost:8080/api/visualization/regions
```

### Custom Visualization
```bash
curl -X POST http://localhost:8080/api/visualization/graph \
  -H "Content-Type: application/json" \
  -d '{
    "userIds": ["alice", "bob"],
    "classifications": {"alice": "SAFE", "bob": "FRAUD"},
    "riskScores": {"alice": 0.1, "bob": 0.9}
  }'
```

---

## 🎨 Region Meanings

| Region | Color | Risk | Meaning |
|--------|-------|------|---------|
| **SAFE** | 🔵 Blue | 0-0.25 | Hành động hợp lệ |
| **SUSPICIOUS** | 🟠 Orange | 0.25-0.65 | Bất thường, cần kiểm tra |
| **FRAUD** | 🔴 Red | 0.65-1.0 | Gian lận rõ ràng |

---

## 📊 Expected Output

```
Canvas: 1200 × 600 pixels
Nodes:  15 (5 per region)
Edges:  11 connections
Regions: 3 circles with defined radius

Node Size: 15-45px (based on risk score)
Color: Automatic by region
Zoom: Interactive D3.js
```

---

## ✅ Verification Checklist

- [ ] Server started on port 8080
- [ ] Visualization page loads without errors
- [ ] Example button works and loads 15 nodes
- [ ] All 3 regions visible with correct colors
- [ ] Nodes positioned within region circles
- [ ] Hover tooltip shows user information
- [ ] Zoom/Pan controls responsive
- [ ] Console has no errors (F12)

---

## 🆘 Common Issues

### Blank Canvas
**Fix:** Check browser console (F12) for errors

### API Not Responding
**Fix:** 
```bash
# Verify server is running
curl http://localhost:8080/api/visualization/example

# Check if port 8080 is in use
netstat -ano | findstr :8080 (Windows)
lsof -i :8080 (Mac/Linux)
```

### Nodes Outside Circles
**Fix:** Refresh page (Ctrl+F5)

---

## 🔗 Related Links
- [Full Documentation](VISUALIZATION_GUIDE.md)
- [API Tests](API_TEST_DEMONSTRATIONS.md)
- [Integration Tests](INTEGRATION_TEST_COMPLETE.md)
- [Quick Reference](QUICK_REFERENCE.md)

---

**Time to First Visualization:** ~2-3 minutes ⏱️
