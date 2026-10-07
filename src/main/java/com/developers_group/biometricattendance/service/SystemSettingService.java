package com.developers_group.biometricattendance.service;

import com.developers_group.biometricattendance.entity.SystemSetting;
import com.developers_group.biometricattendance.exception.ApiException;
import com.developers_group.biometricattendance.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    public SystemSettingService(SystemSettingRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    private void validate(SystemSetting setting) {

        Double lat = setting.getSchoolLatitude();
        Double lon = setting.getSchoolLongitude();
        Double radius = setting.getAllowedRadius();

        if (lat == null || lat < -90 || lat > 90) {
            throw ApiException.badRequest("School latitude must be between -90 and 90");
        }

        if (lon == null || lon < -180 || lon > 180) {
            throw ApiException.badRequest("School longitude must be between -180 and 180");
        }

        if (radius == null || radius <= 0) {
            throw ApiException.badRequest("Allowed radius must be greater than 0 metres");
        }
    }

    @Transactional
    public SystemSetting createSetting(SystemSetting setting) {

        if (systemSettingRepository.count() > 0) {
            throw ApiException.conflict(
                    "System setting already exists. Please update the existing setting.");
        }

        validate(setting);

        return systemSettingRepository.save(setting);
    }

    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    // The single settings row used by the whole system
    public Optional<SystemSetting> getCurrentSetting() {
        return systemSettingRepository.findFirstByOrderBySettingIdAsc();
    }

    public Optional<SystemSetting> getSettingById(Long id) {
        return systemSettingRepository.findById(id);
    }

    @Transactional
    public SystemSetting updateSetting(Long id, SystemSetting updated) {

        SystemSetting existing = systemSettingRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("System setting not found"));

        validate(updated);

        if (updated.getSchoolName() != null) existing.setSchoolName(updated.getSchoolName());

        existing.setSchoolLatitude(updated.getSchoolLatitude());
        existing.setSchoolLongitude(updated.getSchoolLongitude());
        existing.setAllowedRadius(updated.getAllowedRadius());

        if (updated.getWorkStartTime() != null) existing.setWorkStartTime(updated.getWorkStartTime());
        if (updated.getWorkEndTime() != null) existing.setWorkEndTime(updated.getWorkEndTime());

        return systemSettingRepository.save(existing);
    }

    @Transactional
    public void deleteSetting(Long id) {

        if (!systemSettingRepository.existsById(id)) {
            throw ApiException.notFound("System setting not found");
        }

        systemSettingRepository.deleteById(id);
    }
}
