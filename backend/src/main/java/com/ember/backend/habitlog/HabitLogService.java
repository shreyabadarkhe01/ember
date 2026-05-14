package com.ember.backend.habitlog;

import com.ember.backend.common.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HabitLogService {

    private final HabitLogRepository habitLogRepository;

    @Transactional
    public HabitLog updateCompletionDetails(Long userId, Long habitId, Double completionRatio, String feelingTag) {
        HabitLog log = habitLogRepository
                .findByHabitIdAndDate(habitId, LocalDate.now())
                .orElseThrow(() -> new AppException("No log found for today", HttpStatus.NOT_FOUND));

        // Security check — log must belong to this user
        if (!log.getUserId().equals(userId)) {
            throw new AppException("Unauthorized", HttpStatus.FORBIDDEN);
        }

        log.setCompletionRatio(completionRatio);
        log.setFeelingTag(feelingTag);
        return habitLogRepository.save(log);
    }
}