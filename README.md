# NCKH_T1_25_26_QUYEN_HOANG

## Tên đề tài
Sử dụng cơ sở dữ liệu đồ thị (GRAPHDATABASE) để phát hiện lừa bịp trên không gian mạng (cypher space)

---

# CHƯƠNG 1. MỞ ĐẦU

## 1.1 Mở đầu
Sự phát triển nhanh chóng của internet và các dịch vụ số đã làm thay đổi sâu sắc hoạt động kinh tế – xã hội, kéo theo sự bùng nổ về lưu lượng và dữ liệu mạng. Các giao dịch trực tuyến, hệ thống tài khoản số, dịch vụ email, thanh toán điện tử và trao đổi dữ liệu liên tục tạo ra môi trường kết nối rộng lớn nhưng cũng tiềm ẩn rủi ro cao.

Cùng với sự phát triển đó, các hành vi lừa bịp và tấn công mạng ngày càng gia tăng cả về quy mô lẫn mức độ tinh vi. Các đối tượng tấn công thường lợi dụng kẽ hở về kỹ thuật, yếu tố tâm lý người dùng, và cấu trúc liên kết phức tạp giữa nhiều thực thể để che giấu hành vi. Điều này khiến việc phát hiện gian lận trở nên khó khăn nếu chỉ dựa vào quan sát đơn lẻ từng sự kiện.

Nhu cầu phát hiện sớm các hành vi lừa bịp vì vậy trở thành yêu cầu cấp thiết trong an toàn thông tin. Bài toán không chỉ là phát hiện gói tin bất thường, mà còn phải nhận diện chuỗi tương tác, quan hệ giữa các thực thể, và các mẫu hành vi có dấu hiệu gian lận.

Trong thực tế, việc phân tích dữ liệu mạng thường bắt đầu từ các công cụ chuyên dụng như Wireshark và TShark. Wireshark hỗ trợ quan sát lưu lượng trực quan, còn TShark cho phép tự động hóa trích xuất dữ liệu từ các file pcap. Dữ liệu thu thập từ các công cụ này cung cấp cơ sở để xây dựng tập dữ liệu phân tích, phục vụ cho các bước mô hình hóa và phát hiện gian lận.

Tuy nhiên, dữ liệu mạng có tính liên kết cao, ví dụ một IP có thể liên hệ với nhiều domain, một domain dẫn đến nhiều URL, hoặc một tài khoản có thể bị sử dụng từ nhiều điểm truy cập khác nhau. Vì vậy, mô hình hóa và phân tích quan hệ là yêu cầu quan trọng để làm rõ mạng lưới tương tác trong không gian mạng.

## 1.2 Lý do lựa chọn đề tài
Thứ nhất, hành vi lừa bịp trên không gian mạng ngày càng phức tạp, với nhiều tầng che giấu và phân tán hoạt động qua nhiều thực thể. Các phương pháp truyền thống dựa trên luật tĩnh hoặc kiểm tra từng sự kiện riêng lẻ thường khó phát hiện được các mối liên hệ ngầm và các chuỗi hành vi có tính tổ chức.

Thứ hai, phân tích đồ thị là hướng tiếp cận phù hợp để mô hình hóa quan hệ giữa các thực thể. Đồ thị giúp biểu diễn rõ cấu trúc liên kết và hỗ trợ truy vấn theo đường đi, cộng đồng, hoặc chuỗi tương tác bất thường. Đây là những yếu tố quan trọng trong phát hiện lừa bịp.

Thứ ba, cơ sở dữ liệu đồ thị như Neo4j hỗ trợ lưu trữ và truy vấn quan hệ hiệu quả, phù hợp với bài toán cần phân tích sâu về liên kết. Việc kết hợp dữ liệu mạng với graph database giúp nâng cao khả năng phát hiện và trực quan hóa mạng lưới gian lận.

Vì vậy, đề tài “Sử dụng cơ sở dữ liệu đồ thị (GRAPHDATABASE) để phát hiện lừa bịp trên không gian mạng (cypher space)” được lựa chọn nhằm xây dựng một hệ thống thử nghiệm có khả năng khai thác dữ liệu mạng và phân tích quan hệ để nhận diện hành vi lừa bịp.

---

# CHƯƠNG 2. TỔNG QUAN TÌNH HÌNH NGHIÊN CỨU

## 2.1 Các phương pháp phát hiện gian lận
Các phương pháp phát hiện gian lận có thể chia thành ba nhóm chính:

**Rule-based detection**: Dựa trên các luật hoặc ngưỡng được định nghĩa trước. Ưu điểm là dễ triển khai và giải thích, hoạt động tốt với các mẫu đã biết. Nhược điểm là khó thích nghi với các hành vi mới hoặc biến thể tinh vi.

**Machine learning**: Sử dụng dữ liệu lịch sử để huấn luyện mô hình phân loại hoặc phát hiện bất thường. Phương pháp này phát hiện được các mẫu phức tạp nhưng phụ thuộc nhiều vào chất lượng dữ liệu, gán nhãn, và có thể khó giải thích nguyên nhân cảnh báo.

