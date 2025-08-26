package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.SMSProvider;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "sms_api_providers")
public class SmsApiProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SMSProvider providerName;

    private String apiUrl;

    private String apiBalanceUrl;

    private String senderId;

    private String apiKey;

    private String apiSecret;

    private double totalSmsCredits;

    private double usedSmsCredits;

    @Column(columnDefinition = "boolean default true")
    private boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "SmsApiProvider{" +
                "id=" + id +
                ", providerName=" + providerName +
                ", apiUrl='" + apiUrl + '\'' +
                ", apiBalanceUrl='" + apiBalanceUrl + '\'' +
                ", senderId='" + senderId + '\'' +
                ", totalSmsCredits=" + totalSmsCredits +
                ", usedSmsCredits=" + usedSmsCredits +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
