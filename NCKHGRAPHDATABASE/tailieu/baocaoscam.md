

- --

## 📄 Trang 1

1
ĐẠI HỌC PHENIKAA
TRƯỜNG CÔNG NGHỆ THÔNG TIN PHENIKAA
BÁO CÁO TỔNG KẾT
ĐỀ TÀI: XÂY DỰNG HỆ THỐNG PHÁT HIỆN TỘI PHẠM MẠNG
(A SYSTEM FOR CYBER CRIMES DETECTION)
GVHD: TS. Nguyễn LệThu
Lớp tín chỉ: Đồán cơ sởcông nghệthông tin
Khoá: K17 – 2023 – 2027
Chương trình đào tạo: Đại học chính quy
Thành viên nhóm:
Nguyễn Mạnh Quyền
23010198
K17-CNTT_2
Phạm Văn Hoàng
23010245
K17-CNTT_3
Nguyễn Văn Thành
23010191
K17-CNTT_2
Hà Nội, ngày 22 tháng 05 năm 2026


- --

## 📄 Trang 2

BẢNG PHÂN CHIA CÔNG VIỆC
Công việc
Nguyễn Mạnh Quyền
Phạm Văn Hoàng
Nguyễn Văn Thành
Phát triển quản lý Email
100%
Phát triển quản lý URL
100%
Phát triển quản lý IP
100%
Phát triển Risk Model
100%
Phân loại Scam
100%
Phát triển quản lý User
100%
Phát triển quản lý
Session
100%
Phân quyền người dùng
100%
Kết nối các CRUD
100%
Kết nối database Neo4j
50%
50%
Cài đặt database
100%
Import dữliệu Excel
100%
Thiết kếgiao diện web
100%
Xây dựng Graph (D3.js)
100%
Viết báo cáo
40%
30%
30%
Tỷlệđóng góp
33%
33%
33%


- --

## 📄 Trang 3

LỜI MỞĐẦU
Em xin gửi lời tri ân sâu sắc nhất đến cô Nguyễn LệThu – người đã luôn đồng
hành, truyền cảm hứng và dìu dắt em tận tâm trong suốt hành trình thực hiện đề
tài “Xây dựng hệ thống phát hiện tội phạm mạng (A System for Cyber Crimes Detection)”.
Đề tài tập trung xây dựng một hệ thống web sử dụng Neo4j để phân tích quan hệ giữa các thực thể mạng và hỗ trợ phát hiện gian lận theo hướng rule-based kết hợp phân lớp và xác suất.
Không chỉlà người cô truyền đạt tri thức, cô còn là người mởra cho em một thế
giới tư duy mới – sắc sảo, thực tiễn và đầy bản lĩnh. Những kiến thức mà cô
giảng dạy không đơn thuần là lý thuyết, mà là sựkết tinh của trải nghiệm quý
báu trong nghiên cứu và phát triển hệthống, giúp em từng bước hiểu sâu hơn
bản chất vấn đềvà tiếp cận những hướng giải quyết đầy sáng tạo, linh hoạt.
Cô đã không ngại dành nhiều thời gian, tâm huyết đểhướng dẫn, giải thích cặn
kẽnhững khúc mắc chuyên môn, từcác khái niệm nền tảng đến kỹthuật phân
tích, thiết kếvà quản lý hệthống. Không chỉvậy, chính sựtận tụy và nghiêm
cẩn của Cô còn giúp em hình thành thái độnghiên cứu nghiêm túc, tư duy hệ
thống và tinh thần làm việc chuyên nghiệp – những yếu tốvô cùng quý giá
trong lĩnh vực công nghệthông tin.
Đặc biệt, em vô cùng trân trọng sựtin tưởng và khích lệmà cô đã dành cho em
trong suốt quá trình thực hiện đềtài. Chính sựđộng viên ấy là nguồn năng
lượng tinh thần to lớn, giúp em vượt qua nhiều trởngại và không ngừng cốgắng
vươn lên đểhoàn thiện bản thân. Dù chặng đường phía trước còn nhiều thử
thách, em luôn tin rằng những bài học, sựdẫn dắt và niềm tin mà cô dành cho
em sẽlà hành trang quý giá nhất. Em xin gửi đến cô lòng biết ơn chân thành và
lời chúc sức khỏe, bình an và thành công trên con đường giáo dục và nghiên
cứu.


- --

## 📄 Trang 4

Mục Lục
CHƯƠNG I: TỔNG QUAN ĐỀTÀI................................................................... 1

## 1.1. Lý do chọn đềtài........................................................................................1


## 1.2. Mục tiêu và phạm vi nghiên cứu................................................................2

A, Mục tiêu....................................................................................................2
B, Phạm vi nghiên cứu.................................................................................. 2

## 1.3. Đối tượng và phương pháp nghiên cứu......................................................3


## 1.3.1 Đối tượng nghiên cứu..........................................................................3


## 1.3.2 Phương pháp nghiên cứu.....................................................................3


## 1.4. Ý nghĩa khoa học và thực tiễn....................................................................4


## 1.4.1 Ý nghĩa khoa học.................................................................................4


## 1.4.2 Ý nghĩa thực tiễn.................................................................................. 5


## 1.5. Bốcục đồán............................................................................................... 5

CHƯƠNG II: CƠ SỞLÝ THUYẾT.....................................................................7

## 2.1. Tổng quan lĩnh vực.....................................................................................7


## 2.1.1. Hệthống phát hiện gian lận................................................................ 7

A,Các phương pháp phát hiện gian lận phổbiến..........................................7

## 2.1.2. Ứng dụng Web.................................................................................... 9


## 2.1.3. Cơ sởdữliệu đồthị...........................................................................10

## 2.1.4. Các Thuật toán Phân Loại (Classification Algorithms)..................10

## 2.2. Các khái niệm và mô hình liên quan........................................................11


## 2.2.1. UML (Unified Modeling Language)................................................ 11


## 2.2.2. Cơ sởdữliệu quan hệ........................................................................13



- --

## 📄 Trang 5


## 2.2.3. Kiến trúc 3 lớp (Three-Tier Architecture)........................................ 13


## 2.3. Các công trình, nghiên cứu liên quan...................................................... 14


## 2.3.1. Hệthống dựa trên Machine Learning............................................... 14


## 2.3.2. Hệthống dựa trên luật.......................................................................15


## 2.3.3. Hệthống dựa trên cơ sởdữliệu đồthị..............................................15


## 2.4. Công nghệ, ngôn ngữvà công cụsửdụng...............................................16


## 2.4.1. Ngôn ngữlập trình............................................................................ 16


## 2.4.2. Framework.........................................................................................16


## 2.4.3. Cơ sởdữliệu..................................................................................... 16


## 2.4.4. Công cụhỗtrợ...................................................................................17

## 2.4.5. Công nghệ Bảo mật (Security)......................................................17

## 2.4.6. Công nghệFrontend..........................................................................17

## 2.4.7. Đối chiếu yêu cầu cập nhật............................................................18

CHƯƠNG III: PHÂN TÍCH HỆTHỐNG..........................................................18

## 3.1. Khảo sát hiện trạng...................................................................................18


## 3.1.1. Quy trình nghiệp vụhiện tại............................................................. 18

1. Kiểm tra thủcông (Manual Investigation)..............................................18
2. Hệthống dựa trên luật (Rule-Based System)......................................... 19
3. Hệthống Machine Learning....................................................................20
4. Hạn chếchung của các phương pháp hiện tại.........................................21

## 3.1.2. Các vấn đềvà hạn chế.......................................................................21


## 3.1.3. Nhu cầu xây dựng hệthống mới.......................................................23


## 3.2. Phân tích yêu cầu......................................................................................24


## 3.2.1. Yêu cầu nghiệp vụ.............................................................................24



- --

## 📄 Trang 6

1. Quản lý người dùng theo vai trò............................................................. 24
2. Phân tích các thực thểgian lận................................................................24
3. Lưu trữphiên phân tích (Analysis Session)............................................25
4. Tính toán Risk Score...............................................................................25
5. Hiển thịđồthịquan hệ............................................................................26
6. Lưu lịch sửphân tích...............................................................................26
7. Quản trịngười dùng (Admin)................................................................. 26

## 3.2.2. Yêu cầu chức năng............................................................................ 27

A. Nhóm chức năng xác thực......................................................................27
B. Nhóm chức năng phân tích gian lận.......................................................27
C. Nhóm chức năng quản trị(Admin)........................................................ 30

## 3.2.3. Yêu cầu phi chức năng......................................................................30

1. Hiệu năng (Performance)........................................................................ 30
2. Bảo mật (Security)...................................................................................31
3. Tính mởrộng (Scalability)......................................................................31
4. Tính ổn định (Reliability)........................................................................31
5. Khảnăng bảo trì (Maintainability)..........................................................31

## 3.2.4. Mô hình hóa chức năng.....................................................................32


## 3.3. MÔ HÌNH CA SỬDỤNG (USE CASE MODEL).................................32


## 3.3.1. Xác định Actor.................................................................................. 33


## 3.3.2. Danh sách Use Case..........................................................................34


## 3.3.3. Biểu đồUse Case tổng quát (Mô tả).................................................37



- --

## 📄 Trang 7

1.Cấu trúc biểu đồ........................................................................................37
2.Phân quyền tổng quát............................................................................... 37
3.Bảng use case tổng quát........................................................................... 38
4.Sơ đồuse case tổng quát.......................................................................... 46

## 3.3.4. Sơ đồvà bảng đặc tảchi tiết từng Use Case.....................................47

1. UC1: Đăng ký..........................................................................................47
2.UC2: Đăng nhập.......................................................................................49
3. UC3: Đăng xuất.......................................................................................51
4.UC4: Phân tích Email...............................................................................54
5.UC5: Phân tích IP.....................................................................................57
6.UC6: Phân tích URL................................................................................ 60
7.UC7: Tạo phiên phân tích........................................................................ 63
8.UC8: Tính Risk Score.............................................................................. 66
9.UC9: Đồthịquan hệ................................................................................69
10.UC10: Xem chi tiết Node.......................................................................72
11.UC11: Xem lịch sửcá nhân................................................................... 75
12.UC12: Xem toàn bộlịch sửhệthống.....................................................77
13.UC13: Quản lý người dùng....................................................................80
14.UC14: Quản lý Node (Email/IP/URL).................................................. 83

## 3.4. Các biểu đồphân tích...............................................................................87


## 3.4.1. Activity Diagram...............................................................................87


## 3.4.2. Sequence Diagram.............................................................................90



- --

## 📄 Trang 8

A, Sơ đồSequence Diagram tổng quát.......................................................90
B, Sơ đồSequence Diagram chi tiết........................................................... 91

## 3.4.1. Class Diagram................................................................................... 98


## 3.5. Kết luận chương....................................................................................... 99

CHƯƠNG IV: THIẾT KẾHỆTHỐNG...........................................................101

## 4.1. Kiến trúc tổng thể...................................................................................101


## 4.1.1. Mô hình 3 lớp (Three-Tier Architecture)........................................101


## 4.1.2. Sơ đồkiến trúc hệthống................................................................. 105


## 4.1.3. Luồng xửlý tổng quát trong kiến trúc............................................ 105


## 4.1.4. Đánh giá kiến trúc........................................................................... 106


## 4.2. Thiết kếcơ sởdữliệu.............................................................................107


## 4.2.1. ERD (Graph Model Diagram).........................................................107


## 4.2.2. Lược đồquan hệ..............................................................................108


## 4.2.3. Relationship.....................................................................................110


## 4.2.4. Đặc điểm thiết kếCSDL................................................................. 111


## 4.3. Thiết kếthành phần phần mềm..............................................................112


## 4.3.1 Package Diagram..............................................................................112


## 4.3.2 Deployment Diagram.......................................................................113


## 4.4. Thiết kếgiao diện người dùng............................................................... 114

1. Màn hình Đăng nhập.............................................................................114
2. Màn hình Đăng ký.................................................................................115
3.Màn hình Dashboard.............................................................................. 116


- --

## 📄 Trang 9

4. Màn hình Phân tích dữliệu................................................................... 118
5. Màn hình Lịch sửphân tích...................................................................120
6. Màn hình Quản lý người dùng (Admin)...............................................121
7.Màn hình Quản lý Node (Admin / Staff)...............................................122
8. Màn hình Giới thiệu hệthống............................................................... 123

## 4.5. Thiết kếxửlý..........................................................................................124

1. STATE MACHINE – USER................................................................ 124
2.STATE MACHINE – ANALYSIS SESSION...................................... 125
3.STATE MACHINE – NODE (EMAIL/IP/URL)..................................125

## 4.6. Kết luận chương..................................................................................... 126

Chương V: Cài đặt và kết quảthửnghiệm........................................................127

## 5.1. Môi trường triển khai............................................................................. 127


## 5.1.1. Môi trường phần cứng.....................................................................127


## 5.1.2. Môi trường phần mềm.....................................................................127


## 5.1.3. Công nghệsửdụng..........................................................................127


## 5.2. Cài đặt các chức năng chính...................................................................128


## 5.2.1. Chức năng đăng ký (UC1).............................................................. 128


## 5.2.2. Chức năng đăng nhập (UC2)...........................................................130


## 5.2.3. Chức năng upload file Excel (UC4–UC6)......................................132


## 5.2.4. Chức năng truy vấn đồthị(UC7–UC8)..........................................136


## 5.2.6. Chức năng quản lý người dùng (UC13)..........................................184


## 5.2.7. Giao diện cơ chếphân quyền..........................................................191



- --

## 📄 Trang 10


## 5.2.8. Giao diện phân tích rủi do trực tiếp................................................ 196


## 5.2.9. Giao diện Danh sách phiên phân tích............................................. 199


## 5.2.10. Giao diện giới thiệu nhóm phát triển............................................ 201


## 5.3. Kết quảthực nghiệm.............................................................................. 203


## 5.3.1. Môi trường kiểm thử....................................................................... 203


## 5.3.2. Tốc độxửlý.....................................................................................204


## 5.3.3. Đánh giá tính chính xác...................................................................206


## 5.3.4. Đánh giá giao diện người dùng.......................................................207


## 5.4. Đánh giá hệthống...................................................................................208


## 5.4.1. Ưu điểm...........................................................................................208


## 5.4.2. Hạn chế............................................................................................209


## 5.5.Thông tin trong file readme.....................................................................210

KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN........................................................217
I. Kết quảđạt được........................................................................................ 217
II. Hạn chế..................................................................................................... 218
III. Hướng phát triển tiếp theo.......................................................................218
Link REPO.........................................................................................................219
TÀI LIỆU THAM KHẢO.................................................................................220


- --

## 📄 Trang 11

1
CHƯƠNG I: TỔNG QUAN ĐỀTÀI

## 1.1. Lý do chọn đềtài

Trong bối cảnh chuyển đổi sốmạnh mẽhiện nay, các hoạt động giao dịch trực
tuyến, đăng ký tài khoản, gửi email và truy cập website diễn ra với tần suất rất
lớn. Điều này kéo theo sựgia tăng của các hành vi gian lận như:
- Tạo nhiều tài khoản giả
- Sửdụng IP đáng ngờđểgửi spam
- Phát tán URL độc hại
- Liên kết nhiều thực thểnhằm che giấu hành vi bất thường
Hệ thống hiện tại kết hợp rule-based scoring với lớp phân lớp và lớp xác suất để đánh giá rủi ro trên cùng bộ đầu vào đã có. Cụ thể, code đang có rule engine, graph risk propagation và hybrid scoring với KNN + Bayesian estimation; phần so sánh thêm có thể mở rộng sang Logistic Regression, Decision Tree và J48.
Vì vậy, nhóm lựa chọn đềtài:
“Xây dựng hệ thống phát hiện tội phạm mạng (A System for Cyber Crimes Detection)”
Đềtài này được lựa chọn vì:
- Có tính thực tiễn cao trong lĩnh vực an ninh mạng và tài chính.
- Ứng dụng cơ sởdữliệu đồthịthay vì quan hệtruyền thống.
- Giúp sinh viên tiếp cận kiến trúc hệthống hiện đại (Web và Graph Database).
- Có khảnăng mởrộng nhiều các mô hình Machine Learning trong tương lai.


- --

## 📄 Trang 12

2

## 1.2. Mục tiêu và phạm vi nghiên cứu

A, Mục tiêu
1.Mục tiêu của đềtài là xây dựng một hệthống dựa trên công nghệWeb cho
phép:
- Thu thập và lưu trữdữliệu Email, IP, URL, Domain, FileNode, FileHash và VictimAccount.
- Mô hình hóa dữliệu dưới dạng đồthị.
- Tính toán mức độrủi ro (Risk Score) dựa trên:
+ Rủi ro nội tại (Base Risk)
+ Rủi ro lan truyền trên đồthị(Graph Risk)
+ Điểm hybrid kết hợp rule-based, classification và probability.
- Đưa ra kết luận (Verdict) cho từng thực thể.
- Phân quyền người dùng theo vai trò (Admin, Staff, Customer).
- Trực quan hóa đồthịquan hệgiữa các thực thể.
2.Hệthống sửdụng:
- Backend: Java Spring Boot
- Frontend: Thymeleaf, JavaScript và HTML
- Cơ sởdữliệu: Neo4j
B, Phạm vi nghiên cứu
1.Phạm vi của hệthống bao gồm:
Hiện tại, ứng dụng có phạm vi phân tích dựa trên những thông tin sau:
- Phân tích gian lận dựa trên Email, IP, URL, Domain, FileNode, FileHash và VictimAccount.
- Lưu trữdữliệu trong cơ sởdữliệu đồthị.
- Triển khai dưới dạng ứng dụng dựa trên công nghệWeb.


- --

## 📄 Trang 13

3
2.Hệthống chưa bao gồm:
- Kết nối dữliệu thời gian thực từhệthống ngân hàng hoặc doanh nghiệp.
- Tích hợp hệthống cảnh báo tựđộng ngoài môi trường thửnghiệm.
3.Đối tượng sửdụng:
- Quản trịviên (Admin)
- Nhân viên phân tích (Staff)
- Người dùng gửi yêu cầu phân tích (Customer)

## 1.3. Đối tượng và phương pháp nghiên cứu


## 1.3.1 Đối tượng nghiên cứu

Đối tượng nghiên cứu của đề tài bao gồm:
- **Các thực thể chính**: Email, IP Address, URL, Domain, FileNode, FileHash, VictimAccount
- **Mối quan hệ**: Các liên kết giữa các thực thể trong hệ thống (ví dụ: Email được gửi từ IP, URL được chứa trong Email)
- **Phương pháp tính rủi ro**: Dựa trên mô hình đồ thị kết hợp rule-based scoring, phân loại (KNN, Logistic Regression, Decision Tree, J48) và xác suất Bayesian
- **Người sử dụng hệ thống**: Admin, Staff, Customer
- **Dữ liệu phân tích**: Từ Wireshark/TShark (PCAP files), Excel files, và các nguồn dữ liệu tích hợp

## 1.3.2 Phương pháp nghiên cứu

Đềtài sửdụng các phương pháp sau:
1.Phương pháp khảo sát:
- Tìm hiểu các mô hình phát hiện gian lận hiện có.
- Nghiên cứu cơ sởdữliệu đồthị.
2.Phương pháp phân tích và thiết kếhệthống:


- --

## 📄 Trang 14

4
- Phân tích yêu cầu nghiệp vụ.
- Xây dựng mô hình UML (Use Case, Sequence, Class Diagram).
- Thiết kếkiến trúc 3 lớp (Controller – Service – Repository).
3.Phương pháp lập trình và triển khai:
- Sửdụng Spring Boot xây dựng backend.
- Sửdụng Neo4j đểlưu trữdữliệu dạng đồthị.
- Sửdụng Thymeleaf và JavaScript đểxây dựng giao diện.
4.Phương pháp kiểm thử:
- Kiểm thửchức năng.
- Kiểm thửtính chính xác của thuật toán tính rủi ro (risk).
- So sánh kết quảgiữa các phiên phân tích.

## 1.4. Ý nghĩa khoa học và thực tiễn


## 1.4.1 Ý nghĩa khoa học

- Minh họa cách áp dụng mô hình đồthịtrong bài toán phát hiện gian lận.
- Thểhiện cách tách biệt rõ ràng giữa:
+ Rủi ro nội tại (Base Risk)
+ Rủi ro lan truyền trên đồthị(Graph Risk)
+ Rủi ro toàn bộ(Final Risk).
- Là ví dụthực tếvềkiến trúc phần mềm phân tầng.
- Tuân thủkỹthuật phát triển phần mềm (Phân tích, lưu đồthuật toán sửdụng
UML) và thiết kếhệthống vào thực tiễn.


- --

## 📄 Trang 15

5

## 1.4.2 Ý nghĩa thực tiễn

- Có thểứng dụng trong:
+ Hệthống chống gian lận (Spam)
+ Hệthống phòng chống gian lận tài chính
+ Hệthống quản lý tài khoản người dùng
- Hệthống có thểphục vụcho các yêu cầu như:
+ Phát hiện mối quan hệđáng ngờgiữa các thực thể.
+ Hỗtrợnhân viên phân tích ra quyết định nhanh hơn.
+ Trực quan hóa dữliệu giúp tăng khảnăng đánh giá rủi ro.

## 1.5. Bốcục đồán

Đồán gồm 5 chương chính:
- Chương 1: Tổng quan đềtài
Trình bày lý do chọn đềtài, mục tiêu, phạm vi, phương pháp nghiên cứu và ý
nghĩa của hệthống.
- Chương 2: Cơ sởlý thuyết
Giới thiệu các khái niệm liên quan như cơ sởdữliệu đồthị, lưu đồthuật toán
(UML), kiến trúc phần mềm và các công nghệsửdụng.
- Chương 3: Phân tích hệthống
Phân tích yêu cầu nghiệp vụ, xây dựng mô hình Use Case, các biểu đồphân tích
và mô hình dữliệu.
- Chương 4: Thiết kếhệthống
Thiết kếkiến trúc tổng thể, cơ sởdữliệu, các thành phần phần mềm và giao
diện người dùng.


- --

## 📄 Trang 16

6
- Chương 5: Cài đặt và kết quảthửnghiệm
Mô tảmôi trường triển khai, quá trình cài đặt, kết quảthực nghiệm và đánh giá
hệthống.


- --

## 📄 Trang 17

7
CHƯƠNG II: CƠ SỞLÝ THUYẾT

## 2.1. Tổng quan lĩnh vực


## 2.1.1. Hệthống phát hiện gian lận

Phát hiện gian lận (Fraud Detection) là một lĩnh vực quan trọng trong an ninh
thông tin, thương mại điện tử, ngân hàng sốvà các hệthống giao dịch trực
tuyến. Gian lận có thểxuất hiện dưới nhiều hình thức như:
- Giảmạo email
- Lừa đảo phishing
- Tấn công bằng IP ẩn danh
- Phát tán liên kết độc hại
- Gian lận giao dịch tài chính
Mục tiêu của hệthống phát hiện gian lận là:
- Xác định các thực thểhoặc hành vi bất thường
- Đánh giá mức độrủi ro
- Hỗtrợra quyết định phòng ngừa
A,Các phương pháp phát hiện gian lận phổbiến
1. Dựa trên tập Luật hay Nguyên tắc (Rule-Based System)
Hệthống dựa trên các luật cốđịnh, ví dụ:
- Email sửdụng domain dùng một lần
- IP thuộc danh sách đen (blacklist)
- URL có TLD rủi ro cao (.xyz, .top, .ru…)
Ưu điểm:


- --

## 📄 Trang 18

