package com.developers_group.biometricattendance.controller;

import com.developers_group.biometricattendance.dto.DashboardStatisticsDTO;
import com.developers_group.biometricattendance.service.DashboardStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardStatisticsController {

    private final DashboardStatisticsService dashboardStatisticsService;

    public DashboardStatisticsController(
            DashboardStatisticsService dashboardStatisticsService) {

        this.dashboardStatisticsService = dashboardStatisticsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsDTO> getDashboardStatistics() {

        return ResponseEntity.ok(
                dashboardStatisticsService.getDashboardStatistics()
        );
    }
}