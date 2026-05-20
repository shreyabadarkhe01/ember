package com.ember.backend.habit;

import com.ember.backend.habitlog.CompletionDetailsRequest;
import com.ember.backend.habitlog.HabitLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;
    private final HabitLogService habitLogService;

    @PostMapping("/{userId}/habits")
    public ResponseEntity<HabitDto> createHabit(
            @PathVariable Long userId,
            @Valid @RequestBody Habit habit) {
        return ResponseEntity.ok(habitService.createHabit(userId, habit));
    }

    @GetMapping("/{userId}/habits")
    public ResponseEntity<List<HabitDto>> getUserHabits(
            @PathVariable Long userId) {
        return ResponseEntity.ok(habitService.getUserHabits(userId));
    }

    @PatchMapping("/{userId}/habits/{habitId}/scale")
    public ResponseEntity<HabitDto> scaleHabit(
            @PathVariable Long userId,
            @PathVariable Long habitId,
            @RequestParam int energyScore) {
        return ResponseEntity.ok(habitService.scaleHabit(habitId, energyScore));
    }

    @PatchMapping("/{userId}/habits/{habitId}/complete")
    public ResponseEntity<HabitDto> completeHabit(
            @PathVariable Long userId,
            @PathVariable Long habitId) {
        return ResponseEntity.ok(habitService.completeHabit(userId, habitId));
    }

    @PatchMapping("/{userId}/habits/{habitId}/skip")
    public ResponseEntity<HabitDto> skipHabit(
            @PathVariable Long userId,
            @PathVariable Long habitId) {
        return ResponseEntity.ok(habitService.skipHabit(userId, habitId));
    }

    @PatchMapping("/{userId}/habits/{habitId}")
    public ResponseEntity<HabitDto> updateHabit(
            @PathVariable Long userId,
            @PathVariable Long habitId,
            @RequestBody HabitDto dto) {
        return ResponseEntity.ok(habitService.updateHabit(habitId, dto));
    }

    @PatchMapping("/{userId}/habits/{habitId}/reset")
    public ResponseEntity<HabitDto> resetHabit(
            @PathVariable Long userId,
            @PathVariable Long habitId) {
        return ResponseEntity.ok(habitService.resetHabit(userId,habitId));
    }

    @PatchMapping("/{userId}/habits/{habitId}/archive")
    public ResponseEntity<HabitDto> archiveHabit(
            @PathVariable Long userId,
            @PathVariable Long habitId) {
        return ResponseEntity.ok(habitService.archiveHabit(userId, habitId));
    }

    @PatchMapping("/{userId}/habits/{habitId}/unarchive")
    public ResponseEntity<HabitDto> unarchiveHabit(
            @PathVariable Long userId,
            @PathVariable Long habitId) {
        return ResponseEntity.ok(habitService.unarchiveHabit(userId, habitId));
    }
    
    @PatchMapping("/{userId}/habits/{habitId}/log-completion")
    public ResponseEntity<Void> logCompletionDetails(
            @PathVariable Long userId,
            @PathVariable Long habitId,
            @RequestBody CompletionDetailsRequest request) {
        habitLogService.updateCompletionDetails(
                userId,
                habitId,
                request.getCompletionRatio(),
                request.getFeelingTag()
        );
        return ResponseEntity.ok().build();
    }

}