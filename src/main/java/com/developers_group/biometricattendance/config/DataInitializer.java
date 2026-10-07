package com.developers_group.biometricattendance.config;

import com.developers_group.biometricattendance.entity.Admin;
import com.developers_group.biometricattendance.entity.SystemSetting;
import com.developers_group.biometricattendance.repository.AdminRepository;
import com.developers_group.biometricattendance.repository.SystemSettingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Makes a fresh database usable straight away for the demo:
 *  - one default administrator (only if the admin table is empty)
 *  - one default system setting row (only if the settings table is empty)
 *
 * Default login:  username = admin   password = admin123
 * (PLAIN TEXT on purpose - demo/academic use only.)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final SystemSettingRepository systemSettingRepository;

    public DataInitializer(AdminRepository adminRepository,
                           SystemSettingRepository systemSettingRepository) {
        this.adminRepository = adminRepository;
        this.systemSettingRepository = systemSettingRepository;
    }

    @Override
    public void run(String... args) {

        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setFullName("System Administrator");
            admin.setPosition("Administrator");
            admin.setContact("0700000000");
            admin.setEmail("admin@alihsan.sch.tz");
            admin.setUsername("admin");
            admin.setPassword("admin123");
            adminRepository.save(admin);
            System.out.println(">>> Default admin created: admin / admin123");
        }

        if (systemSettingRepository.count() == 0) {
            SystemSetting setting = new SystemSetting();
            setting.setSchoolName("Al-Ihsan Girls Secondary School");
            setting.setSchoolLatitude(-6.1746053);
            setting.setSchoolLongitude(39.2263778);
            setting.setAllowedRadius(100.0);
            setting.setWorkStartTime(LocalTime.of(8, 0));
            setting.setWorkEndTime(LocalTime.of(16, 0));
            systemSettingRepository.save(setting);
            System.out.println(">>> Default system settings created (change them in Admin > Settings)");
        }
    }
}
