package com.developers_group.biometricattendance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebAuthnAuthenticationRequest {

    private String credentialId;
}