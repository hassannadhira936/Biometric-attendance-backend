package com.developers_group.biometricattendance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardStatisticsDTO {

    private long totalEmployees;
    private long activeEmployees;
    private long presentToday;
    private long lateToday;

    public DashboardStatisticsDTO(
            long totalEmployees,
            long activeEmployees,
            long presentToday,
            long lateToday) {

        this.totalEmployees = totalEmployees;
        this.activeEmployees = activeEmployees;
        this.presentToday = presentToday;
        this.lateToday = lateToday;
    }
}