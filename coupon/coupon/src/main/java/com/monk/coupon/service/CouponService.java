package com.monk.coupon.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.monk.coupon.entity.Cart;
import com.monk.coupon.entity.CartItem;
import com.monk.coupon.entity.Coupon;
import com.monk.coupon.repository.CouponRepository;

public class CouponService {

	@Autowired
	private CouponRepository couponRepository;

	public Coupon createCoupon(Coupon coupon) {
		return couponRepository.save(coupon);
	}

	public List<Coupon> getAllCoupons() {
		return couponRepository.findAll();
	}

	public Coupon getCouponById(Long id) {
		return couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Coupon not found"));
	}

	public Coupon updateCoupon(Long id, Coupon updatedCoupon) {
		Coupon coupon = getCouponById(id);
		coupon.setType(updatedCoupon.getType());
		coupon.setDetails(updatedCoupon.getDetails());
		coupon.setActive(updatedCoupon.isActive());
		coupon.setExpiryDate(updatedCoupon.getExpiryDate());
		return couponRepository.save(coupon);
	}

	public void deleteCoupon(Long id) {
		couponRepository.deleteById(id);
	}

	public List<Coupon> getApplicableCoupons(Cart cart) {
		List<Coupon> activeCoupons = couponRepository.findByActiveTrueAndExpiryDateAfter(LocalDateTime.now());
		// Apply business logic to filter applicable coupons based on cart
		return activeCoupons.stream().filter(coupon -> isCouponApplicable(cart, coupon)).collect(Collectors.toList());
	}

	private boolean isCouponApplicable(Cart cart, Coupon coupon) {
		Map<String, Object> details = coupon.getDetails();
		switch (coupon.getType()) {
		case CART_WISE:
			double threshold = (double) details.get("threshold");
			double discount = (double) details.get("discount");
			return cart.getTotalPrice() > threshold;

		case PRODUCT_WISE:
			int productId = (int) details.get("product_id");
			return cart.getItems().stream().anyMatch(item -> item.getProductId() == productId);

		case BXGY:
			List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
			List<Map<String, Object>> getProducts = (List<Map<String, Object>>) details.get("get_products");
			int repetitionLimit = (int) details.get("repition_limit");
			return canApplyBxGy(cart, buyProducts, getProducts, repetitionLimit);

		default:
			return false;
		}
	}

	private boolean canApplyBxGy(Cart cart, List<Map<String, Object>> buyProducts,
			List<Map<String, Object>> getProducts, int limit) {
		int repetitionCount = Integer.MAX_VALUE;

		// Calculate max repetitions based on buy products
		for (Map<String, Object> buyProduct : buyProducts) {
			int productId = (int) buyProduct.get("product_id");
			int quantity = (int) buyProduct.get("quantity");

			int available = cart.getItems().stream().filter(item -> item.getProductId() == productId)
					.mapToInt(CartItem::getQuantity).sum();
			repetitionCount = Math.min(repetitionCount, available / quantity);
		}

		// Cap repetition count by the limit
		return repetitionCount > 0 && repetitionCount <= limit;
	}
	
	public Cart applyCoupon(Long couponId, Cart cart) {
	    Coupon coupon = couponRepository.findById(couponId)
	            .orElseThrow(() -> new RuntimeException("Coupon not found"));

	    if (!isCouponApplicable(cart, coupon)) {
	        throw new RuntimeException("Coupon is not applicable");
	    }

	    Map<String, Object> details = coupon.getDetails();
	    double totalDiscount = 0;

	    switch (coupon.getType()) {
	        case CART_WISE:
	            double threshold = (double) details.get("threshold");
	            double discount = (double) details.get("discount");
	            if (cart.getTotalPrice() > threshold) {
	                totalDiscount = cart.getTotalPrice() * (discount / 100);
	            }
	            break;

	        case PRODUCT_WISE:
	            int productId = (int) details.get("product_id");
	            double productDiscount = (double) details.get("discount");
	            for (CartItem item : cart.getItems()) {
	                if (item.getProductId() == productId) {
	                    totalDiscount += item.getQuantity() * item.getPrice() * (productDiscount / 100);
	                }
	            }
	            break;

	        case BXGY:
	            List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
	            List<Map<String, Object>> getProducts = (List<Map<String, Object>>) details.get("get_products");
	            int repetitionLimit = (int) details.get("repition_limit");

	            int repetitions = calculateRepetitions(cart, buyProducts, repetitionLimit);
	            totalDiscount = applyBxGyDiscount(cart, getProducts, repetitions);
	            break;

	        default:
	            break;
	    }

	    cart.setTotalDiscount(totalDiscount);
	    cart.setFinalPrice(cart.getTotalPrice() - totalDiscount);
	    return cart;
	}

