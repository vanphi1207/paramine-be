package me.ihqqq.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "account_meta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountMeta {

    @Id
    @Column(name = "unique_id", length = 64)
    String uniqueId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    @Builder.Default
    Role role = Role.USER;

    @Column(name = "banned", nullable = false)
    @Builder.Default
    boolean banned = false;

    @Column(name = "updated_at")
    Instant updatedAt;
}