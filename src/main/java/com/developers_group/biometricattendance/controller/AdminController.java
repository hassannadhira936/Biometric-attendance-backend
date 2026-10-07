package com.developers_group.biometricattendance.controller;

import org.springframework.web.multipart.MultipartFile;
import com.developers_group.biometricattendance.dto.ChangePasswordDTO;
import com.developers_group.biometricattendance.dto.LoginRequestDTO;
import com.developers_group.biometricattendance.entity.Admin;
import com.developers_group.biometricattendance.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        return new ResponseEntity<>(adminService.createAdmin(admin), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {

        return adminService.getAdminById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.updateAdmin(id, admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {

        adminService.deleteAdmin(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Admin> login(@RequestBody LoginRequestDTO request) {

        return ResponseEntity.ok(
                adminService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordDTO request) {

        adminService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/{id}/profile-photo")
    public ResponseEntity<Admin> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(
                adminService.uploadProfilePhoto(id, file)
        );
    }
}
