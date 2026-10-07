package com.developers_group.biometricattendance.service;

import com.developers_group.biometricattendance.entity.Biometric;
import com.developers_group.biometricattendance.repository.BiometricRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WebAuthnCredentialService {

    private final BiometricRepository biometricRepository;

    public WebAuthnCredentialService(
            BiometricRepository biometricRepository) {

        this.biometricRepository = biometricRepository;
    }

    public Optional<Biometric> findByCredentialId(
            String credentialId) {

        return biometricRepository.findByFingerprintId(
                credentialId);
    }

    public Biometric save(Biometric biometric) {
        return biometricRepository.save(biometric);
    }
}