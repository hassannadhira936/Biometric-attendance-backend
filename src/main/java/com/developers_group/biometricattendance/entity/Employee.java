package com.developers_group.biometricattendance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;


@Entity
@Table(name = "employee")
@Getter
@Setter
public class Employee {

    /**
     * Employee ID is now the PRIMARY KEY and a STRING, e.g. "EMP004".
     * It is typed by the administrator - it is NOT auto-generated.
     * Attendance and Biometric rows reference it with a VARCHAR foreign key.
     */
    @Id
    @Column(name = "employee_id", length = 50, nullable = false)
    private String employeeId;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String phone;

    private String department;

    private String position;

    private LocalDate hireDate;

    @Column(unique = true, nullable = false)
    private String username;

    // PLAIN TEXT on purpose (demo/academic project - no hashing).
    // WRITE_ONLY: accepted from the request, never returned in JSON.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    private String status;

    @ToString.Exclude
    @Column(columnDefinition = "LONGTEXT")
    private String profilePhoto;

}
