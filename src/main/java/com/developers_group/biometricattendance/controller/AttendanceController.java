package com.developers_group.biometricattendance.controller;

import com.developers_group.biometricattendance.dto.AdminAttendanceDTO;
import com.developers_group.biometricattendance.dto.AttendanceSummaryDTO;
import com.developers_group.biometricattendance.dto.DailyAttendanceSummaryDTO;
import com.developers_group.biometricattendance.dto.WeeklyAttendanceDTO;
import com.developers_group.biometricattendance.entity.Attendance;
import com.developers_group.biometricattendance.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // employeeId is the string Employee ID (e.g. EMP004).
    // method = BIOMETRIC (default) or PASSWORD (fallback when WebAuthn is unavailable).

    @PostMapping("/check-in")
    public ResponseEntity<Attendance> checkIn(
            @RequestParam String employeeId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "BIOMETRIC") String method) {

        return ResponseEntity.ok(
                attendanceService.checkIn(employeeId, latitude, longitude, method));
    }

    @PostMapping("/check-out")
    public ResponseEntity<Attendance> checkOut(
            @RequestParam String employeeId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "BIOMETRIC") String method) {

        return ResponseEntity.ok(
                attendanceService.checkOut(employeeId, latitude, longitude, method));
    }

    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }

    // All attendance rows with employee names/locations for the administrator table
    @GetMapping("/admin")
    public ResponseEntity<List<AdminAttendanceDTO>> getAllAttendanceForAdmin() {
        return ResponseEntity.ok(attendanceService.getAllAttendanceForAdmin());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {

        attendanceService.deleteAttendance(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Attendance>> getEmployeeAttendance(@PathVariable String employeeId) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(employeeId));
    }

    @GetMapping("/employee/{employeeId}/summary")
    public ResponseEntity<AttendanceSummaryDTO> getEmployeeSummary(@PathVariable String employeeId) {
        return ResponseEntity.ok(attendanceService.getEmployeeSummary(employeeId));
    }

    @GetMapping("/employee/{employeeId}/weekly")
    public ResponseEntity<List<WeeklyAttendanceDTO>> getWeeklyAttendance(
            @PathVariable String employeeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        return ResponseEntity.ok(
                attendanceService.getWeeklyAttendance(
                        employeeId,
                        LocalDate.parse(startDate),
                        LocalDate.parse(endDate)));
    }

    @GetMapping("/summary/date/{date}")
    public ResponseEntity<DailyAttendanceSummaryDTO> getDailySummary(@PathVariable String date) {
        return ResponseEntity.ok(attendanceService.getDailySummary(LocalDate.parse(date)));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(@PathVariable String date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(LocalDate.parse(date)));
    }
}
