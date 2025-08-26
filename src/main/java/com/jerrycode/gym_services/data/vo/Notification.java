package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.Status;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member memberId;

    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = true)
    private SmsCampaign campaign;

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

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", memberId=" + (memberId != null ? memberId.getId() : "null") +
                ", campaignId=" + (campaign != null ? campaign.getId() : "null") +
                ", templateId=" + (template != null ? template.getId() : "null") +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", message='" + message + '\'' +
                ", sentAt=" + sentAt +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }


}