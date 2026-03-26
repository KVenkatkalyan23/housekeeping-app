package com.ibe.housekeeping.shift.controller;

import com.ibe.housekeeping.shift.dto.ShiftListItemResponse;
import com.ibe.housekeeping.shift.repository.ShiftRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shifts")
@PreAuthorize("hasRole('ADMIN')")
public class ShiftController {

    private final ShiftRepository shiftRepository;

    public ShiftController(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @GetMapping
    public List<ShiftListItemResponse> getShifts() {
        return shiftRepository.findAllByOrderByStartTimeAscShiftCodeAsc()
                .stream()
                .map(shift -> new ShiftListItemResponse(
                        shift.getId(),
                        shift.getShiftCode(),
                        shift.getShiftName(),
                        shift.getStartTime(),
                        shift.getEndTime(),
                        shift.getDurationMinutes()
                ))
                .toList();
    }
}
