package com.jerrycode.gym_services.data.vo;

import com.jerrycode.gym_services.utils.Language;
import com.jerrycode.gym_services.utils.TemplateType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "templates")
@Data
public class SmsTemplate  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TemplateType name;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private Language language;

    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    @Override
    public String toString() {
        return "SmsTemplate{" +
                "id=" + id +
                ", name=" + name +
                ", language=" + language +
                ", description='" + description + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
