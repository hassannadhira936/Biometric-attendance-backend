package com.developers_group.biometricattendance.repository;

import com.developers_group.biometricattendance.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    // The school has exactly one settings row - this returns it
    Optional<SystemSetting> findFirstByOrderBySettingIdAsc();
}
