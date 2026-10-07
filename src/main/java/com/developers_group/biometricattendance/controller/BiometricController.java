package com.developers_group.biometricattendance.controller;

import com.developers_group.biometricattendance.dto.BiometricDTO;
import com.developers_group.biometricattendance.entity.Biometric;
import com.developers_group.biometricattendance.service.BiometricService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biometrics")
public class BiometricController {

    private final BiometricService biometricService;

    public BiometricController(BiometricService biometricService) {
        this.biometricService = biometricService;
    }

    // Register a device biometric for an employee (string Employee ID)
    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<Biometric> createBiometric(
            @PathVariable String employeeId,
            @RequestBody Biometric biometric) {

        return new ResponseEntity<>(
                biometricService.createBiometric(employeeId, biometric),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BiometricDTO>> getAllBiometrics() {
        return ResponseEntity.ok(biometricService.getAllBiometrics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BiometricDTO> getBiometricById(@PathVariable Long id) {

        return biometricService.getBiometricById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<BiometricDTO>> getEmployeeBiometrics(@PathVariable String employeeId) {
        return ResponseEntity.ok(biometricService.getEmployeeBiometrics(employeeId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Biometric> updateBiometric(
            @PathVariable Long id,
            @RequestBody Biometric biometric) {

        return ResponseEntity.ok(biometricService.updateBiometric(id, biometric));
    }

    // Stamp "last used" after a successful biometric check
    @PatchMapping("/{id}/used")
    public ResponseEntity<BiometricDTO> markUsed(@PathVariable Long id) {
        return ResponseEntity.ok(biometricService.markUsed(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBiometric(@PathVariable Long id) {

        biometricService.deleteBiometric(id);

        return ResponseEntity.noContent().build();
    }
}
