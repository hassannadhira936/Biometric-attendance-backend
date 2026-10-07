package com.developers_group.biometricattendance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    // Administrator: username.  Employee: username OR Employee ID (e.g. EMP004).
    private String username;

    private String password;
}
