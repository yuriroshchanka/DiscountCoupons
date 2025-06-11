package com.discount.service;

import com.discount.model.Coupon;
import com.discount.model.CouponUsage;
import com.discount.repository.CouponRepository;
import com.discount.repository.CouponUsageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final GeoLocationService geoLocationService;

    public CouponService(CouponRepository couponRepository, 
                        CouponUsageRepository couponUsageRepository,
                        GeoLocationService geoLocationService) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.geoLocationService = geoLocationService;
    }

    @Transactional
    public Coupon createCoupon(String code, Integer maxUses, String country) {
        // Check if coupon with same code (case-insensitive) already exists
        if (couponRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new IllegalStateException("Coupon code already exists");
        }
        
        Coupon coupon = new Coupon(code, maxUses, country);
        return couponRepository.save(coupon);
    }

    @Transactional
    public void useCoupon(String code, String userId, String ipAddress, String country) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        // If country is provided in the request, use it directly
        // Otherwise, try to determine it from IP address
        String userCountry = country != null ? country.toUpperCase() : 
            geoLocationService.getCountryFromIp(ipAddress);

        // Only validate country if the coupon has a country restriction
        if (coupon.getCountry() != null && !coupon.getCountry().isEmpty() && 
            !coupon.getCountry().equalsIgnoreCase(userCountry)) {
            throw new IllegalArgumentException("Coupon is not valid for your country");
        }

        if (!coupon.isAvailable()) {
            throw new IllegalStateException("Coupon has reached maximum uses");
        }

        if (couponUsageRepository.findByCouponIdAndUserId(coupon.getId(), userId).isPresent()) {
            throw new IllegalStateException("User has already used this coupon");
        }

        coupon.incrementUses();
        couponRepository.save(coupon);
        couponUsageRepository.save(new CouponUsage(coupon, userId));
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
    }
} 