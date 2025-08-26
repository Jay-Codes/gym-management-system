package com.jerrycode.gym_services.data.vo;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Packages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "priceUSD")
    private double priceUSD;
    @Column(name = "priceTZS")
    private double priceTZS;
    @Column(name = "duration")
    private int duration;
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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Packages{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", priceUSD=" + priceUSD +
                ", priceTZS=" + priceTZS +
                ", duration=" + duration +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }


}