8
- Dễtriển khai
- Dễgiải thích
Nhược điểm:
- Không linh hoạt
- Không phát hiện được mạng lưới gian lận
2. Machine Learning (Học máy)
Hệthống sửdụng mô hình học máy đểphát hiện gian lận dựa trên dữliệu lịch
sử.
Các thuật toán phổbiến:
- Decision Tree
- Random Forest
- Neural Network
Ưu điểm:
- Tựđộng học từdữliệu
- Độchính xác cao nếu có dữliệu lớn
Nhược điểm:
- Cần dữliệu huấn luyện lớn
- Khó giải thích kết quả
- Tốn tài nguyên tính toán
3. Phân tích hành vi (Behavioral Analysis)
Dựa trên hành vi bất thường như:
- Đăng nhập từnhiều IP trong thời gian ngắn


- --

## 📄 Trang 19

9
- Tần suất gửi email bất thường
- Truy cập hệthống ngoài khung giờ
Phương pháp này tập trung vào phân tích chuỗi sựkiện thay vì chỉphân tích
thực thểđơn lẻ.
4. Phân tích dựa trên đồthị(Graph-Based Analysis)
Đây là phương pháp hiện đại và phù hợp với bài toán gian lận có tính mạng lưới.
Thay vì đánh giá từng thực thểriêng lẻ, hệthống xem xét:
- Quan hệgiữa Email – IP – URL
- Cấu trúc mạng lưới liên kết
- Lan truyền rủi ro qua các nút (node)
Phương pháp này đặc biệt hiệu quảkhi:
- Các thực thểgian lận có liên hệvới nhau
- Gian lận được tổchức theo mạng lưới
Hệthống trong đềtài lựa chọn hướng tiếp cận này đểgiảm false positive và
nâng cao khảnăng phát hiện gian lận theo ngữcảnh.

## 2.1.2. Ứng dụng Web

Ứng dụng sửdụng công nghệWeb là mô hình phần mềm hoạt động trên máy
chủ(server) và người dùng truy cập thông qua trình duyệt (Client), dựa trên
giao thức truyền thông HTTP(s).
Ưu điểm của ứng dụng sửdụng công nghệWeb:
- Không cần cài đặt trên máy người dùng
- Dễcập nhật
- Dễmởrộng


- --

## 📄 Trang 20

10
Hệthống trong đềtài được xây dựng theo mô hình công nghệWeb xây dựng
một ứng dụng web (Web Application)bao gồm:
- Backend xửlý nghiệp vụphân tích gian lận
- Frontend hiển thịkết quảdưới dạng đồthịtrực quan
Việc sửdụng Web Application giúp:
- Quản lý tập trung
- Phân quyền theo vai trò dựa trên đặc quyền khác nhau (Admin, Staff,
Customer)
- Triển khai linh hoạt

## 2.1.3. Cơ sởdữliệu đồthị

Cơ sởdữliệu đồthị[12] là loại cơ sởdữliệu lưu trữdữliệu dưới dạng:
- Node (Đỉnh)
- Relationship (Cạnh)
- Properties (Thuộc tính)
Khác với cơ sởdữliệu quan hệsửdụng bảng và JOIN, cơ sởdữliệu đồthịlưu
trữmối quan hệtrực tiếp giữa các thực thể.
Trong bài toán của đềtài:
Node gồm:
- Email
- IP Address
- URL
- Domain
- FileNode
- FileHash
- VictimAccount
- AnalysisSession
Relationship gồm:


- --

## 📄 Trang 21

11
- SENT_FROM_IP
- CONTAINS_URL
- HOSTED_ON
- HOSTED_ON_DOMAIN
- RESOLVES_TO
- CONNECTS_TO
- HAS_EMAIL
- HAS_IP
- HAS_URL
- HAS_DOMAIN
- HAS_FILE
- HAS_FILEHASH
- HAS_VICTIM
- DOWNLOADS
- HAS_HASH
- RECEIVED
Ưu điểm của CSDL đồthị:
- Truy vấn quan hệnhanh hơn
- Phát hiện cụm gian lận
- Hỗtrợlan truyền rủi ro
Đây là nền tảng quan trọng đểxây dựng mô hình xác định rủi ro (Graph Risk
Propagation) trong hệthống.

## 2.1.4. Các Thuật toán Phân Loại (Classification Algorithms)

Để nâng cao độ chính xác trong phát hiện gian lận, hệ thống kết hợp nhiều thuật toán phân loại trên cùng bộ đầu vào:

**1. K-Nearest Neighbor (KNN)**
- Nguyên tắc: Phân loại dựa trên các hàng xóm gần nhất trong không gian đặc trưng
- Ứng dụng trong hệ thống: So sánh session hiện tại với các phiên phân tích lịch sử

**2. Logistic Regression**
- Nguyên tắc: Ước lượng xác suất xuất hiện của lớp dương dựa trên mô hình tuyến tính
- Ứng dụng trong hệ thống: Ước lượng xác suất dựa trên Vector đặc trưng của session

**3. Decision Tree (Cây Quyết Định)**
- Nguyên tắc: Phân chia không gian đặc trưng theo các tiêu chí để tạo mô hình cây phân loại
- Ứng dụng trong hệ thống: Tạo các quy tắc tự động để phân loại session theo độ rủi ro

**4. J48 (Triển khai của C4.5)**
- Nguyên tắc: Xây dựng cây quyết định bằng cách chọn thuộc tính phân chia tối ưu
- Ứng dụng trong hệ thống: Xây dựng mô hình phân loại nâng cao với khả năng cắt nhánh

**Kết hợp Hybrid Scoring:**
Hệ thống thực hiện đánh giá rủi ro theo công thức:
- Final Risk Score = 0.4 × Rule Score + 0.3 × KNN Score + 0.3 × Bayesian Score

## 2.2. Các khái niệm và mô hình liên quan


## 2.2.1. UML (Unified Modeling Language)

Unified Modeling Language [13] là ngôn ngữmô hình hóa tiêu chuẩn dùng
trong phân tích và thiết kếhệthống phần mềm đểđưa ra các sơ đồcấu trúc, sơ
đồchức năng cũng như lưu đồthuật toán.
UML giúp:
- Mô tảcấu trúc hệthống
- Mô tảhành vi hệthống
- Chuẩn hóa tài liệu thiết kế
Các biểu đồUML được sửdụng trong đềtài gồm:
1.Use Case Diagram (Sơ đồngười dùng)
Mô tảtương tác giữa người dùng và hệthống.


- --

## 📄 Trang 22

12
2.Activity Diagram (Sơ đồchuỗi hành động)
Mô tảluồng xửlý nghiệp vụnhư:
- Nhập dữliệu
- Tính Base Risk
- Lan truyền Graph Risk
- Trảkết quả
3.Sequence Diagram (Sơ đồtuần tự)
Mô tảtrình tựtương tác giữa:
- Controller
- FraudAnalysisService
- GraphRiskService
- Database
4.Class Diagram
Mô tảcấu trúc lớp như:
- Email
- IPAddress
- URL
- FraudAnalysisService
- GraphResponseDTO
Việc sửdụng UML giúp hệthống được thiết kếrõ ràng trước khi triển khai lập
trình.


- --

## 📄 Trang 23

13

## 2.2.2. Cơ sởdữliệu quan hệ

Cơ sởdữliệu quan hệ(Relational Database) lưu trữdữliệu dưới dạng bảng.
Đặc điểm:
- Sửdụng khóa chính (Primary Key)
- Sửdụng khóa ngoại (Foreign Key)
- Truy vấn bằng SQL
Ví dụhệquản trịCSDL:
- MySQL
- Microsoft SQL Server
Tuy nhiên, khi dữliệu có nhiều quan hệphức tạp, việc JOIN nhiều bảng sẽlàm
giảm hiệu năng.
Do đó, đềtài lựa chọn cơ sởdữliệu đồthịthay vì CSDL quan hệtruyền thống.

## 2.2.3. Kiến trúc 3 lớp (Three-Tier Architecture)

Kiến trúc 3 lớp gồm:
1. Presentation Layer
- HTML
- CSS
- JavaScript
- Hiển thịđồthị
2. Business Layer
- Tính Base Risk
- Lan truyền Graph Risk


- --

## 📄 Trang 24

14
- Quyết định Verdict
3. Data Layer
- Truy vấn Neo4j
- Lưu session phân tích
Ưu điểm:
- Phân tách rõ ràng trách nhiệm
- Dễbảo trì
- Dễmởrộng
Hệthống áp dụng kiến trúc này đểđảm bảo:
- Frontend không tựsuy luận
- Backend là bộnão trung tâm
- Database chỉlưu trữvà cung cấp dữliệu

## 2.3. Các công trình, nghiên cứu liên quan

Hiện nay, nhiều hệthống phát hiện gian lận đã được triển khai trong thực tế.

## 2.3.1. Hệthống dựa trên Machine Learning

Ưu điểm:
- Phát hiện mẫu phức tạp
- Độchính xác cao
Nhược điểm:
- Cần dữliệu lớn
- Khó giải thích


- --

## 📄 Trang 25

15
- Không minh bạch khi ra quyết định

## 2.3.2. Hệthống dựa trên luật

Ưu điểm:
- Dễtriển khai
- Phù hợp hệthống nhỏ
Nhược điểm:
- Cứng nhắc
- Không phát hiện được mạng lưới gian lận

## 2.3.3. Hệthống dựa trên cơ sởdữliệu đồthị

Một sốtổchức sửdụng:
- Neo4j
- TigerGraph
Ưu điểm:
- Phân tích quan hệđa chiều
- Phát hiện cụm gian lận
- Hỗtrợlan truyền rủi ro
Nhược điểm:
- Cần thiết kếtrọng sốhợp lý
- Tối ưu truy vấn phức tạp
Hệthống trong đềtài lựa chọn hướng tiếp cận này để:
- Giảm false positive


- --

## 📄 Trang 26

16
- Tăng khảnăng phát hiện gian lận theo ngữcảnh

## 2.4. Công nghệ, ngôn ngữvà công cụsửdụng


## 2.4.1. Ngôn ngữlập trình

Hệthống sửdụng:
- Java
Lý do lựa chọn:
- Hướng đối tượng mạnh
- Bảo mật cao
- Tích hợp tốt với Spring Framework

## 2.4.2. Framework

- Spring Boot
Ưu điểm:
- Cấu hình tựđộng
- Tích hợp REST API
- Dễtriển khai

## 2.4.3. Cơ sởdữliệu

- Neo4j
Sửdụng để:
- Lưu node Email, IP, URL, Domain, FileNode, FileHash, VictimAccount
- Lưu relationship giữa các thực thể và session
- Hỗtrợtruy vấn quan hệ và phát hiện chuỗi liên kết bất thường


- --

## 📄 Trang 27

17

## 2.4.4. Công cụhỗtrợ

- IDE: IntelliJ IDEA
- Quản lý mã nguồn: Git
- Build tool: Maven
- Network Analysis: Wireshark / TShark (để capture và phân tích PCAP files)
- Data Import: Apache POI (để đọc và import Excel files)
- Visualization: D3.js

## 2.4.5. Công nghệ Bảo mật (Security)

- Authentication (Xác thực): Session-based authentication với Spring Security
- Authorization (Phân quyền): Role-based access control (RBAC), gồm Admin, Staff, Customer
- Database Security: Connection pooling + Parameterized queries
- API Security: HTTPS/TLS + Rate limiting

## 2.4.6. Công nghệFrontend

- HTML
- CSS
- JavaScript
- Thư viện trực quan hóa: D3.js
D3.js giúp:
- Vẽnode
- Vẽrelationship
- Tô màu theo RiskScore
- Hiển thịtrực quan mạng lưới gian lận

## 2.4.7. Đối chiếu yêu cầu cập nhật

Đối chiếu với mục tiêu bổ sung theo yêu cầu:

- 1) Công điểm dựa trên rule base: Đã có (rule-based scoring)
- 2) KNN (phân loại): Đã có
- 3) Logistic Regression: Đã mô tả trong phần so sánh/mở rộng
- 4) Decision Tree: Đã mô tả
- 5) J48: Đã mô tả
- 6) Data từ Wireshark/TShark: Đã có
- 7) Frontend: Thymeleaf + D3.js (Neo4j là database)
- 8) Database: Neo4j (mô hình đồ thị)
- 9) Authentication: Session-based / Spring Security
- 10) Authorization: Role-based (Admin, Staff, Customer)

Xác minh từ code hiện tại:

- HybridRiskScoringService: KNN + Bayesian + Rule score
- FraudAnalysisService: Rule engine và base risk scoring
- PacketCaptureService: hỗ trợ Wireshark/TShark PCAP analysis
- ExcelImportService: import dữ liệu Excel (Apache POI)
- AuthController + DashboardController: xác thực và phân quyền theo vai trò

Công thức hybrid scoring trong hệ thống:

- Final Score = 0.4 x Rule + 0.3 x KNN + 0.3 x Bayesian

## 2.4.8. Nội dung đã triển khai bổ sung

Nhằm mở rộng phạm vi đề tài từ mô hình phân tích cơ bản sang mô hình phát hiện gian lận có chiều sâu hơn, nhóm đã triển khai thêm các nội dung sau và tích hợp trực tiếp vào hệ thống hiện tại.

1. Mở rộng mô hình dữ liệu thực thể

Giai đoạn đầu hệ thống tập trung vào ba thực thể chính (Email, IP, URL). Ở phiên bản mở rộng, mô hình dữ liệu đã được bổ sung thêm Domain, FileNode, FileHash và VictimAccount để phản ánh đầy đủ hơn chuỗi hành vi tấn công trong thực tế.

Việc mở rộng này giúp hệ thống trả lời được các câu hỏi phân tích trước đây chưa xử lý tốt, ví dụ:
- URL độc hại dẫn tới file nào được tải xuống.
- Các file khác nhau có dùng chung hash hay không.
- Một thực thể đáng ngờ liên quan tới tài khoản nạn nhân nào.

Nhờ đó, đồ thị quan hệ không chỉ thể hiện kết nối ở mức truy cập (IP/URL/Domain) mà còn thể hiện liên kết ở mức tạo tác (file, hash) và tác động (victim account).

2. Bổ sung lớp phân loại trong kiến trúc hybrid scoring

Ngoài rule-based scoring, hệ thống đã bổ sung lớp phân loại (classification layer) và lớp xác suất (probability layer), tạo thành mô hình đánh giá rủi ro lai nhiều tầng.

Cụ thể:
- Rule layer: tổng hợp tín hiệu luật nghiệp vụ và chỉ báo rủi ro có thể giải thích.
- KNN layer: đo độ tương đồng phiên hiện tại với dữ liệu lịch sử bằng không gian đặc trưng.
- Bayesian layer: ước lượng xác suất rủi ro theo các đặc trưng xuất hiện đồng thời.

Điểm cuối cùng được tổng hợp theo trọng số:
- Final Score = 0.4 x Rule + 0.3 x KNN + 0.3 x Bayesian

Thiết kế này giúp cân bằng giữa khả năng giải thích (từ rule), khả năng học theo mẫu gần (từ KNN) và khả năng ước lượng xác suất (từ Bayes), từ đó giảm thiên lệch khi chỉ dùng một kỹ thuật đơn lẻ.

3. Chuẩn bị khung so sánh thuật toán phân loại

Bên cạnh KNN đang sử dụng trong luồng hybrid chính, báo cáo và thiết kế hệ thống đã bổ sung khung lý thuyết cho Logistic Regression, Decision Tree và J48 để phục vụ so sánh mô hình trong các giai đoạn thực nghiệm mở rộng.

Ý nghĩa của phần bổ sung này:
- Tạo nền tảng học thuật để đánh giá ưu/nhược của từng thuật toán trên cùng bộ dữ liệu.
- Dễ mở rộng pipeline thí nghiệm mà không phải thay đổi kiến trúc tổng thể.
- Hỗ trợ đối chiếu kết quả giữa mô hình tuyến tính, mô hình cây và mô hình lân cận.

4. Hoàn thiện lớp bảo mật và phân quyền

Hệ thống đã được chuẩn hóa theo hướng bảo mật ứng dụng web nhiều lớp:
- Xác thực theo phiên đăng nhập với Spring Security.
- Phân quyền theo vai trò (Admin, Staff, Customer) cho từng nhóm chức năng.
- Chuẩn hóa truy vấn có tham số để giảm rủi ro tấn công injection.
- Bổ sung định hướng triển khai HTTPS/TLS và giới hạn tần suất truy cập API.

Nhờ cơ chế này, cùng một nguồn dữ liệu phân tích nhưng mỗi vai trò chỉ được truy cập đúng phạm vi nghiệp vụ, đảm bảo nguyên tắc tối thiểu quyền hạn trong vận hành thực tế.

5. Bổ sung pipeline dữ liệu thực nghiệm

Phần dữ liệu đầu vào đã được mô tả rõ theo hướng thực nghiệm và có khả năng tái lập:
- Thu thập lưu lượng bằng Wireshark/TShark (PCAP).
- Chuẩn hóa và nhập dữ liệu bảng bằng Apache POI (Excel).
- Đồng bộ vào Neo4j để truy vấn quan hệ và trực quan hóa bằng D3.js.

Điểm quan trọng của pipeline mở rộng là hợp nhất dữ liệu mạng và dữ liệu nghiệp vụ vào cùng mô hình đồ thị, từ đó tăng chất lượng phát hiện mẫu gian lận liên thực thể.

6. Kết quả đạt được sau khi bổ sung

Sau khi hoàn thiện các phần mở rộng, hệ thống đã chuyển từ mô hình phát hiện theo thực thể đơn sang mô hình phát hiện theo ngữ cảnh quan hệ. Điều này mang lại các giá trị chính:
- Tăng khả năng phát hiện chuỗi hành vi gian lận thay vì chỉ cảnh báo từng điểm bất thường.
- Tăng chất lượng giải thích kết quả nhờ kết hợp chỉ báo rule và liên kết đồ thị.
- Tạo nền tảng kỹ thuật sẵn sàng cho các thí nghiệm so sánh mô hình phân loại ở các bước nghiên cứu tiếp theo.

Phần bổ sung này bảo đảm báo cáo phản ánh đúng trạng thái triển khai hiện tại của hệ thống, đồng thời giữ được khả năng mở rộng cho các giai đoạn phát triển kế tiếp.


- --

## 📄 Trang 28

18
CHƯƠNG III: PHÂN TÍCH HỆTHỐNG

## 3.1. Khảo sát hiện trạng


## 3.1.1. Quy trình nghiệp vụhiện tại

Trong bối cảnh chuyển đổi sốmạnh mẽ, các hoạt động trực tuyến như giao dịch
điện tử, đăng ký tài khoản, thanh toán online và trao đổi dữliệu qua Internet
ngày càng phổbiến. Tuy nhiên, cùng với sựphát triển đó là sựgia tăng của các
hình thức gian lận như:
- Email lừa đảo (Phishing Email)
- Tấn công thông qua IP độc hại
- URL giảmạo website
- Tạo nhiều tài khoản giảcó liên kết với nhau
Hiện nay, việc phát hiện gian lận trong môi trường trực tuyến thường được thực
hiện theo các phương pháp sau:
1. Kiểm tra thủcông (Manual Investigation)
Đây là phương pháp truyền thống, thường được áp dụng trong các tổchức nhỏ
hoặc khi cần xác minh các trường hợp nghi ngờcụthể.
Quy trình thực hiện:
- Nhân viên an ninh mạng tiếp nhận thông tin nghi ngờ(Email/IP/URL).
- Tra cứu thông tin trên các hệthống blacklist công khai.
- Kiểm tra lịch sửtruy cập nội bộ.
- Đối chiếu với dữliệu giao dịch trước đó.
- Đưa ra kết luận dựa trên kinh nghiệm cá nhân.


- --

## 📄 Trang 29

19
Ưu điểm:
- Linh hoạt.
- Có thểxửlý các trường hợp đặc biệt.
Hạn chế:
- Tốn nhiều thời gian.
- Phụthuộc vào kinh nghiệm cá nhân.
- Không phù hợp khi khối lượng dữliệu lớn.
- Không thểphát hiện các mối quan hệphức tạp giữa nhiều thực thể.
2. Hệthống dựa trên luật (Rule-Based System)
Hệthống Rule-Based sửdụng tập hợp các luật cốđịnh đểđánh giá mức độrủi
ro.
Ví dụ:
- Nếu IP thuộc danh sách đen →đánh dấu nguy hiểm.
- Nếu Email chứa từkhóa nghi ngờ→tăng điểm rủi ro.
- Nếu URL có domain mới đăng ký gần đây →cảnh báo.
- Nếu một IP tạo quá nhiều tài khoản trong thời gian ngắn →nghi ngờspam.
Quy trình hoạt động:
Input →So khớp với tập luật →Tính điểm rủi ro →Kết luận.
Ưu điểm:
- Dễtriển khai.
- Dễkiểm soát.
- Không cần dữliệu huấn luyện.


- --

## 📄 Trang 30

20
Hạn chế:
- Không phát hiện được các mối quan hệẩn.
- Dễbịvượt qua khi kẻgian thay đổi hành vi.
- Khó mởrộng khi sốlượng luật tăng.
- Không phân tích được cấu trúc mạng lưới gian lận.
3. Hệthống Machine Learning
Phương pháp này sửdụng các thuật toán học máy đểdựđoán hành vi gian lận.
Quy trình:
- Thu thập dữliệu lịch sử.
- Trích xuất đặc trưng (feature engineering).
- Huấn luyện mô hình.
- Dựđoán rủi ro trên dữliệu mới.
Ưu điểm:
- Độchính xác cao nếu có dữliệu lớn.
- Tựđộng học và thích nghi.
Hạn chế:
- Cần lượng dữliệu huấn luyện lớn.
- Khó giải thích kết quả(Black-box model).
- Không trực quan hóa được mối quan hệgiữa các thực thể.
- Tập trung vào từng bản ghi riêng lẻthay vì toàn bộmạng lưới.


- --

## 📄 Trang 31

21
4. Hạn chếchung của các phương pháp hiện tại
Các phương pháp trên chủyếu tập trung vào phân tích từng thực thểriêng lẻ
(Email, IP hoặc URL). Tuy nhiên, trong thực tế:
- Một Email có thểliên kết với nhiều IP.
- Một IP có thểđược sửdụng bởi nhiều tài khoản.
- Một URL có thểđược chia sẻqua nhiều Email khác nhau.
- Các thực thểnày tạo thành một mạng lưới liên kết phức tạp.
Việc chỉphân tích từng phần tửđộc lập sẽbỏsót:
- Chuỗi liên kết gian lận.
- Nhóm tài khoản có hành vi phối hợp.
- Mạng lưới lan truyền rủi ro.
Điều này đòi hỏi một phương pháp tiếp cận mới dựa trên mô hình quan hệvà
cấu trúc mạng lưới.

## 3.1.2. Các vấn đềvà hạn chế

Qua khảo sát các phương pháp hiện tại, có thểtổng hợp các vấn đềchính như
sau:
1. Khó phát hiện mối quan hệẩn giữa Email – IP – URL
Trong cơ sởdữliệu quan hệ(SQL), các thực thểđược lưu trong bảng và liên kết
bằng khóa ngoại. Khi cần phân tích quan hệđa tầng, hệthống phải thực hiện
nhiều phép JOIN phức tạp.
Kết quả:
- Truy vấn chậm.
- Cấu trúc truy vấn phức tạp.


- --

## 📄 Trang 32

22
- Khó mởrộng khi dữliệu lớn.
2. Dữliệu phân tán, thiếu trực quan
Thông tin được lưu trữdưới dạng bảng nên:
- Không thểquan sát cấu trúc mạng lưới.
- Không thểnhìn thấy đường lan truyền rủi ro.
- Không dễdàng phát hiện cụm (cluster) gian lận.
3. Khó theo dõi lịch sửphân tích
Nhiều hệthống không lưu lại đầy đủ:
- Phiên phân tích
- Người thực hiện
- Mức độrủi ro theo thời điểm
- Sựthay đổi của dữliệu
Điều này gây khó khăn cho việc kiểm tra và đối chiếu.
4. Không có cơ chếlan truyền rủi ro (Risk Propagation)
Trong thực tế:
- Nếu một IP bịđánh giá nguy hiểm,
- Và IP đó liên kết với nhiều Email,
- Thì các Email đó cũng có nguy cơ cao.
Tuy nhiên, hệthống truyền thống không có cơ chếtính toán và lan truyền mức
độrủi ro qua các quan hệ.
5. Khảnăng mởrộng hạn chế


