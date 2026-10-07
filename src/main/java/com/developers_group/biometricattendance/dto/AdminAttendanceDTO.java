package com.developers_group.biometricattendance.dto;

import com.developers_group.biometricattendance.entity.Attendance;
import com.developers_group.biometricattendance.entity.Employee;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** One row of the administrator's "View Employees Attendance" table. */
@Getter
@Setter
@NoArgsConstructor
public class AdminAttendanceDTO {

    private Long attendanceId;

    private String employeeId;

    private String firstName;

    private String lastName;

    private String employeeName;

    private String department;

    private String position;

    private LocalDate attendanceDate;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Double checkInLatitude;

    private Double checkInLongitude;

    private Double checkOutLatitude;

    private Double checkOutLongitude;

    private String status;

    private Double workingHours;

    private String verificationMethod;

    public static AdminAttendanceDTO from(Attendance a) {

        AdminAttendanceDTO dto = new AdminAttendanceDTO();
        Employee e = a.getEmployee();

        dto.attendanceId = a.getAttendanceId();
        dto.employeeId = e.getEmployeeId();
        dto.firstName = e.getFirstName();
        dto.lastName = e.getLastName();
        dto.employeeName = ((e.getFirstName() == null ? "" : e.getFirstName()) + " "
                + (e.getLastName() == null ? "" : e.getLastName())).trim();
        dto.department = e.getDepartment();
        dto.position = e.getPosition();
        dto.attendanceDate = a.getAttendanceDate();
        dto.checkInTime = a.getCheckInTime();
        dto.checkOutTime = a.getCheckOutTime();
        dto.checkInLatitude = a.getCheckInLatitude();
        dto.checkInLongitude = a.getCheckInLongitude();
        dto.checkOutLatitude = a.getCheckOutLatitude();
        dto.checkOutLongitude = a.getCheckOutLongitude();
        dto.status = a.getStatus();
        dto.workingHours = a.getWorkingHours();
        dto.verificationMethod = a.getVerificationMethod();

        return dto;
    }
}
