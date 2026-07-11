package me.ihqqq.auth.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class OfflineUuid {

    private OfflineUuid() {
    }

    public static String forUsername(String username) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        return uuid.toString().replace("-", "");
    }
}