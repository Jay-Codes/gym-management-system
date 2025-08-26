package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.Language;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logo")
    private String logo;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_email", nullable = false, unique = true)
    private String companyEmail;

    @Column(name = "tin", unique = true)
    private String tin;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "website")
    private String website;

    @Column(name = "founder")
    private String founder;

    @Column(name = "manager")
    private String manager;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "company_subscription_start_date")
    private LocalDateTime companySubscriptionStartDate;

    @Column(name = "company_subscription_end_date")
    private LocalDateTime companySubscriptionEndDate;

    @Enumerated(EnumType.STRING)
    private Language preferredLanguage = Language.SW;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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
        return "CompanyProfile{" +
                "id=" + id +
                ", logo='" + logo + '\'' +
                ", companyName='" + companyName + '\'' +
                ", companyEmail='" + companyEmail + '\'' +
                ", tin='" + tin + '\'' +
                ", description='" + (description != null ? (description.length() > 50 ? description.substring(0, 47) + "..." : description) : null) + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", founder='" + founder + '\'' +
                ", manager='" + manager + '\'' +
                ", accountName='" + accountName + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", companySubscriptionStartDate=" + companySubscriptionStartDate +
                ", companySubscriptionEndDate=" + companySubscriptionEndDate +
                ", preferredLanguage=" + preferredLanguage +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}