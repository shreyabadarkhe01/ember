package com.ember.backend.checkin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByUserId(Long userId);
    Optional<CheckIn> findByUserIdAndCheckInDate(Long userId, LocalDate date);
    List<CheckIn> findByUserIdOrderByCheckInDateDesc(Long userId);

    // Check if user already checked in on a specific date
    boolean existsByUserIdAndDate(Long userId, LocalDate date);

    // Get latest check-in for a user
    Optional<CheckIn> findTopByUserIdOrderByDateDesc(Long userId);
}