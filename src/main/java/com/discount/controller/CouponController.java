package com.discount.controller;

import com.discount.model.Coupon;
import com.discount.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        Coupon coupon = couponService.createCoupon(
            request.code(),
            request.maxUses(),
            request.country()
        );
        return ResponseEntity.ok(coupon);
    }

    @PostMapping("/{code}/use")
    public ResponseEntity<Void> useCoupon(
            @PathVariable String code,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "X-Country", required = false) String country,
            HttpServletRequest request) {
        String ipAddress = forwardedFor != null ? forwardedFor.split(",")[0].trim() : request.getRemoteAddr();
        couponService.useCoupon(code, userId, ipAddress, country);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Coupon> getCouponByCode(@PathVariable String code) {
        return ResponseEntity.ok(couponService.getCouponByCode(code));
    }

    public record CreateCouponRequest(
        @NotBlank String code,
        @Min(1) Integer maxUses,
        @NotBlank String country
    ) {}
} 