package com.monk.coupon.entity;

public class CartItem {
	
    private int productId;    // ID of the product
    private int quantity;     // Quantity of the product
    private double price;     // Price per unit of the product
    private double totalDiscount; // Discount applied to this item (if any)
    
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getTotalDiscount() {
		return totalDiscount;
	}
	public void setTotalDiscount(double totalDiscount) {
		this.totalDiscount = totalDiscount;
	}
    
    
}
