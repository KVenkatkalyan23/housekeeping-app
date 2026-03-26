package com.ibe.housekeeping.staff.config;

import com.ibe.housekeeping.auth.dto.CreateUserRequest;
import com.ibe.housekeeping.auth.repository.UserRepository;
import com.ibe.housekeeping.auth.service.AuthService;
import com.ibe.housekeeping.common.enums.AvailabilityStatus;
import com.ibe.housekeeping.common.enums.Role;
import com.ibe.housekeeping.entity.Shift;
import com.ibe.housekeeping.entity.StaffProfile;
import com.ibe.housekeeping.entity.User;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import com.ibe.housekeeping.staff.repository.StaffProfileRepository;
import java.time.LocalTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableConfigurationProperties(StaffBootstrapProperties.class)
public class StaffBootstrapConfig {

    @Bean
    public CommandLineRunner bootstrapStaffProfiles(
            StaffBootstrapProperties properties,
            AuthService authService,
            UserRepository userRepository,
            ShiftRepository shiftRepository,
            StaffProfileRepository staffProfileRepository
    ) {
        return args -> {
            if (!properties.enabled()) {
                return;
            }

            seedShifts(shiftRepository);
            seedStaffUsersAndProfiles(properties, authService, userRepository, shiftRepository, staffProfileRepository);
        };
    }

    @Transactional
    protected void seedStaffUsersAndProfiles(
            StaffBootstrapProperties properties,
            AuthService authService,
            UserRepository userRepository,
            ShiftRepository shiftRepository,
            StaffProfileRepository staffProfileRepository
    ) {
        List<String> shiftCodes = List.of("MORN", "AFT");

        for (int index = 0; index < properties.count(); index++) {
            String username = index == 0 ? "staff" : "staff%02d".formatted(index);

            if (!userRepository.existsByUsername(username)) {
                authService.createUser(new CreateUserRequest(username, properties.defaultPassword(), Role.STAFF));
            }

            User user = userRepository.findByUsername(username).orElseThrow();
            Shift shift = shiftRepository.findByShiftCode(shiftCodes.get(index % shiftCodes.size())).orElseThrow();

            StaffProfile staffProfile = staffProfileRepository.findByUserId(user.getId())
                    .orElseGet(() -> StaffProfile.builder().user(user).build());

            staffProfile.setFullName("Staff Member %02d".formatted(index + 1));
            staffProfile.setPhone("+15550000%02d".formatted(index + 1));
            staffProfile.setEmail("staff%02d@housekeeping.local".formatted(index + 1));
            staffProfile.setCurrentShift(shift);
            staffProfile.setAvailabilityStatus(AvailabilityStatus.OFF_DUTY);

            staffProfileRepository.save(staffProfile);
        }
    }

    @Transactional
    protected void seedShifts(ShiftRepository shiftRepository) {
        upsertShift(shiftRepository, "MORN", "Morning Shift", LocalTime.of(8, 0), LocalTime.of(12, 0), 240);
        upsertShift(shiftRepository, "AFT", "Afternoon Shift", LocalTime.of(13, 0), LocalTime.of(17, 0), 240);
    }

    private void upsertShift(
            ShiftRepository shiftRepository,
            String shiftCode,
            String shiftName,
            LocalTime startTime,
            LocalTime endTime,
            int durationMinutes
    ) {
        Shift shift = shiftRepository.findByShiftCode(shiftCode)
                .orElseGet(Shift::new);

        shift.setShiftCode(shiftCode);
        shift.setShiftName(shiftName);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setDurationMinutes(durationMinutes);

        shiftRepository.save(shift);
    }
}
