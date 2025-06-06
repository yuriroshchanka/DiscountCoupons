# Discount Coupons Service

A REST service for managing discount coupons with country-based restrictions and usage tracking.

## Features

- Create new discount coupons with unique codes
- Track coupon usage with country-based restrictions
- Prevent multiple uses of the same coupon by a single user
- IP-based country detection for coupon validation
- Concurrent-safe coupon usage tracking

## Technical Stack

- Java 17
- Spring Boot 3.2.3
- Spring Data JPA
- H2 Database (in-memory)
- MaxMind GeoIP2 for IP-based country detection
- Maven for dependency management

## API Endpoints

### Create Coupon
```
POST /api/coupons
Content-Type: application/json

{
    "code": "SUMMER2024",
    "maxUses": 100,
    "country": "US"
}
```

### Use Coupon
```
POST /api/coupons/{code}/use
X-User-Id: user123
```

## Running the Application

1. Ensure you have Java 17 installed
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on port 8080. You can access the H2 database console at http://localhost:8080/h2-console.

## Database Configuration

- URL: jdbc:h2:mem:coupondb
- Username: sa
- Password: password

## Notes

- The GeoIP2 implementation is currently mocked. In a production environment, you would need to download and use the actual GeoLite2 database.
- The application uses pessimistic locking to handle concurrent coupon usage safely.
- All coupon codes are stored in uppercase to ensure case-insensitive matching. 