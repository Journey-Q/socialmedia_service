package org.example.socialmedia_services.entity.subscription;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscription_user_id", columnList = "user_id"),
                @Index(name = "idx_subscription_status", columnList = "status"),
                @Index(name = "idx_subscription_end_date", columnList = "end_date")
        })
@Getter
@Setter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subscription_package_id")
    private String subscriptionPackageId;

    @Column(name = "start_date", nullable = false)
    @CreationTimestamp
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, CANCELLED

    @Column(name = "subscription_type")
    private String subscriptionType = "PREMIUM"; // PREMIUM, BASIC, etc.

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public Subscription() {
        this.createdAt = LocalDateTime.now();
        this.startDate = LocalDateTime.now();
        this.status = "ACTIVE";
        this.subscriptionType = "PREMIUM";
    }

    // Constructor with parameters
    public Subscription(Long userId, LocalDateTime endDate) {
        this();
        this.userId = userId;
        this.endDate = endDate;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "subscriptionId=" + subscriptionId +
                ", userId=" + userId +
                ", subscriptionPackageId='" + subscriptionPackageId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", subscriptionType='" + subscriptionType + '\'' +
                '}';
    }
}
