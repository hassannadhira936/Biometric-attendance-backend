package com.developers_group.biometricattendance.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyAttendanceSummaryDTO {

    private LocalDate date;

    private long totalAttendance;

    private long presentDays;

    private long lateDays;

    public DailyAttendanceSummaryDTO(
            LocalDate date,
            long totalAttendance,
            long presentDays,
            long lateDays) {

        this.date = date;
        this.totalAttendance = totalAttendance;
        this.presentDays = presentDays;
        this.lateDays = lateDays;
    }
}