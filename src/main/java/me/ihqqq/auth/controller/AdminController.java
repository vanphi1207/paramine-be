package me.ihqqq.auth.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.auth.dto.request.UpdateAccountStatusRequest;
import me.ihqqq.auth.dto.response.AdminAccountResponse;
import me.ihqqq.auth.dto.response.AdminOverviewResponse;
import me.ihqqq.auth.dto.response.ApiResponse;
import me.ihqqq.auth.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    AdminService adminService;

    @GetMapping("/overview")
    ApiResponse<AdminOverviewResponse> overview() {
        return ApiResponse.<AdminOverviewResponse>builder()
                .result(adminService.getOverview())
                .build();
    }

    @GetMapping("/accounts")
    ApiResponse<List<AdminAccountResponse>> accounts(@RequestParam(required = false) String search) {
        return ApiResponse.<List<AdminAccountResponse>>builder()
                .result(adminService.listAccounts(search))
                .build();
    }

    @PostMapping("/accounts/{uniqueId}/status")
    ApiResponse<Void> setStatus(@PathVariable String uniqueId,
                                @RequestBody @Valid UpdateAccountStatusRequest request) {
        adminService.setBanned(uniqueId, request.isBanned());
        return ApiResponse.<Void>builder()
                .message("Cập nhật trạng thái tài khoản thành công")
                .build();
    }
}