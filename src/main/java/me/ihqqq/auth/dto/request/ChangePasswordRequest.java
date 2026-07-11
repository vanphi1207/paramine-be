package me.ihqqq.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    String currentPassword;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, max = 64, message = "PASSWORD_TOO_SHORT")
    String newPassword;
}
