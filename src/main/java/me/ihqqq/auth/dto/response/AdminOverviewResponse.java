package me.ihqqq.auth.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminOverviewResponse {
    long totalAccounts;
    long premiumAccounts;
    long onlineNow;
    long newAccountsToday;
}