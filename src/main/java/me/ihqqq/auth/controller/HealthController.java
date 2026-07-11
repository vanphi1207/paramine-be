package me.ihqqq.auth.controller;

import me.ihqqq.auth.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    ApiResponse<Void> health() {
        try (Connection ignored = dataSource.getConnection()) {
            return ApiResponse.<Void>builder().message("Kết nối database nLogin OK").build();
        } catch (Exception e) {
            return ApiResponse.<Void>builder()
                    .code(9998)
                    .message("Không kết nối được database: " + e.getMessage())
                    .build();
        }
    }
}
