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
} 