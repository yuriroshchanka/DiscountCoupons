package com.discount.controller;

import com.discount.model.Coupon;
import com.discount.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
public class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCoupon_shouldReturnCreatedCoupon() throws Exception {
        // Arrange
        CouponController.CreateCouponRequest request = 
            new CouponController.CreateCouponRequest("TEST123", 10, "US");
        Coupon expectedCoupon = new Coupon(request.code(), request.maxUses(), request.country());
        
        when(couponService.createCoupon(
            eq(request.code()),
            eq(request.maxUses()),
            eq(request.country())
        )).thenReturn(expectedCoupon);

        // Act & Assert
        mockMvc.perform(post("/api/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(request.code()))
            .andExpect(jsonPath("$.maxUses").value(request.maxUses()))
            .andExpect(jsonPath("$.country").value(request.country()));

        verify(couponService).createCoupon(request.code(), request.maxUses(), request.country());
    }

    @Test
    void createCoupon_shouldReturnBadRequest_whenCodeIsBlank() throws Exception {
        // Arrange
        CouponController.CreateCouponRequest request = 
            new CouponController.CreateCouponRequest("", 10, "US");

        // Act & Assert
        mockMvc.perform(post("/api/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCoupon_shouldReturnBadRequest_whenMaxUsesIsLessThanOne() throws Exception {
        // Arrange
        CouponController.CreateCouponRequest request = 
            new CouponController.CreateCouponRequest("TEST123", 0, "US");

        // Act & Assert
        mockMvc.perform(post("/api/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCoupon_shouldReturnBadRequest_whenCountryIsBlank() throws Exception {
        // Arrange
        CouponController.CreateCouponRequest request = 
            new CouponController.CreateCouponRequest("TEST123", 10, "");

        // Act & Assert
        mockMvc.perform(post("/api/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCoupons_shouldReturnListOfCoupons() throws Exception {
        // Arrange
        List<Coupon> expectedCoupons = Arrays.asList(
            new Coupon("CODE1", 10, "US"),
            new Coupon("CODE2", 5, "UK")
        );
        when(couponService.getAllCoupons()).thenReturn(expectedCoupons);

        // Act & Assert
        mockMvc.perform(get("/api/coupons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("CODE1"))
            .andExpect(jsonPath("$[0].maxUses").value(10))
            .andExpect(jsonPath("$[0].country").value("US"))
            .andExpect(jsonPath("$[1].code").value("CODE2"))
            .andExpect(jsonPath("$[1].maxUses").value(5))
            .andExpect(jsonPath("$[1].country").value("UK"));

        verify(couponService).getAllCoupons();
    }

    @Test
    void getCouponByCode_shouldReturnCoupon() throws Exception {
        // Arrange
        String code = "TEST123";
        Coupon expectedCoupon = new Coupon(code, 10, "US");
        when(couponService.getCouponByCode(code)).thenReturn(expectedCoupon);

        // Act & Assert
        mockMvc.perform(get("/api/coupons/{code}", code))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.maxUses").value(10))
            .andExpect(jsonPath("$.country").value("US"));

        verify(couponService).getCouponByCode(code);
    }

    @Test
    void getCouponByCode_shouldReturnNotFound_whenCouponDoesNotExist() throws Exception {
        // Arrange
        String code = "NONEXISTENT";
        when(couponService.getCouponByCode(code)).thenThrow(new IllegalArgumentException("Coupon not found"));

        // Act & Assert
        mockMvc.perform(get("/api/coupons/{code}", code))
            .andExpect(status().isNotFound());

        verify(couponService).getCouponByCode(code);
    }

    @Test
    void useCoupon_shouldReturnSuccess() throws Exception {
        // Arrange
        String code = "TEST123";
        String userId = "user123";
        String ipAddress = "192.168.1.1";

        // Act & Assert
        mockMvc.perform(post("/api/coupons/{code}/use", code)
                .header("X-User-Id", userId)
                .with(request -> {
                    request.setRemoteAddr(ipAddress);
                    return request;
                }))
            .andExpect(status().isOk());

        verify(couponService).useCoupon(code, userId, ipAddress);
    }

    @Test
    void useCoupon_shouldReturnBadRequest_whenUserIdIsMissing() throws Exception {
        // Arrange
        String code = "TEST123";
        String ipAddress = "192.168.1.1";

        // Act & Assert
        mockMvc.perform(post("/api/coupons/{code}/use", code)
                .with(request -> {
                    request.setRemoteAddr(ipAddress);
                    return request;
                }))
            .andExpect(status().isBadRequest());
    }

    @Test
    void useCoupon_shouldReturnNotFound_whenCouponDoesNotExist() throws Exception {
        // Arrange
        String code = "NONEXISTENT";
        String userId = "user123";
        String ipAddress = "192.168.1.1";
        
        doThrow(new IllegalArgumentException("Coupon not found"))
            .when(couponService).useCoupon(code, userId, ipAddress);

        // Act & Assert
        mockMvc.perform(post("/api/coupons/{code}/use", code)
                .header("X-User-Id", userId)
                .with(request -> {
                    request.setRemoteAddr(ipAddress);
                    return request;
                }))
            .andExpect(status().isNotFound());

        verify(couponService).useCoupon(code, userId, ipAddress);
    }

    @Test
    void useCoupon_shouldReturnConflict_whenMaxUsesReached() throws Exception {
        // Arrange
        String code = "TEST123";
        String userId = "user123";
        String ipAddress = "192.168.1.1";
        
        doThrow(new IllegalStateException("Maximum uses reached"))
            .when(couponService).useCoupon(code, userId, ipAddress);

        // Act & Assert
        mockMvc.perform(post("/api/coupons/{code}/use", code)
                .header("X-User-Id", userId)
                .with(request -> {
                    request.setRemoteAddr(ipAddress);
                    return request;
                }))
            .andExpect(status().isConflict());

        verify(couponService).useCoupon(code, userId, ipAddress);
    }

    @Test
    void useCoupon_shouldReturnConflict_whenUserAlreadyUsedCoupon() throws Exception {
        // Arrange
        String code = "TEST123";
        String userId = "user123";
        String ipAddress = "192.168.1.1";
        
        doThrow(new IllegalStateException("User already used this coupon"))
            .when(couponService).useCoupon(code, userId, ipAddress);

        // Act & Assert
        mockMvc.perform(post("/api/coupons/{code}/use", code)
                .header("X-User-Id", userId)
                .with(request -> {
                    request.setRemoteAddr(ipAddress);
                    return request;
                }))
            .andExpect(status().isConflict());

        verify(couponService).useCoupon(code, userId, ipAddress);
    }
}