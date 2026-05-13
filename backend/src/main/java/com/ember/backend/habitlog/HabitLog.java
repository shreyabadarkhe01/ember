package com.ember.backend.habitlog;

import com.ember.backend.habit.HabitStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "habit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HabitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long habitId;

    @Column(nullable = false)
    private String habitName; // snapshot — so autopsy works even if habit is renamed/archived

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HabitStatus status; // DONE or SKIPPED

    // Post-done dialog fields (optional — null until dialog is implemented)
    private Double completionRatio;  // 0.0 to 1.0 — how much of the version they actually did
    private String feelingTag;       // DRAINED / NEUTRAL / ENERGISED

    private Integer energyScore;     // snapshot of the day's energy score

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}