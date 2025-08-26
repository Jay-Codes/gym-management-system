package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.SMSProvider;
import com.jerrycode.gym_services.utils.Status;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "sms_packages")
public class SmsPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String SmsPackageName;

    private double smsCount;

    private double price;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private SMSProvider smsPackageProviderName;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "SmsPackage{" +
                "id=" + id +
                ", smsPackageName='" + SmsPackageName + '\'' +
                ", smsCount=" + smsCount +
                ", price=" + price +
                ", status=" + status +
                ", smsPackageProviderName=" + smsPackageProviderName +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}
