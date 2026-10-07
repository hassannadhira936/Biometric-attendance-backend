package com.developers_group.biometricattendance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BiometricDTO {

    private Long biometricId;

    private String employeeId;

    /*
     * WebAuthn Credential ID.
     *
     * Hii inaweza kutumwa frontend kwa matumizi ya
     * WebAuthn authentication.
     */
    private String fingerprintId;

    private String fingerName;

    private String status;

    /*
     * Public key haitumwi kwa kawaida kwenye frontend.
     *
     * Backend ndiyo inaitumia wakati wa verification.
     */
    private String publicKey;

    private Long signCount;

    public BiometricDTO(
            Long biometricId,
            String employeeId,
            String fingerprintId,
            String fingerName,
            String status,
            String publicKey,
            Long signCount) {

        this.biometricId = biometricId;
        this.employeeId = employeeId;
        this.fingerprintId = fingerprintId;
        this.fingerName = fingerName;
        this.status = status;
        this.publicKey = publicKey;
        this.signCount = signCount;
    }
}