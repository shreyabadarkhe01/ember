package com.ember.backend.habitlog;

import com.ember.backend.habit.HabitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {

    // All logs for a user in a date range — used by AutopsyService
    List<HabitLog> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    // All logs for a specific habit — used for per-habit stats
    List<HabitLog> findByHabitId(Long habitId);

    // Check if a habit was already logged today — prevent duplicates
    Optional<HabitLog> findByHabitIdAndDate(Long habitId, LocalDate date);

    // Count completions for a user in range — quick query for autopsy
    @Query("SELECT COUNT(l) FROM HabitLog l WHERE l.userId = :userId AND l.status = 'DONE' AND l.date BETWEEN :from AND :to")
    int countCompletionsByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    boolean existsByHabitIdAndDateAndStatus(Long habitId, LocalDate date, HabitStatus status);

    void deleteByHabitIdAndDate(Long habitId, LocalDate date);
}