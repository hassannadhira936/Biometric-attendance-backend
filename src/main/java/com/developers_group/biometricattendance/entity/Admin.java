package com.developers_group.biometricattendance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "admin")
@Getter
@Setter
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String fullName;

    private String position;

    private String contact;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    // PLAIN TEXT on purpose (demo/academic project - no hashing).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @ToString.Exclude
    @Column(length = 500)
    private String profilePhoto;
}