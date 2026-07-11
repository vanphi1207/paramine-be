-- Chạy 1 lần thủ công trên cùng database chứa bảng "nlogin" (hoặc 1 database
-- riêng nếu bạn muốn tách biệt hoàn toàn - khi đó set 2 DataSource, nhưng
-- project này mặc định dùng chung 1 DataSource cho đơn giản).
--
-- Bảng này KHÔNG liên quan gì tới schema của plugin nLogin - chỉ dùng nội bộ
-- để chặn JWT bị logout/refresh còn bị tái sử dụng.

CREATE TABLE IF NOT EXISTS invalidated_token (
    id           VARCHAR(36) NOT NULL PRIMARY KEY,   -- jti (JWT ID)
    expiry_time  DATETIME    NOT NULL
);
