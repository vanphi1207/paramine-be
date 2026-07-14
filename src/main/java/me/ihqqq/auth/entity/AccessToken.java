package me.ihqqq.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Token đăng nhập dạng opaque (chuỗi UUID ngẫu nhiên, không tự chứa thông tin
 * như JWT) được lưu trong DB. Client gửi lại token này qua header
 * "Authorization: Bearer <token>", server tra bảng này để xác thực.
 *
 * Ưu điểm so với JWT: thu hồi (revoke)/đăng xuất chỉ cần xoá 1 dòng, không cần
 * bảng "invalidated_token" riêng để chặn token cũ như trước.
 */
@Entity
@Table(name = "access_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessToken {

    @Id
    @Column(name = "token", length = 36)
    String token;

    @Column(name = "username", nullable = false)
    String username;

    @Column(name = "expiry_time", nullable = false)
    Instant expiryTime;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}