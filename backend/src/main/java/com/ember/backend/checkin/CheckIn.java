package com.ember.backend.checkin;

import com.ember.backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @Min(0) @Max(24)
    @Column(name = "sleep_hours")
    private Double sleepHours;

    private String notes;

    private LocalDate checkInDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (checkInDate == null) {
            checkInDate = LocalDate.now();
        }
    }

    // Tracks where the energy score came from
    @Column(name = "source")
    private String source;  // "samsung_health", "manual", "health_connect"

    // Date of the check-in (separate from createdAt timestamp)
    @Column(name = "date")
    private LocalDate date;

//    @Column(name = "sleep_hours")
//    private Double sleepHours;  // e.g. 7.5

    @Column(name = "hrv_ms")
    private Double hrvMs;       // e.g. 55.0

    @Column(name = "resting_heart_rate")
    private Integer restingHeartRate;  // e.g. 62

    @Column(name = "steps")
    private Integer steps;      // e.g. 9000

    @Column(name = "calories_burned")
    private Integer caloriesBurned;    // e.g. 2200

}