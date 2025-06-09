package com.discount.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GeoLocationServiceTest {

    @Autowired
    private GeoLocationService geoLocationService;

    @Test
    void getCountryFromIp_ShouldReturnCountry() {
        // Given
        String ipAddress = "192.168.1.1";

        // When
        String result = geoLocationService.getCountryFromIp(ipAddress);

        // Then
        assertNotNull(result);
        assertEquals("US", result);
    }

    @Test
    void getCountryFromIp_ShouldThrowException_WhenInvalidIp() {
        // Given
        String invalidIp = "invalid.ip.address";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            geoLocationService.getCountryFromIp(invalidIp)
        );
    }

    @Test
    void getCountryFromIp_ShouldReturnUS_ForAnyValidIp() {
        // Given
        String[] validIps = {
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "8.8.8.8"
        };

        // When & Then
        for (String ip : validIps) {
            String result = geoLocationService.getCountryFromIp(ip);
            assertEquals("US", result, "Expected US for IP: " + ip);
        }
    }
} 