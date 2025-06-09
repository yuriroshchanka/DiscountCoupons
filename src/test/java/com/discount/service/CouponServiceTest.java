package com.discount.service;

import com.discount.model.Coupon;
import com.discount.model.CouponUsage;
import com.discount.repository.CouponRepository;
import com.discount.repository.CouponUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        testCoupon = new Coupon();
        testCoupon.setCode("TEST123");
        testCoupon.setMaxUses(5);
        testCoupon.setCurrentUses(0);
        testCoupon.setCountry("US");
        testCoupon = couponRepository.save(testCoupon);
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
    void getAllCoupons_ShouldReturnListOfCoupons() {
        // Given
        Coupon anotherCoupon = new Coupon();
        anotherCoupon.setCode("ANOTHER");
        anotherCoupon.setMaxUses(3);
        anotherCoupon.setCurrentUses(0);
        anotherCoupon.setCountry("CA");
        couponRepository.save(anotherCoupon);

        // When
        List<Coupon> coupons = couponService.getAllCoupons();

        // Then
        assertNotNull(coupons);
        assertTrue(coupons.size() >= 2);
        assertTrue(coupons.stream().anyMatch(c -> c.getCode().equals("TEST123")));
        assertTrue(coupons.stream().anyMatch(c -> c.getCode().equals("ANOTHER")));
    }

    @Test
    void getCouponByCode_ShouldReturnCoupon() {
        // When
        Coupon found = couponService.getCouponByCode(testCoupon.getCode());

        // Then
        assertNotNull(found);
        assertEquals(testCoupon.getCode(), found.getCode());
        assertEquals(testCoupon.getMaxUses(), found.getMaxUses());
        assertEquals(testCoupon.getCountry(), found.getCountry());
        assertEquals(testCoupon.getCurrentUses(), found.getCurrentUses());
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

    @Test
    void useCoupon_ShouldIncrementUsesAndCreateUsageRecord() {
        // Given
        String userId = "user123";
        String ipAddress = "192.168.1.1";

        // When
        couponService.useCoupon(testCoupon.getCode(), userId, ipAddress);

        // Then
        Coupon updatedCoupon = couponRepository.findByCodeIgnoreCase(testCoupon.getCode()).orElseThrow();
        assertEquals(1, updatedCoupon.getCurrentUses());
        
        Optional<CouponUsage> usage = couponUsageRepository.findByCouponIdAndUserId(testCoupon.getId(), userId);
        assertTrue(usage.isPresent());
        assertEquals(testCoupon.getId(), usage.get().getCoupon().getId());
        assertEquals(userId, usage.get().getUserId());
    }

    @Test
    void useCoupon_ShouldThrowException_WhenCouponNotFound() {
        // Given
        String invalidCode = "INVALID";
        String userId = "user123";
        String ipAddress = "192.168.1.1";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            couponService.useCoupon(invalidCode, userId, ipAddress)
        );
    }

    @Test
    void useCoupon_ShouldThrowException_WhenMaxUsesReached() {
        // Given
        String userId = "user123";
        String ipAddress = "192.168.1.1";
        testCoupon.setCurrentUses(testCoupon.getMaxUses());
        couponRepository.save(testCoupon);

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            couponService.useCoupon(testCoupon.getCode(), userId, ipAddress)
        );
    }

    @Test
    void useCoupon_ShouldThrowException_WhenUserAlreadyUsedCoupon() {
        // Given
        String userId = "user123";
        String ipAddress = "192.168.1.1";
        CouponUsage usage = new CouponUsage(testCoupon, userId);
        couponUsageRepository.save(usage);

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            couponService.useCoupon(testCoupon.getCode(), userId, ipAddress)
        );
    }
} 