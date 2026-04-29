package com.ember.backend.biometric;

import com.ember.backend.checkin.CheckIn;
import com.ember.backend.checkin.CheckInRepository;
import com.ember.backend.energy.EnergyBreakdown;
import com.ember.backend.energy.EnergyCalculator;
import com.ember.backend.common.AppException;
import com.ember.backend.user.User;
import com.ember.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BiometricCheckinService {

    private final EnergyCalculator energyCalculator;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    /**
     * Process biometric data → calculate energy score → create check-in.
     *
     * Flow:
     * 1. Validate user exists
     * 2. Check not already checked in today
     * 3. Calculate energy score from biometrics
     * 4. If score null (no data), throw error asking for manual entry
     * 5. Save check-in with calculated score
     * 6. Return breakdown so user sees WHY they got this score
     */
    public BiometricCheckinResponseDto processCheckin(Long userId, BiometricDataDto dto) {

        // 1. Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // 2. Check already checked in today
        LocalDate today = LocalDate.now();
        boolean alreadyCheckedIn = checkInRepository
                .existsByUserIdAndDate(userId, today);
        if (alreadyCheckedIn) {
            throw new AppException("Already checked in today", HttpStatus.CONFLICT);
        }

        // 3. Calculate energy score
        Integer energyScore = energyCalculator.calculate(dto);
        EnergyBreakdown breakdown = energyCalculator.breakdown(dto);

        // 4. If no biometric data, require manual score
        if (energyScore == null) {
            throw new AppException(
                "No biometric data provided. Please enter energy score manually.",
                HttpStatus.BAD_REQUEST
            );
        }

        // 5. Save check-in
        CheckIn checkIn = new CheckIn();
        checkIn.setUser(user);
        checkIn.setEnergyScore(energyScore);
        checkIn.setNotes(dto.getNote());
        checkIn.setSource(dto.getSource());
        checkIn.setDate(today);
        checkIn.setCreatedAt(LocalDateTime.now());

        CheckIn saved = checkInRepository.save(checkIn);

        // 6. Return response with breakdown
        return BiometricCheckinResponseDto.builder()
                .checkInId(saved.getId())
                .userId(userId)
                .energyScore(energyScore)
                .breakdown(breakdown)
                .date(today)
                .source(dto.getSource())
                .build();
    }

    /**
     * Preview energy score without saving check-in.
     * Validates user exists but doesn't check for existing check-in.
     */
    public BiometricCheckinResponseDto previewOnly(Long userId, BiometricDataDto dto) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        // Calculate energy score
        Integer energyScore = energyCalculator.calculate(dto);
        EnergyBreakdown breakdown = energyCalculator.breakdown(dto);

        // If no biometric data, require manual score
        if (energyScore == null) {
            throw new AppException(
                "No biometric data provided. Please enter energy score manually.",
                HttpStatus.BAD_REQUEST
            );
        }

        // Return preview response without saving
        return BiometricCheckinResponseDto.builder()
                .checkInId(null)
                .userId(userId)
                .energyScore(energyScore)
                .breakdown(breakdown)
                .date(LocalDate.now())
                .source(dto.getSource())
                .build();
    }
}
