-- Chạy 1 lần thủ công trên cùng database chứa bảng "nlogin".
--
-- Bảng này KHÔNG liên quan gì tới schema của plugin nLogin - dùng để lưu dữ
-- liệu riêng của web app gắn theo từng tài khoản: role (USER/ADMIN) và trạng
-- thái cấm (banned). Khoá chính "unique_id" khớp với cột unique_id của bảng
-- "nlogin".
--
-- Mặc định mọi tài khoản là USER. Để cấp quyền admin cho 1 tài khoản, chạy:
--
--   INSERT INTO account_meta (unique_id, role, banned, updated_at)
--   SELECT unique_id, 'ADMIN', FALSE, NOW()
--   FROM nlogin WHERE last_name = 'TenTaiKhoan'
--   ON DUPLICATE KEY UPDATE role = 'ADMIN';

CREATE TABLE IF NOT EXISTS account_meta (
    unique_id   VARCHAR(64) NOT NULL PRIMARY KEY,
    role        VARCHAR(16) NOT NULL DEFAULT 'USER',
    banned      BOOLEAN     NOT NULL DEFAULT FALSE,
    updated_at  DATETIME    NULL
);
