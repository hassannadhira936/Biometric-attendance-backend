package com.developers_group.biometricattendance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceSummaryDTO {

    private String employeeId;

    private long presentDays;

    private long lateDays;

    private double totalWorkingHours;

    public AttendanceSummaryDTO(
            String employeeId,
            long presentDays,
            long lateDays,
            double totalWorkingHours) {

        this.employeeId = employeeId;
        this.presentDays = presentDays;
        this.lateDays = lateDays;
        this.totalWorkingHours = totalWorkingHours;
    }
}