**Graph-based detection**: Mô hình hóa các thực thể và quan hệ dưới dạng đồ thị. Phương pháp này phù hợp với bài toán gian lận có tính liên kết cao, cho phép truy vấn mối quan hệ, phát hiện cộng đồng, và xác định các chuỗi tương tác bất thường. Thách thức nằm ở việc xây dựng lược đồ đồ thị phù hợp và tối ưu truy vấn khi dữ liệu lớn.

## 2.2 Các hệ thống phát hiện xâm nhập mạng
Các hệ thống phát hiện xâm nhập và phân tích lưu lượng mạng là nền tảng để thu thập dữ liệu phục vụ phát hiện gian lận.

**Wireshark** là công cụ giao diện đồ họa phổ biến để capture packet và phân tích lưu lượng. Công cụ này hỗ trợ lọc theo giao thức, địa chỉ IP, cổng, và cho phép người phân tích quan sát trực quan các phiên kết nối.

**TShark** là phiên bản dòng lệnh của Wireshark, hỗ trợ tự động hóa quá trình capture và trích xuất dữ liệu. TShark phù hợp để xử lý nhiều file pcap, phục vụ xây dựng dataset và tích hợp vào pipeline phân tích.

Nguyên lý chung của các công cụ này gồm: thu thập gói tin từ môi trường mạng, phân tích lưu lượng để trích xuất trường thông tin quan trọng, và phát hiện dấu hiệu bất thường dựa trên thống kê, luật hoặc mô hình học máy. Dữ liệu thu thập là cơ sở để chuyển sang giai đoạn phân tích đồ thị.

## 2.3 Ứng dụng cơ sở dữ liệu đồ thị
Graph database là hệ quản trị dữ liệu tối ưu cho việc lưu trữ và truy vấn quan hệ giữa các thực thể. Dữ liệu được biểu diễn dưới dạng **node** (thực thể) và **relationship** (mối quan hệ).

Trong bài toán phát hiện lừa bịp, graph database cho phép truy vấn theo đường đi, phát hiện cụm liên kết, và xác định các thực thể đóng vai trò trung gian quan trọng. Điều này giúp phát hiện mạng lưới gian lận thay vì chỉ cảnh báo các sự kiện đơn lẻ.

Neo4j là hệ quản trị graph database phổ biến với ngôn ngữ truy vấn Cypher. Việc áp dụng Neo4j giúp tăng tốc truy vấn quan hệ, trực quan hóa mạng lưới, và hỗ trợ phát hiện các mẫu kết nối bất thường.

## 2.4 Nhận xét và khoảng trống nghiên cứu
Nhiều nghiên cứu đã sử dụng machine learning để phát hiện gian lận, tuy nhiên phần lớn tập trung vào dữ liệu giao dịch hoặc dữ liệu ứng dụng. Các nghiên cứu khai thác graph-based detection thường ít kết hợp với dữ liệu network traffic.

Trong khi đó, dữ liệu mạng chứa nhiều dấu vết quan trọng về hành vi và quan hệ giữa các thực thể, nhưng chưa được khai thác hiệu quả bằng cơ sở dữ liệu đồ thị. Điều này tạo ra khoảng trống nghiên cứu rõ rệt: thiếu các hệ thống thử nghiệm kết hợp phân tích dữ liệu mạng và graph database để đánh giá khả năng phát hiện lừa bịp theo quan hệ.

Do đó, việc xây dựng một hệ thống kết hợp dữ liệu mạng và cơ sở dữ liệu đồ thị để phát hiện lừa bịp là cần thiết và có giá trị thực tiễn.

---

# CHƯƠNG 3. MỤC TIÊU, PHƯƠNG PHÁP VÀ PHẠM VI NGHIÊN CỨU

## 3.1 Mục tiêu nghiên cứu
**Mục tiêu tổng quát**: Xây dựng hệ thống phát hiện lừa bịp trên không gian mạng dựa trên phân tích dữ liệu mạng và cơ sở dữ liệu đồ thị.

**Mục tiêu cụ thể**:
- Thu thập dữ liệu mạng từ môi trường thử nghiệm.
- Phân tích gói tin và trích xuất trường dữ liệu cần thiết.
- Xây dựng dataset phục vụ phân tích.
- Mô hình hóa dữ liệu dưới dạng đồ thị.
- Phát hiện hành vi lừa bịp dựa trên quan hệ và mẫu bất thường.

## 3.2 Nội dung nghiên cứu
- Nghiên cứu các phương pháp phát hiện lừa bịp.
- Thu thập và xử lý dữ liệu mạng.
- Thiết kế hệ thống phân tích dữ liệu.
- Xây dựng cơ sở dữ liệu đồ thị.
- Thử nghiệm hệ thống và đánh giá kết quả.

## 3.3 Phương pháp nghiên cứu
- **Phân tích dữ liệu mạng**: thu thập, làm sạch và chuẩn hóa dữ liệu.
- **Phân tích đồ thị**: truy vấn quan hệ, phát hiện mẫu liên kết bất thường.
- **Thực nghiệm hệ thống**: đánh giá hiệu quả trên tập dữ liệu thử nghiệm.

