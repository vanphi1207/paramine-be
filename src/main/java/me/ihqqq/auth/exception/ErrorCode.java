package me.ihqqq.auth.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),

    ACCOUNT_NOT_FOUND(1101, "Tài khoản không tồn tại trong game", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_REGISTERED(1102, "Tài khoản chưa đặt mật khẩu trong game (dùng /register trước)", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_EXISTS(1103, "Tên tài khoản đã tồn tại", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(1104, "Sai tên tài khoản hoặc mật khẩu", HttpStatus.UNAUTHORIZED),
    CURRENT_PASSWORD_INCORRECT(1105, "Mật khẩu hiện tại không đúng", HttpStatus.UNAUTHORIZED),
    ACCOUNT_BANNED(1106, "Tài khoản đã bị khoá", HttpStatus.FORBIDDEN),

    USERNAME_INVALID(1110, "Tên tài khoản 3-16 ký tự, chỉ gồm chữ/số/gạch dưới", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1111, "Tên tài khoản không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1112, "Mật khẩu không được để trống", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(1113, "Mật khẩu phải từ {min} ký tự", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1114, "Email không hợp lệ", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1115, "Email không được để trống", HttpStatus.BAD_REQUEST),

    UNAUTHENTICATED(1120, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1121, "Bạn không có quyền truy cập tài nguyên này", HttpStatus.FORBIDDEN),
    ;

    int code;
    String message;
    HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}