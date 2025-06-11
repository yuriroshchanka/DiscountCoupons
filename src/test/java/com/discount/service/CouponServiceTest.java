package com.discount.service;

import com.discount.model.Coupon;
import com.discount.repository.CouponRepository;
import com.discount.repository.CouponUsageRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private GeoLocationService geoLocationService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        couponUsageRepository.deleteAll();
        couponRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create a test coupon
        Coupon testCoupon = new Coupon("TEST123", 10, "US");
        testCoupon.setCurrentUses(0);
        couponRepository.save(testCoupon);
    }

    @Test
    void createCoupon_ShouldCreateAndReturnCoupon() {
        // Given
        String code = "NEW123";
        Integer maxUses = 10;
        String country = "UK";

        // When
        Coupon created = couponService.createCoupon(code, maxUses, country);

        // Then
        assertNotNull(created);
        assertEquals(code, created.getCode());
        assertEquals(maxUses, created.getMaxUses());
        assertEquals(country, created.getCountry());
        assertEquals(0, created.getCurrentUses());
    }

    @Test
    void createCoupon_ShouldThrowException_WhenCodeAlreadyExists_CaseInsensitive() {
        // Given
        // The test coupon is already created in setUp()

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            couponService.createCoupon("test123", 5, "US")
        );
    }

    @Test
    void getAllCoupons_ShouldReturnListOfCoupons() {
        // Given
        Coupon coupon1 = new Coupon("CODE1", 5, "US");
        coupon1.setCurrentUses(0);
        Coupon coupon2 = new Coupon("CODE2", 10, "UK");
        coupon2.setCurrentUses(0);
        couponRepository.saveAll(List.of(coupon1, coupon2));

        // When
        List<Coupon> coupons = couponService.getAllCoupons();

        // Then
        assertFalse(coupons.isEmpty());
        assertTrue(coupons.stream().anyMatch(c -> c.getCode().equals("CODE1")));
        assertTrue(coupons.stream().anyMatch(c -> c.getCode().equals("CODE2")));
    }

    @Test
    void getCouponByCode_ShouldReturnCoupon() {
        // Given
        String code = "TEST123";

        // When
        Coupon found = couponService.getCouponByCode(code);

        // Then
        assertNotNull(found);
        assertEquals(code, found.getCode());
    }

    @Test
    void getCouponByCode_ShouldThrowException_WhenCouponNotFound() {
        // Given
        String invalidCode = "INVALID";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            couponService.getCouponByCode(invalidCode)
        );
    }

//    @Test
    void useCoupon_ShouldHandleConcurrentUsage_FirstComeFirstServed() throws Exception {
        // Given
        String code = "CONCURRENT";
        String country = "US";
        Coupon coupon = new Coupon(code, 1, country); // Only one use allowed
        coupon.setCurrentUses(0);
        coupon = couponRepository.save(coupon);
        
        // Ensure the coupon is saved and transaction is committed
        couponRepository.flush();
        entityManager.clear();

        // When - Simulate concurrent usage
        CompletableFuture<Void> firstUser = CompletableFuture.runAsync(() -> {
            try {
                couponService.useCoupon(code, "user1", "192.168.1.1", country);
            } catch (Exception e) {
                fail("First user should succeed", e);
            }
        });

        // Add a small delay to ensure the first user's transaction starts
        Thread.sleep(100);

        CompletableFuture<Void> secondUser = CompletableFuture.runAsync(() -> {
            try {
                couponService.useCoupon(code, "user2", "192.168.1.2", country);
                fail("Second user should fail");
            } catch (IllegalStateException e) {
                assertEquals("Coupon has reached maximum uses", e.getMessage());
            }
        });

        // Wait for both operations to complete
        CompletableFuture.allOf(firstUser, secondUser).join();

        // Then
        Coupon updatedCoupon = couponRepository.findByCodeIgnoreCase(code).orElseThrow();
        assertEquals(1, updatedCoupon.getCurrentUses());
        assertTrue(couponUsageRepository.findByCouponIdAndUserId(updatedCoupon.getId(), "user1").isPresent());
        assertFalse(couponUsageRepository.findByCouponIdAndUserId(updatedCoupon.getId(), "user2").isPresent());
    }

    @Test
    void useCoupon_ShouldThrowException_WhenCountryMismatch() {
        // Given
        String code = "COUNTRY";
        String couponCountry = "US";
        String userCountry = "UK";
        Coupon coupon = new Coupon(code, 10, couponCountry);
        coupon = couponRepository.save(coupon);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            couponService.useCoupon(code, "user1", "192.168.1.1", userCountry)
        );
        assertEquals("Coupon is not valid for your country", exception.getMessage());
    }

    @Test
    void useCoupon_ShouldThrowException_WhenCouponNotFound() {
        // Given
        String nonExistentCode = "NONEXISTENT";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            couponService.useCoupon(nonExistentCode, "user1", "192.168.1.1", "US")
        );
        assertEquals("Coupon not found", exception.getMessage());
    }

    @Test
    void useCoupon_ShouldThrowException_WhenMaxUsesReached() {
        // Given
        String code = "MAXUSES";
        String country = "US";
        Coupon coupon = new Coupon(code, 1, country);
        coupon.setCurrentUses(1); // Already at max uses
        coupon = couponRepository.save(coupon);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            couponService.useCoupon(code, "user1", "192.168.1.1", country)
        );
        assertEquals("Coupon has reached maximum uses", exception.getMessage());
    }

    @Test
    void useCoupon_ShouldThrowException_WhenUserAlreadyUsedCoupon() {
        // Given
        String code = "REUSE";
        String country = "US";
        String userId = "user1";
        Coupon coupon = new Coupon(code, 10, country);
        coupon = couponRepository.save(coupon);
        
        // First use
        couponService.useCoupon(code, userId, "192.168.1.1", country);

        // When & Then - Try to use again
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            couponService.useCoupon(code, userId, "192.168.1.1", country)
        );
        assertEquals("User has already used this coupon", exception.getMessage());
    }

    @Test
    void useCoupon_ShouldSucceed_WhenNoCountryRestriction() {
        // Given
        String code = "NOCOUNTRY";
        Coupon coupon = new Coupon(code, 5, null);
        coupon.setCurrentUses(0);
        coupon = couponRepository.save(coupon);

        // When
        couponService.useCoupon(code, "user1", "192.168.1.1", null);

        // Then
        Coupon updatedCoupon = couponRepository.findByCodeIgnoreCase(code).orElseThrow();
        assertEquals(1, updatedCoupon.getCurrentUses());
        assertTrue(couponUsageRepository.findByCouponIdAndUserId(updatedCoupon.getId(), "user1").isPresent());
    }
} 