**Công cụ sử dụng**:
- Wireshark
- TShark
- Neo4j

## 3.4 Đối tượng nghiên cứu
Các đối tượng dữ liệu chính:
- IP
- Domain
- URL
- Email
- Victim Account

## 3.5 Phạm vi nghiên cứu
Đề tài giới hạn trong môi trường thử nghiệm. Dữ liệu mạng được thu thập có kiểm soát và không triển khai trên hệ thống thực tế. Phạm vi này giúp tập trung vào mô hình dữ liệu và đánh giá phương pháp phân tích đồ thị.

---

# CHƯƠNG 4. KẾT QUẢ NGHIÊN CỨU VÀ THẢO LUẬN

## 4.1 Thiết kế hệ thống thử nghiệm
Hệ thống thử nghiệm được xây dựng theo chuỗi xử lý: thu thập dữ liệu mạng → trích xuất trường thông tin → xây dựng đồ thị → truy vấn và phát hiện mẫu liên kết bất thường. Cấu trúc này đảm bảo dữ liệu thô từ lưu lượng mạng được chuyển đổi thành mô hình quan hệ, giúp phân tích hành vi lừa bịp theo mạng lưới tương tác.

Các thành phần chính:
- **Thu thập dữ liệu**: lưu lượng mạng được capture và lưu dưới dạng file pcap.
- **Trích xuất dữ liệu**: TShark được sử dụng để lấy các trường quan trọng (IP nguồn/đích, domain, URL, cổng, thời gian).
- **Mô hình hóa đồ thị**: dữ liệu được chuyển thành node và relationship để nạp vào Neo4j.
- **Phân tích & truy vấn**: dùng Cypher để tìm đường đi, cụm liên kết và mối quan hệ bất thường.

## 4.2 Xây dựng mô hình dữ liệu đồ thị
Mô hình dữ liệu tập trung vào các thực thể chính và quan hệ giữa chúng. Các node tiêu biểu gồm: `IP`, `Domain`, `URL`, `Email`, `VictimAccount`. Các relationship phản ánh tương tác, ví dụ:
- `IP` **CONNECTS_TO** `IP`
- `IP` **RESOLVES** `Domain`
- `Domain` **HOSTS** `URL`
- `Email` **LINKS_TO** `URL`
- `VictimAccount` **ACCESSED_FROM** `IP`

Việc chuẩn hóa thực thể và quan hệ giúp truy vết chuỗi tương tác, từ đó phát hiện các mẫu có dấu hiệu lừa bịp như: một `URL` độc hại liên kết đến nhiều `Email`, hoặc một `IP` trung gian kết nối đến nhiều `VictimAccount`.

## 4.3 Kết quả phát hiện mẫu bất thường
Hệ thống thử nghiệm cho thấy các mẫu liên kết đáng ngờ có thể được phát hiện rõ ràng khi mô hình hóa bằng đồ thị. Một số kết quả điển hình:
- Nhóm `IP` trung gian có tần suất kết nối cao đến nhiều `VictimAccount`.
- Một số `Domain` có mối liên hệ với nhiều `URL` bất thường trong khoảng thời gian ngắn.
- Các chuỗi liên kết `Email → URL → Domain → IP` lặp lại nhiều lần thể hiện dấu hiệu chiến dịch lừa bịp có tổ chức.

Các mẫu này khó nhận diện nếu chỉ phân tích gói tin độc lập, nhưng được làm rõ khi truy vấn quan hệ trên đồ thị.

## 4.4 Đánh giá và thảo luận
So với cách tiếp cận truyền thống, phân tích đồ thị có ưu điểm là:
- Hiển thị rõ cấu trúc liên kết giữa các thực thể.
- Dễ truy vết theo đường đi để phát hiện chuỗi hành vi.
- Hỗ trợ phát hiện cụm gian lận thay vì chỉ cảnh báo từng sự kiện riêng lẻ.

Tuy nhiên, vẫn tồn tại một số hạn chế:
- Phụ thuộc vào chất lượng dữ liệu đầu vào; dữ liệu thiếu hoặc nhiễu sẽ làm giảm hiệu quả phát hiện.
- Việc thiết kế lược đồ đồ thị cần phù hợp để tránh truy vấn phức tạp và tốn tài nguyên.
- Hệ thống thử nghiệm chưa đánh giá ở quy mô lớn nên cần mở rộng thêm.

Nhìn chung, kết quả cho thấy việc kết hợp phân tích dữ liệu mạng và cơ sở dữ liệu đồ thị có tiềm năng cao trong phát hiện lừa bịp trên không gian mạng.

---

# PHÂN BỔ TRANG CHUẨN (tham khảo báo cáo 30 trang)
Phần | Số trang
--- | ---
Chương 1 | 2–3
Chương 2 | 4–6
Chương 3 | 3–4
Chương 4 (Kết quả) | 12–15
Kết luận | 2–3

Phần dài nhất nên là kết quả nghiên cứu và thảo luận vì hội đồng đánh giá đóng góp của đề tài ở phần này.
