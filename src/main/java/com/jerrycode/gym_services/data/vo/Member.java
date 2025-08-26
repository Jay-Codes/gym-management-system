package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.Language;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String gender;

    private String height;

    private String weight;

    private String memo;

    @Enumerated(EnumType.STRING)
    private Language preferredLanguage = Language.SW;

    private String packageSubscribed;

    private LocalDateTime subscribedStartDate;

    private LocalDateTime subscribedEndDate;

    private double packageTotalAmount;

    private double packagePaidAmount;

    private double packageRemainingAmount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pre-persist and pre-update hooks for timestamps
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Set default language if not already assigned
        if (this.preferredLanguage == null) {
            this.preferredLanguage = Language.SW;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", height='" + height + '\'' +
                ", weight='" + weight + '\'' +
                ", preferredLanguage=" + preferredLanguage +
                ", packageSubscribed='" + packageSubscribed + '\'' +
                ", subscribedStartDate=" + subscribedStartDate +
                ", subscribedEndDate=" + subscribedEndDate +
                ", packageTotalAmount=" + packageTotalAmount +
                ", packagePaidAmount=" + packagePaidAmount +
                ", packageRemainingAmount=" + packageRemainingAmount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }


}
