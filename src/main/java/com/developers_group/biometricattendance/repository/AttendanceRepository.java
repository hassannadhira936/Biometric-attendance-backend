package com.developers_group.biometricattendance.repository;

import com.developers_group.biometricattendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // One record per employee per day
    Optional<Attendance> findByEmployeeEmployeeIdAndAttendanceDate(
            String employeeId,
            LocalDate attendanceDate
    );

    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);

    List<Attendance> findByEmployeeEmployeeIdOrderByAttendanceDateDesc(String employeeId);

    List<Attendance> findAllByOrderByAttendanceDateDescCheckInTimeDesc();

    List<Attendance> findByEmployeeEmployeeIdAndAttendanceDateBetween(
            String employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    long countByEmployeeEmployeeIdAndStatus(String employeeId, String status);

    long countByAttendanceDate(LocalDate attendanceDate);

    long countByAttendanceDateAndStatus(LocalDate attendanceDate, String status);

    @Query("""
            SELECT COALESCE(SUM(a.workingHours), 0)
            FROM Attendance a
            WHERE a.employee.employeeId = :employeeId
            """)
    double sumWorkingHoursByEmployeeId(@Param("employeeId") String employeeId);

    // Used when an employee is deleted
    void deleteByEmployeeEmployeeId(String employeeId);
}
