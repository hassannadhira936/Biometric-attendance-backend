package com.developers_group.biometricattendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BiometricAttendanceApplication {

    public static void main(String[] args) {

        // Al-Ihsan Girls Secondary School is in Tanzania (East Africa Time).
        // Setting the JVM time zone once makes LocalDate.now(), LocalDateTime.now()
        // and Hibernate/MySQL timestamps all agree, whatever the PC's own zone is.
        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Dar_es_Salaam"));

        SpringApplication.run(BiometricAttendanceApplication.class, args);
    }
}