- --

## 📄 Trang 33

23
Khi thêm loại thực thểmới (ví dụ: Device, Phone Number, Location), hệthống
SQL cần:
- Thêm bảng mới.
- Thay đổi cấu trúc truy vấn.
- Tối ưu lại toàn bộlogic JOIN.
Điều này làm tăng độphức tạp và chi phí bảo trì.

## 3.1.3. Nhu cầu xây dựng hệthống mới

Từcác vấn đềtrên, đặt ra yêu cầu xây dựng một hệthống có khảnăng:
1.Phân tích dựa trên đồthịquan hệ.
2.Mô hình hóa các thực thểdưới dạng node.
3.Mô hình hóa liên kết dưới dạng relationship.
4.Tính toán Risk Score dựa trên quan hệ.
5.Lan truyền mức độrủi ro giữa các node.
6.Trực quan hóa mạng lưới gian lận.
7.Lưu trữđầy đủlịch sửphân tích.
8.Hỗtrợmởrộng dễdàng trong tương lai.
Việc áp dụng cơ sởdữliệu đồthịgiúp:
- Truy vấn quan hệnhiều tầng nhanh hơn.
- Phân tích cấu trúc mạng lưới hiệu quả.
- Hỗtrợcác thuật toán lan truyền rủi ro.
- Trực quan hóa dữliệu dễdàng.


- --

## 📄 Trang 34

24
Do đó, đềtài lựa chọn hướng tiếp cận xây dựng hệthống phát hiện gian lận dựa
trên phân tích đồthịnhằm khắc phục các hạn chếcủa mô hình truyền thống và
nâng cao hiệu quảphát hiện gian lận trong môi trường trực tuyến.

## 3.2. Phân tích yêu cầu


## 3.2.1. Yêu cầu nghiệp vụ

Yêu cầu nghiệp vụmô tảnhững chức năng hệthống phải thực hiện đểđáp ứng
nhu cầu thực tếcủa người sửdụng.
Dựa trên khảo sát hiện trạng và mục tiêu đềtài, hệthống cần đáp ứng các yêu
cầu nghiệp vụsau:
1. Quản lý người dùng theo vai trò
Hệthống phải hỗtrợphân quyền theo 3 vai trò:
- Admin: quản trịtoàn bộhệthống.
- Staff: thực hiện phân tích gian lận.
- Customer: sửdụng hệthống đểtra cứu và phân tích.
Mỗi vai trò có phạm vi truy cập và quyền hạn khác nhau. Việc phân quyền giúp
đảm bảo:
- Bảo mật hệthống.
- Kiểm soát truy cập dữliệu.
- Phân tách rõ ràng trách nhiệm.
2. Phân tích các thực thểgian lận
Hệthống phải cho phép phân tích các loại thực thểsau:
- Email
- IP Address


- --

## 📄 Trang 35

25
- URL
Người dùng có thểnhập một hoặc nhiều thực thểđểtiến hành phân tích.
3. Lưu trữphiên phân tích (Analysis Session)
Mỗi lần phân tích được xem là một phiên độc lập, bao gồm:
- Người thực hiện
- Thời điểm thực hiện
- Các node liên quan
- Các relationship được tạo
- Kết quảRisk Score
Việc lưu phiên giúp:
- Theo dõi lịch sử.
- Kiểm tra lại kết quả.
- Phục vụaudit hệthống.
4. Tính toán Risk Score
Hệthống phải tính toán mức độrủi ro cho từng node dựa trên:
- Base Risk (rủi ro nội tại).
- Graph Risk (rủi ro lan truyền qua quan hệ).
Risk Score cuối cùng được sửdụng đểxác định verdict:
- AN TOÀN
- CÓ DẤU HIỆU
- ĐÁNG NGHI NGỜ


- --

## 📄 Trang 36

26
- GIAN LẬN
5. Hiển thịđồthịquan hệ
Hệthống cần trực quan hóa:
- Node (Email, IP, URL, Domain, FileNode, FileHash, VictimAccount)
- Relationship (HAS_EMAIL, HAS_IP, HAS_URL, HAS_DOMAIN, HAS_FILE, HAS_FILEHASH, HAS_VICTIM, SENT_FROM_IP, CONTAINS_URL, HOSTED_ON, HOSTED_ON_DOMAIN, RESOLVES_TO, DOWNLOADS, HAS_HASH, RECEIVED, CONNECTS_TO)
- Mức độrủi ro bằng màu sắc
Việc hiển thịđồthịgiúp người dùng:
- Quan sát cấu trúc mạng lưới.
- Phát hiện cụm gian lận.
- Hiểu ngữcảnh của kết quảphân tích.
6. Lưu lịch sửphân tích
Hệthống cần lưu trữ:
- Lịch sửphân tích theo người dùng.
- Lịch sửtoàn hệthống (Admin có quyền xem).
Điều này đảm bảo khảnăng truy vết và kiểm soát hoạt động.
7. Quản trịngười dùng (Admin)
Admin có quyền:
- Tạo tài khoản Staff.
- Khóa/Mởkhóa tài khoản.
- Xóa tài khoản.
- Xem toàn bộlịch sửphân tích.


- --

## 📄 Trang 37

27

## 3.2.2. Yêu cầu chức năng

Yêu cầu chức năng mô tảchi tiết các chức năng hệthống phải cung cấp.
A. Nhóm chức năng xác thực
1. Đăng ký (Customer)
- Customer có thểtạo tài khoản mới.
- Thông tin gồm: username, mật khẩu.
- Hệthống kiểm tra trùng username.
- Mật khẩu được mã hóa trước khi lưu.
Staff không được tựđăng ký, tài khoản do Admin tạo.
2. Đăng nhập
- Người dùng nhập username và mật khẩu.
- Hệthống xác thực thông tin.
- Kiểm tra trạng thái tài khoản (hoạt động/bịkhóa).
- Tạo session đăng nhập nếu hợp lệ.
3. Đăng xuất
- Hủy session hiện tại.
- Chuyển vềtrang đăng nhập.
B. Nhóm chức năng phân tích gian lận
1. Tạo phiên phân tích
- Khi người dùng bắt đầu phân tích, hệthống tạo một Analysis Session mới.
- Gán phiên với người thực hiện.


- --

## 📄 Trang 38

28
- Lưu thời điểm bắt đầu.
2. Nhập Email/IP/URL
- Người dùng nhập dữliệu đầu vào.
- Hệthống chuẩn hóa dữliệu (hash/normalize).
- Kiểm tra định dạng hợp lệ.
3. Kiểm tra tồn tại trong hệthống
- Kiểm tra node đã tồn tại trong cơ sởdữliệu chưa.
- Nếu chưa có →tạo node mới.
- Nếu đã có →sửdụng node hiện có.
4. Tạo Node và Relationship
- Tạo các node Email, IP, URL, Domain, FileNode, FileHash và VictimAccount.
- Tạo relationship giữa các node theo ngữcảnh:
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
+CONNECTS_TO
- Gán trọng sốcho relationship.
5. Tính toán Risk Score
Quy trình tính toán gồm:
- Tính Base Risk cho từng node.
- Gọi GraphRiskService đểtính Graph Risk.
- Tổng hợp Final Risk.


- --

## 📄 Trang 39

29
- Gán verdict dựa trên Risk Score.
6. Hiển thịkết quả
Kết quảbao gồm:
- Base Risk
- Final Risk
- Verdict
- Danh sách node
- Danh sách relationship
7. Hiển thịđồthị
- Trực quan hóa node và relation.
- Màu sắc thểhiện mức độrủi ro.
- Có thểclick vào node đểxem chi tiết.
8. Xem chi tiết node
Hiển thị:
- Loại node (Email/IP/URL)
- Base Risk
- Graph Risk
- Final Risk
- Verdict
9. Xem lịch sửphân tích
- Customer và Staff xem lịch sửcá nhân.


- --

## 📄 Trang 40

30
- Admin xem toàn bộlịch sửhệthống.
C. Nhóm chức năng quản trị(Admin)
1. Tạo tài khoản Staff
- Nhập thông tin user mới.
- Gán role Staff.
- Lưu vào hệthống.
2. Khóa/Mởkhóa tài khoản
- Admin có thểthay đổi trạng thái tài khoản.
- Tài khoản bịkhóa không thểđăng nhập.
3. Xóa tài khoản
- Xóa user khỏi hệthống.
- Không ảnh hưởng dữliệu phân tích cũ (chỉmất quyền truy cập).
4. Xem toàn bộlịch sửhệthống
- Xem tất cảphiên phân tích.
- Có thểlọc theo thời gian hoặc người thực hiện.

## 3.2.3. Yêu cầu phi chức năng

Yêu cầu phi chức năng mô tảcác tiêu chí chất lượng của hệthống.
1. Hiệu năng (Performance)
- Thời gian phản hồi < 3 giây cho mỗi lần phân tích thông thường.
- Truy vấn đồthịphải được tối ưu.
- Hệthống hỗtrợđồng thời nhiều người dùng.


- --

## 📄 Trang 41

31
2. Bảo mật (Security)
- Mật khẩu phải được mã hóa.
- Phân quyền rõ ràng theo vai trò.
- Không cho phép truy cập trái phép vào API.
- Kiểm tra session ởmỗi request.
- Không lộthông tin nội bộhệthống.
3. Tính mởrộng (Scalability)
- Có thểthêm loại node mới (Device, Phone Number…).
- Có thểmởrộng thuật toán tính Risk.
- Có thểmởrộng sang API REST hoặc microservices trong tương lai.
4. Tính ổn định (Reliability)
- Không mất dữliệu khi xảy ra lỗi giữa chừng.
- Có cơ chếrollback transaction khi lỗi.
- Lưu log hoạt động đểkiểm tra sựcố.
5. Khảnăng bảo trì (Maintainability)
- Tách biệt rõ:
+Controller.
+Service.
+Repository.
- Code theo kiến trúc 3 lớp.
- Dễdàng nâng cấp hoặc thay đổi thuật toán Risk.


- --

## 📄 Trang 42

32

## 3.2.4. Mô hình hóa chức năng


## 3.3. MÔ HÌNH CA SỬDỤNG (USE CASE MODEL)

Mô hình Use Case được sửdụng đểmô tảcác tương tác giữa các tác nhân
(Actor) và hệthống phân tích gian lận dựa trên đồthị. Thông qua mô hình này,
ta xác định rõ:
- Các chức năng mà hệthống cung cấp
- Phạm vi quyền hạn của từng loại người dùng
- Luồng xửlý nghiệp vụtổng quát
Hệthống được xây dựng theo mô hình phân quyền nhiều vai trò, đảm bảo tính
bảo mật và quản trịtập trung.


- --

## 📄 Trang 43

33

## 3.3.1. Xác định Actor

Hệthống có 03 Actor chính:
1.Customer
- Là người dùng thông thường
- Có thểtựđăng ký tài khoản
- Thực hiện phân tích dữliệu
- Xem lịch sửcá nhân
2.Staff
- Tài khoản do Admin cấp
- Thực hiện phân tích dữliệu
- Xem lịch sửcá nhân
- Không có quyền quản trịhệthống
3.Admin
- Quản trịhệthống
- Có toàn bộquyền của Staff và Customer
- Có thêm quyền quản lý người dùng
- Xem toàn bộlịch sửhệthống
Quan hệkếthừa:
- Admin kếthừa quyền của Staff
- Staff và Customer có các quyền phân tích tương đương


- --

## 📄 Trang 44

34

## 3.3.2. Danh sách Use Case

Bảng 1. Danh sách Use Case của hệthống
STT
Mã
Use
Case
Tên Use Case
Tác nhân
Mô tả
1
UC1
Đăng ký
Customer
Customer tạo tài khoản mới
bằng cách nhập thông tin
đăng ký (username, mật
khẩu…). Hệthống kiểm tra
trùng lặp và lưu tài khoản nếu
hợp lệ.
2
UC2
Đăng nhập
Admin,
Staff,
Customer
Người dùng nhập thông tin
xác thực (username, mật
khẩu). Hệthống kiểm tra tính
hợp lệvà tạo phiên đăng nhập
theo vai trò.
3
UC3
Đăng xuất
Admin,
Staff,
Customer
Người dùng kết thúc phiên
làm việc. Hệthống hủy
session và chuyển vềtrang
đăng nhập.
4
UC4
Phân tích Email
Admin,
Staff,
Customer
Người dùng nhập Email cần
kiểm tra. Hệthống phân tích
mối quan hệ, tính toán mức
độrủi ro và lưu kết quảvào


- --

## 📄 Trang 45

35
phiên phân tích.
5
UC5
Phân tích IP
Admin,
Staff,
Customer
Người dùng nhập địa chỉIP.
Hệthống kiểm tra tồn tại
trong cơ sởdữliệu đồthị, tính
Risk Score và cập nhật quan
hệliên quan.
6
UC6
Phân tích URL
Admin,
Staff,
Customer
Người dùng nhập URL. Hệ
thống thực hiện phân tích,
đánh giá nguy cơ và cập nhật
vào mạng lưới quan hệ.
7
UC7
Tạo phiên phân
tích
Admin,
Staff,
Customer
Khi thực hiện phân tích, hệ
thống tạo một Analysis
Session đểlưu trữtoàn bộdữ
liệu và kết quảcủa lần phân
tích đó.
8
UC8
Tính Risk Score
Admin,
Staff,
Customer
Hệthống tính toán điểm rủi ro
dựa trên Base Risk và Graph
Risk, tổng hợp thành Final
Risk Score.
9
UC9
Xem đồthịquan
hệ
Admin,
Staff,
Customer
Hệthống hiển thịnetwork
graph gồm các node (Email,
IP, URL) và relationship, thể
hiện mức độrủi ro bằng màu
sắc.


- --

## 📄 Trang 46

36
10
UC10
Xem chi tiết
Node
Admin,
Staff,
Customer
Người dùng xem thông tin chi
tiết của một thực thể: loại,
Risk Score, sốlượng liên kết
và các quan hệliên quan.
11
UC11
Xem lịch sửcá
nhân
Admin,
Staff,
Customer
Người dùng xem danh sách
các phiên phân tích do chính
mình thực hiện.
12
UC12
Xem toàn bộlịch
sửhệthống
Admin
Admin xem tất cảcác phiên
phân tích của toàn hệthống.
13
UC13
Quản lý người
dùng
Admin
Admin thực hiện tạo tài khoản
Staff, khóa/mởkhóa tài khoản
và xóa tài khoản khỏi hệ
thống.
14
UC14
Quản lý Node
(Email/IP/URL)
Admin,
Staff
Admin thực hiện chỉnh sửa
thông tin hoặc xóa các node
Email, IP, URL trong hệ
thống đồthị; hệthống cập
nhật hoặc xóa các quan hệ
liên quan và tính toán lại mức
độrủi ro nếu cần.


- --

## 📄 Trang 47

37

## 3.3.3. Biểu đồUse Case tổng quát (Mô tả)

1.Cấu trúc biểu đồ
- Các Actor nằm bên trái hệthống.
- Hệthống được biểu diễn bằng một hình chữnhật.
- Các Use Case nằm bên trong hệthống.
- Admin có quan hệGeneralization (kếthừa) từStaff.
- Use Case "Phân tích dữliệu" có quan hệ<<include>> với:
+Tạo phiên phân tích.
+Tính Risk Score.
+Hiển thịđồthị.
2.Phân quyền tổng quát
Customer có thể:
- Đăng ký
- Đăng nhập
- Đăng xuất
- Phân tích Email/IP/URL
- Xem đồthị
- Xem lịch sửcá nhân
Staff có thể:
- Đăng nhập
- Đăng xuất


- --

## 📄 Trang 48

38
- Phân tích Email/IP/URL
- Xem đồthị
- Xem lịch sửcá nhân
Admin có thể:
- Đăng nhập
- Phân tích Email/IP/URL
- Xem đồthị
- Xem toàn bộlịch sửhệthống
- Quản lý người dùng
3.Bảng use case tổng quát
Mã
UC
Tên Use
Case
Actor
chính
Actor phụ
Mô tả
tổng
quan
Tiền
điều
kiện
Hậu
điều
kiện
Use
Cas
e
liên
qua
n
UC
1
Đăng ký
Custo
mer
Hệthống xác
thực
Custo
mer
tạo tài
khoản
mới
bằng
cách
nhập
Ngư
ời
dùng
chưa
có
tài
khoả
n
Tài
khoả
n
được
tạo
thành
công
UC2


- --

## 📄 Trang 49

39
userna
me và
mật
khẩu.
Hệ
thống
kiểm
tra
trùng
lặp và
mã hóa
mật
khẩu
trước
khi
lưu.
UC
2
Đăng nhập
Admin
, Staff,
Custo
mer
Hệthống xác
thực
Người
dùng
nhập
thông
tin xác
thực để
truy
cập hệ
thống.
Hệ
thống
kiểm
Tài
khoả
n tồn
tại
và
chưa
bị
khóa
Phiên
đăng
nhập
được
tạo
UC3
,
UC4
–
UC1
3


- --

## 📄 Trang 50

40
tra
thông
tin và
phân
quyền
theo
vai trò.
UC
3
Đăng xuất
Admin
, Staff,
Custo
mer
Hệthống
Người
dùng
kết
thúc
phiên
làm
việc và
thoát
khỏi hệ
thống.
Ngư
ời
dùng
đang
đăng
nhập
Phiên
làm
việc
bị
hủy
UC2
UC
4
Phân tích
Email
Admin
, Staff,
Custo
mer
Hệthống phân
tích
Nhập
Email
để
phân
tích
mức
độrủi
ro dựa
trên
Base
Đã
đăng
nhập
Kết
quả
phân
tích
Emai
l
được
hiển
thị
UC7
,
UC8
,
UC9


- --

## 📄 Trang 51

41
Risk
và
Graph
Risk.
UC
5
Phân tích IP
Admin
, Staff,
Custo
mer
Hệthống phân
tích
Nhập
IP để
phân
tích
mức
độrủi
ro và
các
quan
hệliên
quan.
Đã
đăng
nhập
Kết
quả
phân
tích
IP
được
hiển
thị
UC7
,
UC8
,
UC9
UC
6
Phân tích
URL
Admin
, Staff,
Custo
mer
Hệthống phân
tích
Nhập
URL
để
phân
tích
Risk
Score
và các
node
liên
quan.
Đã
đăng
nhập
Kết
quả
phân
tích
URL
được
hiển
thị
UC7
,
UC8
,
UC9
UC
Tạo phiên
Admin
Hệthống
Hệ
Ngư
Sessi
UC4


- --

## 📄 Trang 52

42
7
phân tích
, Staff,
Custo
mer
thống
tạo
Analys
is
Sessio
n để
lưu lại
toàn bộ
dữliệu
phân
tích.
ời
dùng
thực
hiện
phân
tích
on
được
lưu
vào
CSD
L
,
UC5
,
UC6
,
UC1
1,
UC1
2
UC
8
Tính Risk
Score
Admin
, Staff,
Custo
mer
FraudAnalysisS
ervice
Tính
Base
Risk,
Graph
Risk
và
Final
Risk
cho
thực
thể.
Có
dữ
liệu
phân
tích
Final
Risk
được
xác
định
UC4
,
UC5
,
UC6
UC
9
Xem đồthị
quan hệ
Admin
, Staff,
Custo
mer
GraphQuerySer
vice
Hiển
thị
mạng
lưới
Email
Có
phiê
n
phân
tích
Đồ
thị
được
rende
r
UC4
–
UC6
,
UC1


- --

## 📄 Trang 53

43
– IP –
URL
bằng
đồthị
trực
quan.
thành
công
0
UC
10
Xem chi tiết
Node
Admin
, Staff,
Custo
mer
Hệthống
Hiển
thị
thông
tin chi
tiết của
node
(Base
Risk,
Final
Risk,
Verdic
t).
Đồ
thị
đang
hiển
thị
Thôn
g tin
node
được
hiển
thị
UC9
UC
11
Xem lịch sử
cá nhân
Admin
, Staff,
Custo
mer
Hệthống lưu trữ
Hiển
thịcác
phiên
phân
tích do
chính
người
dùng
thực
Đã
đăng
nhập
Danh
sách
sessi
on cá
nhân
được
hiển
thị
UC7


- --

## 📄 Trang 54

44
hiện.
UC
12
Xem toàn
bộlịch sử
hệthống
Admin
Hệthống lưu trữ
Admin
xem
toàn bộ
session
phân
tích
của
mọi
người
dùng.
Adm
in
đăng
nhập
Danh
sách
toàn
bộ
sessi
on
hiển
thị
UC7
,
UC1
3
UC
13
Quản lý
người dùng
Admin
Hệthống quản
lý tài khoản
Admin
tạo tài
khoản
Staff,
khóa/
mở
khóa
và xóa
tài
khoản.
Adm
in
đăng
nhập
Tài
khoả
n
được
cập
nhật
UC1
,
UC2
,
UC1
2
UC
14
Quản lý
Node
(Email/IP/U
RL)
Admin
Hệ
thống
cho
phép
Admin
chỉnh
Adm
in đã
đăng
nhập
Node
được
cập
nhật
hoặc
bị
UC1
,
UC4
,
UC5
,


- --

## 📄 Trang 55

45
sửa
hoặc
xóa
các
node
Email,
IP,
URL
(Staff
chỉ
sửa)
trong
cơ sở
dữliệu
đồthị.
Khi
xóa
node,
hệ
thống
đồng
thời
xóa
các
quan
hệliên
quan
và cập
xóa
khỏi
hệ
thống
; các
quan
hệ
liên
quan
được
xửlý
UC6


- --

## 📄 Trang 56

46
nhật lại
trạng
thái rủi
ro nếu
cần.
4.Sơ đồuse case tổng quát


- --

## 📄 Trang 57

47

## 3.3.4. Sơ đồvà bảng đặc tảchi tiết từng Use Case

1. UC1: Đăng ký
A,Bảng đặc tảUC1-Đăng ký
Mục
Nội dung
Sốvà tên
UC
UC1 – Đăng ký
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép Customer tạo tài khoản mới đểsử
dụng hệthống. Hệthống kiểm tra trùng username và mã hóa
mật khẩu trước khi lưu vào cơ sởdữliệu.
Tác nhân
chính
Customer
Tác nhân
phụ(nếu có)
Hệthống xác thực
Sựkiện kích
hoạt
Người dùng truy cập trang đăng ký và chọn chức năng tạo tài
khoản.
Tiền điều
kiện
Người dùng chưa có tài khoản trong hệthống.
Hậu điều
kiện
Tài khoản mới được tạo và lưu thành công trong cơ sởdữliệu.
Luồng
1. Người dùng mởtrang đăng ký.


- --

## 📄 Trang 58

48
thông
thường
2. Người dùng nhập username và mật khẩu.
3. Người dùng nhấn nút Đăng ký.
4. Hệthống kiểm tra trùng username.
5. Hệthống mã hóa mật khẩu.
6. Hệthống lưu tài khoản vào cơ sởdữliệu.
7. Thông báo đăng ký thành công.
Luồng thay
thế
4a. Username đã tồn tại →Hệthống hiển thịthông báo lỗi và
yêu cầu nhập lại.
Các ngoại lệ
Lỗi kết nối cơ sởdữliệu →Hệthống hiển thịthông báo lỗi hệ
thống.
Độưu tiên
Cao
Các quy tắc
nghiệp vụ
- Username phải duy nhất trong hệthống.
- Mật khẩu phải được mã hóa trước khi lưu trữ.
- Tài khoản mới mặc định có vai trò Customer.
Các giả
thuyết
- Hệthống và cơ sởdữliệu hoạt động bình thường.
- Người dùng có kết nối Internet ổn định.
B,Sơ đồUC1-Đăng ký


