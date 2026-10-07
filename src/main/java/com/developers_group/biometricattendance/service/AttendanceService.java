package com.developers_group.biometricattendance.service;

import com.developers_group.biometricattendance.dto.AdminAttendanceDTO;
import com.developers_group.biometricattendance.dto.AttendanceSummaryDTO;
import com.developers_group.biometricattendance.dto.DailyAttendanceSummaryDTO;
import com.developers_group.biometricattendance.dto.WeeklyAttendanceDTO;
import com.developers_group.biometricattendance.entity.Attendance;
import com.developers_group.biometricattendance.entity.Employee;
import com.developers_group.biometricattendance.entity.SystemSetting;
import com.developers_group.biometricattendance.exception.ApiException;
import com.developers_group.biometricattendance.repository.AttendanceRepository;
import com.developers_group.biometricattendance.repository.EmployeeRepository;
import com.developers_group.biometricattendance.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceService {

    private static final LocalTime DEFAULT_WORK_START = LocalTime.of(8, 0);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final SystemSettingRepository systemSettingRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            SystemSettingRepository systemSettingRepository) {

        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.systemSettingRepository = systemSettingRepository;
    }

    // =====================================================
    // CHECK-IN
    // =====================================================

    @Transactional
    public Attendance checkIn(
            String employeeId,
            Double latitude,
            Double longitude,
            String verificationMethod) {

        Employee employee = findActiveEmployee(employeeId);

        LocalDate today = LocalDate.now();

        if (attendanceRepository
                .findByEmployeeEmployeeIdAndAttendanceDate(employee.getEmployeeId(), today)
                .isPresent()) {

            throw ApiException.conflict("You have already signed in today.");
        }

        SystemSetting setting = getConfiguredSetting();

        // GPS geofence - the server is the final authority
        double distance = enforceGeofence("Check-in", latitude, longitude, setting);

        LocalDateTime now = LocalDateTime.now();

        LocalTime workStart = setting.getWorkStartTime() != null
                ? setting.getWorkStartTime()
                : DEFAULT_WORK_START;

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(now);
        attendance.setCheckInLatitude(latitude);
        attendance.setCheckInLongitude(longitude);
        attendance.setVerificationMethod(cleanMethod(verificationMethod));
        attendance.setStatus(now.toLocalTime().isAfter(workStart) ? "Late" : "Present");

        return attendanceRepository.save(attendance);
    }

    // =====================================================
    // CHECK-OUT
    // =====================================================

    @Transactional
    public Attendance checkOut(
            String employeeId,
            Double latitude,
            Double longitude,
            String verificationMethod) {

        Employee employee = findActiveEmployee(employeeId);

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeEmployeeIdAndAttendanceDate(employee.getEmployeeId(), today)
                .orElseThrow(() -> ApiException.badRequest(
                        "No check-in record found for today. Please sign in first."));

        if (attendance.getCheckOutTime() != null) {
            throw ApiException.conflict("You have already signed out today.");
        }

        SystemSetting setting = getConfiguredSetting();

        double distance = enforceGeofence("Check-out", latitude, longitude, setting);

        LocalDateTime now = LocalDateTime.now();

        attendance.setCheckOutTime(now);
        attendance.setCheckOutLatitude(latitude);
        attendance.setCheckOutLongitude(longitude);

        // Working hours = time between check-in and check-out, in hours (2 decimals)
        double minutes = Duration.between(attendance.getCheckInTime(), now).toMinutes();
        attendance.setWorkingHours(Math.round((minutes / 60.0) * 100.0) / 100.0);

        return attendanceRepository.save(attendance);
    }

    // =====================================================
    // QUERIES
    // =====================================================

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAllByOrderByAttendanceDateDescCheckInTimeDesc();
    }

    public List<AdminAttendanceDTO> getAllAttendanceForAdmin() {
        return attendanceRepository
                .findAllByOrderByAttendanceDateDescCheckInTimeDesc()
                .stream()
                .map(AdminAttendanceDTO::from)
                .toList();
    }

    public void deleteAttendance(Long id) {

        if (!attendanceRepository.existsById(id)) {
            throw ApiException.notFound("Attendance not found");
        }

        attendanceRepository.deleteById(id);
    }

    public List<Attendance> getEmployeeAttendance(String employeeId) {

        String id = requireEmployee(employeeId);

        return attendanceRepository.findByEmployeeEmployeeIdOrderByAttendanceDateDesc(id);
    }

    public AttendanceSummaryDTO getEmployeeSummary(String employeeId) {

        String id = requireEmployee(employeeId);

        long presentDays = attendanceRepository.countByEmployeeEmployeeIdAndStatus(id, "Present");
        long lateDays = attendanceRepository.countByEmployeeEmployeeIdAndStatus(id, "Late");
        double totalHours = attendanceRepository.sumWorkingHoursByEmployeeId(id);

        return new AttendanceSummaryDTO(id, presentDays, lateDays, totalHours);
    }

    public DailyAttendanceSummaryDTO getDailySummary(LocalDate date) {

        return new DailyAttendanceSummaryDTO(
                date,
                attendanceRepository.countByAttendanceDate(date),
                attendanceRepository.countByAttendanceDateAndStatus(date, "Present"),
                attendanceRepository.countByAttendanceDateAndStatus(date, "Late")
        );
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date);
    }

    // Monday-Friday report. Days in the future are "Pending" (not "Absent").
    public List<WeeklyAttendanceDTO> getWeeklyAttendance(
            String employeeId,
            LocalDate startDate,
            LocalDate endDate) {

        String id = requireEmployee(employeeId);

        List<Attendance> records =
                attendanceRepository.findByEmployeeEmployeeIdAndAttendanceDateBetween(
                        id, startDate, endDate);

        LocalDate today = LocalDate.now();

        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek().getValue() <= 5)
                .map(date -> {

                    Attendance attendance = records.stream()
                            .filter(a -> date.equals(a.getAttendanceDate()))
                            .findFirst()
                            .orElse(null);

                    if (attendance == null) {
                        return new WeeklyAttendanceDTO(
                                date,
                                date.getDayOfWeek().toString(),
                                date.isAfter(today) ? "Pending" : "Absent",
                                0.0
                        );
                    }

                    return new WeeklyAttendanceDTO(
                            date,
                            date.getDayOfWeek().toString(),
                            attendance.getStatus(),
                            attendance.getWorkingHours() == null ? 0.0 : attendance.getWorkingHours()
                    );
                })
                .toList();
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String requireEmployee(String employeeId) {

        String id = EmployeeService.normalizeId(employeeId);

        if (!employeeRepository.existsById(id)) {
            throw ApiException.notFound("Employee not found: " + id);
        }

        return id;
    }

    private Employee findActiveEmployee(String employeeId) {

        Employee employee = employeeRepository.findById(EmployeeService.normalizeId(employeeId))
                .orElseThrow(() -> ApiException.notFound("Employee not found"));

        if ("Inactive".equalsIgnoreCase(employee.getStatus())) {
            throw ApiException.forbidden(
                    "This employee account is inactive. Please contact the administrator.");
        }

        return employee;
    }

    private SystemSetting getConfiguredSetting() {

        SystemSetting setting = systemSettingRepository.findFirstByOrderBySettingIdAsc()
                .orElseThrow(() -> ApiException.badRequest(
                        "System settings are not configured. Ask the administrator to open Settings."));

        if (setting.getSchoolLatitude() == null
                || setting.getSchoolLongitude() == null
                || setting.getAllowedRadius() == null) {

            throw ApiException.badRequest(
                    "School GPS location is not configured. Ask the administrator to set it in Settings.");
        }

        return setting;
    }

    // Returns the distance in metres, or throws when outside the allowed radius
    private double enforceGeofence(
            String action,
            Double latitude,
            Double longitude,
            SystemSetting setting) {

        if (latitude == null || longitude == null
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {

            throw ApiException.badRequest(action + " rejected: a valid GPS location is required.");
        }

        double distance = calculateDistance(
                latitude,
                longitude,
                setting.getSchoolLatitude(),
                setting.getSchoolLongitude()
        );

        if (distance > setting.getAllowedRadius()) {
            throw ApiException.forbidden(
                    action + " rejected: you are outside the allowed school area. "
                            + "Distance: " + Math.round(distance) + " m, allowed: "
                            + Math.round(setting.getAllowedRadius()) + " m.");
        }

        return distance;
    }

    // Haversine formula (metres)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {

        final double earthRadius = 6371000;

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);

        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static String cleanMethod(String method) {
        return "PASSWORD".equalsIgnoreCase(method) ? "PASSWORD" : "BIOMETRIC";
    }
}
