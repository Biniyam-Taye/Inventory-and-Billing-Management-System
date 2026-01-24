package com.inventory.common;

import java.io.Serializable;
import java.util.Date;

public class SaleRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int productId;
    private String productName;
    private int quantity;
    private double totalPrice;
    private Date saleDate;

    public SaleRecord(int id, int productId, String productName, int quantity, double totalPrice, Date saleDate) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.saleDate = saleDate;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Date getSaleDate() {
        return saleDate;
    }
}
