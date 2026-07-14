package me.ihqqq.auth.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.auth.dto.request.*;
import me.ihqqq.auth.dto.response.ApiResponse;
import me.ihqqq.auth.dto.response.AuthenticationResponse;
import me.ihqqq.auth.dto.response.IntrospectResponse;
import me.ihqqq.auth.service.AuthenticationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.login(request))
                .build();
    }

    @PostMapping("/register")
    ApiResponse<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.register(request))
                .build();
    }

    @PostMapping("/change-password")
    ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công")
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }
}