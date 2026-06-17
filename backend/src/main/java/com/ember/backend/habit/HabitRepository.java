package com.ember.backend.habit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserId(Long userId);
    List<Habit> findByUserIdAndStatus(Long userId, HabitStatus status);
    Optional<Habit> findByIdAndUserId(Long habitId, Long userId);
    void deleteByUserId(Long userId);
}