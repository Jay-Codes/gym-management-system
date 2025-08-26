package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.Status;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_notifications")
@Data
public class CompanyNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyProfile company;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private CompanySmsCampaign campaign;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private SmsTemplate template;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private String errorMessage;
}
