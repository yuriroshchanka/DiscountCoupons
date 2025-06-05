package com.discount.repository;

import com.discount.model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    @Query("SELECT cu FROM CouponUsage cu WHERE cu.coupon.id = :couponId AND cu.userId = :userId")
    Optional<CouponUsage> findByCouponIdAndUserId(@Param("couponId") Long couponId, @Param("userId") String userId);
} 