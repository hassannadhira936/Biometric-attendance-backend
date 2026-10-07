package com.developers_group.biometricattendance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "biometric")
@Getter
@Setter
public class Biometric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long biometricId;

    // Employee ambaye credential hii inamilikiwa
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_biometric_employee")
    )
    private Employee employee;

    /*
     * WebAuthn Credential ID.
     *
     * Hii si fingerprint image.
     * Ni ID ya credential iliyotengenezwa na Windows Hello,
     * fingerprint device, Face unlock, au platform authenticator.
     */
    @Column(
            name = "fingerprint_id",
            nullable = false,
            length = 2048
    )
    private String fingerprintId;

    /*
     * Public key ya WebAuthn credential.
     *
     * Backend itaitumia kuthibitisha signature
     * wakati employee ana-login/check-in/check-out.
     */
    @Lob
    @Column(name = "public_key", nullable = false)
    private String publicKey;

    /*
     * WebAuthn signature counter.
     *
     * Hutumika kusaidia server kugundua matumizi yasiyo sahihi
     * ya credential.
     */
    @Column(name = "sign_count", nullable = false)
    private Long signCount = 0L;

    // Mfano: "Right Thumb", "Windows Hello", "Device biometric"
    private String fingerName;

    // Active | Inactive
    private String status;

    /*
     * Employee ID inayorudishwa kwenye JSON response.
     */
    @JsonProperty("employeeId")
    public String getEmployeeId() {
        return employee == null ? null : employee.getEmployeeId();
    }
}