- --

## 📄 Trang 59

49
2.UC2: Đăng nhập
A,Bảng đặc tảUC2-Đăng nhập
Mục
Nội dung
Sốvà tên
UC
UC2 – Đăng nhập
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép người dùng (Admin, Staff, Customer)
truy cập vào hệthống bằng tài khoản hợp lệ. Sau khi xác thực
thành công, hệthống tạo phiên đăng nhập và điều hướng người
dùng đến giao diện phù hợp với vai trò của họ.
Tác nhân
chính
Admin, Staff, Customer
Tác nhân
Hệthống xác thực


- --

## 📄 Trang 60

50
phụ(nếu
có)
Sựkiện
kích hoạt
Người dùng truy cập trang đăng nhập và lựa chọn chức năng
đăng nhập hệthống.
Tiền điều
kiện
- Tài khoản tồn tại trong hệthống.
- Tài khoản đang ởtrạng thái hoạt động (không bịkhóa).
Hậu điều
kiện
- Phiên đăng nhập được tạo thành công.
- Người dùng được chuyển đến dashboard tương ứng với vai trò.
Luồng
thông
thường
1. Người dùng mởtrang đăng nhập.
2. Người dùng nhập username/email và mật khẩu.
3. Người dùng nhấn nút Đăng nhập.
4. Hệthống xác thực thông tin đăng nhập.
5. Hệthống kiểm tra trạng thái tài khoản.
6. Hệthống xác định vai trò người dùng.
7. Hệthống tạo session đăng nhập.
8. Hệthống hiển thịgiao diện tương ứng với vai trò.
Luồng
thay thế
4a. Thông tin đăng nhập không chính xác →Hệthống hiển thị
thông báo lỗi và yêu cầu nhập lại.
5a. Tài khoản bịkhóa →Hệthống từchối đăng nhập và hiển thị
thông báo.
Các ngoại
lệ
- Lỗi kết nối cơ sởdữliệu.
- Lỗi hệthống trong quá trình xác thực.


- --

## 📄 Trang 61

51
Độưu
tiên
Cao
Các quy
tắc
nghiệp vụ
- Mật khẩu phải được mã hóa và so sánh với dữliệu đã lưu.
- Tài khoản bịkhóa không được phép đăng nhập.
- Quyền truy cập được phân theo vai trò
(Admin/Staff/Customer).
Các giả
thuyết
- Hệthống xác thực hoạt động bình thường.
- Cơ sởdữliệu sẵn sàng truy cập.
- Người dùng có kết nối Internet ổn định.
B, Sơ đồUC2-Đăng nhập
3. UC3: Đăng xuất
A,Bảng đặc tảUC3-Đăng xuất


- --

## 📄 Trang 62

52
Mục
Nội dung
Sốvà tên
UC
UC3 – Đăng xuất
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép người dùng kết thúc phiên làm việc và
thoát khỏi hệthống. Khi đăng xuất, hệthống sẽhủy session
hiện tại và chuyển người dùng vềtrang đăng nhập.
Tác nhân
chính
Admin, Staff, Customer
Tác nhân
phụ(nếu
có)
Hệthống
Sựkiện
kích hoạt
Người dùng chọn chức năng “Đăng xuất” trên giao diện hệ
thống.
Tiền điều
kiện
- Người dùng đã đăng nhập vào hệthống.
- Session đăng nhập đang tồn tại và còn hiệu lực.
Hậu điều
kiện
- Session đăng nhập bịhủy.
- Người dùng bịchuyển vềtrang đăng nhập.
- Không thểtruy cập chức năng hệthống nếu chưa đăng nhập
lại.
Luồng
thông
1. Người dùng chọn chức năng Đăng xuất.
2. Hệthống xác nhận yêu cầu đăng xuất.


- --

## 📄 Trang 63

53
thường
3. Hệthống hủy session hiện tại.
4. Hệthống xóa thông tin xác thực tạm thời (token/cookie nếu
có).
5. Hệthống chuyển người dùng vềtrang đăng nhập.
Luồng thay
thế
1a. Session đã hết hạn trước đó →Hệthống tựđộng chuyển về
trang đăng nhập.
Các ngoại
lệ
- Lỗi hệthống trong quá trình hủy session →Hiển thịthông
báo lỗi và yêu cầu thửlại.
Độưu tiên
Cao
Các quy tắc
nghiệp vụ
- Sau khi đăng xuất, mọi request yêu cầu xác thực phải bịtừ
chối.
- Không cho phép truy cập URL nội bộkhi chưa đăng nhập.
Các giả
thuyết
- Hệthống quản lý session hoạt động ổn định.
- Cơ sởdữliệu và server đang hoạt động bình thường.


- --

## 📄 Trang 64

54
B, Sơ đồUC3-Đăng
xuất
4.UC4: Phân tích Email.
A,Bảng đặc tảUC4-Phân tích Email
Mục
Nội dung
Sốvà tên
UC
UC4 – Phân tích Email
Người
tạo UC
Nhóm phát triển hệthống


- --

## 📄 Trang 65

55
Mô tả
Use Case này cho phép người dùng nhập một địa chỉEmail đểhệ
thống thực hiện phân tích mức độrủi ro. Hệthống sẽkiểm tra sự
tồn tại của Email trong cơ sởdữliệu đồthị, tạo hoặc cập nhật
node, thiết lập các mối quan hệliên quan và tính toán Risk Score
(Base Risk, Graph Risk, Final Risk).
Tác nhân
chính
Admin, Staff, Customer
Tác nhân
phụ(nếu
có)
Hệthống phân tích, FraudAnalysisService, CSDL đồthị
Sựkiện
kích hoạt
Người dùng nhập Email vào form phân tích và nhấn nút “Phân
tích”.
Tiền điều
kiện
- Người dùng đã đăng nhập hệthống.
- Email nhập vào đúng định dạng.
- Hệthống và CSDL hoạt động bình thường.
Hậu điều
kiện
- Tạo Analysis Session mới.
- Node Email được tạo hoặc cập nhật trong CSDL.
- Risk Score được tính toán và lưu.
- Kết quảphân tích được hiển thịcho người dùng.
Luồng
thông
thường
1. Người dùng nhập địa chỉEmail.
2. Người dùng nhấn nút Phân tích.
3. Hệthống kiểm tra định dạng Email.
4. Hệthống tạo Analysis Session mới.


- --

## 📄 Trang 66

56
5. Hệthống kiểm tra Email đã tồn tại trong CSDL chưa.
6. Nếu chưa tồn tại →tạo node Email mới.
7. Hệthống truy vấn các quan hệliên quan (IP, URL nếu có).
8. FraudAnalysisService tính Base Risk.
9. Hệthống tính Graph Risk dựa trên quan hệ.
10. Hệthống tính Final Risk.
11. Lưu kết quảvào CSDL.
12. Hiển thịkết quảvà cho phép xem đồthị.
Luồng
thay thế
3a. Email sai định dạng →Hệthống hiển thịthông báo lỗi.
6a. Email đã tồn tại →Cập nhật thông tin và tiếp tục tính Risk.
Các
ngoại lệ
- Lỗi kết nối CSDL.
- Lỗi trong quá trình tính toán Risk Score.
- Hệthống quá tải hoặc timeout.
Độưu
tiên
Rất cao (Chức năng cốt lõi của hệthống)
Các quy
tắc
nghiệp vụ
- Email phải tuân thủđịnh dạng chuẩn RFC.
- Risk Score được tính dựa trên Base Risk và Graph Risk.
- Kết quảphân tích phải được lưu trong Analysis Session tương
ứng.
Các giả
thuyết
- CSDL đồthị(Neo4j) sẵn sàng truy vấn.
- FraudAnalysisService hoạt động ổn định.


- --

## 📄 Trang 67

57
- Người dùng có quyền thực hiện phân tích.
B, Sơ đồUC4-Phân tích Email
5.UC5: Phân tích IP
A,Bảng đặc tảUC5-Phân tích IP
Mục
Nội dung
Sốvà tên
UC
UC5 – Phân tích IP


- --

## 📄 Trang 68

58
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép người dùng nhập một địa chỉIP đểhệ
thống phân tích mức độrủi ro. Hệthống kiểm tra sựtồn tại của
IP trong cơ sởdữliệu đồthị, tạo hoặc cập nhật node IP, thiết lập
các quan hệliên quan (Email, URL nếu có) và tính toán Risk
Score.
Tác nhân
chính
Admin, Staff, Customer
Tác nhân
phụ(nếu
có)
Hệthống phân tích, FraudAnalysisService, CSDL đồthị
Sựkiện
kích hoạt
Người dùng nhập địa chỉIP vào form phân tích và nhấn nút
“Phân tích”.
Tiền điều
kiện
- Người dùng đã đăng nhập.
- IP nhập vào đúng định dạng IPv4 hoặc IPv6.
- Hệthống và CSDL hoạt động bình thường.
Hậu điều
kiện
- Tạo Analysis Session mới.
- Node IP được tạo hoặc cập nhật.
- Risk Score được tính toán và lưu.
- Kết quảphân tích IP được hiển thị.
Luồng
thông
1. Người dùng nhập địa chỉIP.
2. Người dùng nhấn nút Phân tích.


- --

## 📄 Trang 69

59
thường
3. Hệthống kiểm tra định dạng IP.
4. Hệthống tạo Analysis Session.
5. Kiểm tra IP đã tồn tại trong CSDL chưa.
6. Nếu chưa tồn tại →tạo node IP mới.
7. Truy vấn các node liên quan (Email, URL).
8. Tính Base Risk dựa trên blacklist hoặc rule nội bộ.
9. Tính Graph Risk dựa trên quan hệ.
10. Tính Final Risk.
11. Lưu kết quảvào CSDL.
12. Hiển thịkết quảvà cho phép xem đồthị.
Luồng
thay thế
3a. IP sai định dạng →Hiển thịthông báo lỗi.
6a. IP đã tồn tại →Cập nhật thông tin và tiếp tục tính Risk.
Các ngoại
lệ
- Lỗi truy vấn CSDL.
- Timeout khi truy vấn quan hệlớn.
- Lỗi hệthống trong quá trình tính toán.
Độưu
tiên
Rất cao
Các quy
tắc
nghiệp vụ
- IP phải đúng chuẩn IPv4 hoặc IPv6.
- Risk Score được tổng hợp từBase Risk và Graph Risk.
- Mỗi lần phân tích phải tạo Analysis Session riêng.
Các giả
- Neo4j hoạt động ổn định.


- --

## 📄 Trang 70

60
thuyết
- Hệthống blacklist nội bộcó dữliệu đầy đủ.
- Người dùng có quyền phân tích.
B, Sơ đồUC5-Phân tích IP
6.UC6: Phân tích URL
A,Bảng đặc tảUC6-Phân tích URL
Mục
Nội dung


- --

## 📄 Trang 71

61
Sốvà tên
UC
UC6 – Phân tích URL
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép người dùng nhập một đường dẫn URL để
hệthống phân tích mức độrủi ro. Hệthống kiểm tra sựtồn tại
của URL trong cơ sởdữliệu đồthị, tạo hoặc cập nhật node URL,
thiết lập quan hệvới Email/IP liên quan và tính toán Risk Score.
Tác nhân
chính
Admin, Staff, Customer
Tác nhân
phụ(nếu
có)
Hệthống phân tích, FraudAnalysisService, CSDL đồthị
Sựkiện
kích hoạt
Người dùng nhập URL vào form phân tích và nhấn nút “Phân
tích”.
Tiền điều
kiện
- Người dùng đã đăng nhập hệthống.
- URL nhập vào đúng định dạng hợp lệ(http/https).
- Hệthống và CSDL hoạt động bình thường.
Hậu điều
kiện
- Tạo Analysis Session mới.
- Node URL được tạo hoặc cập nhật.
- Risk Score được tính toán và lưu.
- Kết quảphân tích URL được hiển thị.
Luồng
1. Người dùng nhập URL.


- --

## 📄 Trang 72

62
thông
thường
2. Người dùng nhấn nút Phân tích.
3. Hệthống kiểm tra định dạng URL.
4. Hệthống tạo Analysis Session.
5. Kiểm tra URL đã tồn tại trong CSDL chưa.
6. Nếu chưa tồn tại →tạo node URL mới.
7. Truy vấn các node liên quan (Email, IP).
8. Tính Base Risk (dựa vào domain bất thường, blacklist nội
bộ…).
9. Tính Graph Risk dựa trên quan hệmạng lưới.
10. Tính Final Risk.
11. Lưu kết quảvào CSDL.
12. Hiển thịkết quảvà cho phép xem đồthị.
Luồng
thay thế
3a. URL sai định dạng →Hiển thịthông báo lỗi.
6a. URL đã tồn tại →Cập nhật và tiếp tục tính Risk.
Các ngoại
lệ
- Lỗi truy vấn CSDL đồthị.
- Lỗi phân tích domain.
- Timeout khi dữliệu quan hệquá lớn.
Độưu
tiên
Rất cao
Các quy
tắc
nghiệp vụ
- URL phải bắt đầu bằng http:// hoặc https://.
- Risk Score được tính từBase Risk và Graph Risk.


- --

## 📄 Trang 73

63
- Mỗi lần phân tích phải gắn với một Analysis Session.
Các giả
thuyết
- CSDL Neo4j hoạt động ổn định.
- Hệthống blacklist và rule nội bộsẵn sàng.
- Người dùng có quyền thực hiện phân tích.
B, Sơ đồUC6-Phân tích URL
7.UC7: Tạo phiên phân tích
A,Bảng đặc tảUC7-Tạo phiên phân tích


- --

## 📄 Trang 74

64
Mục
Nội dung
Sốvà tên
UC
UC7 – Tạo phiên phân tích
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép hệthống tạo một Analysis Session mỗi
khi người dùng thực hiện phân tích Email/IP/URL. Phiên phân
tích lưu toàn bộthông tin vềdữliệu đầu vào, node liên quan,
Risk Score và thời điểm thực hiện đểphục vụtruy vết và xem lại
lịch sử.
Tác nhân
chính
Admin, Staff, Customer
Tác nhân
phụ(nếu
có)
Hệthống, FraudAnalysisService, CSDL
Sựkiện
kích hoạt
Người dùng thực hiện hành động phân tích (UC4, UC5 hoặc
UC6).
Tiền điều
kiện
- Người dùng đã đăng nhập hệthống.
- Dữliệu đầu vào hợp lệ.
- Hệthống sẵn sàng tạo session.
Hậu điều
kiện
- Một Analysis Session mới được tạo.
- Session được lưu vào cơ sởdữliệu.
- Session liên kết với các node được phân tích.


- --

## 📄 Trang 75

65
Luồng
thông
thường
1. Người dùng thực hiện phân tích Email/IP/URL.
2. Hệthống khởi tạo một Analysis Session mới.
3. Ghi nhận thông tin: người thực hiện, thời gian, loại dữliệu
phân tích.
4. Liên kết Session với các node liên quan (HAS_EMAIL,
HAS_IP, HAS_URL).
5. Lưu Session vào cơ sởdữliệu.
6. TrảSession ID cho các bước xửlý tiếp theo (tính Risk, hiển thị
kết quả).
Luồng
thay thế
2a. Session chưa thểtạo do lỗi dữliệu →Hệthống hủy phân tích
và thông báo lỗi.
Các ngoại
lệ
- Lỗi kết nối cơ sởdữliệu.
- Lỗi ghi transaction.
- Hết tài nguyên hệthống.
Độưu
tiên
Rất cao (vì liên quan đến toàn bộchức năng phân tích và lưu lịch
sử).
Các quy
tắc
nghiệp vụ
- Mỗi lần phân tích phải tạo một Session riêng biệt.
- Session phải gắn với một User cụthể.
- Không cho phép chỉnh sửa Session sau khi đã lưu hoàn tất.
Các giả
thuyết
- Hệthống lưu trữổn định.
- Transaction được đảm bảo tính toàn vẹn (ACID).
- CSDL đồthịhoạt động bình thường.


- --

## 📄 Trang 76

66
B, Sơ đồUC7-Tạo phiên phân tích
8.UC8: Tính Risk Score
A,Bảng đặc tảUC8-Tính Risk Score
Mục
Nội dung
Sốvà tên
UC
UC8 – Tính Risk Score
Người
Nhóm phát triển hệthống


- --

## 📄 Trang 77

67
tạo UC
Mô tả
Use Case này cho phép hệthống tính toán Risk Score dựa trên dữ
liệu Email/IP/URL được phân tích. Hệthống sửdụng các tiêu chí
như blacklist, tần suất xuất hiện, mối quan hệtrong đồthị, lịch sử
rủi ro đểxác định mức độnguy hiểm. Kết quảđược lưu vào
Analysis Session.
Tác nhân
chính
Hệthống (FraudAnalysisService)
Tác nhân
phụ(nếu
có)
CSDL đồthị, Risk Rule Engine
Sựkiện
kích hoạt
Sau khi hoàn tất phân tích Email/IP/URL (UC4, UC5, UC6) và
tạo Session (UC7).
Tiền điều
kiện
- Analysis Session đã được tạo.
- Dữliệu phân tích hợp lệ.
- Các node liên quan đã được lưu vào hệthống.
Hậu điều
kiện
- Risk Score được tính toán.
- Mức đánh giá (Safe / Medium / High Risk) được xác định.
- Kết quảđược lưu vào Analysis Session.
Luồng
thông
thường
1. Hệthống nhận dữliệu phân tích từSession.
2. Kiểm tra dữliệu có nằm trong blacklist không.
3. Phân tích mối quan hệtrong đồthị(liên kết với IP nguy hiểm,
URL độc hại, email rủi ro cao...).


- --

## 📄 Trang 78

68
4. Tính toán điểm rủi ro dựa trên trọng sốquy tắc.
5. Tổng hợp điểm và xác định mức Risk Level.
6. Lưu Risk Score và Risk Level vào Session.
7. Trảkết quảvềgiao diện người dùng.
Luồng
thay thế
3a. Không tìm thấy dữliệu liên kết trong hệthống →Risk Score
mặc định thấp.
2a. Dữliệu nằm trong blacklist →Risk Score tăng cao ngay lập
tức.
Các
ngoại lệ
- Lỗi truy vấn CSDL.
- Lỗi xửlý thuật toán tính điểm.
- Lỗi ghi kết quảvào Session.
Độưu
tiên
Rất cao (chức năng cốt lõi của hệthống Fraud Detection).
Các quy
tắc
nghiệp
vụ
- Risk Score được tính dựa trên tập quy tắc định sẵn.
- Trọng sốmỗi yếu tốphải được cấu hình trong hệthống.
- Nếu dữliệu nằm trong blacklist →Risk Level tối thiểu là High.
- Kết quảphải được lưu đểphục vụtruy vết.
Các giả
thuyết
- Bộquy tắc Risk đã được cấu hình.
- Dữliệu lịch sửtồn tại trong hệthống.
- Hệthống đảm bảo tính toàn vẹn dữliệu khi tính toán.
B, Sơ đồUC8-Tính Risk Score


- --

## 📄 Trang 79

69
9.UC9: Đồthịquan hệ
A,Bảng đặc tảUC9-Đồthịquan hệ
Mục
Nội dung
Sốvà tên
UC
UC9 – Đồthịquan hệ
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép người dùng xem mạng lưới quan hệgiữa


- --

## 📄 Trang 80

70
Email – IP – URL dưới dạng đồthịtrực quan. Hệthống truy vấn
dữliệu từCSDL đồthịvà hiển thịcác node cùng mối quan hệ
(SENT_FROM_IP, CONTAINS_URL, HOSTED_ON...).
Tác nhân
chính
Admin, Staff, Customer
Tác nhân
phụ(nếu
có)
GraphQueryService, CSDL đồthị
Sựkiện
kích hoạt
Người dùng chọn chức năng “Xem đồthị” sau khi hoàn tất phân
tích (UC4, UC5, UC6).
Tiền điều
kiện
- Người dùng đã đăng nhập.
- Analysis Session tồn tại.
- Có dữliệu phân tích được lưu trong hệthống.
Hậu điều
kiện
- Đồthịquan hệđược hiển thịthành công trên giao diện.
- Người dùng có thểtương tác với các node.
Luồng
thông
thường
1. Người dùng chọn chức năng xem đồthị.
2. Hệthống lấy Session hiện tại.
3. Hệthống truy vấn các node Email, IP, URL và các mối quan
hệliên quan.
4. Hệthống xây dựng cấu trúc dữliệu Graph (nodes & links).
5. Gửi dữliệu đến giao diện frontend.
6. Frontend render đồthịtrực quan.
7. Người dùng xem và tương tác với đồthị.


- --

## 📄 Trang 81

71
Luồng
thay thế
3a. Không có dữliệu liên kết →Hệthống hiển thịthông báo
“Không có dữliệu quan hệ”.
Các ngoại
lệ
- Lỗi truy vấn CSDL.
- Lỗi render đồthịtrên giao diện.
- Dữliệu graph không hợp lệ.
Độưu
tiên
Cao (chức năng trực quan hóa quan trọng).
Các quy
tắc
nghiệp vụ
- Chỉhiển thịdữliệu thuộc Session hiện tại (trừAdmin xem toàn
bộ).
- Các node phải hiển thịBase Risk và Final Risk.
- Các quan hệphải đúng theo chuẩn: SENT_FROM_IP,
CONTAINS_URL, HOSTED_ON.
Các giả
thuyết
- Dữliệu đã được lưu đầy đủtrong Session.
- Frontend hỗtrợthư viện vẽđồthị(D3.js hoặc tương đương).
- Hệthống đảm bảo hiệu năng khi render nhiều node.
B, Sơ đồUC9-Đồthịquan hệ


- --

## 📄 Trang 82

72
10.UC10: Xem chi tiết Node
A,Bảng đặc tảUC10-Xem chi tiết Node
Mục
Nội dung
Sốvà tên
UC
UC10 – Xem chi tiết Node
Người tạo
Nhóm phát triển hệthống


- --

## 📄 Trang 83

73
UC
Mô tả
Use Case này cho phép người dùng xem thông tin chi tiết của
một node (Email/IP/URL) trên đồthị, bao gồm Base Risk,
Graph Risk, Final Risk, Verdict và các quan hệliên quan.
Tác nhân
chính
Admin, Staff, Customer
Tác nhân
phụ(nếu
có)
Hệthống, GraphQueryService
Sựkiện
kích hoạt
Người dùng nhấn chọn vào một node trên đồthịquan hệ(UC9).
Tiền điều
kiện
- Người dùng đã đăng nhập.
- Đồthịquan hệđang được hiển thị.
- Node được chọn tồn tại trong Session.
Hậu điều
kiện
- Thông tin chi tiết của node được hiển thịđầy đủtrên giao
diện.
- Người dùng có thểtiếp tục thao tác hoặc quay lại đồthị.
Luồng
thông
thường
1. Người dùng click vào một node trên đồthị.
2. Hệthống nhận ID của node.
3. Hệthống truy vấn dữliệu chi tiết của node từCSDL.
4. Hệthống lấy thông tin Risk Score (Base, Graph, Final).
5. Hệthống xác định Verdict (Safe / Suspicious / Dangerous).
6. Hệthống hiển thịbảng thông tin chi tiết trên giao diện.


- --

## 📄 Trang 84

