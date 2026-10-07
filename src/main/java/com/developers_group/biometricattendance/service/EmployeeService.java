package com.developers_group.biometricattendance.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import com.developers_group.biometricattendance.entity.Employee;
import com.developers_group.biometricattendance.exception.ApiException;
import com.developers_group.biometricattendance.repository.AttendanceRepository;
import com.developers_group.biometricattendance.repository.BiometricRepository;
import com.developers_group.biometricattendance.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final BiometricRepository biometricRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository,
            BiometricRepository biometricRepository) {

        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.biometricRepository = biometricRepository;
    }

    // =========================
    // EMPLOYEE ID NORMALISATION
    // "emp004", " EMP004 " and "EMP004" are all stored/searched as "EMP004"
    // =========================

    public static String normalizeId(String employeeId) {

        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw ApiException.badRequest("Employee ID is required");
        }

        return employeeId.trim().toUpperCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimOrNull(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean sameIgnoreCase(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    // =========================
    // CREATE EMPLOYEE
    // =========================

    @Transactional
    public Employee createEmployee(Employee employee) {

        String id = normalizeId(employee.getEmployeeId());

        if (isBlank(employee.getFirstName()) || isBlank(employee.getLastName())) {
            throw ApiException.badRequest("First name and last name are required");
        }

        if (isBlank(employee.getUsername())) {
            throw ApiException.badRequest("Username is required");
        }

        if (isBlank(employee.getPassword())
                || employee.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw ApiException.badRequest(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        if (employeeRepository.existsById(id)) {
            throw ApiException.conflict("This Employee ID already exists");
        }

        if (employeeRepository.existsByUsernameIgnoreCase(employee.getUsername().trim())) {
            throw ApiException.conflict("This username is already in use");
        }

        if (!isBlank(employee.getEmail())
                && employeeRepository.existsByEmailIgnoreCase(employee.getEmail().trim())) {
            throw ApiException.conflict("This email is already in use");
        }

        employee.setEmployeeId(id);
        employee.setFirstName(employee.getFirstName().trim());
        employee.setLastName(employee.getLastName().trim());
        employee.setUsername(employee.getUsername().trim());
        employee.setEmail(isBlank(employee.getEmail()) ? null : employee.getEmail().trim());

        if (isBlank(employee.getStatus())) {
            employee.setStatus("Active");
        }

        // PLAIN TEXT password (demo/academic use) - saved exactly as typed
        return employeeRepository.save(employee);
    }

    // =========================
    // GET ALL / ONE
    // =========================

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(String employeeId) {
        return employeeRepository.findById(normalizeId(employeeId));
    }

    // =========================
    // UPDATE EMPLOYEE
    // The Employee ID itself is the primary key and cannot be changed.
    // =========================

    @Transactional
    public Employee updateEmployee(String employeeId, Employee updated) {

        Employee existing = employeeRepository.findById(normalizeId(employeeId))
                .orElseThrow(() -> ApiException.notFound("Employee not found"));

        if (!isBlank(updated.getUsername())
                && !sameIgnoreCase(existing.getUsername(), updated.getUsername())
                && employeeRepository.existsByUsernameIgnoreCase(updated.getUsername().trim())) {

            throw ApiException.conflict("This username is already in use");
        }

        if (!isBlank(updated.getEmail())
                && !sameIgnoreCase(existing.getEmail(), updated.getEmail())
                && employeeRepository.existsByEmailIgnoreCase(updated.getEmail().trim())) {

            throw ApiException.conflict("This email is already in use");
        }

        if (!isBlank(updated.getFirstName())) existing.setFirstName(updated.getFirstName().trim());
        if (!isBlank(updated.getLastName())) existing.setLastName(updated.getLastName().trim());
        if (!isBlank(updated.getEmail())) existing.setEmail(updated.getEmail().trim());
        if (!isBlank(updated.getUsername())) existing.setUsername(updated.getUsername().trim());

        existing.setPhone(trimOrNull(updated.getPhone()));
        existing.setDepartment(trimOrNull(updated.getDepartment()));
        existing.setPosition(trimOrNull(updated.getPosition()));

        if (updated.getHireDate() != null) existing.setHireDate(updated.getHireDate());
        if (!isBlank(updated.getStatus())) existing.setStatus(updated.getStatus());

        // null = "photo not sent, keep the old one"; "" = "remove photo"
        if (updated.getProfilePhoto() != null) existing.setProfilePhoto(updated.getProfilePhoto());

        // Password is only changed when a new one is typed
        if (!isBlank(updated.getPassword())) {
            if (updated.getPassword().length() < MIN_PASSWORD_LENGTH) {
                throw ApiException.badRequest(
                        "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            }
            existing.setPassword(updated.getPassword());
        }

        return employeeRepository.save(existing);
    }

    // =========================
    // ACTIVATE / DEACTIVATE
    // =========================

    @Transactional
    public Employee updateStatus(String employeeId, String status) {

        if (!"Active".equalsIgnoreCase(status) && !"Inactive".equalsIgnoreCase(status)) {
            throw ApiException.badRequest("Status must be Active or Inactive");
        }

        Employee employee = employeeRepository.findById(normalizeId(employeeId))
                .orElseThrow(() -> ApiException.notFound("Employee not found"));

        employee.setStatus("Inactive".equalsIgnoreCase(status) ? "Inactive" : "Active");

        return employeeRepository.save(employee);
    }

    // =========================
    // DELETE EMPLOYEE
    // Attendance and biometric rows point to the employee (foreign key),
    // so they are removed first.
    // =========================

    @Transactional
    public void deleteEmployee(String employeeId) {

        String id = normalizeId(employeeId);

        if (!employeeRepository.existsById(id)) {
            throw ApiException.notFound("Employee not found");
        }

        attendanceRepository.deleteByEmployeeEmployeeId(id);
        biometricRepository.deleteByEmployeeEmployeeId(id);
        employeeRepository.deleteById(id);
    }

    // =========================
    // EMPLOYEE LOGIN
    // Accepts the username OR the Employee ID. Plain-text comparison.
    // =========================

    public Employee login(String identifier, String password) {

        if (isBlank(identifier) || password == null || password.isEmpty()) {
            throw ApiException.badRequest("Please enter your Employee ID/username and password");
        }

        String entered = identifier.trim();

        Employee employee = employeeRepository.findByUsernameIgnoreCase(entered)
                .or(() -> employeeRepository.findById(entered.toUpperCase()))
                .orElseThrow(() -> ApiException.unauthorized(
                        "Login failed. Employee ID/Username or password is not recognized."));

        if (!password.equals(employee.getPassword())) {
            throw ApiException.unauthorized(
                    "Login failed. Employee ID/Username or password is not recognized.");
        }

        if ("Inactive".equalsIgnoreCase(employee.getStatus())) {
            throw ApiException.forbidden(
                    "This employee account is inactive. Please contact the administrator.");
        }

        return employee;
    }

    // =========================
// UPLOAD EMPLOYEE PROFILE PHOTO
// =========================

    @Transactional
    public Employee uploadProfilePhoto(String employeeId, MultipartFile photo)
            throws IOException {

        String id = normalizeId(employeeId);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Employee not found"));

        if (photo == null || photo.isEmpty()) {
            throw ApiException.badRequest("Please select a profile photo");
        }

        // Only allow image files
        String contentType = photo.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw ApiException.badRequest("Only image files are allowed");
        }

        // Create upload folder
        Path uploadDirectory = Paths.get("uploads", "employee");

        Files.createDirectories(uploadDirectory);

        // Get extension
        String originalFilename = photo.getOriginalFilename();

        String extension = ".jpg";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(
                    originalFilename.lastIndexOf(".")
            ).toLowerCase();
        }

        // Unique filename
        String fileName = id + "-" + UUID.randomUUID() + extension;

        Path targetPath = uploadDirectory.resolve(fileName);

        // Save file
        Files.copy(
                photo.getInputStream(),
                targetPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        // Delete old photo if it was previously stored as a file
        String oldPhoto = employee.getProfilePhoto();

        if (oldPhoto != null
                && oldPhoto.startsWith("/uploads/employee/")) {

            try {
                Path oldPath = Paths.get(
                        oldPhoto.substring(1)
                );

                Files.deleteIfExists(oldPath);

            } catch (Exception ignored) {
                // Do not fail upload if old file cannot be deleted
            }
        }

        // Save only the file path in database
        employee.setProfilePhoto("/uploads/employee/" + fileName);

        return employeeRepository.save(employee);
    }

    // =========================
    // CHANGE PASSWORD
    // =========================

    @Transactional
    public void changePassword(String employeeId, String currentPassword, String newPassword) {

        Employee employee = employeeRepository.findById(normalizeId(employeeId))
                .orElseThrow(() -> ApiException.notFound("Employee not found"));

        if (currentPassword == null || !currentPassword.equals(employee.getPassword())) {
            throw ApiException.badRequest("Current password is incorrect");
        }

        if (isBlank(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw ApiException.badRequest(
                    "New password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        employee.setPassword(newPassword);

        employeeRepository.save(employee);
    }
}
