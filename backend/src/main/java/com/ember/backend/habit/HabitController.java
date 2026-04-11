package com.ember.backend.habit;

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
        return ResponseEntity.ok(habitService.completeHabit(habitId));
    }
}