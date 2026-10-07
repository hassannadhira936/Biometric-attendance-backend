package com.developers_group.biometricattendance.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class WeeklyAttendanceDTO {

    private LocalDate date;

    private String day;

    private String status;

    private Double workingHours;

    public WeeklyAttendanceDTO(
            LocalDate date,
            String day,
            String status,
            Double workingHours) {

        this.date = date;
        this.day = day;
        this.status = status;
        this.workingHours = workingHours;
    }
}