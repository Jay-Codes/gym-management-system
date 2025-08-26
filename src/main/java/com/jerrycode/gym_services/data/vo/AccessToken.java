package com.jerrycode.gym_services.data.vo;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Data
@Table(name = "access_tokens")
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 500, nullable = false, unique = true)
    private String token; // JWT or hashed token

    @Column(columnDefinition = "TEXT")
    private String abilities; // Comma-separated, e.g., "read,write"

    @Column(nullable = false)
    private Instant issuedAt;

    private Instant expiresAt;

    private boolean revoked;

    @Override
    public String toString() {
        return "AccessToken{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", token='[PROTECTED]'" +
                ", abilities='" + abilities + '\'' +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                ", revoked=" + revoked +
                '}';
    }

}