74
Luồng
thay thế
3a. Node không tồn tại →Hệthống hiển thịthông báo lỗi.
Các ngoại
lệ
- Lỗi truy vấn CSDL.
- Dữliệu Risk không đầy đủ.
- Lỗi hiển thịgiao diện.
Độưu tiên
Trung bình – Cao (hỗtrợphân tích chuyên sâu).
Các quy
tắc nghiệp
vụ
- Final Risk = kết hợp Base Risk + Graph Risk.
- Verdict được xác định theo ngưỡng Risk Score hệthống.
- Người dùng chỉxem được node thuộc phạm vi Session của
mình (trừAdmin).
Các giả
thuyết
- Dữliệu Risk đã được tính trước đó (UC8).
- Session đang hoạt động hợp lệ.
- Hệthống phản hồi truy vấn trong thời gian chấp nhận được.
B,Sơ đồUC10-Xem chi tiết Node


- --

## 📄 Trang 85

75
11.UC11: Xem lịch sửcá nhân
A,Bảng đặc tảUC11-Xem lịch sửcá nhân
Mục
Nội dung
Sốvà tên
UC
UC11 – Xem lịch sửcá nhân
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép người dùng xem danh sách các phiên
phân tích (Analysis Session) do chính mình thực hiện, bao gồm
thời gian tạo, loại thực thểphân tích và mức độrủi ro tổng hợp.
Tác nhân
chính
Admin, Staff, Customer


- --

## 📄 Trang 86

76
Tác nhân
phụ(nếu
có)
Hệthống lưu trữ, AnalysisSessionService
Sựkiện
kích hoạt
Người dùng chọn chức năng “Xem lịch sửcá nhân” từgiao diện
hệthống.
Tiền điều
kiện
- Người dùng đã đăng nhập.
- Tài khoản đang ởtrạng thái hoạt động.
Hậu điều
kiện
- Danh sách các phiên phân tích của người dùng được hiển thị.
- Người dùng có thểchọn một session đểxem chi tiết.
Luồng
thông
thường
1. Người dùng truy cập mục “Lịch sửcá nhân”.
2. Hệthống xác định ID người dùng hiện tại.
3. Hệthống truy vấn các Analysis Session thuộc vềngười dùng.
4. Hệthống sắp xếp theo thời gian (mới nhất trước).
5. Hệthống hiển thịdanh sách session trên giao diện.
6. Người dùng có thểchọn một session đểxem chi tiết hoặc đồ
thị.
Luồng
thay thế
3a. Không có session nào →Hệthống hiển thịthông báo “Chưa
có lịch sửphân tích”.
Các ngoại
lệ
- Lỗi truy vấn cơ sởdữliệu.
- Lỗi tải dữliệu session.
Độưu tiên
Trung bình
Các quy
- Người dùng chỉđược xem session do mình tạo (trừAdmin ở


- --

## 📄 Trang 87

77
tắc nghiệp
vụ
UC12).
- Session phải được lưu thành công trước đó (UC7).
- Mỗi session gắn với một người dùng duy nhất.
Các giả
thuyết
- Hệthống lưu trữsession hoạt động bình thường.
- Dữliệu session được lưu đầy đủvà chính xác.
B,Sơ đồUC11-Xem lịch sửcá nhân
12.UC12: Xem toàn bộlịch sửhệthống
A,Bảng đặc tảUC12-Xem lịch sửtoàn bộhệthống
Mục
Nội dung


- --

## 📄 Trang 88

78
Sốvà tên
UC
UC12 – Xem toàn bộlịch sửhệthống
Người tạo
UC
Nhóm phát triển hệthống
Mô tả
Use Case này cho phép Admin xem toàn bộcác phiên phân tích
(Analysis Session) của tất cảngười dùng trong hệthống nhằm
phục vụquản lý, kiểm tra và giám sát hoạt động phân tích.
Tác nhân
chính
Admin
Tác nhân
phụ(nếu
có)
Hệthống lưu trữ, AnalysisSessionService
Sựkiện
kích hoạt
Admin chọn chức năng “Xem toàn bộlịch sửhệthống” từgiao
diện quản trị.
Tiền điều
kiện
- Admin đã đăng nhập.
- Tài khoản Admin đang ởtrạng thái hoạt động.
Hậu điều
kiện
- Danh sách toàn bộsession trong hệthống được hiển thị.
- Admin có thểxem chi tiết từng session.
Luồng
thông
thường
1. Admin truy cập mục “Lịch sửhệthống”.
2. Hệthống xác thực quyền Admin.
3. Hệthống truy vấn tất cảAnalysis Session trong cơ sởdữliệu.
4. Hệthống sắp xếp session theo thời gian (mới nhất trước).
5. Hệthống hiển thịdanh sách session kèm thông tin người thực


- --

## 📄 Trang 89

79
hiện.
6. Admin có thểchọn một session đểxem chi tiết hoặc đồthị
quan hệ.
Luồng
thay thế
3a. Không có session nào trong hệthống →Hiển thịthông báo
“Chưa có dữliệu phân tích”.
Các ngoại
lệ
- Người dùng không có quyền Admin →Từchối truy cập và
hiển thịthông báo lỗi quyền truy cập.
- Lỗi truy vấn cơ sởdữliệu.
Độưu tiên
Trung bình
Các quy
tắc nghiệp
vụ
- ChỉAdmin mới có quyền truy cập chức năng này.
- Mỗi session phải hiển thịđầy đủ: người thực hiện, loại thực
thểphân tích, thời gian và mức độrủi ro.
- Dữliệu hiển thịphải phản ánh đúng thông tin đã lưu ởUC7.
Các giả
thuyết
- Hệthống phân quyền hoạt động chính xác.
- Cơ sởdữliệu hoạt động ổn định và lưu trữđầy đủcác session.
B, Sơ đồUC12-Xem lịch sửtoàn bộhệthống


- --

## 📄 Trang 90

80
13.UC13: Quản lý người dùng
A,Bảng đặc tảUC13-Quản lý người dùng
Mục
Nội dung
Sốvà tên
UC
UC13 – Quản lý người dùng
Người tạo
UC
Nhóm phát triển hệthống


- --

## 📄 Trang 91

81
Mô tả
Use Case này cho phép Admin thực hiện các chức năng quản lý
tài khoản người dùng như: tạo tài khoản Staff, khóa/mởkhóa tài
khoản, cập nhật thông tin và xóa tài khoản khỏi hệthống.
Tác nhân
chính
Admin
Tác nhân
phụ(nếu
có)
Hệthống quản lý tài khoản, Hệthống xác thực
Sựkiện
kích hoạt
Admin truy cập mục “Quản lý người dùng” trong giao diện
quản trị.
Tiền điều
kiện
- Admin đã đăng nhập.
- Tài khoản Admin đang hoạt động và có quyền quản trị.
Hậu điều
kiện
- Thông tin tài khoản được cập nhật chính xác trong hệthống.
- Các thay đổi được lưu vào cơ sởdữliệu.
Luồng
thông
thường
1. Admin truy cập chức năng “Quản lý người dùng”.
2. Hệthống hiển thịdanh sách tất cảngười dùng.
3. Admin chọn một hành động: tạo mới / khóa / mởkhóa / cập
nhật / xóa tài khoản.
4. Admin nhập hoặc xác nhận thông tin cần thay đổi.
5. Hệthống kiểm tra tính hợp lệcủa dữliệu.
6. Hệthống cập nhật dữliệu vào cơ sởdữliệu.
7. Hệthống hiển thịthông báo thao tác thành công.


- --

## 📄 Trang 92

82
Luồng thay
thế
5a. Thông tin nhập không hợp lệ→Hiển thịthông báo lỗi và
yêu cầu nhập lại.
3a. Admin hủy thao tác →Hệthống quay lại danh sách người
dùng.
Các ngoại
lệ
- Không có quyền Admin →Từchối truy cập.
- Lỗi kết nối cơ sởdữliệu.
- Không thểxóa tài khoản đang hoạt động (đang đăng nhập).
Độưu tiên
Cao
Các quy
tắc nghiệp
vụ
- ChỉAdmin được phép truy cập chức năng này.
- Username/Email phải duy nhất trong hệthống.
- Không được xóa tài khoản Admin mặc định.
- Tài khoản bịkhóa không được phép đăng nhập (liên quan
UC2).
Các giả
thuyết
- Hệthống phân quyền hoạt động chính xác.
- Cơ sởdữliệu người dùng hoạt động ổn định.
B, Sơ đồUC13-Quản lý người dùng


- --

## 📄 Trang 93

83
14.UC14: Quản lý Node (Email/IP/URL)
A,Bảng đặc tảUC14-Quản lý Node (Email/IP/URL)
Mục
Nội dung
Sốvà tên
UC
UC14 – Quản lý Node (Email/IP/URL)
Người tạo
Nhóm phát triển hệthống


- --

## 📄 Trang 94

84
UC
Mô tả
Use Case này cho phép Admin thực hiện các chức năng quản lý
các node Email, IP, URL trong hệthống phân tích đồthịnhư:
chỉnh sửa thông tin node (ví dụ: mức độrủi ro, trạng thái), và xóa
node khỏi cơ sởdữliệu. Khi xóa node, hệthống đồng thời xửlý
các quan hệliên quan đểđảm bảo tính toàn vẹn dữliệu.
Tác nhân
chính
Admin
Tác nhân
phụ(nếu
có)
Hệthống phân tích đồthị, Cơ sởdữliệu Neo4j
Sựkiện
kích hoạt
Admin truy cập mục “Quản lý Node” trong giao diện quản trịhệ
thống.
Tiền điều
kiện
- Admin đã đăng nhập thành công.
- Tài khoản Admin có quyền quản trịhệthống.
- Node cần chỉnh sửa/xóa tồn tại trong hệthống.
Hậu điều
kiện
- Node được cập nhật hoặc bịxóa thành công.
- Các quan hệliên quan được cập nhật hoặc xóa theo.
- Dữliệu đồthịđảm bảo tính nhất quán.
Luồng
thông
thường
1. Admin truy cập chức năng “Quản lý Node”.
2. Hệthống hiển thịdanh sách các node (Email/IP/URL).
3. Admin chọn một node cụthể.
4. Admin chọn hành động: chỉnh sửa hoặc xóa node.


- --

## 📄 Trang 95

85
5. Nếu chỉnh sửa: Admin cập nhật thông tin cần thay đổi (ví dụ
mức độrủi ro).
6. Hệthống kiểm tra tính hợp lệcủa dữliệu.
7. Hệthống cập nhật hoặc xóa node trong cơ sởdữliệu.
8. Hệthống hiển thịthông báo thao tác thành công.
Luồng
thay thế
6a. Dữliệu không hợp lệ→Hiển thịthông báo lỗi và yêu cầu
nhập lại.
4a. Admin hủy thao tác →Hệthống quay lại danh sách node.
Các ngoại
lệ
- Không có quyền Admin →Từchối truy cập.
- Node không tồn tại trong hệthống.
- Lỗi kết nối cơ sởdữliệu.
- Node đang được sửdụng trong phiên phân tích đang hoạt động
→Hệthống yêu cầu xác nhận trước khi xóa.
Độưu
tiên
Trung bình – Cao
Các quy
tắc
nghiệp vụ
- ChỉAdmin được phép chỉnh sửa hoặc xóa node.
- Khi xóa node phải xóa toàn bộquan hệliên quan (DETACH
DELETE).
- Không được phép chỉnh sửa ID hệthống của node.
- Mọi thay đổi phải được ghi log đểphục vụkiểm tra sau này.
Các giả
thuyết
- Hệthống phân quyền hoạt động chính xác.
- Cơ sởdữliệu đồthịhoạt động ổn định.


- --

## 📄 Trang 96

86
- Các phiên phân tích đang chạy được quản lý đúng cách.
B,Sơ đồUC14-Quản lý Node (Email/IP/URL)


- --

## 📄 Trang 97

87

## 3.4. Các biểu đồphân tích


## 3.4.1. Activity Diagram

A, Activity Diagram-Customer.
B, Activity Diagram-Staff.


- --

## 📄 Trang 98

88


- --

## 📄 Trang 99

89
C, Activity Diagram-ADMIN.


- --

## 📄 Trang 100

90

## 3.4.2. Sequence Diagram

A, Sơ đồSequence Diagram tổng quát.


- --

## 📄 Trang 101

91
B, Sơ đồSequence Diagram chi tiết.
1.Đăng ký.
2.Đăng nhập.


- --

## 📄 Trang 102

92
3.Đăng xuất.
4-5-6: Phân tích dữliệu


- --

## 📄 Trang 103

93
7. Tạo phiên phân tích.


- --

## 📄 Trang 104

94
8.Tính Risk Score.
9.Đồthịquan hệ


- --

## 📄 Trang 105

95
10. Xem chi tiết Node
11-12: Xem lịch sửphân tích


- --

## 📄 Trang 106

96
13.Quản lý người dùng
14. Quản lý Node(Email/IP/URL)


- --

## 📄 Trang 107

97


- --

## 📄 Trang 108

98

## 3.4.1. Class Diagram



- --

## 📄 Trang 109

99

## 3.5. Kết luận chương

Trong chương này, nhóm đã tiến hành phân tích và mô hình hóa toàn bộhệ
thống phát hiện gian lận dựa trên công nghệSpring Boot và cơ sởdữliệu đồthị
Neo4j. Các nội dung phân tích được xây dựng một cách hệthống, đảm bảo thể
hiện đầy đủyêu cầu nghiệp vụ, cấu trúc hệthống và luồng xửlý dữliệu.
Trước hết, nhóm đã xác định các tác nhân chính của hệthống bao gồm: Admin,
Staff và Customer, cùng với các quyền hạn và phạm vi chức năng tương ứng.
Từđó, hệthống được phân rã thành 14 Use Case chính (UC1–UC14), bao quát
toàn bộcác chức năng như: đăng ký, đăng nhập, phân tích Email/IP/URL, tính
toán Risk Score, hiển thịđồthịquan hệ, quản lý lịch sửphân tích và quản lý
người dùng.
Tiếp theo, các biểu đồphân tích đã được xây dựng nhằm làm rõ cấu trúc và
hành vi của hệthống:
- Use Case Diagram: Thểhiện tổng quan các chức năng và mối quan hệgiữa
tác nhân với hệthống.
- Activity Diagram: Mô tảchi tiết luồng xửlý nghiệp vụtheo từng vai trò
(Admin, Staff, Customer).
- Sequence Diagram: Làm rõ quá trình tương tác giữa Controller – Service –
Repository – Database trong từng chức năng.
- Class Diagram: Thểhiện cấu trúc lớp theo mô hình phân tầng (Controller –
Service – Repository – Model – DTO – Util), đảm bảo tuân thủnguyên tắc thiết
kếhướng đối tượng và kiến trúc MVC.
Hệthống được thiết kếtheo kiến trúc phân lớp rõ ràng, trong đó:
- Controller chịu trách nhiệm tiếp nhận request từngười dùng.


- --

## 📄 Trang 110

100
- Service xửlý nghiệp vụ, đặc biệt là FraudAnalysisService đóng vai trò trung
tâm trong việc tính toán Base Risk, Graph Risk và Final Risk.
- Repository tương tác với cơ sởdữliệu Neo4j đểtruy xuất và lưu trữdữliệu.
- DTO đảm nhiệm việc truyền dữliệu giữa backend và frontend.
- Util hỗtrợcác chức năng phụtrợnhư hash dữliệu và xác định verdict.
Việc áp dụng cơ sởdữliệu đồthịNeo4j giúp hệthống mô hình hóa hiệu quả
các mối quan hệgiữa Email – IP – URL thông qua các quan hệnhư
SENT_FROM_IP, CONTAINS_URL và HOSTED_ON. Điều này cho phép hệ
thống thực hiện lan truyền rủi ro (Graph Risk Propagation) một cách linh hoạt
và chính xác hơn so với mô hình dữliệu quan hệtruyền thống.
Nhìn chung, chương này đã xây dựng đầy đủcơ sởphân tích và thiết kếcho hệ
thống, làm tiền đềcho việc triển khai và cài đặt trong chương tiếp theo. Các mô
hình được trình bày đảm bảo tính chặt chẽ, nhất quán và phù hợp với yêu cầu
thực tếcủa bài toán phát hiện gian lận.


- --

## 📄 Trang 111

101
CHƯƠNG IV: THIẾT KẾHỆTHỐNG

## 4.1. Kiến trúc tổng thể

Hệthống phát hiện gian lận được xây dựng theo mô hình kiến trúc phân lớp, cụ
thểlà mô hình 3-Tier Architecture kết hợp với nguyên tắc tách biệt trách nhiệm.
Kiến trúc này giúp hệthống:
- 
Dễmởrộng.
- 
Dễbảo trì.
- 
Phân tách rõ ràng giữa giao diện – nghiệp vụ– dữliệu
- 
Phù hợp với mô hình Spring Boot MVC

## 4.1.1. Mô hình 3 lớp (Three-Tier Architecture)

Hệthống được chia thành 3 tầng chính:
1. Presentation Layer (Tầng giao diện)
Đây là tầng tương tác trực tiếp với người dùng.
Thành phần bao gồm:
Các file HTML trong thư mục:
- templates
+ login.html
+ register.html
+ dashboard
+ admin
+ staff
+ customer
Static resources:


- --

## 📄 Trang 112

102
+ CSS
+ JavaScript
Chức năng:
- Hiển thịgiao diện đăng nhập, đăng ký
- Dashboard theo vai trò:
+ Admin
+ Staff
+ Customer
- Hiển thịđồthịquan hệ(network graph)
- Gửi yêu cầu phân tích lên backend thông qua HTTP Request
Tầng này không chứa logic nghiệp vụ.
2. Business Logic Layer (Tầng xửlý nghiệp vụ)
Đây là tầng quan trọng nhất của hệthống, xửlý toàn bộlogic phân tích gian lận.
Thành phần chính:
- Controllers:
+ AuthController
+ AdminController
+ StaffController
+ CustomerController
+ DashboardController
- Services:


- --

## 📄 Trang 113

103
+ FraudAnalysisService (trung tâm)
+ GraphRiskService
+ GraphQueryService
+ UserService
+ AnalysisSessionService
+ ExcelImportService
Chức năng chính:
- Xác thực người dùng
- Phân quyền theo vai trò
- Tính toán Base Risk
- Lan truyền Graph Risk trên Neo4j
- Tổng hợp Final Risk
- Quyết định Verdict
- Lưu lịch sửphân tích (AnalysisSession)
- Quản lý người dùng
Tầng này không trực tiếp truy cập giao diện mà thông qua Controller.
3. Data Access Layer (Tầng truy xuất dữliệu)
Tầng này chịu trách nhiệm giao tiếp với cơ sởdữliệu Neo4j.
Thành phần:
- Repository:
+ UserRepository


- --

## 📄 Trang 114

104
+ EmailRepository
+ URLRepository
+ IPAddressRepository
- Neo4j Graph Database
Chức năng:
- Lưu và truy vấn Node:
+ User
+ Email
+ IPAddress
+ URL
+ AnalysisSession
- Quản lý Relationship:
+ SENT_FROM_IP
+ CONTAINS_URL
+ HOSTED_ON
+ HAS_EMAIL
+ HAS_IP
+ HAS_URL


- --

## 📄 Trang 115

105

## 4.1.2. Sơ đồkiến trúc hệthống


## 4.1.3. Luồng xửlý tổng quát trong kiến trúc

Quá trình xửlý một yêu cầu phân tích gian lận diễn ra như sau:
1.Người dùng (Admin/Staff/Customer) nhập Email/IP/URL trên giao diện.
2.Giao diện gửi HTTP Request đến Controller.
3.Controller gọi FraudAnalysisService.


- --

## 📄 Trang 116

106
4.FraudAnalysisService:
- Tính Base Risk
- Gọi GraphRiskService đểtính Graph Risk
- Tổng hợp Final Risk
- Quyết định Verdict
5.Service lưu dữliệu vào Neo4j thông qua Repository.
6.Kết quảđược trảvềController.
7.Controller trảGraphResponseDTO cho giao diện.
8.Giao diện hiển thịđồthịquan hệ.

## 4.1.4. Đánh giá kiến trúc

- Ưu điểm:
+ Phân lớp rõ ràng
+ Dễmởrộng thêm loại node mới
+ Có thểtích hợp AI / ML sau này
+ Dễbảo trì và test từng tầng
- Khảnăng mởrộng:
+ Có thểtách Service thành Microservices
+ Có thểdeploy Neo4j riêng server
+ Có thểtích hợp Redis cache
+ Có thểtriển khai trên Docker/Kubernetes


- --

## 📄 Trang 117

107

## 4.2. Thiết kếcơ sởdữliệu

Hệthống sửdụng Neo4j Graph Database làm cơ sởdữliệu chính đểlưu trữvà
xửlý quan hệgiữa các thực thểgian lận (Email – IP – URL).
Mô hình dữliệu được xây dựng theo hướng Graph Model, bao gồm:
- Node (Thực thể)
- Relationship (Quan hệ)
- Property (Thuộc tính)

## 4.2.1. ERD (Graph Model Diagram)

Trong Neo4j, ERD được thểhiện dưới dạng Graph Schema thay vì bảng quan
hệ.
Thực thểchính:
- User
- AnalysisSession
- Email
- IPAddress
- URL
Quan hệchính:
- SENT_FROM_IP
- CONTAINS_URL
- HOSTED_ON
- HAS_EMAIL
- HAS_IP


- --

## 📄 Trang 118

108
- HAS_URL
- CREATED

## 4.2.2. Lược đồquan hệ

Mặc dù dùng Neo4j, ta vẫn mô tảtheo dạng bảng logic đểphục vụbáo cáo.
USER
Tên cột
Kiểu dữliệu Mô tả
id
Long
Khóa chính
username String
Tên đăng nhập (unique)
email
String
Email người dùng
password
String
Mật khẩu đã mã hóa
role
String
ADMIN / STAFF /


- --

## 📄 Trang 119

109
CUSTOMER
locked
Boolean
Trạng thái khóa
ANALYSIS_SESSION
Tên cột
Kiểu dữliệu
Mô tả
id
String
Mã phiên phân tích
createdAt
LocalDateTim
e
Thời gian tạo
createdBy String
Username người tạo
EMAIL NODE
Tên thuộc tính Kiểu
Mô tả
id
String
Hash ID
value
String
Email thực tế
baseRisk
Double Rủi ro nội tại
graphRisk
Double Rủi ro lan truyền
finalRisk
Double Tổng rủi ro
verdict
String
SAFE / SUSPICIOUS / DANGEROUS
IPADDRESS NODE
Thuộc tính Kiểu


- --

## 📄 Trang 120

110
id
String
value
String
baseRisk
Double
graphRisk
Double
finalRisk
Double
verdict
String
URL NODE
Thuộc tính Kiểu
id
String
value
String
baseRisk
Double
graphRisk
Double
finalRisk
Double
verdict
String

## 4.2.3. Relationship

Quan hệ
Từ
Đến
Ý nghĩa
CREATED
User
AnalysisSession User tạo phiên
HAS_EMAIL
Session Email
Phiên chứa Email
HAS_IP
Session IP
Phiên chứa IP


- --

## 📄 Trang 121

111
HAS_URL
Session URL
Phiên chứa URL
SENT_FROM_IP
Email
IP
Email gửi từIP
CONTAINS_UR
L
Email
URL
Email chứa URL
HOSTED_ON
URL
IP
URL host trên IP

## 4.2.4. Đặc điểm thiết kếCSDL

Tối ưu cho:
- 
Phân tích quan hệnhiều tầng
- 
Risk Propagation
- 
Truy vấn pattern nhanh


- --

## 📄 Trang 122

112

## 4.3. Thiết kếthành phần phần mềm


## 4.3.1 Package Diagram



- --

## 📄 Trang 123

113

## 4.3.2 Deployment Diagram



- --

## 📄 Trang 124

114

## 4.4. Thiết kếgiao diện người dùng

