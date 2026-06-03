package com.ember.backend.habitlog;

import com.ember.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HabitLogService {

    private final HabitLogRepository habitLogRepository;

    @Transactional
    public HabitLog updateCompletionDetails(Long userId, Long habitId, Double completionRatio, String feelingTag) {
        System.out.println("=== log-completion called: userId=" + userId + " habitId=" + habitId + " date=" + LocalDate.now());

        Optional<HabitLog> found = habitLogRepository.findByHabitIdAndDate(habitId, LocalDate.now());
        System.out.println("=== log found: " + found.isPresent());

        HabitLog log = found.orElseThrow(() -> new AppException("No log found for today", HttpStatus.NOT_FOUND));

        if (!log.getUserId().equals(userId)) {
            throw new AppException("Unauthorized", HttpStatus.FORBIDDEN);
        }
        log.setCompletionRatio(completionRatio);
        log.setFeelingTag(feelingTag);
        return habitLogRepository.save(log);
    }
}