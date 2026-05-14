package com.ember.backend.checkin;

import com.ember.backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "check_ins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1) @Max(5)
    private Integer energyScore;

    private String notes;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

    @Column(name = "source")
    private String source;

    @Column(name = "sleep_hours")
    private Double sleepHours;

    @Column(name = "hrv_ms")
    private Double hrvMs;

    @Column(name = "resting_heart_rate")
    private Integer restingHeartRate;

    @Column(name = "steps")
    private Integer steps;

    @Column(name = "nudge_text", columnDefinition = "TEXT")
    private String nudgeText;

    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (checkInDate == null) {
            checkInDate = LocalDate.now();
        }
    }
}