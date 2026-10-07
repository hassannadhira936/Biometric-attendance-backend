package com.developers_group.biometricattendance.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import com.developers_group.biometricattendance.entity.Admin;
import com.developers_group.biometricattendance.exception.ApiException;
import com.developers_group.biometricattendance.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean sameIgnoreCase(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    // =========================
    // CREATE ADMIN
    // =========================

    @Transactional
    public Admin createAdmin(Admin admin) {

        if (isBlank(admin.getUsername())) {
            throw ApiException.badRequest("Username is required");
        }

        if (isBlank(admin.getPassword()) || admin.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw ApiException.badRequest(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        if (adminRepository.existsByUsernameIgnoreCase(admin.getUsername().trim())) {
            throw ApiException.conflict("Username already exists");
        }

        if (!isBlank(admin.getEmail())
                && adminRepository.existsByEmailIgnoreCase(admin.getEmail().trim())) {
            throw ApiException.conflict("Email already exists");
        }

        admin.setUsername(admin.getUsername().trim());

        // PLAIN TEXT password (demo/academic use)
        return adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    // =========================
    // UPDATE ADMIN PROFILE (password is NOT changed here)
    // =========================

    @Transactional
    public Admin updateAdmin(Long id, Admin updated) {

        Admin existing = adminRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Admin not found"));

        if (!isBlank(updated.getUsername())
                && !sameIgnoreCase(existing.getUsername(), updated.getUsername())
                && adminRepository.existsByUsernameIgnoreCase(updated.getUsername().trim())) {

            throw ApiException.conflict("Username already exists");
        }

        if (!isBlank(updated.getEmail())
                && !sameIgnoreCase(existing.getEmail(), updated.getEmail())
                && adminRepository.existsByEmailIgnoreCase(updated.getEmail().trim())) {

            throw ApiException.conflict("Email already exists");
        }

        if (!isBlank(updated.getFullName())) existing.setFullName(updated.getFullName().trim());
        if (updated.getPosition() != null) existing.setPosition(updated.getPosition());
        if (updated.getContact() != null) existing.setContact(updated.getContact());
        if (!isBlank(updated.getEmail())) existing.setEmail(updated.getEmail().trim());
        if (!isBlank(updated.getUsername())) existing.setUsername(updated.getUsername().trim());
        if (updated.getProfilePhoto() != null) existing.setProfilePhoto(updated.getProfilePhoto());

        return adminRepository.save(existing);
    }

    @Transactional
    public void deleteAdmin(Long id) {

        if (!adminRepository.existsById(id)) {
            throw ApiException.notFound("Admin not found");
        }

        if (adminRepository.count() <= 1) {
            throw ApiException.badRequest("The last administrator cannot be deleted");
        }

        adminRepository.deleteById(id);
    }

    // =========================
    // ADMIN LOGIN (plain-text comparison)
    // =========================

    public Admin login(String username, String password) {

        if (isBlank(username) || password == null || password.isEmpty()) {
            throw ApiException.badRequest("Please enter administrator credentials");
        }

        Admin admin = adminRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> ApiException.unauthorized(
                        "Login failed. Username or password is not recognized."));

        if (!password.equals(admin.getPassword())) {
            throw ApiException.unauthorized(
                    "Login failed. Username or password is not recognized.");
        }

        return admin;
    }

    // =========================
    // CHANGE PASSWORD
    // =========================

    @Transactional
    public void changePassword(Long adminId, String currentPassword, String newPassword) {

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> ApiException.notFound("Admin not found"));

        if (currentPassword == null || !currentPassword.equals(admin.getPassword())) {
            throw ApiException.badRequest("Current password is incorrect");
        }

        if (isBlank(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw ApiException.badRequest(
                    "New password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        admin.setPassword(newPassword);

        adminRepository.save(admin);
    }

    @Transactional
    public Admin uploadProfilePhoto(Long adminId, MultipartFile file) {

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> ApiException.notFound("Admin not found"));

        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Please select a profile photo");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw ApiException.badRequest("Profile photo must be 2MB or less");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw ApiException.badRequest("Please select a valid image file");
        }

        try {

            Path uploadDirectory = Paths.get("uploads", "admin");

            Files.createDirectories(uploadDirectory);

            String fileName = UUID.randomUUID() + ".jpg";

            Path filePath = uploadDirectory.resolve(fileName);

            Files.write(filePath, file.getBytes());

            /*
             * Delete the previous uploaded file if it was stored
             * using the new file-storage system.
             *
             * Old Base64 data is NOT treated as a file.
             */
            String oldPhoto = admin.getProfilePhoto();

            if (oldPhoto != null && oldPhoto.startsWith("/uploads/admin/")) {

                String oldFileName =
                        oldPhoto.substring("/uploads/admin/".length());

                Path oldFilePath =
                        uploadDirectory.resolve(oldFileName);

                Files.deleteIfExists(oldFilePath);
            }

            String photoPath = "/uploads/admin/" + fileName;

            admin.setProfilePhoto(photoPath);

            return adminRepository.save(admin);

        } catch (IOException e) {

            throw ApiException.badRequest(
                    "Failed to save profile photo");
        }
    }
}

