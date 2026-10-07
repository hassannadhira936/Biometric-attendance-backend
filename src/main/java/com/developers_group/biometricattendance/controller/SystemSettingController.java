package com.developers_group.biometricattendance.controller;

import com.developers_group.biometricattendance.entity.SystemSetting;
import com.developers_group.biometricattendance.service.SystemSettingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    public SystemSettingController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @PostMapping
    public ResponseEntity<SystemSetting> createSetting(@RequestBody SystemSetting setting) {
        return new ResponseEntity<>(systemSettingService.createSetting(setting), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAllSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }

    // The school's one settings row (GPS centre, radius, work hours).
    // Used by the employee page to show the geofence, and by the admin Settings page.
    @GetMapping("/current")
    public ResponseEntity<SystemSetting> getCurrentSetting() {

        return systemSettingService.getCurrentSetting()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemSetting> getSettingById(@PathVariable Long id) {

        return systemSettingService.getSettingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SystemSetting> updateSetting(
            @PathVariable Long id,
            @RequestBody SystemSetting setting) {

        return ResponseEntity.ok(systemSettingService.updateSetting(id, setting));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSetting(@PathVariable Long id) {

        systemSettingService.deleteSetting(id);

        return ResponseEntity.noContent().build();
    }
}
