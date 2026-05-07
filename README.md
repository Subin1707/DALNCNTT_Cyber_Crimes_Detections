# DALN

## Tên đề tài
Xây dựng hệ thống phát hiện tội phạm mạng (A System for Cyber Crimes Detection)

## Tổng quan
Dự án là một ứng dụng web Spring Boot dùng Neo4j để mô hình hóa và phân tích quan hệ giữa các thực thể phục vụ phát hiện gian lận/tội phạm mạng. Hệ thống hiện tại hỗ trợ phân quyền theo vai trò, tạo phiên phân tích, nhập dữ liệu từ Excel, phân tích đơn lẻ, trực quan hóa graph và ghi nhật ký cảnh báo.

### Dữ liệu đầu vào
- Lưu lượng mạng và dấu vết truy xuất từ Wireshark, TShark
- File Excel chứa các trường thực thể đã chuẩn hóa
- Các thực thể đầu vào gồm Email, IP, URL, Domain, FileNode, FileHash, VictimAccount

### Công nghệ sử dụng
- Backend: Java, Spring Boot
- Frontend: Thymeleaf, HTML, CSS, JavaScript, D3.js
- Cơ sở dữ liệu đồ thị: Neo4j
- Xác thực và phân quyền: session theo vai trò Admin, Staff, Customer
- Đọc dữ liệu bảng: Apache POI

## Mục tiêu phát triển
Hệ thống được thiết kế theo hướng kết hợp ba lớp đánh giá rủi ro:
- Rule-based scoring: cộng điểm theo khuyến nghị, blacklist và các luật nghiệp vụ
- Classification layer: so sánh/đối chiếu các thuật toán phân lớp trên cùng bộ input đã có
- Probability layer: ước lượng xác suất xuất hiện của lớp đối tượng và các mẫu quan hệ

Trong code hiện tại, lớp phân tích đã có:
- Rule-based analysis cho từng thực thể
- Graph risk propagation dựa trên quan hệ trong Neo4j
- Hybrid scoring với KNN và Bayesian estimation
- Hỗ trợ mở rộng để bổ sung Logistic Regression, Decision Tree và J48 cho phần so sánh mô hình

## Kiến trúc hệ thống
### 1. Presentation Layer
- Trang đăng nhập, đăng ký, dashboard
- Màn hình phân tích cho Admin, Staff, Customer
- Trang quản lý user và node

### 2. Business Layer
- Xác thực và phân quyền
- Phân tích fraud theo từng thực thể
- Tạo AnalysisSession
- Tính risk score và verdict
- Phân tích graph và sinh chatbot giải thích

### 3. Data Layer
- Neo4j lưu node, relationship và session
- Repository cho User và AnalysisSession
- Query service để đọc, cập nhật, xóa, block/unblock node

## Các module chính
- AuthController: đăng nhập, đăng ký, đăng xuất
- DashboardController: điều hướng dashboard theo vai trò
- AdminController: quản lý staff, user, graph, session và node
- StaffController: phân tích, quản lý node, xem lịch sử, log và chatbot
- CustomerController: phân tích, xem graph, xem lịch sử, nhận giải thích
- FraudAnalysisService: rule engine và tạo node/quan hệ nền tảng
- AnalysisSessionService: xử lý batch phân tích theo phiên
- GraphQueryService: truy vấn và cập nhật đồ thị
- ExcelImportService: nhập và chuẩn hóa dữ liệu Excel
- GraphRiskService: lan truyền rủi ro trên đồ thị
- HybridRiskScoringService: kết hợp rule, KNN và Bayes
- EnhancedChatbotService: sinh phân tích nhiều lớp cho node
- AlertLoggingService: lưu cảnh báo và phát hiện

## Luồng xử lý chính
1. Người dùng đăng nhập theo vai trò.
2. Người dùng nạp dữ liệu thủ công hoặc upload Excel.
3. Hệ thống tạo AnalysisSession mới.
4. Dữ liệu được chuẩn hóa và kiểm tra hợp lệ.
5. FraudAnalysisService chấm điểm từng thực thể bằng rule-based engine.
6. GraphQueryService và GraphRiskService xây dựng, đọc và lan truyền quan hệ trên Neo4j.
7. HybridRiskScoringService tổng hợp điểm từ rule-based, KNN và Bayesian estimation.
8. Hệ thống trả về verdict, risk score, indicators và features.
9. Dữ liệu được ghi lại để xem lịch sử và trực quan hóa bằng graph.

## Các thực thể và quan hệ chính
### Node
- User
- AnalysisSession
- Email
- IPAddress
- URL
- Domain
- File
- FileHash
- VictimAccount

### Relationship
- HAS_EMAIL
- HAS_IP
- HAS_URL
- HAS_DOMAIN
- HAS_FILE
- HAS_FILEHASH
- HAS_VICTIM
- SENT_FROM_IP
- CONTAINS_URL
- HOSTED_ON
- HOSTED_ON_DOMAIN
- RESOLVES_TO
- DOWNLOADS
- HAS_HASH
- RECEIVED
- CONNECTS_TO

## Tình trạng hiện tại
- Đã có đăng ký và đăng nhập theo session
- Đã có phân quyền Admin, Staff, Customer
- Đã có phân tích từng thực thể và phân tích theo session
- Đã có nhập Excel và hiển thị graph
- Đã có quản lý node, user, lịch sử và log
- Đã có SSE để đẩy cập nhật graph gần thời gian thực

## Ghi chú triển khai
- Hiện tại ứng dụng dùng Neo4j làm kho dữ liệu trung tâm cho graph và session
- Nếu cần tách dữ liệu giao dịch phi đồ thị, có thể mở rộng thêm MySQL ở lớp lưu trữ khác
- File cấu hình Neo4j là dữ liệu nhạy cảm, nên chuyển sang biến môi trường khi triển khai thực tế

## Hướng mở rộng
- Bổ sung Logistic Regression, Decision Tree, J48 để so sánh với KNN
- Bổ sung thêm đặc trưng từ PCAP để tăng chất lượng phân lớp
- Mở rộng tập node sang Device, PhoneNumber, GeoLocation
- Bổ sung mô-đun cảnh báo ngoài hệ thống thử nghiệm
- Tối ưu truy vấn Cypher và chỉ mục Neo4j
