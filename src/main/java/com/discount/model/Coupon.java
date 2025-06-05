package com.discount.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String code;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @NotNull
    @Min(1)
    private Integer maxUses;

    @NotNull
    @Min(0)
    private Integer currentUses;

    @NotBlank
    private String country;

    @Version
    private Long version;

    public Coupon() {
    }

    public Coupon(String code, Integer maxUses, String country) {
        this.code = code.toUpperCase();
        this.maxUses = maxUses;
        this.currentUses = 0;
        this.country = country.toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public Integer getCurrentUses() {
        return currentUses;
    }

    public void setCurrentUses(Integer currentUses) {
        this.currentUses = currentUses;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isAvailable() {
        return currentUses < maxUses;
    }

    public void incrementUses() {
        if (!isAvailable()) {
            throw new IllegalStateException("Coupon has reached maximum uses");
        }
        this.currentUses++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(id, coupon.id) &&
               Objects.equals(code, coupon.code) &&
               Objects.equals(createdAt, coupon.createdAt) &&
               Objects.equals(maxUses, coupon.maxUses) &&
               Objects.equals(currentUses, coupon.currentUses) &&
               Objects.equals(country, coupon.country) &&
               Objects.equals(version, coupon.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, createdAt, maxUses, currentUses, country, version);
    }
}