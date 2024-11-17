package com.monk.coupon.entity;

import java.time.LocalDateTime;
import java.util.Map;

import com.monk.coupon.enume.CouponType;
import com.monk.coupon.utility.JsonConverter;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Coupon {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Enumerated(EnumType.STRING)
	    private CouponType type;

	    @Convert(converter = JsonConverter.class) // Custom converter to handle JSON
	    private Map<String, Object> details;

	    private boolean active;
	    private LocalDateTime expiryDate;
	    
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public CouponType getType() {
			return type;
		}
		public void setType(CouponType type) {
			this.type = type;
		}
		public Map<String, Object> getDetails() {
			return details;
		}
		public void setDetails(Map<String, Object> details) {
			this.details = details;
		}
		public boolean isActive() {
			return active;
		}
		public void setActive(boolean active) {
			this.active = active;
		}
		public LocalDateTime getExpiryDate() {
			return expiryDate;
		}
		public void setExpiryDate(LocalDateTime expiryDate) {
			this.expiryDate = expiryDate;
		}
	    
	    
}
