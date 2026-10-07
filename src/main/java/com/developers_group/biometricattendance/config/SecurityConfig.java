package com.developers_group.biometricattendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
     * Passwords in the current academic project are stored
     * as plain text.
     *
     * Therefore Spring Security must compare them as plain text
     * for now.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /*
     * Authentication provider that uses our EmployeeUserDetailsService.
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    /*
     * AuthenticationManager used by our REST employee login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthenticationProvider) {

        return new ProviderManager(
                daoAuthenticationProvider
        );
    }

    /*
     * Store authenticated employee information in the HTTP session.
     *
     * This is important because WebAuthn registration happens
     * in a later HTTP request.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            @Value("${app.webauthn.rp-id:localhost}") String rpId,
            @Value("${app.webauthn.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:5176}") String[] allowedOrigins)
            throws Exception {

        http
                //Enable CORS for the frontend and WebAuthn requests
                .cors(cors ->{})

                // REST API
                .csrf(csrf -> csrf.disable())

                /*
                 * Persist Spring Security authentication
                 * between HTTP requests.
                 */
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(
                                securityContextRepository
                        )
                        .requireExplicitSave(true)
                )

                .authorizeHttpRequests(auth -> auth

                        // Normal employee login
                        .requestMatchers(
                                "/api/employees/login"
                        ).permitAll()

                        /*
                         * Spring Security WebAuthn endpoints.
                         *
                         * The WebAuthn filters themselves perform
                         * the appropriate authentication work.
                         */
                        .requestMatchers(
                                "/webauthn/**",
                                "/login/webauthn"
                        ).permitAll()

                        // Existing application APIs
                        .anyRequest().permitAll()
                )

                /*
                 * WebAuthn / Passkeys
                 */
                .webAuthn(webAuthn -> webAuthn
                        .rpName(
                                "Al-Ihsan Girls Secondary School"
                        )
                        .rpId(rpId)
                        /*
                         * The React/Vite frontend actually runs on
                         * http://localhost:5173 (see Vite's default dev
                         * server port). This MUST be in the allowed-origins
                         * set, or Webauthn4JRelyingPartyOperations will
                         * reject every real registration/authentication
                         * ceremony performed from that page with an origin
                         * mismatch - the extra 5174-5176 ports are kept for
                         * convenience in case Vite falls back to them when
                         * 5173 is already taken.
                         */
                        .allowedOrigins(allowedOrigins)
                );

        return http.build();
    }
}