1. Màn hình Đăng nhập
A, Mục đích
Cho phép người dùng xác thực đểtruy cập hệthống.
B, Thành phần giao diện
- Logo hệthống
- Trường Email
- Trường Password
- Nút “Đăng nhập”
- Liên kết “Đăng ký”
C, Chức năng xửlý
1.Kiểm tra dữliệu đầu vào


- --

## 📄 Trang 125

115
2.So khớp mật khẩu đã mã hóa
3.Kiểm tra trạng thái tài khoản (ACTIVE / LOCKED)
4.Phân quyền:
+ ADMIN →Dashboard Admin
+ STAFF →Dashboard Staff
+ CUSTOMER →Dashboard Customer
5.Hiển thịthông báo nếu:
+ Sai mật khẩu
+ Tài khoản bịkhóa
+ Không tồn tại email
2. Màn hình Đăng ký


- --

## 📄 Trang 126

116
A, Mục đích
Cho phép khách hàng tạo tài khoản mới.
B, Thành phần
- Email
- Password
- Confirm Password
- Nút Đăng ký
C, Xửlý
- Kiểm tra email trùng
- Kiểm tra định dạng hợp lệ
- Mã hóa mật khẩu
- Gán role mặc định: CUSTOMER
- Lưu vào database
- Hiển thịthông báo thành công
3.Màn hình Dashboard


- --

## 📄 Trang 127

117
A, Mục đích
Hiển thịtổng quan hệthống và điều hướng chức năng.
B, Bốcục
1. Header
- Logo
- Tên người dùng
- Vai trò
- Nút Logout
2. Menu
- Dashboard
- Phân tích dữliệu
- Lịch sửphân tích
- Graph Visualization
- (Quản lý người dùng – chỉAdmin)
- (Quản lý Node – Admin/Staff)
3. Khu vực xung quanh
Hiển thịnội dung theo chức năng được chọn.
Nội dung Dashboard
- Tổng sốphiên phân tích
- Tổng Email đã phân tích
- Tổng IP


- --

## 📄 Trang 128

118
- Tổng URL
- Sốlượng High Risk
- Biểu đồphân bốRisk Level
C, Phân quyền Dashboard
Admin
- Xem toàn bộhệthống
- Thống kê tất cảuser
- Quản lý user
Staff
- Xem dữliệu phân tích
- Quản lý node
Customer
- Xem dữliệu cá nhân
- Upload file
4. Màn hình Phân tích dữliệu
A, Mục đích


- --

## 📄 Trang 129

119
Cho phép upload file Excel và thực hiện phân tích gian lận.
B, Thành phần
- Nút Upload file
- Hiển thịtên file
- Nút Analyze
- Bảng kết quả
C, Bảng kết quảgồm:
- Email
- IP
- URL
- Risk Score
- Risk Level
- Verdict
- Indicators
D, Chức năng xửlý
1.Đọc file Excel
2.Phân tích risk
3.Tạo AnalysisSession
4.Tạo node Email/IP/URL
5.Tạo quan hệ:
+ SENT_FROM_IP


- --

## 📄 Trang 130

120
+ CONTAINS_URL
+ HOSTED_ON
6.Lưu vào Neo4j
7.Hiển thịkết quả
5. Màn hình Lịch sửphân tích
A, Mục đích
Hiển thịcác phiên phân tích đã thực hiện.
B, Thành phần
- Bảng danh sách session
- Các cột:
+ Session ID
+ File name
+ Created At
+ Status
+ Total Rows
- Nút “Xem chi tiết”


- --

## 📄 Trang 131

121
C, Xửlý
- Truy vấn session theo user
- Hiển thịchi tiết khi chọn
6. Màn hình Quản lý người dùng (Admin)
A, Mục đích
Quản lý tài khoản hệthống.
B, Thành phần
- Danh sách user
- Tạo mới
- Khóa
- Mởkhóa
- Xóa
C, Quy tắc
- Không xóa Admin mặc định
- Username phải duy nhất
- Tài khoản bịkhóa không được đăng nhập


- --

## 📄 Trang 132

122
7.Màn hình Quản lý Node (Admin / Staff)
A, Mục đích
Quản lý dữliệu Email, IP, URL trong hệthống.
B, Thành phần
- Bộlọc loại node
- Danh sách node
- Edit
- Delete (Soft delete)
C, Chức năng
- Cập nhật riskScore
- Cập nhật status
- Lưu lịch sửchỉnh sửa


- --

## 📄 Trang 133

123
8. Màn hình Giới thiệu hệthống
A, Mục đích
Cung cấp thông tin tổng quan vềhệthống Fraud Detection System, bao gồm
chức năng chính và phiên bản phần mềm.
B, Thành phần giao diện
- Tiêu đềtrang: “Giới thiệu”
- Thông tin hệthống: Logo bảo mật, tên hệthống và mô tảngắn
- Phần “Vềhệthống”: Đoạn mô tảmục tiêu và công nghệsửdụng
- Phần “Tính năng chính”: Danh sách các chức năng nổi bật (giám sát, phân
tích rủi ro, cảnh báo, báo cáo, quản lý người dùng)
- Phần “Phiên bản”: Hiển thịversion và thời gian phát hành
C, Đặc điểm
- Giao diện dạng một cột, bốcục rõ ràng
- Thiết kếtối giản, tập trung vào nội dung
- Trang thông tin tĩnh, không có thao tác xửlý dữliệu


- --

## 📄 Trang 134

124

## 4.5. Thiết kếxửlý

1. STATE MACHINE – USER


- --

## 📄 Trang 135

125
2.STATE MACHINE – ANALYSIS SESSION
3.STATE MACHINE – NODE (EMAIL/IP/URL)


- --

## 📄 Trang 136

126

## 4.6. Kết luận chương


# CHƯƠNG 4 ĐÃ TRÌNH BÀY TOÀN BỘQUÁ TRÌNH THIẾT KẾHỆTHỐNG PHÁT HIỆN GIAN LẬN DỰA

trên công nghệSpring Boot và cơ sởdữliệu đồthịNeo4j.
Trước hết, kiến trúc tổng thểcủa hệthống được xây dựng theo mô hình 3 lớp
(Presentation Layer – Business Layer – Data Layer), đảm bảo tính phân tách
trách nhiệm, dễmởrộng và bảo trì. Sơ đồkiến trúc hệthống đã thểhiện rõ mối
quan hệgiữa các thành phần như Controller, Service, Repository và cơ sởdữ
liệu Neo4j.
Tiếp theo, thiết kếcơ sởdữliệu được mô tảdưới dạng mô hình thực thểvà lược
đồquan hệ, đồng thời phân tích chi tiết các bảng dữliệu chính như User,
AnalysisSession, Email, IPAddress và URL. Cấu trúc dữliệu được tối ưu đểhỗ
trợphân tích đồthịvà lan truyền rủi ro.
Phần thiết kếthành phần phần mềm đã trình bày Class Diagram chi tiết,
Package Diagram và Deployment Diagram, giúp làm rõ cấu trúc tổchức mã
nguồn cũng như cách triển khai hệthống trong môi trường thực tế.
Ngoài ra, thiết kếgiao diện người dùng đã được mô tảthông qua wireframe và
đặc tảchức năng từng màn hình cho ba vai trò chính: Admin, Staff và Customer.
Các sơ đồxửlý như Sequence Diagram và State Machine Diagram đã minh họa
rõ luồng tương tác và vòng đời đối tượng trong hệthống.
Nhìn chung, chương này đã xây dựng nền tảng thiết kếđầy đủvà chặt chẽ, làm
cơ sởcho quá trình triển khai và cài đặt hệthống được trình bày trong chương
tiếp theo.


- --

## 📄 Trang 137

127
Chương V: Cài đặt và kết quảthửnghiệm

## 5.1. Môi trường triển khai


## 5.1.1. Môi trường phần cứng

- CPU: Intel Core i5 hoặc tương đương
- RAM: 8GB
- Ổcứng: 256GB SSD
- Kết nối Internet ổn định

## 5.1.2. Môi trường phần mềm

- Hệđiều hành: Windows 10 / Windows 11
- JDK: Java 17
- Framework: Spring Boot 3.x
- Cơ sởdữliệu: Neo4j AuraDB / Neo4j Desktop
- IDE: IntelliJ IDEA / VS Code
- Công cụquản lý mã nguồn: Git / GitHub
- Trình duyệt: Google Chrome

## 5.1.3. Công nghệsửdụng

- Spring Boot (REST API)
- Spring Data Neo4j
- Session-based Authentication (Spring Security)
- BCrypt Password Encoder
- Neo4j Graph Database


- --

## 📄 Trang 138

128
- HTML, CSS, JavaScript (Frontend)
- PlantUML (thiết kếUML)
- Figma (thiết kếgiao diện)

## 5.2. Cài đặt các chức năng chính

Hệthống đã triển khai đầy đủcác chức năng theo đặc tảtừUC1 đến UC14.

## 5.2.1. Chức năng đăng ký (UC1)

Mục đích
Cho phép người dùng tạo tài khoản mới đểtruy cập hệthống phát hiện gian lận.
Thành phần giao diện
- Tiêu đề: Create Account
- Biểu tượng người dùng phía trên


- --

## 📄 Trang 139

129
- Trường nhập:
+ Username
+ Email
+ Password
- Nút Register
- Liên kết chuyển sang trang Login
- Thông báo lỗi (nếu nhập sai dữliệu)
Đặc điểm thiết kế
- Thiết kếdạng Card trung tâm màn hình
- Nền gradient xanh lá
- Bo góc mềm, hiệu ứng đổbóng nhẹ
- Nút Register có hiệu ứng hover
Chức năng
- Kiểm tra dữliệu đầu vào:
- Không được đểtrống
- Email đúng định dạng
- Mật khẩu đủđộdài
- Tạo tài khoản mới trong hệthống
- Thông báo thành công hoặc lỗi
- Điều hướng sang trang đăng nhập sau khi đăng ký thành công


- --

## 📄 Trang 140

130

## 5.2.2. Chức năng đăng nhập (UC2)

Mục đích
Cho phép người dùng (Admin, Staff, Customer) xác thực tài khoản đểtruy cập
vào hệthống theo đúng phân quyền.
Thành phần giao diện
- Tiêu đề: Welcome Back
- Biểu tượng người dùng phía trên
- Trường nhập:
+ Email
+ Password


- --

## 📄 Trang 141

131
- Nút Login
- Liên kết chuyển sang trang Register
- Thông báo lỗi khi đăng nhập sai
Đặc điểm thiết kế
- Giao diện dạng Card đặt giữa màn hình
- Nền gradient tím – xanh hiện đại
- Bo góc mềm, đổbóng nhẹ
- Nút Login có hiệu ứng hover
- Thiết kếtối giản, tập trung vào thao tác đăng nhập
Chức năng xửlý
- Kiểm tra dữliệu đầu vào:
+ Không đểtrống
+ Email đúng định dạng
- Xác thực thông tin với cơ sởdữliệu
- Kiểm tra trạng thái tài khoản (ACTIVE / LOCKED)
- Phân quyền sau đăng nhập:
+ Admin →Trang quản trị
+ Staff →Trang xửlý dữliệu
+ Customer →Trang phân tích cá nhân
- Hiển thịthông báo lỗi nếu:
+ Sai email/mật khẩu


- --

## 📄 Trang 142

132
+ Tài khoản bịkhóa
+ Lỗi hệthống

## 5.2.3. Chức năng upload file Excel (UC4–UC6)

A, Chức năng upload file excel của admin
Mục đích
Cho phép người dùng tải lên file Excel chứa danh sách Email, IP, URL đểhệ
thống thực hiện phân tích và hiển thịkết quảđánh giá rủi ro.
Thành phần giao diện
- Tiêu đề: Nhập File Excel
- Nút Chọn file / Upload
- Thanh thông báo trạng thái xửlý
- Bảng kết quảphân tích gồm các cột:
+ STT
+ Email


- --

## 📄 Trang 143

133
+ IP
+ URL
+ Risk Level
+ Verdict
+ Trạng thái
Chức năng
- Kiểm tra định dạng file (.xlsx)
- Đọc dữliệu từfile Excel
- Gửi dữliệu sang backend đểphân tích
- Tính toán:
+ riskScore
+ riskLevel (low / medium / high)
+ verdict (AN TOÀN / ĐÁNG NGHI NGỜ/ NGUY HIỂM)
- Lưu dữliệu vào cơ sởdữliệu
- Hiển thịkết quảtheo dạng bảng
- Hiển thịtrạng thái xửlý thành công
Đặc điểm thiết kế
- Giao diện dạng card bo góc
- Bảng dữliệu rõ ràng, có phân dòng
- Cột trạng thái được tô màu trực quan
- Thiết kếtối giản, dễtheo dõi sốlượng lớn dữliệu


- --

## 📄 Trang 144

134
B, Chức năng upload file excel của staff
Mục đích
Hiển thịtrạng thái sau khi người dùng tải file Excel lên hệthống, xác nhận file
đã được tiếp nhận và xửlý thành công.
Thành phần giao diện
- Tiêu đề: Nhập file Excel
- Mô tảđịnh dạng file hỗtrợ: .xlsx / .xls (Email, IP, URL)
- Nút Chọn tệp
- Nút Tải lên
- Thanh thông báo trạng thái (màu xanh lá):
+ Icon xác nhận
+ Nội dung: Upload thành công: Book1.xlsx
Chức năng
- Kiểm tra định dạng file trước khi upload
- Gửi file lên server
- Nhận phản hồi từbackend
- Hiển thịthông báo thành công nếu:
+ File hợp lệ


- --

## 📄 Trang 145

135
+ Dữliệu được đọc thành công
+ Không xảy ra lỗi hệthống
Đặc điểm thiết kế
- Thông báo màu xanh lá thểhiện trạng thái thành công
- Thiết kếdạng banner nổi bật, dễnhận biết
- Giao diện tối giản, tập trung vào phản hồi hệthống
- Hỗtrợngười dùng xác nhận nhanh kết quảthao tác
C, Chức năng upload file excel của customer
Mục đích
Hiển thịtrạng thái sau khi người dùng tải file Excel lên hệthống, xác nhận file
đã được tiếp nhận và xửlý thành công.
Thành phần giao diện
- Tiêu đề: Nhập file Excel
- Mô tảđịnh dạng file hỗtrợ: .xlsx / .xls (Email, IP, URL)
- Nút Chọn tệp
- Nút Tải lên
- Thanh thông báo trạng thái (màu xanh lá):
+ Icon xác nhận


- --

## 📄 Trang 146

136
+ Nội dung: Upload thành công: Book1.xlsx
Chức năng
- Kiểm tra định dạng file trước khi upload
- Gửi file lên server
- Nhận phản hồi từbackend
- Hiển thịthông báo thành công nếu:
+ File hợp lệ
+ Dữliệu được đọc thành công
+ Không xảy ra lỗi hệthống
Đặc điểm thiết kế
- Thông báo màu xanh lá thểhiện trạng thái thành công
- Thiết kếdạng banner nổi bật, dễnhận biết
- Giao diện tối giản, tập trung vào phản hồi hệthống
- Hỗtrợngười dùng xác nhận nhanh kết quảthao tác

## 5.2.4. Chức năng truy vấn đồthị(UC7–UC8)

A, Đồthịliên kết gian lận


- --

## 📄 Trang 147

137
Mục đích
Hiển thịtrực quan mối quan hệgiữa Email – IP – URL nhằm hỗtrợphát hiện
cụm gian lận và các thực thểcó mức độrủi ro cao.
Thành phần giao diện
- Tiêu đề: Đồthịgian lận
- Bộlọc phiên (Session Filter):
+ Dropdown chọn phiên phân tích
+ Nút Tải lại
- Khu vực hiển thịđồthị(Graph Canvas):
+ Các node hình tròn đại diện cho:
~ Email
~ IP Address
~ URL
+ Các đường liên kết thểhiện quan hệ:


- --

## 📄 Trang 148

138
~ SENT_FROM_IP
~ CONTAINS_URL
~ HOSTED_ON
- Màu sắc phân loại mức độrủi ro:
+ Xanh: An toàn
+ Vàng: Trung bình
+ Đỏ: Nguy cơ cao
Chức năng
- Tải dữliệu theo phiên phân tích
- Hiển thịquan hệmạng lưới giữa các thực thể
- Phân biệt mức độrủi ro bằng màu sắc
- Hỗtrợkéo, thả, zoom đểquan sát chi tiết
- Giúp phát hiện:
+ Một IP dùng cho nhiều Email
+ Một URL liên kết nhiều Email đáng ngờ
+ Cụm node rủi ro cao tập trung
Ý nghĩa trong hệthống
Giao diện đồthịlà công cụquan trọng giúp:
- Phân tích hành vi gian lận theo cụm
- Phát hiện mối liên hệẩn
- Hỗtrợquyết định xửlý (block, theo dõi, cảnh báo)


- --

## 📄 Trang 149

139
B, Bảng phân tích kết quảtheo phiên
Mục đích
Hiển thịchi tiết kết quảphân tích gian lận của một phiên xửlý, bao gồm thông
tin Email – IP – URL và mức độrủi ro tương ứng.
Thành phần giao diện
- Tiêu đề: Kết quảphân tích theo phiên
- Bảng dữliệu kết quả, gồm các cột chính:
+ Email
+ IP
+ URL
+ Risk Score
+ Risk Level
+ Decision (Action)


- --

## 📄 Trang 150

140
- Màu sắc phân loại:
+ Xanh: An toàn
+ Cam/Vàng: Nghi ngờ
+ Đỏ: Nguy cơ cao
Chức năng
- Hiển thịdanh sách các thực thểđã phân tích trong phiên
- Tính toán và hiển thị:
+ Điểm rủi ro (riskScore)
+ Mức độrủi ro (Low / Medium / High)
- Đềxuất hành động:
+ ALLOW
+ REVIEW
+ BLOCK
- Phân biệt trực quan bằng màu sắc đểdễnhận diện thực thểnguy hiểm
Ý nghĩa trong hệthống
- Giúp quản trịviên đánh giá nhanh tình trạng rủi ro
- Hỗtrợra quyết định xửlý (chặn, theo dõi, cho phép)
- Là căn cứđểcập nhật trạng thái node trong cơ sởdữliệu

## 5.2.5. Chức năng quản lý node (UC14)

1. Chức năng quản lý node của admin.
A, Quản lý Email


- --

## 📄 Trang 151

141
- 
ĐồthịEmail
Mục đích
Hiển thịmạng lưới các Email trong hệthống nhằm phân tích mối liên hệvà phát
hiện cụm Email có dấu hiệu gian lận.
Thành phần giao diện
- Tiêu đề: ĐồthịEmail
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho Email
+ Các cạnh thểhiện mối liên hệ
- Màu sắc phân loại:
+ Xanh: Email an toàn


- --

## 📄 Trang 152

142
+ Đỏ: Email có nguy cơ cao
+ Vàng: Email đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các Email theo dạng mạng lưới
- Phân cụm các Email có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm Email gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ
- 
Bảng dữliệu Email
Mục đích


- --

## 📄 Trang 153

143
Quản lý danh sách Email đã được phân tích, theo dõi mức độrủi ro và thực hiện
các thao tác quản trị.
Thành phần giao diện
- Tiêu đề: Bảng Email
- Nút chức năng:
+ Tìm kiếm
+ Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (Email)
+ Value (địa chỉEmail)
+ Status
+ Risk Score
+ Linked Entities
+ Verdict
+ Indicators
+ Actions
- Màu sắc phân loại:
+ Đỏ: Email nguy cơ cao
+ Xanh: Email an toàn
+ Nền nhạt đỏ: Bản ghi có rủi ro cao
Chức năng


- --

## 📄 Trang 154

144
- Hiển thịdanh sách Email theo từng phiên phân tích
- Phân loại mức độrủi ro (Low / Medium / High)
- Hiển thịsốlượng thực thểliên kết (IP, URL…)
- Cập nhật trạng thái (Allow / Block)
- Thao tác quản trị:
+ Xem chi tiết
+ Sửa
+ Xóa (Soft delete)
Ý nghĩa trong hệthống
- Cho phép quản trịviên kiểm soát Email nghi ngờgian lận
- Hỗtrợquyết định chặn (BLOCK) hoặc cho phép (ALLOW)
- Kết hợp với đồthịgiúp phân tích cảdữliệu dạng bảng và trực quan mạng
lưới
- 
Khung sửa thông tin node Email


- --

## 📄 Trang 155

145
Mục đích
Cho phép quản trịviên chỉnh sửa thông tin của một Email node trực tiếp trên hệ
thống.
Thành phần giao diện
Giao diện hiển thịdưới dạng popup modal khi nhấn nút Sửa trong Bảng Email.
Bao gồm:
- Tiêu đề: Chỉnh sửa Node
- Trường thông tin:
+ Type: Email (không chỉnh sửa)
+ Value: Địa chỉEmail (có thểchỉnh sửa nếu hệthống cho phép)
- Nút chức năng:
+ Hủy
+ Lưu
Chức năng
- Cập nhật giá trịEmail
- Gửi yêu cầu cập nhật xuống backend
- Lưu thay đổi vào cơ sởdữliệu (Neo4j)
- Tựđộng cập nhật lại bảng và đồthịsau khi lưu thành công
Luồng xửlý
1.Người dùng nhấn Sửa
2.Hệthống mởpopup và load dữliệu hiện tại
3.Người dùng chỉnh sửa thông tin


- --

## 📄 Trang 156

146
4.Nhấn Lưu
5.Backend cập nhật node tương ứng
6.Frontend refresh lại dữliệu
Ý nghĩa trong hệthống
- Hỗtrợquản trịdữliệu linh hoạt
- Cho phép điều chỉnh khi phát hiện sai sót
- Đảm bảo tính chính xác của các node trong đồthịphân tích gian lận
B, Quản lý IPAdress
- 
ĐồthịIPAdress
Mục đích
Hiển thịmạng lưới các IPAdress trong hệthống nhằm phân tích mối liên hệvà
phát hiện cụm IPAdress có dấu hiệu gian lận.
Thành phần giao diện
- Tiêu đề: ĐồthịIPAdress


- --

## 📄 Trang 157

147
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho IPAdress
+ Các cạnh thểhiện mối liên hệ
- Màu sắc phân loại:
+ Xanh: IPAdress an toàn
+ Đỏ: IPAdress có nguy cơ cao
+ Vàng: IPAdress đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các IPAdress theo dạng mạng lưới
- Phân cụm các IPAdress có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm IPAdress gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ
- 
Bảng dữliệu IPAdress


- --

## 📄 Trang 158

148
Mục đích
- 
Quản lý danh sách IPAdress đã được phân tích, theo dõi mức độrủi ro và
thực hiện các thao tác quản trị.
Thành phần giao diện
- Tiêu đề: Bảng IPAdress
- Nút chức năng:
+ Tìm kiếm
+ Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (IPAdress)
+ Value (địa chỉIPAdress)
+ Status
+ Risk Score


- --

## 📄 Trang 159

149
+ Linked Entities
+ Verdict
+ Indicators
+ Actions
- Màu sắc phân loại:
+ Đỏ: IPAdress nguy cơ cao
+ Xanh: IPAdress an toàn
+ Nền nhạt đỏ: Bản ghi có rủi ro cao
Chức năng
- Hiển thịdanh sách IPAdress theo từng phiên phân tích
- Phân loại mức độrủi ro (Low / Medium / High)
- Hiển thịsốlượng thực thểliên kết (Email, URL…)
- Cập nhật trạng thái (Allow / Block)
- Thao tác quản trị:
+ Xem chi tiết
+ Sửa
+ Xóa (Soft delete)
Ý nghĩa trong hệthống
- Cho phép quản trịviên kiểm soát IPAdress nghi ngờgian lận
- Hỗtrợquyết định chặn (BLOCK) hoặc cho phép (ALLOW)
- Kết hợp với đồthịgiúp phân tích cảdữliệu dạng bảng và trực quan mạng
lưới


