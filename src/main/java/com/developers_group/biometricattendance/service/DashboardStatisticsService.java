package com.developers_group.biometricattendance.service;

import com.developers_group.biometricattendance.dto.DashboardStatisticsDTO;
import com.developers_group.biometricattendance.repository.AttendanceRepository;
import com.developers_group.biometricattendance.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardStatisticsService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardStatisticsService(
            EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository) {

        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public DashboardStatisticsDTO getDashboardStatistics() {

        long totalEmployees =
                employeeRepository.count();

        long activeEmployees =
                employeeRepository.countByStatus("Active");

        LocalDate today = LocalDate.now();

        long presentToday =
                attendanceRepository.countByAttendanceDateAndStatus(
                        today,
                        "Present"
                );

        long lateToday =
                attendanceRepository.countByAttendanceDateAndStatus(
                        today,
                        "Late"
                );

        return new DashboardStatisticsDTO(
                totalEmployees,
                activeEmployees,
                presentToday,
                lateToday
        );
    }
}