package com.monk.coupon.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monk.coupon.entity.Cart;
import com.monk.coupon.entity.Coupon;
import com.monk.coupon.service.CouponService;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/coupons")
public class CouponController {
	
    @Autowired
    private CouponService couponService;

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody @Valid Coupon coupon) {
        return ResponseEntity.ok(couponService.createCoupon(coupon));
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable Long id, @RequestBody @Valid Coupon coupon) {
        return ResponseEntity.ok(couponService.updateCoupon(id, coupon));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/applicable-coupons")
    public ResponseEntity<Map<String, Object>> getApplicableCoupons(@RequestBody Cart cart) {
        List<Coupon> applicableCoupons = couponService.getApplicableCoupons(cart);

        List<Map<String, Object>> response = applicableCoupons.stream()
                .map(coupon -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("coupon_id", coupon.getId());
                    map.put("type", coupon.getType());
                    map.put("discount", couponService.calculateDiscount(cart, coupon));
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("applicable_coupons", response));
    }

    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<Cart> applyCoupon(@PathVariable Long id, @RequestBody Cart cart) {
        Cart updatedCart = couponService.applyCoupon(id, cart);
        return ResponseEntity.ok(updatedCart);
    }
}