- --

## 📄 Trang 160

150
- 
Khung chỉnh sửa node IPAdress
Mục đích
Cho phép quản trịviên chỉnh sửa thông tin của một IPAdress node trực tiếp trên
hệthống.
Thành phần giao diện
Giao diện hiển thịdưới dạng popup modal khi nhấn nút Sửa trong Bảng
IPAdress.
Bao gồm:
- Tiêu đề: Chỉnh sửa Node
- Trường thông tin:
+ Type: IPAdress (không chỉnh sửa)
+ Value: Địa chỉIPAdress (có thểchỉnh sửa nếu hệthống cho phép)
- Nút chức năng:
+ Hủy


- --

## 📄 Trang 161

151
+ Lưu
Chức năng
- Cập nhật giá trịIPAdress
- Gửi yêu cầu cập nhật xuống backend
- Lưu thay đổi vào cơ sởdữliệu (Neo4j)
- Tựđộng cập nhật lại bảng và đồthịsau khi lưu thành công
Luồng xửlý
1.Người dùng nhấn Sửa
2.Hệthống mởpopup và load dữliệu hiện tại
3.Người dùng chỉnh sửa thông tin
4.Nhấn Lưu
5.Backend cập nhật node tương ứng
6.Frontend refresh lại dữliệu
Ý nghĩa trong hệthống
- Hỗtrợquản trịdữliệu linh hoạt
- Cho phép điều chỉnh khi phát hiện sai sót
- Đảm bảo tính chính xác của các node trong đồthịphân tích gian lận
C, Quản lý URL
- 
ĐồthịURL


- --

## 📄 Trang 162

152
Mục đích
Hiển thịmạng lưới các URL trong hệthống nhằm phân tích mối liên hệvà phát
hiện cụm URL có dấu hiệu gian lận.
Thành phần giao diện
- Tiêu đề: ĐồthịURL
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho URL
+ Các cạnh thểhiện mối liên hệ
- Màu sắc phân loại:
+ Xanh: URL an toàn
+ Đỏ: URL có nguy cơ cao


- --

## 📄 Trang 163

153
+ Vàng: URL đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các URL theo dạng mạng lưới
- Phân cụm các URL có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm URL gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ
- 
Bảng dữliệu URL
Mục đích
- 
Quản lý danh sách URL đã được phân tích, theo dõi mức độrủi ro và thực
hiện các thao tác quản trị.


- --

## 📄 Trang 164

154
Thành phần giao diện
- Tiêu đề: Bảng URL
- Nút chức năng:
+ Tìm kiếm
+ Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (URL)
+ Value (địa chỉURL)
+ Status
+ Risk Score
+ Linked Entities
+ Verdict
+ Indicators
+ Actions
- Màu sắc phân loại:
+ Đỏ: URL nguy cơ cao
+ Xanh: URL an toàn
+ Nền nhạt đỏ: Bản ghi có rủi ro cao
Chức năng
- Hiển thịdanh sách URL theo từng phiên phân tích


- --

## 📄 Trang 165

155
- Phân loại mức độrủi ro (Low / Medium / High)
- Hiển thịsốlượng thực thểliên kết (IP, Email…)
- Cập nhật trạng thái (Allow / Block)
- Thao tác quản trị:
+ Xem chi tiết
+ Sửa
+ Xóa (Soft delete)
Ý nghĩa trong hệthống
- Cho phép quản trịviên kiểm soát URL nghi ngờgian lận
- Hỗtrợquyết định chặn (BLOCK) hoặc cho phép (ALLOW)
- Kết hợp với đồthịgiúp phân tích cảdữliệu dạng bảng và trực quan mạng
lưới
- 
Khung sửa dữliệu node URL
Mục đích


- --

## 📄 Trang 166

156
Cho phép quản trịviên chỉnh sửa thông tin của một URL node trực tiếp trên hệ
thống.
Thành phần giao diện
Giao diện hiển thịdưới dạng popup modal khi nhấn nút Sửa trong Bảng URL.
Bao gồm:
- Tiêu đề: Chỉnh sửa Node
- Trường thông tin:
+ Type: URL (không chỉnh sửa)
+ Value: Địa chỉURL (có thểchỉnh sửa nếu hệthống cho phép)
- Nút chức năng:
+ Hủy
+ Lưu
Chức năng
- Cập nhật giá trịURL
- Gửi yêu cầu cập nhật xuống backend
- Lưu thay đổi vào cơ sởdữliệu (Neo4j)
- Tựđộng cập nhật lại bảng và đồthịsau khi lưu thành công
Luồng xửlý
1.Người dùng nhấn Sửa
2.Hệthống mởpopup và load dữliệu hiện tại
3.Người dùng chỉnh sửa thông tin
4.Nhấn Lưu


- --

## 📄 Trang 167

157
5.Backend cập nhật node tương ứng
6.Frontend refresh lại dữliệu
Ý nghĩa trong hệthống
- Hỗtrợquản trịdữliệu linh hoạt
- Cho phép điều chỉnh khi phát hiện sai sót
- Đảm bảo tính chính xác của các node trong đồthịphân tích gian lận
2. Chức năng quản lý node của staff.
A, Quản lý Email
- 
ĐồthịEmail
Mục đích
Hiển thịmạng lưới các Email trong hệthống nhằm phân tích mối liên hệvà phát
hiện cụm Email có dấu hiệu gian lận.
Thành phần giao diện
- Tiêu đề: ĐồthịEmail


- --

## 📄 Trang 168

158
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho Email
+ Các cạnh thểhiện mối liên hệ
- Màu sắc phân loại:
+ Xanh: Email an toàn
+ Đỏ: Email có nguy cơ cao
+ Vàng: Email đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các Email theo dạng mạng lưới
- Phân cụm các Email có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm Email gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ


- --

## 📄 Trang 169

159
- 
Bảng dữliệu Email
Mục đích
Hiển thịdanh sách các Email đã được phân tích và có mức độrủi ro thấp (An
toàn), phục vụviệc theo dõi và quản lý dữliệu.
Thành phần giao diện
- Tiêu đề: Bảng Email
- Thanh chức năng:
+ Ô tìm kiếm (lọc theo Email)
+ Nút Tìm kiếm
+ Nút Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (Email)
+ Value (địa chỉEmail)


- --

## 📄 Trang 170

160
+ Status
+ Risk Score
+ Linked Entities
+ Verdict
+ Actions
- Hiển thịtrực quan:
+ Risk Score = 0 (màu xanh)
+ Verdict: AN TOÀN
+ Nút hành động: Sửa
Chức năng
- Tìm kiếm Email theo từkhóa
- Xem mức độrủi ro hiện tại
- Kiểm tra sốlượng thực thểliên kết
- Chỉnh sửa thông tin node nếu cần
Ý nghĩa trong hệthống
- Hỗtrợgiám sát các Email không có dấu hiệu bất thường
- Cho phép quản trịviên xác nhận lại trạng thái
- Đảm bảo dữliệu sạch và nhất quán trong hệthống phân tích gian lận
- 
Khung sửa dữliệu node Email


- --

## 📄 Trang 171

161
Mục đích
Cho phép quản trịviên chỉnh sửa thông tin của một Email node trực tiếp trên hệ
thống.
Thành phần giao diện
Giao diện hiển thịdưới dạng popup modal khi nhấn nút Sửa trong Bảng Email.
Bao gồm:
- Tiêu đề: Chỉnh sửa Node
- Trường thông tin:
+ Type: Email (không chỉnh sửa)
+ Value: Địa chỉEmail (có thểchỉnh sửa nếu hệthống cho phép)
- Nút chức năng:
+ Hủy


- --

## 📄 Trang 172

162
+ Lưu
Chức năng
- Cập nhật giá trịEmail
- Gửi yêu cầu cập nhật xuống backend
- Lưu thay đổi vào cơ sởdữliệu (Neo4j)
- Tựđộng cập nhật lại bảng và đồthịsau khi lưu thành công
Luồng xửlý
1.Người dùng nhấn Sửa
2.Hệthống mởpopup và load dữliệu hiện tại
3.Người dùng chỉnh sửa thông tin
4.Nhấn Lưu
5.Backend cập nhật node tương ứng
6.Frontend refresh lại dữliệu
Ý nghĩa trong hệthống
- Hỗtrợquản trịdữliệu linh hoạt
- Cho phép điều chỉnh khi phát hiện sai sót
- Đảm bảo tính chính xác của các node trong đồthịphân tích gian lận
B, Quản lý IPAdress
- 
ĐồthịIPAdress


- --

## 📄 Trang 173

163
Mục đích
Hiển thịmạng lưới các IPAdress trong hệthống nhằm phân tích mối liên hệvà
phát hiện cụm IPAdress có dấu hiệu gian lận.
Thành phần giao diện
- Tiêu đề: ĐồthịIPAdress
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho IPAdress
+ Các cạnh thểhiện mối liên hệ
- Màu sắc phân loại:
+ Xanh: IPAdress an toàn


- --

## 📄 Trang 174

164
+ Đỏ: IPAdress có nguy cơ cao
+ Vàng: IPAdress đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các IPAdress theo dạng mạng lưới
- Phân cụm các IPAdress có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm IPAdress gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ
- 
Bảng dữliệu IPAdress
Mục đích


- --

## 📄 Trang 175

165
Hiển thịdanh sách các IPAdress đã được phân tích và có mức độrủi ro thấp
(An toàn), phục vụviệc theo dõi và quản lý dữliệu.
Thành phần giao diện
- Tiêu đề: Bảng IP Node
- Thanh chức năng:
+ Ô tìm kiếm (lọc theo IPAdress)
+ Nút Tìm kiếm
+ Nút Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (IPAdress)
+ Value (địa chỉIPAdress)
+ Status
+ Risk Score
+ Linked Entities
+ Verdict
+ Actions
- Hiển thịtrực quan:
+ Risk Score = 0 (màu xanh)
+ Verdict: AN TOÀN
+ Nút hành động: Sửa
Chức năng


- --

## 📄 Trang 176

166
- Tìm kiếm IPAdress theo từkhóa
- Xem mức độrủi ro hiện tại
- Kiểm tra sốlượng thực thểliên kết
- Chỉnh sửa thông tin node nếu cần
Ý nghĩa trong hệthống
- Hỗtrợgiám sát các IPAdress không có dấu hiệu bất thường
- Cho phép quản trịviên xác nhận lại trạng thái
- Đảm bảo dữliệu sạch và nhất quán trong hệthống phân tích gian lận
- 
Khung sửa dữliệu node IPAdress
Mục đích
Cho phép quản trịviên chỉnh sửa thông tin của một IPAdress node trực tiếp trên
hệthống.


- --

## 📄 Trang 177

167
Thành phần giao diện
Giao diện hiển thịdưới dạng popup modal khi nhấn nút Sửa trong Bảng
IPAdress.
Bao gồm:
- Tiêu đề: Chỉnh sửa Node
- Trường thông tin:
+ Type: IPAdress (không chỉnh sửa)
+ Value: Địa chỉIPAdress (có thểchỉnh sửa nếu hệthống cho phép)
- Nút chức năng:
+ Hủy
+ Lưu
Chức năng
- Cập nhật giá trịIPAdress
- Gửi yêu cầu cập nhật xuống backend
- Lưu thay đổi vào cơ sởdữliệu (Neo4j)
- Tựđộng cập nhật lại bảng và đồthịsau khi lưu thành công
Luồng xửlý
1.Người dùng nhấn Sửa
2.Hệthống mởpopup và load dữliệu hiện tại
3.Người dùng chỉnh sửa thông tin
4.Nhấn Lưu
5.Backend cập nhật node tương ứng


- --

## 📄 Trang 178

168
6.Frontend refresh lại dữliệu
Ý nghĩa trong hệthống
- Hỗtrợquản trịdữliệu linh hoạt
- Cho phép điều chỉnh khi phát hiện sai sót
- Đảm bảo tính chính xác của các node trong đồthịphân tích gian lận
C, Quản lý URL
- 
ĐồthịURL
Mục đích
Hiển thịmạng lưới các URL trong hệthống nhằm phân tích mối liên hệvà phát
hiện cụm URL có dấu hiệu gian lận.
Thành phần giao diện
- Tiêu đề: ĐồthịURL
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích


- --

## 📄 Trang 179

169
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho URL
+ Các cạnh thểhiện mối liên hệ
- Màu sắc phân loại:
+ Xanh: URL an toàn
+ Đỏ: URL có nguy cơ cao
+ Vàng: URL đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các URL theo dạng mạng lưới
- Phân cụm các URL có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm URL gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ
- 
Bảng dữliệu URL


- --

## 📄 Trang 180

170
Mục đích
Hiển thịdanh sách các URL đã được phân tích và có mức độrủi ro thấp (An
toàn), phục vụviệc theo dõi và quản lý dữliệu.
Thành phần giao diện
- Tiêu đề: Bảng URL Node
- Thanh chức năng:
+ Ô tìm kiếm (lọc theo URL)
+ Nút Tìm kiếm
+ Nút Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (URL)
+ Value (địa chỉURL)


- --

## 📄 Trang 181

171
+ Status
+ Risk Score
+ Linked Entities
+ Verdict
+ Actions
- Hiển thịtrực quan:
+ Risk Score = 0 (màu xanh)
+ Verdict: AN TOÀN
+ Nút hành động: Sửa
Chức năng
- Tìm kiếm URL theo từkhóa
- Xem mức độrủi ro hiện tại
- Kiểm tra sốlượng thực thểliên kết
- Chỉnh sửa thông tin node nếu cần
Ý nghĩa trong hệthống
- Hỗtrợgiám sát các URL không có dấu hiệu bất thường
- Cho phép quản trịviên xác nhận lại trạng thái
- Đảm bảo dữliệu sạch và nhất quán trong hệthống phân tích gian lận
- 
Khung sửa dữliệu URL


- --

## 📄 Trang 182

172
Mục đích
Cho phép quản trịviên chỉnh sửa thông tin của một URL node trực tiếp trên hệ
thống.
Thành phần giao diện
Giao diện hiển thịdưới dạng popup modal khi nhấn nút Sửa trong Bảng URL.
Bao gồm:
- Tiêu đề: Chỉnh sửa Node
- Trường thông tin:
+ Type: URL (không chỉnh sửa)
+ Value: Địa chỉURL (có thểchỉnh sửa nếu hệthống cho phép)
- Nút chức năng:


- --

## 📄 Trang 183

173
+ Hủy
+ Lưu
Chức năng
- Cập nhật giá trịURL
- Gửi yêu cầu cập nhật xuống backend
- Lưu thay đổi vào cơ sởdữliệu (Neo4j)
- Tựđộng cập nhật lại bảng và đồthịsau khi lưu thành công
Luồng xửlý
1.Người dùng nhấn Sửa
2.Hệthống mởpopup và load dữliệu hiện tại
3.Người dùng chỉnh sửa thông tin
4.Nhấn Lưu
5.Backend cập nhật node tương ứng
6.Frontend refresh lại dữliệu
Ý nghĩa trong hệthống
- Hỗtrợquản trịdữliệu linh hoạt
- Cho phép điều chỉnh khi phát hiện sai sót
- Đảm bảo tính chính xác của các node trong đồthịphân tích gian lận
3. Chức năng quản lý node của customer.
A, Quản lý Email
- 
ĐồthịEmail


- --

## 📄 Trang 184

174
Mục đích
Hiển thịmạng lưới các Email trong hệthống nhằm phân tích mối liên hệvà phát
hiện cụm Email có dấu hiệu gian lận.
Thành phần giao diện
- Tiêu đề: ĐồthịEmail
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho Email
+ Các cạnh thểhiện mối liên hệ


- --

## 📄 Trang 185

175
- Màu sắc phân loại:
+ Xanh: Email an toàn
+ Đỏ: Email có nguy cơ cao
+ Vàng: Email đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các Email theo dạng mạng lưới
- Phân cụm các Email có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm Email gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ
- 
Bảng dữliệu Email


- --

## 📄 Trang 186

176
Mục đích
Quản lý và theo dõi danh sách các Email node trong hệthống, bao gồm thông
tin rủi ro và trạng thái xửlý.
Thành phần giao diện
- Tiêu đề: Bảng Email Node
- Thanh tìm kiếm:
+ Ô nhập Email
+ Nút Tìm
+ Nút Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (Email)
+ Value (địa chỉEmail)
+ Status (valid / suspicious)


- --

## 📄 Trang 187

177
+ Risk Level (low / medium / high)
+ Risk Score (giá trịsố)
+ Verdict (AN TOÀN / ĐÁNG NGHI NGỜ/ BLOCK)
+ Indicators (dấu hiệu cảnh báo)
- Hiển thịtrực quan:
+ Risk Level hiển thịdạng badge màu (xanh – low)
+ Risk Score = 0 hiển thịmàu xanh
+ Verdict = AN TOÀN
Chức năng
- Tìm kiếm Email theo giá trị
- Lọc và xem mức độrủi ro
- Quan sát chỉbáo (Indicators)
- Theo dõi trạng thái xửlý của từng Email
- Hỗtrợchỉnh sửa hoặc cập nhật thông tin (nếu có quyền)
Ý nghĩa trong hệthống
- Cung cấp cái nhìn tổng quan vềtoàn bộEmail node
- Hỗtrợkiểm soát rủi ro ởmức chi tiết từng thực thể
- Là cơ sởdữliệu đầu vào cho việc phân tích đồthịgian lận
B, Quản lý IPAdress
- 
ĐồthịIPAdress


- --

## 📄 Trang 188

178
Mục đích
Hiển thịmạng lưới các IPAdress trong hệthống nhằm phân tích mối liên hệvà
phát hiện cụm IPAdress có dấu hiệu gian lận.
Thành phần giao diện
- Tiêu đề: ĐồthịIPAdress
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho IPAdress
+ Các cạnh thểhiện mối liên hệ
- Màu sắc phân loại:
+ Xanh: IPAdress an toàn
+ Đỏ: IPAdress có nguy cơ cao


- --

## 📄 Trang 189

179
+ Vàng: IPAdress đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các IPAdress theo dạng mạng lưới
- Phân cụm các IPAdress có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm IPAdress gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ
- 
Bảng dữliệu IPAdress
Mục đích
Quản lý và theo dõi danh sách các IPAdress node trong hệthống, bao gồm
thông tin rủi ro và trạng thái xửlý.


- --

## 📄 Trang 190

180
Thành phần giao diện
- Tiêu đề: Bảng IPAdress Node
- Thanh tìm kiếm:
+ Ô nhập IPAdress
+ Nút Tìm
+ Nút Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (IPAdress)
+ Value (địa chỉIPAdress)
+ Status (valid / suspicious)
+ Risk Level (low / medium / high)
+ Risk Score (giá trịsố)
+ Verdict (AN TOÀN / ĐÁNG NGHI NGỜ/ BLOCK)
+ Indicators (dấu hiệu cảnh báo)
- Hiển thịtrực quan:
+ Risk Level hiển thịdạng badge màu (xanh – low)
+ Risk Score = 0 hiển thịmàu xanh
+ Verdict = AN TOÀN
Chức năng
- Tìm kiếm IPAdress theo giá trị


- --

## 📄 Trang 191

181
- Lọc và xem mức độrủi ro
- Quan sát chỉbáo (Indicators)
- Theo dõi trạng thái xửlý của từng IPAdress
- Hỗtrợchỉnh sửa hoặc cập nhật thông tin (nếu có quyền)
Ý nghĩa trong hệthống
- Cung cấp cái nhìn tổng quan vềtoàn bộIPAdress node
- Hỗtrợkiểm soát rủi ro ởmức chi tiết từng thực thể
- Là cơ sởdữliệu đầu vào cho việc phân tích đồthịgian lận
C, Quản lý URL
- 
ĐồthịURL
Mục đích
Hiển thịmạng lưới các URL trong hệthống nhằm phân tích mối liên hệvà phát
hiện cụm URL có dấu hiệu gian lận.
Thành phần giao diện


- --

## 📄 Trang 192

182
- Tiêu đề: ĐồthịURL
- Thanh lọc dữliệu:
+ Ô chọn phiên phân tích
+ Nút “Tải lại”
- Khu vực hiển thịđồthị:
+ Các node đại diện cho URL
+ Các cạnh thểhiện mối liên hệ
- Màu sắc phân loại:
+ Xanh: URL an toàn
+ Đỏ: URL có nguy cơ cao
+ Vàng: URL đáng nghi ngờ
Chức năng
- Hiển thịtrực quan các URL theo dạng mạng lưới
- Phân cụm các URL có liên kết bất thường
- Tô màu theo mức độrủi ro
- Hỗtrợquan sát nhanh các điểm bất thường trong hệthống
Ý nghĩa trong hệthống
- Giúp phát hiện nhóm URL gian lận hoạt động theo cụm
- Hỗtrợphân tích hành vi bất thường
- Tăng khảnăng phát hiện gian lận dựa trên mối quan hệthay vì chỉphân tích
đơn lẻ
- 
Bảng dữliệu URL


- --

## 📄 Trang 193

183
Mục đích
Quản lý và theo dõi danh sách các URL node trong hệthống, bao gồm thông tin
rủi ro và trạng thái xửlý.
Thành phần giao diện
- Tiêu đề: Bảng URL Node
- Thanh tìm kiếm:
+ Ô nhập URL
+ Nút Tìm
+ Nút Reset
- Bảng dữliệu gồm các cột:
+ ID
+ Type (URL)
+ Value (địa chỉURL)
+ Status (valid / suspicious)
+ Risk Level (low / medium / high)
+ Risk Score (giá trịsố)


- --

## 📄 Trang 194

184
+ Verdict (AN TOÀN / ĐÁNG NGHI NGỜ/ BLOCK)
+ Indicators (dấu hiệu cảnh báo)
- Hiển thịtrực quan:
+ Risk Level hiển thịdạng badge màu (xanh – low)
+ Risk Score = 0 hiển thịmàu xanh
+ Verdict = AN TOÀN
Chức năng
- Tìm kiếm URL theo giá trị
- Lọc và xem mức độrủi ro
- Quan sát chỉbáo (Indicators)
- Theo dõi trạng thái xửlý của từng URL
- Hỗtrợchỉnh sửa hoặc cập nhật thông tin (nếu có quyền)
Ý nghĩa trong hệthống
- Cung cấp cái nhìn tổng quan vềtoàn bộURL node
- Hỗtrợkiểm soát rủi ro ởmức chi tiết từng thực thể
- Là cơ sởdữliệu đầu vào cho việc phân tích đồthịgian lận

## 5.2.6. Chức năng quản lý người dùng (UC13)

A, Giao diện tạo tài khoản cho nhân viên


- --

## 📄 Trang 195

185
Mục đích
Cung cấp giao diện trung tâm cho quản trịviên (Admin) thực hiện các chức
năng quản lý hệthống.
Thành phần giao diện
- Tiêu đề: Admin Dashboard
- Thông tin người dùng:
+ Hiển thịemail đăng nhập (admin@system.local)
+ Vai trò: ADMIN
- Nút chức năng:
+ Logout (đăng xuất)
- Thanh điều hướng (Navigation Menu):
+ Dashboard
+ Quản lý Email
+ Quản lý IP
+ Quản lý URL
+ Quản lý Tài khoản


- --

## 📄 Trang 196

186
+ Giới thiệu
- Khu vực chức năng chính:
+ Tạo tài khoản Staff
+ Ô nhập Email
+ Ô nhập Mật khẩu
+ Nút “Tạo Staff”
Chức năng
- Điều hướng đến các module quản lý
- Tạo tài khoản Staff mới
- Đăng xuất khỏi hệthống
- Truy cập trang giới thiệu hệthống
Ý nghĩa trong hệthống
- Là trung tâm điều khiển của Admin
- Quản lý toàn bộdữliệu Email, IP, URL
- Quản lý tài khoản người dùng
- Phân quyền và kiểm soát hệthống
B, Danh sách quản lý tài khoản của Admin


