package me.ihqqq.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "nlogin")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NLoginAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai")
    Long ai;

    @Column(name = "last_name", nullable = false)
    String lastName;

    @Column(name = "unique_id")
    String uniqueId;

    @Column(name = "mojang_id")
    String mojangId;

    @Column(name = "bedrock_id")
    String bedrockId;

    @Column(name = "password")
    String password;

    @Column(name = "last_ip")
    String lastIp;

    @Column(name = "last_seen")
    Instant lastSeen;

    @Column(name = "creation_date")
    Instant creationDate;

    @Column(name = "email")
    String email;

    @Column(name = "discord")
    String discord;

    @Column(name = "settings")
    String settings;

    public boolean isRegistered() {
        return password != null && !password.isEmpty();
    }

    public boolean isPremium() {
        return mojangId != null && !mojangId.isEmpty();
    }
}