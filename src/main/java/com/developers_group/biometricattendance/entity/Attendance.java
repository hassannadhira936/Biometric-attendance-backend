package com.developers_group.biometricattendance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_employee_date",
                columnNames = {"employee_id", "attendance_date"}
        )
)
@Getter
@Setter
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    // FK -> employee.employee_id (VARCHAR).
    // Not serialised: the whole Employee (with its photo) would be repeated in
    // every attendance row. Only "employeeId" is exposed, see getEmployeeId().
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_attendance_employee")
    )
    private Employee employee;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Double checkInLatitude;

    private Double checkInLongitude;

    private Double checkOutLatitude;

    private Double checkOutLongitude;

    // Present | Late
    private String status;

    // Hours between check-in and check-out (calculated by the server)
    private Double workingHours;

    // BIOMETRIC (WebAuthn fingerprint/face/PIN) or PASSWORD (fallback)
    private String verificationMethod;

    @JsonProperty("employeeId")
    public String getEmployeeId() {
        return employee == null ? null : employee.getEmployeeId();
    }
}
