package com.developers_group.biometricattendance.service;

import com.developers_group.biometricattendance.entity.Employee;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Bridges the application's own {@link Employee} identity to Spring Security's
 * WebAuthn {@link PublicKeyCredentialUserEntity}.
 *
 * <p>Spring Security's own WebAuthn registration filter
 * (PublicKeyCredentialCreationOptionsFilter -&gt; Webauthn4JRelyingPartyOperations)
 * will already create a {@link PublicKeyCredentialUserEntity} on the fly - keyed by
 * {@code Authentication.getName()} - the very first time an authenticated employee
 * requests registration options, using a RANDOM id.
 *
 * <p>That default behaviour is fine on its own, but this service makes the mapping
 * explicit and gives the WebAuthn user id a STABLE, deterministic value derived from
 * the Employee's own primary key, instead of leaving it to a random value that only
 * happens to be remembered because it was saved once. Ensuring the row exists (with a
 * stable id) at the moment of a successful password login also means the first call to
 * POST /webauthn/register/options never has to create anything - it only has to find it.
 *
 * <p>This class is purely a bridge/lookup helper. It does NOT store any cryptographic
 * material itself - the actual credential (public key, signature counter, attestation,
 * etc.) is written by Spring Security's WebAuthnRegistrationFilter into the
 * {@code user_credentials} table via {@link org.springframework.security.web.webauthn.management.UserCredentialRepository}.
 */
@Service
public class WebAuthnUserAccountService {

    /**
     * Namespace prefix mixed into the stable id derivation so that the digest is
     * specific to this application's employee-id space (defensive, not a secret).
     */
    private static final String ID_NAMESPACE = "al-ihsan-biometric-attendance:employee:";

    private final PublicKeyCredentialUserEntityRepository userEntityRepository;

    public WebAuthnUserAccountService(
            PublicKeyCredentialUserEntityRepository userEntityRepository) {

        this.userEntityRepository = userEntityRepository;
    }

    /**
     * Idempotently ensures that a {@link PublicKeyCredentialUserEntity} exists for the
     * given employee, keyed by the employee's username (the same value Spring Security
     * puts in {@code Authentication.getName()} once the employee is logged in).
     *
     * <p>If a row already exists for this username it is left untouched and returned as-is
     * (its id is NEVER regenerated). If it does not exist yet, one is created with a
     * deterministic id derived from the employee's stable {@code employeeId} primary key.
     *
     * @param employee the employee that has just been authenticated
     * @return the existing or newly created WebAuthn user entity for this employee
     */
    public PublicKeyCredentialUserEntity ensureUserEntity(Employee employee) {

        String username = employee.getUsername();

        PublicKeyCredentialUserEntity existing =
                userEntityRepository.findByUsername(username);

        if (existing != null) {
            return existing;
        }

        PublicKeyCredentialUserEntity created =
                ImmutablePublicKeyCredentialUserEntity.builder()
                        .id(stableIdFor(employee.getEmployeeId()))
                        .name(username)
                        .displayName(displayNameFor(employee))
                        .build();

        userEntityRepository.save(created);

        return created;
    }

    /**
     * Derives a stable, deterministic 32-byte WebAuthn user id from the employee's own
     * primary key, so the same Employee always maps to the same WebAuthn user id even if
     * this bridge is called again (e.g. on every login) or the JVM restarts. Using a
     * cryptographic digest (rather than the raw employeeId bytes) keeps the id at a
     * fixed, WebAuthn-friendly length without leaking the human-readable employee id.
     */
    private Bytes stableIdFor(String employeeId) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    (ID_NAMESPACE + employeeId).getBytes(StandardCharsets.UTF_8));

            return new Bytes(hash);

        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is a mandatory JDK algorithm; this should never happen.
            throw new IllegalStateException(
                    "SHA-256 MessageDigest is not available", ex);
        }
    }

    private String displayNameFor(Employee employee) {

        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();

        String fullName = (
                (firstName == null ? "" : firstName.trim())
                        + " "
                        + (lastName == null ? "" : lastName.trim())
        ).trim();

        return fullName.isEmpty() ? employee.getUsername() : fullName;
    }
}
