package com.ember.backend.habit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HabitResetScheduler {

    private final HabitRepository habitRepository;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyHabits() {
        log.info("Running daily habit reset...");

        List<Habit> toReset = habitRepository.findAll()
                .stream()
                .filter(h -> h.getStatus() == HabitStatus.DONE
                        || h.getStatus() == HabitStatus.SKIPPED)
                .toList();

        toReset.forEach(h -> h.setStatus(HabitStatus.ACTIVE));
        habitRepository.saveAll(toReset);

        log.info("Reset {} habits to ACTIVE", toReset.size());
    }
}