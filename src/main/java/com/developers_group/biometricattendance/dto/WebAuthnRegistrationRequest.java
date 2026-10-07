package com.developers_group.biometricattendance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebAuthnRegistrationRequest {

    private String employeeId;

    private String credentialId;

    private String publicKey;

    private Long signCount;

    private String fingerName;
}