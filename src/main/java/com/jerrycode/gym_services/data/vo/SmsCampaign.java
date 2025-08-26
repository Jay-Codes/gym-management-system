package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.CampaignStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_campaigns")
@Data
public class SmsCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private SmsTemplate template;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member memberId;

    private LocalDateTime scheduledAt;

    private LocalDateTime executedAt;

    private boolean executed;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CampaignStatus status; // SCHEDULED, RUNNING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Override
    public String toString() {
        return "SmsCampaign{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", templateId=" + (template != null ? template.getId() : "null") +
                ", memberId=" + (memberId != null ? memberId.getId() : "null") +
                ", scheduledAt=" + scheduledAt +
                ", executedAt=" + executedAt +
                ", executed=" + executed +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