- --

## 📄 Trang 197

187
Mục đích
Cho phép Admin quản lý tài khoản người dùng trong hệthống, bao gồm tạo
mới, chỉnh sửa, khóa/mởkhóa và xóa tài khoản.
Thành phần giao diện
1. Tiêu đềtrang
- Quản lý Tài khoản
2. Thông tin người dùng
- Hiển thịemail đăng nhập
- Vai trò: ADMIN
3. Nút chức năng
- Logout (Đăng xuất)
4. Thanh điều hướng
- Dashboard
- Quản lý Email


- --

## 📄 Trang 198

188
- Quản lý IP
- Quản lý URL
- Quản lý Tài khoản
- Giới thiệu
5. Khu vực chính – Danh sách tài khoản
Bảng hiển thịdanh sách người dùng gồm các cột:
- Email
- Role (ADMIN / STAFF / CUSTOMER)
- Actions
6. Nút thao tác (Actions)
- Sửa
- Xóa
- (Có thểcó khóa/mởkhóa tùy trạng thái tài khoản)
Chức năng
- Hiển thịtoàn bộtài khoản trong hệthống
- Phân biệt vai trò người dùng
- Chỉnh sửa thông tin tài khoản
- Xóa tài khoản khỏi hệthống
- Kiểm soát tài khoản ADMIN không bịxóa trái phép
Ý nghĩa trong hệthống
- Đảm bảo phân quyền rõ ràng giữa Admin, Staff, Customer


- --

## 📄 Trang 199

189
- Tăng cường kiểm soát bảo mật
- Hỗtrợquản trịngười dùng tập trung
- Đảm bảo tính toàn vẹn dữliệu tài khoản
C, Khung sửa thông tin tài khoản của admin
Mục đích
Cho phép Admin cập nhật thông tin tài khoản người dùng trong hệthống mà
không cần rời khỏi trang Quản lý Tài khoản.
Thành phần giao diện
1. Tiêu đềhộp thoại
- Chỉnh sửa User
2. Trường thông tin hiển thị
- Email
+ Hiển thịemail của người dùng
+ Có thểởchếđộchỉđọc (không cho sửa)


- --

## 📄 Trang 200

190
- Vai trò (Role)
+ Danh sách chọn (Dropdown)
+ Giá trị: ADMIN / STAFF / CUSTOMER
- Trạng thái (Status)
+ Dropdown chọn:
~ ACTIVE
~ INACTIVE / LOCKED
3. Nút chức năng
- Hủy
+ Đóng popup, không lưu thay đổi
- Lưu
+ Xác nhận cập nhật thông tin
+ Gửi dữliệu vềserver
Chức năng
- Thay đổi vai trò người dùng
- Khóa hoặc mởkhóa tài khoản
- Cập nhật trạng thái hoạt động
- Lưu thay đổi vào cơ sởdữliệu
- Hiển thịthông báo thành công hoặc lỗi
Luồng xửlý
1.Admin nhấn nút Sửa tại danh sách tài khoản.


- --

## 📄 Trang 201

191
2.Hệthống hiển thịpopup chỉnh sửa.
3.Admin thay đổi Role hoặc Status.
4.Nhấn Lưu.
5.Hệthống kiểm tra hợp lệ.
6.Cập nhật dữliệu và đóng popup.
7.Danh sách tài khoản được refresh.
Ý nghĩa trong hệthống
- Giúp quản lý phân quyền linh hoạt.
- Kiểm soát trạng thái đăng nhập của người dùng.
- Tăng tính bảo mật khi có thểkhóa tài khoản nghi vấn.
- Đảm bảo quản trịtập trung.

## 5.2.7. Giao diện cơ chếphân quyền.

A, Admin
Mục đích
Hiển thịthông tin tổng quan của quản trịviên đang đăng nhập và cung cấp chức
năng đăng xuất hệthống.
Thành phần giao diện
1. Tiêu đềhệthống
- Nội dung: Admin Dashboard


- --

## 📄 Trang 202

192
- Kiểu chữlớn, nổi bật
- Màu xanh dương đậm, thểhiện quyền quản trị
2. Thông tin người dùng
- Hiển thị:
+ Email đăng nhập: admin@system.local
+ Vai trò: ADMIN
- Dạng chữnhỏhơn tiêu đề, nằm bên dưới
3. Nút chức năng
- Logout
+ Màu đỏ
+ Bo góc
+ Nằm phía bên phải màn hình
+ Có hiệu ứng hover
Chức năng
- Xác nhận người đang đăng nhập là Admin
- Hiển thịvai trò đểtránh nhầm lẫn quyền
- Cho phép đăng xuất khỏi hệthống
- Sau khi Logout:
+ Xóa session
+ Điều hướng vềtrang đăng nhập
Ý nghĩa trong hệthống


- --

## 📄 Trang 203

193
- Thểhiện rõ phân quyền người dùng
- Tăng tính minh bạch và bảo mật
- Là điểm truy cập chung của mọi module quản trị
- Tạo cảm giác chuyên nghiệp và nhất quán giao diện
B, Customer
Mục đích
Hiển thịthông tin người dùng Customer đang đăng nhập và cung cấp chức năng
đăng xuất hệthống.
Thành phần giao diện
1. Tiêu đềhệthống
- Nội dung: Customer Dashboard
- Font chữlớn, rõ ràng
- Thểhiện khu vực làm việc dành cho người dùng cuối
2. Thông tin người dùng
- Hiển thị:
+ Email đăng nhập: customer1@test.com
+ Vai trò: CUSTOMER
+ Đặt bên dưới tiêu đề
3. Nút chức năng
- Logout


- --

## 📄 Trang 204

194
+ Nằm phía bên phải màn hình
+ Thiết kếbo góc, màu sáng
+ Có hiệu ứng hover khi di chuột
Chức năng
- Xác định người dùng đang đăng nhập
- Hiển thịđúng vai trò đểđảm bảo phân quyền
- Cho phép đăng xuất hệthống
- Sau khi đăng xuất:
+ Hủy session
+ Chuyển vềtrang đăng nhập
Ý nghĩa trong hệthống
- Phân biệt rõ giao diện Customer với Admin và Staff
- Giới hạn quyền truy cập chỉởmức xem kết quảphân tích
- Đảm bảo tính bảo mật và kiểm soát truy cập
- Tạo sựnhất quán trong thiết kếtoàn hệthống
C, Staff
Mục đích
Hiển thịthông tin nhân viên (Staff) đang đăng nhập và cung cấp chức năng
đăng xuất khỏi hệthống.
Thành phần giao diện


- --

## 📄 Trang 205

195
1. Tiêu đềhệthống
- Nội dung: Staff Dashboard
- Font chữlớn, rõ ràng
- Thểhiện khu vực làm việc dành cho nhân viên phân tích
2. Thông tin người dùng
- Hiển thị:
+ Email đăng nhập: staff1@gmail.com
+ Vai trò: STAFF
- Đặt ngay bên dưới tiêu đề
3. Nút chức năng
- Logout
+ Nằm bên phải màn hình
+ Thiết kếtối giản, màu sáng
+ Có hiệu ứng hover
Chức năng
- Xác định tài khoản đang hoạt động
- Thểhiện vai trò đểđảm bảo phân quyền
- Cho phép đăng xuất khỏi hệthống
- Khi đăng xuất:
+ Hủy session
+ Chuyển vềtrang đăng nhập


- --

## 📄 Trang 206

196
Ý nghĩa trong hệthống
- Phân biệt rõ giao diện Staff với Admin và Customer
- Đảm bảo Staff chỉtruy cập các chức năng được cấp quyền (phân tích dữliệu,
xem kết quả, không quản lý tài khoản)
- Tăng tính bảo mật và kiểm soát truy cập theo vai trò
- Giữsựnhất quán trong thiết kếUI toàn hệthống

## 5.2.8. Giao diện phân tích rủi do trực tiếp

Mục đích
Cho phép người dùng (Admin/Staff) nhập trực tiếp Email – IP – URL đểthực
hiện phân tích rủi ro tức thời và xem kết quảđánh giá.
Thành phần giao diện
1.Khu vực nhập dữliệu
- Tiêu đề: Phân tích gian lận
- Mô tảngắn: Rủi ro trực tiếp là kết quảđánh giá từdữliệu bạn nhập vào tại
thời điểm hiện tại, chưa bao gồm ảnh hưởng lan truyền từcác node khác trong
hệthống.
- Ô nhập:


- --

## 📄 Trang 207

197
+ Email
+ IP
+ URL
- Nút chức năng:
+ Phân tích
2.Khu vực hiển thịkết quả
a. Thông tin tổng quan
- Nhãn mức rủi ro (ví dụ: medium) hiển thịdạng badge màu
- Kết luận (AN TOÀN / ĐÁNG NGỜ/ NGUY HIỂM)
- Điểm rủi ro (ví dụ: 35)
b. Thanh hiển thịRisk Score
- Thanh progress bar thểhiện mức độrủi ro
- Giá trịsốhiển thịbên phải
c. Dấu hiệu phát hiện (Indicators)
- Danh sách chi tiết:
+ CONTAINS_URL
+ HOSTED_ON
+ SENT_FROM_IP
- Hiển thịcông thức lan truyền:
+ base
+ depth


- --

## 📄 Trang 208

198
+ decay
+ final
→Thểhiện logic tính toán của hệthống (Graph propagation)
d. Nút điều khiển
- Nút Đóng
+ Đóng cửa sổkết quả
+ Quay lại giao diện nhập
Chức năng
- Gửi dữliệu lên backend (FraudAnalysisService)
- Tính:
+ Base Risk
+ Graph Risk (nếu có)
+ Final Risk
+ Verdict
- Hiển thịkết quảtrực quan
- Cho phép kiểm tra nhanh từng trường hợp riêng lẻ
Ý nghĩa trong hệthống
- Là chức năng cốt lõi của hệthống phát hiện gian lận
- Minh bạch hóa quá trình tính điểm rủi ro
- Hỗtrợchuyên viên phân tích đưa ra quyết định
- Thểhiện năng lực xửlý dữliệu theo thời gian thực


- --

## 📄 Trang 209

199

## 5.2.9. Giao diện Danh sách phiên phân tích

Mục đích
Hiển thịtoàn bộcác phiên phân tích (Analysis Session) đã được tạo trong hệ
thống, cho phép người dùng lựa chọn đểxem chi tiết kết quả.
Thành phần giao diện
1.Tiêu đề
- Nội dung: Tất cảdữliệu (ALL)
- Thểhiện đây là danh sách tổng hợp các phiên đã xửlý.
2.Danh sách phiên phân tích**


- --

## 📄 Trang 210

200
Mỗi dòng hiển thị:
- Session ID (UUID hoặc mã phiên)
- Tên file nhập (Book1.xlsx, TEST.xlsx, MANUAL_INPUT…)
- Timestamp (thời điểm xửlý – dạng epoch hoặc ISO time)
Ví dụhiển thị:
a6cba3d0-1a12-4f4c-8078-fa9308686443 | Book1.xlsx | 1771893047522
Hoặc:
session-001 | 2026-02-19T13:43:24.28Z
3.Cơ chếchọn phiên
- Khi người dùng click vào một dòng:
+ Hệthống load chi tiết dữliệu của phiên đó
+ Hiển thị:
~ Bảng kết quả
~ Đồthịgian lận
~ Thống kê risk
- Phiên đang được chọn được highlight.
Chức năng
- Lấy danh sách session từdatabase (AnalysisSession node)
- Sắp xếp theo thời gian giảm dần
- Cho phép truy xuất lại kết quảcũ
- Hỗtrợkiểm tra, so sánh các lần phân tích


- --

## 📄 Trang 211

201
Ý nghĩa trong hệthống
- Đảm bảo khảnăng traceability
- Cho phép kiểm toán dữliệu đã xửlý
- Hỗtrợtheo dõi lịch sửphân tích
- Phục vụcông tác điều tra và báo cáo

## 5.2.10. Giao diện giới thiệu nhóm phát triển

Mục đích
Cung cấp thông tin vềnhóm phát triển hệthống và mô tảtổng quan vềmục tiêu,
định hướng của dựán Fraud Detection System.
Thành phần giao diện
1.Phần tiêu đề
- Tiêu đềchính: Giới thiệu nhóm phát triển
- Dòng mô tảphụ: Hệthống phát hiện và phân tích rủi ro dựa trên Graph
Database
- Thanh điều hướng phía trên gồm:


- --

## 📄 Trang 212

202
+ Dashboard
+ Quản lý Email
+ Quản lý IP
+ Quản lý URL
+ Quản lý Tài khoản
+ Giới thiệu
→Cho phép người dùng chuyển nhanh giữa các module.
2.Khu vực “Thành viên nhóm”
Hiển thịdạng thẻ(card layout), mỗi card gồm:
- Ảnh đại diện (avatar)
- Họvà tên
- Vai trò trong dựán
- Mô tảngắn vềnhiệm vụđảm nhận
Ví dụnội dung:
- Phát triển Backend
- Thiết kếDatabase
- Xây dựng thuật toán phân tích
- Thiết kếgiao diện
3.Khu vực “Giới thiệu hệthống”
Phần mô tảtổng quan về:
- Mục tiêu hệthống


- --

## 📄 Trang 213

203
- Công nghệsửdụng
- Định hướng ứng dụng thực tế
- Giá trịmang lại
Nội dung nhấn mạnh:
- Phát hiện gian lận
- Phân tích dữliệu Email – IP – URL
- Ứng dụng Graph Database
- Hỗtrợdoanh nghiệp và tổchức
Chức năng
- Hiển thịthông tin nhóm phát triển
- Cung cấp tài liệu mô tảhệthống
- Tăng tính minh bạch và chuyên nghiệp của sản phẩm
- Là trang giới thiệu trong báo cáo và demo
Ý nghĩa trong hệthống
- Thểhiện tính học thuật và nghiêm túc của đồán
- Cung cấp thông tin nhóm thực hiện
- Tăng tính hoàn chỉnh của hệthống
- Phù hợp sửdụng trong bảo vệtốt nghiệp

## 5.3. Kết quảthực nghiệm


## 5.3.1. Môi trường kiểm thử

Hệthống được triển khai và đánh giá trong môi trường thửnghiệm với cấu hình
cụthểnhư sau:


- --

## 📄 Trang 214

204
Phần cứng
- Hệđiều hành: Windows 10 Pro 64-bit
- Bộxửlý: Intel Core i5 (4 nhân)
- RAM: 8GB
- Ổcứng: SSD 256GB
Phần mềm
- Ngôn ngữlập trình: Java 17
- Framework Backend: Spring Boot
- Cơ sởdữliệu: Neo4j Graph Database
- Công cụbuild: Maven
- Trình duyệt kiểm thử: Google Chrome
- Công cụthiết kếgiao diện: HTML, CSS, JavaScript
Kiến trúc triển khai
Hệthống được triển khai theo mô hình 3-layer:
- Presentation Layer (Web UI)
- Business Layer (Controller – Service)
- Data Layer (Repository – Neo4j)
Tất cảthành phần chạy trên môi trường local phục vụkiểm thửchức năng và
hiệu năng.

## 5.3.2. Tốc độxửlý

Thực nghiệm được tiến hành bằng cách import các file Excel có sốlượng bản
ghi khác nhau. Mỗi bản ghi bao gồm:


- --

## 📄 Trang 215

205
- Email
- IP Address
- URL
Hệthống thực hiện các bước:
1.Đọc file Excel
2.Kiểm tra dữliệu
3.Tạo/ghi đè node trong Neo4j
4.Tạo quan hệEmail – IP – URL
5.Tính toán riskScore
6.Lan truyền rủi ro (risk propagation)
Kết quảđo thời gian xửlý
Sốlượng bản ghi
Thời gian xửlý trung
bình
50 dòng
1 – 2 giây
200 dòng
3 – 5 giây
500 dòng
7 – 12 giây
Nhận xét
- Thời gian xửlý tăng tương đối tuyến tính theo sốlượng bản ghi.
- Với dưới 500 bản ghi, hệthống phản hồi nhanh, phù hợp môi trường demo.
- Neo4j xửlý truy vấn quan hệrất hiệu quảdo tối ưu cho mô hình đồthị.
- Chưa xuất hiện tình trạng nghẽn cổchai trong môi trường kiểm thử.
Đánh giá hiệu năng


- --

## 📄 Trang 216

206
Hệthống đáp ứng tốt yêu cầu xửlý dữliệu ởquy mô nhỏvà trung bình. Với
quy mô lớn hơn (trên 10.000 node), cần bổsung tối ưu hóa index và caching.

## 5.3.3. Đánh giá tính chính xác

Hệthống sửdụng cơ chếđánh giá rủi ro dựa trên rule-based kết hợp lan truyền
rủi ro trong đồthị.
Các tiêu chí đánh giá rủi ro
Email:
- Domain tạm thời (mailinator, tempmail…)
- Domain không phổbiến
IP:
- IP private (192.168.x.x, 10.x.x.x)
- IP thuộc blacklist
URL:
- TLD rủi ro cao (.xyz, .top…)
- Sửdụng HTTP không mã hóa
Quan hệlan truyền:
- Email chứa URL rủi ro
- Email gửi từIP đáng ngờ
- URL được host trên IP nguy hiểm
Kết quảthực nghiệm
- Phát hiện chính xác các email sửdụng domain tạm thời.
- Phát hiện IP nội bộ/private.


- --

## 📄 Trang 217

207
- Phát hiện URL có TLD rủi ro cao.
- Cơ chếlan truyền rủi ro hoạt động đúng:
+ RiskScore tăng khi node liên kết với node nguy hiểm.
+ Độsâu lan truyền được tính theo depth và decay.
Độtin cậy
- Hệthống cho kết quảphù hợp với tập dữliệu thửnghiệm.
- Tính chính xác phụthuộc vào:
+ Bộluật định nghĩa rủi ro
+ Chất lượng dữliệu đầu vào
- Chưa sửdụng Machine Learning nên khảnăng phát hiện mẫu gian lận phức
tạp còn hạn chế.

## 5.3.4. Đánh giá giao diện người dùng

Hệthống cung cấp các module giao diện chính:
- Đăng ký / Đăng nhập
- Dashboard theo vai trò (Admin, Staff, Customer)
- Quản lý Email / IP / URL
- Chỉnh sửa và Soft Delete node
- Phân tích trực tiếp
- Đồthịgian lận
- Lịch sửphiên phân tích
- Quản lý tài khoản
Đánh giá UX/UI


- --

## 📄 Trang 218

208
- Giao diện trực quan, dễsửdụng.
- Phân biệt rõ mức rủi ro bằng màu sắc:
+ Xanh: An toàn
+ Vàng: Trung bình
+ Đỏ: Nguy hiểm
- Phân quyền rõ ràng theo vai trò.
- Có popup xác nhận khi chỉnh sửa hoặc xóa.
- Dễthao tác trong môi trường demo.
Hệthống đáp ứng tốt yêu cầu trình diễn và thực nghiệm.

## 5.4. Đánh giá hệthống


## 5.4.1. Ưu điểm

1.Sửdụng Graph Database phù hợp bài toán gian lận.
2.Truy vết quan hệEmail – IP – URL hiệu quả.
3.Cơ chếlan truyền rủi ro giúp phát hiện nguy cơ gián tiếp.
4.Kiến trúc 3-layer rõ ràng, dễbảo trì.
5.Phân quyền hệthống đầy đủ:
+ Admin
+ Staff
+ Customer
6.Hỗtrợimport dữliệu từExcel.
7.Hỗtrợchỉnh sửa và soft delete node.


- --

## 📄 Trang 219

209
8.Lưu lịch sửphân tích phục vụtruy vết.
9.Thiết kếhệthống theo chuẩn UML đầy đủ.

## 5.4.2. Hạn chế

1.Hệthống hiện tại chủyếu dựa trên rule-based.
2.Chưa tích hợp Machine Learning.
3.Hiệu năng có thểgiảm khi dữliệu tăng lớn.
4.Chưa triển khai trên môi trường production.
5.Chưa có hệthống logging và monitoring chuyên sâu.
6.Chưa có cơ chếcaching.
7.Chưa có API public cho tích hợp bên ngoài.


- --

## 📄 Trang 220

210

## 5.5.Thông tin trong file readme



- --

## 📄 Trang 221

211


- --

## 📄 Trang 222

212


- --

## 📄 Trang 223

213


- --

## 📄 Trang 224

214


- --

## 📄 Trang 225

215


- --

## 📄 Trang 226

216


- --

## 📄 Trang 227

217
KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN
I. Kết quảđạt được
Sau quá trình nghiên cứu và xây dựng, đềtài đã đạt được:
- Xây dựng thành công hệthống phát hiện gian lận dựa trên Graph Database.
- Thiết kếkiến trúc 3-layer rõ ràng và có khảnăng mởrộng.
- Xây dựng đầy đủ:
+ 14 Use Case
+ Activity Diagram
+ Sequence Diagram
+ Class Diagram
+ ERD
+ Deployment Diagram
- Triển khai hoàn chỉnh các chức năng:
+ Phân tích Email – IP – URL
+ Tính toán và lan truyền rủi ro
+ Trực quan hóa đồthịgian lận
+ Quản lý node
+ Quản lý tài khoản
+ Phân quyền hệthống
- Hệthống hoạt động ổn định trong môi trường thửnghiệm.
Đềtài đáp ứng mục tiêu nghiên cứu đềra ban đầu.


- --

## 📄 Trang 228

218
II. Hạn chế
- Chưa tích hợp AI/ML nâng cao.
- Chưa xửlý dữliệu lớn theo thời gian thực quy mô doanh nghiệp.
- Chưa triển khai Cloud production.
- Chưa tích hợp hệthống cảnh báo tựđộng.
- Chưa đánh giá bằng tập dữliệu thực tếlớn.
III. Hướng phát triển tiếp theo
Trong tương lai, hệthống có thểphát triển theo các hướng:
1.Tích hợp Machine Learning đểtăng độchính xác.
2.Áp dụng thuật toán Graph Analytics nâng cao.
3.Tối ưu truy vấn bằng index và caching.
4.Triển khai hệthống trên Cloud (AWS, GCP, Azure).
5.Tích hợp Kafka đểxửlý dữliệu streaming real-time.
6.Xây dựng dashboard thống kê nâng cao.
7.Phát triển REST API cho tích hợp bên thứba.
8.Bổsung logging và monitoring chuyên nghiệp.


- --

## 📄 Trang 229

219
Link REPO
https://github.com/Subin1707/DACSCNTT
LINK DEMO
https://youtu.be/QiX1fXcoXKY?feature=shared


- --

## 📄 Trang 230

220
TÀI LIỆU THAM KHẢO
1.Tài liệu tổng quan vềfraud detection với graph database
2. Efficient phishing URL detection using graph-based ML
3. Phishing URL detection với neural networks
4. Phishing website detection bằng ML và CNN
5. Heuristic ML approaches cho URL & email phishing
6. Phishing website detection sửdụng advanced ML & GNN
7. URL phishing detection qua Autoencoder
8. Phát hiện email & URL lừa đảo dùng ML có giám sát
9. Phát hiện URL phishing dựa trên BERT
10. Phát hiện scam phishing trên Ethereum bằng graph network
11. Self-supervised deep graph learning cho Ethereum phishing
[12] Yannakakis, M. (1990, April). Graph-theoretic methods in database theory. In Proceedings of the ninth
ACM SIGACT-SIGMOD-SIGART symposium on Principles of database systems (pp. 230-242).
[13] Rumpe, B. (2016). Modeling with UML (Vol. 98). Cham: Springer.
