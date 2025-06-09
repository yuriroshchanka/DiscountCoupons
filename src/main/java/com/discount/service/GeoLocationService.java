package com.discount.service;

import com.maxmind.geoip2.DatabaseReader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Pattern;

@Service
public class GeoLocationService {
    private final DatabaseReader reader;
    private static final Pattern IPV4_PATTERN = 
        Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    public GeoLocationService() throws IOException {
        // In a production environment, you would want to download and use the actual GeoLite2 database
        // Assuming for this example, we'll use a mock implementation
        this.reader = null;
    }

    public String getCountryFromIp(String ipAddress) {
        if (!isValidIpAddress(ipAddress)) {
            throw new IllegalArgumentException("Invalid IP address format");
        }

        try {
            // In a real implementation, you would use the GeoIP2 database
            // Assuming for this example, we'll return a mock country
            return "US";
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine country from IP", e);
        }
    }

    private boolean isValidIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }
        return IPV4_PATTERN.matcher(ipAddress).matches();
    }
} 