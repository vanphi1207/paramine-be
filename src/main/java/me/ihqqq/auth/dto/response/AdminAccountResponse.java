package me.ihqqq.auth.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAccountResponse {
    String id;
    String username;
    String email;
    boolean premium;
    boolean banned;
    String role;
    String createdAt;
}