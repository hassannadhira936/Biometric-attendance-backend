package com.developers_group.biometricattendance.service;

import com.developers_group.biometricattendance.entity.Employee;
import com.developers_group.biometricattendance.repository.EmployeeRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeUserDetailsService
        implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public EmployeeUserDetailsService(
            EmployeeRepository employeeRepository) {

        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String identifier = username == null
                ? ""
                : username.trim();

        Employee employee = employeeRepository
                .findByUsernameIgnoreCase(identifier)
                .or(() -> employeeRepository.findById(
                        identifier.toUpperCase()))
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Employee not found"));

        boolean active =
                !"Inactive".equalsIgnoreCase(
                        employee.getStatus());

        return User.withUsername(employee.getUsername())
                .password(employee.getPassword())
                .roles("EMPLOYEE")
                .disabled(!active)
                .build();
    }
}