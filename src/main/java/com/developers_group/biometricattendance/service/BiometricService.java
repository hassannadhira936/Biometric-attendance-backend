package com.developers_group.biometricattendance.service;

import com.developers_group.biometricattendance.dto.BiometricDTO;
import com.developers_group.biometricattendance.entity.Biometric;
import com.developers_group.biometricattendance.entity.Employee;
import com.developers_group.biometricattendance.exception.ApiException;
import com.developers_group.biometricattendance.repository.BiometricRepository;
import com.developers_group.biometricattendance.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BiometricService {

    private final BiometricRepository biometricRepository;
    private final EmployeeRepository employeeRepository;

    public BiometricService(
            BiometricRepository biometricRepository,
            EmployeeRepository employeeRepository) {

        this.biometricRepository = biometricRepository;
        this.employeeRepository = employeeRepository;
    }

    /*
     * Convert Entity -> DTO
     */
    private BiometricDTO convertToDTO(Biometric biometric) {

        return new BiometricDTO(
                biometric.getBiometricId(),
                biometric.getEmployee().getEmployeeId(),
                biometric.getFingerprintId(),
                biometric.getFingerName(),
                biometric.getStatus(),
                biometric.getPublicKey(),
                biometric.getSignCount()
        );
    }

    /*
     * CREATE BIOMETRIC
     *
     * Hii method tutaitumia kuhifadhi WebAuthn credential
     * baada ya registration kuthibitishwa na backend.
     */
    @Transactional
    public Biometric createBiometric(
            String employeeId,
            Biometric biometric) {

        String normalizedEmployeeId =
                EmployeeService.normalizeId(employeeId);

        Employee employee = employeeRepository
                .findById(normalizedEmployeeId)
                .orElseThrow(() ->
                        ApiException.notFound("Employee not found"));

        /*
         * Credential ID lazima iwepo.
         */
        if (biometric.getFingerprintId() == null ||
                biometric.getFingerprintId().isBlank()) {

            throw ApiException.badRequest(
                    "Biometric credential ID is required");
        }

        /*
         * Public key lazima iwepo kwa WebAuthn verification.
         */
        if (biometric.getPublicKey() == null ||
                biometric.getPublicKey().isBlank()) {

            throw ApiException.badRequest(
                    "WebAuthn public key is required");
        }

        /*
         * Usihifadhi credential duplicate
         * kwa employee huyo huyo.
         */
        if (biometricRepository
                .existsByEmployeeEmployeeIdAndFingerprintId(
                        employee.getEmployeeId(),
                        biometric.getFingerprintId())) {

            return biometricRepository
                    .findByEmployeeEmployeeId(
                            employee.getEmployeeId())
                    .stream()
                    .filter(existing ->
                            biometric.getFingerprintId()
                                    .equals(existing.getFingerprintId()))
                    .findFirst()
                    .orElseThrow();
        }

        /*
         * ID itengenezwe na database.
         */
        biometric.setBiometricId(null);

        /*
         * Link biometric na employee.
         */
        biometric.setEmployee(employee);

        /*
         * Default sign counter.
         */
        if (biometric.getSignCount() == null) {
            biometric.setSignCount(0L);
        }

        /*
         * Default status.
         */
        if (biometric.getStatus() == null ||
                biometric.getStatus().isBlank()) {

            biometric.setStatus("Active");
        }

        /*
         * Default name.
         */
        if (biometric.getFingerName() == null ||
                biometric.getFingerName().isBlank()) {

            biometric.setFingerName("Device biometric");
        }

        return biometricRepository.save(biometric);
    }

    /*
     * GET ALL BIOMETRICS
     */
    public List<BiometricDTO> getAllBiometrics() {

        return biometricRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    /*
     * GET BIOMETRIC BY ID
     */
    public Optional<BiometricDTO> getBiometricById(Long id) {

        return biometricRepository
                .findById(id)
                .map(this::convertToDTO);
    }

    /*
     * GET BIOMETRICS OF ONE EMPLOYEE
     */
    public List<BiometricDTO> getEmployeeBiometrics(
            String employeeId) {

        String normalizedEmployeeId =
                EmployeeService.normalizeId(employeeId);

        if (!employeeRepository.existsById(normalizedEmployeeId)) {

            throw ApiException.notFound(
                    "Employee not found");
        }

        return biometricRepository
                .findByEmployeeEmployeeId(normalizedEmployeeId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    /*
     * GET ONE BIOMETRIC BY CREDENTIAL ID
     *
     * Tutaitumia wakati wa WebAuthn authentication.
     */
    public Optional<Biometric> getByCredentialId(
            String credentialId) {

        return biometricRepository
                .findByFingerprintId(credentialId);
    }

    /*
     * UPDATE BIOMETRIC
     */
    @Transactional
    public Biometric updateBiometric(
            Long id,
            Biometric updated) {

        Biometric existing = biometricRepository
                .findById(id)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Biometric not found"));

        if (updated.getFingerprintId() != null &&
                !updated.getFingerprintId().isBlank()) {

            existing.setFingerprintId(
                    updated.getFingerprintId());
        }

        if (updated.getPublicKey() != null &&
                !updated.getPublicKey().isBlank()) {

            existing.setPublicKey(
                    updated.getPublicKey());
        }

        if (updated.getSignCount() != null) {

            existing.setSignCount(
                    updated.getSignCount());
        }

        if (updated.getFingerName() != null) {

            existing.setFingerName(
                    updated.getFingerName());
        }

        if (updated.getStatus() != null) {

            existing.setStatus(
                    updated.getStatus());
        }

        return biometricRepository.save(existing);
    }

    /*
     * UPDATE SIGN COUNT
     *
     * WebAuthn authentication ikifanikiwa,
     * counter mpya itahifadhiwa hapa.
     */
    @Transactional
    public Biometric updateSignCount(
            Long biometricId,
            Long newSignCount) {

        Biometric biometric = biometricRepository
                .findById(biometricId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Biometric not found"));

        biometric.setSignCount(newSignCount);

        return biometricRepository.save(biometric);
    }

    /*
     * MARK BIOMETRIC AS USED
     */
    @Transactional
    public BiometricDTO markUsed(Long id) {

        Biometric existing = biometricRepository
                .findById(id)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Biometric not found"));

        /*
         * Kwa sasa hakuna lastUsed column.
         * Authentication ya kweli itatumia signCount.
         */
        return convertToDTO(existing);
    }

    /*
     * DELETE BIOMETRIC
     */
    @Transactional
    public void deleteBiometric(Long id) {

        if (!biometricRepository.existsById(id)) {

            throw ApiException.notFound(
                    "Biometric not found");
        }

        biometricRepository.deleteById(id);
    }
}