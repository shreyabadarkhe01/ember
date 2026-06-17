package com.ember.backend.checkin;

import com.ember.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByUserId(Long userId);
    Optional<CheckIn> findByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);
    List<CheckIn> findByUserIdOrderByCheckInDateDesc(Long userId);
    boolean existsByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);
    Optional<CheckIn> findTopByUserIdOrderByCheckInDateDesc(Long userId);
    List<CheckIn> findByUserIdAndCheckInDateBetweenOrderByCheckInDateAsc(
            Long userId, LocalDate start, LocalDate end);

    void deleteByUser(User user);
}