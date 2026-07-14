-- Chạy 1 lần thủ công trên cùng database chứa bảng "nlogin".
--
-- Bảng này KHÔNG liên quan gì tới schema của plugin nLogin - dùng nội bộ để
-- lưu access token dạng opaque (thay cho JWT). Đăng nhập tạo 1 dòng, đăng
-- xuất xoá dòng đó, hết hạn thì coi như không hợp lệ (có thể dọn định kỳ
-- bằng job xoá các dòng expiry_time < NOW()).

CREATE TABLE IF NOT EXISTS access_token (
    token        VARCHAR(36)  NOT NULL PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL,
    expiry_time  DATETIME     NOT NULL,
    created_at   DATETIME     NOT NULL,
    INDEX idx_access_token_username (username),
    INDEX idx_access_token_expiry (expiry_time)
);
