package com.developers_group.biometricattendance.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.developers_group.biometricattendance.dto.ChangePasswordDTO;
import com.developers_group.biometricattendance.dto.LoginRequestDTO;
import com.developers_group.biometricattendance.dto.StatusUpdateDTO;
import com.developers_group.biometricattendance.entity.Employee;
import com.developers_group.biometricattendance.service.EmployeeService;
import com.developers_group.biometricattendance.service.WebAuthnUserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CORS is configured once for every /api/** URL in config/CorsConfig.java
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final WebAuthnUserAccountService webAuthnUserAccountService;

    public EmployeeController(
            EmployeeService employeeService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            WebAuthnUserAccountService webAuthnUserAccountService) {

        this.employeeService = employeeService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.webAuthnUserAccountService = webAuthnUserAccountService;
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return new ResponseEntity<>(employeeService.createEmployee(employee), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // {id} is the string Employee ID, e.g. /api/employees/EMP004
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {

        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable String id,
            @RequestBody Employee employee) {

        return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
    }

    // Activate / deactivate
    @PatchMapping("/{id}/status")
    public ResponseEntity<Employee> updateStatus(
            @PathVariable String id,
            @RequestBody StatusUpdateDTO request) {

        return ResponseEntity.ok(employeeService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {

        employeeService.deleteEmployee(id);

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/profile-photo")
    public ResponseEntity<Employee> uploadProfilePhoto(
            @PathVariable String id,
            @RequestParam("photo") MultipartFile photo) throws IOException {

        return ResponseEntity.ok(
                employeeService.uploadProfilePhoto(id, photo)
        );
    }
    @PostMapping("/login")
    public ResponseEntity<Employee> login(
            @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        try {

            /*
             * Create an unauthenticated username/password request.
             */
            Authentication authenticationRequest =
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.getUsername(),
                            request.getPassword()
                    );

            /*
             * Spring Security verifies the employee
             * through EmployeeUserDetailsService.
             */
            Authentication authenticationResponse =
                    authenticationManager.authenticate(
                            authenticationRequest
                    );

            /*
             * Create a new SecurityContext.
             */
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(
                    authenticationResponse
            );

            /*
             * Put authentication into the current request.
             */
            SecurityContextHolder.setContext(context);

            /*
             * IMPORTANT:
             * Persist the authenticated employee in the HTTP session.
             *
             * This allows the next request, such as:
             *
             * POST /webauthn/register/options
             *
             * to know which employee is logged in.
             */
            securityContextRepository.saveContext(
                    context,
                    httpRequest,
                    httpResponse
            );

            /*
             * Keep returning the same Employee JSON
             * that the existing React frontend expects.
             */
            Employee employee = employeeService.login(
                    request.getUsername(),
                    request.getPassword()
            );

            /*
             * WEBAUTHN BRIDGE:
             *
             * Make sure this employee already has a corresponding
             * Spring Security WebAuthn user entity (user_entities row)
             * with a STABLE id before they ever reach the "register
             * biometric" screen. This is idempotent - it is a no-op on
             * every login after the first one.
             */
            try {
                webAuthnUserAccountService.ensureUserEntity(employee);
            } catch (Exception ex) {
                // Password login must NEVER fail because of the WebAuthn bridge.
                System.err.println(
                        "WebAuthn user entity could not be prepared: "
                                + ex.getMessage());
            }

            return ResponseEntity.ok(employee);

        } catch (AuthenticationException ex) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }
    @PostMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(
            @PathVariable String id,
            @RequestBody ChangePasswordDTO request) {

        employeeService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok("Password changed successfully");
    }

}