	private int calculateRepetitions(Cart cart, List<Map<String, Object>> buyProducts, int limit) {
	    int repetitions = Integer.MAX_VALUE;
	    for (Map<String, Object> buyProduct : buyProducts) {
	        int productId = (int) buyProduct.get("product_id");
	        int quantity = (int) buyProduct.get("quantity");
	        int available = cart.getItems().stream()
	                .filter(item -> item.getProductId() == productId)
	                .mapToInt(CartItem::getQuantity)
	                .sum();
	        repetitions = Math.min(repetitions, available / quantity);
	    }
	    return Math.min(repetitions, limit);
	}

	private double applyBxGyDiscount(Cart cart, List<Map<String, Object>> getProducts, int repetitions) {
	    double discount = 0;
	    for (Map<String, Object> getProduct : getProducts) {
	        int productId = (int) getProduct.get("product_id");
	        int quantity = (int) getProduct.get("quantity") * repetitions;

	        for (CartItem item : cart.getItems()) {
	            if (item.getProductId() == productId) {
	                discount += Math.min(item.getQuantity(), quantity) * item.getPrice();
	            }
	        }
	    }
	    return discount;
	}
	
	public double calculateDiscount(Cart cart, Coupon coupon) {
	    String type = coupon.getType().name();
	    Map<String, Object> details = coupon.getDetails();

	    switch (type) {
	        case "cart-wise":
	            return calculateCartWiseDiscount(cart, details);
	        case "product-wise":
	            return calculateProductWiseDiscount(cart, details);
	        case "bxgy":
	            return calculateBxGyDiscount(cart, details);
	        default:
	            throw new IllegalArgumentException("Unknown coupon type: " + type);
	    }
	}

	private double calculateCartWiseDiscount(Cart cart, Map<String, Object> details) {
	    double threshold = (double) details.get("threshold");
	    double discountPercentage = (double) details.get("discount");
	    double total = cart.getItems().stream()
	            .mapToDouble(item -> item.getQuantity() * item.getPrice())
	            .sum();

	    return total > threshold ? total * (discountPercentage / 100.0) : 0;
	}

	private double calculateProductWiseDiscount(Cart cart, Map<String, Object> details) {
	    int productId = (int) details.get("product_id");
	    double discountPercentage = (double) details.get("discount");

	    return cart.getItems().stream()
	            .filter(item -> item.getProductId() == productId)
	            .mapToDouble(item -> item.getQuantity() * item.getPrice() * (discountPercentage / 100.0))
	            .sum();
	}

	private double calculateBxGyDiscount(Cart cart, Map<String, Object> details) {
	    List<Map<String, Object>> buyProducts = (List<Map<String, Object>>) details.get("buy_products");
	    List<Map<String, Object>> getProducts = (List<Map<String, Object>>) details.get("get_products");
	    int repetitionLimit = (int) details.get("repition_limit");

	    // Calculate how many times the "buy" condition is met
	    int repetitions = calculateRepetitions(cart, buyProducts, repetitionLimit);

	    // Calculate the total discount for the "get" products
	    return repetitions * getProducts.stream()
	            .mapToDouble(getProduct -> {
	                int productId = (int) getProduct.get("product_id");
	                int quantity = (int) getProduct.get("quantity");

	                return cart.getItems().stream()
	                        .filter(item -> item.getProductId() == productId)
	                        .mapToDouble(item -> item.getPrice() * quantity)
	                        .findFirst()
	                        .orElse(0.0);
	            })
	            .sum();
	}
}
