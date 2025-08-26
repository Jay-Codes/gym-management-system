package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.CampaignStatus;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_sms_campaigns")
@Data
public class CompanySmsCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private SmsTemplate template;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyProfile company;

    private LocalDateTime scheduledAt;
    private LocalDateTime executedAt;
    private boolean executed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
