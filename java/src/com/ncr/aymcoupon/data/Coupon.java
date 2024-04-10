package com.ncr.aymcoupon.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Coupon {

    private int Id;
    private boolean Used;
    private String UsedDate = "";
    private String CouponPromotionName = "";
    private String CouponPromotionCode = "";
    private String CouponExpiry = "";
    private String CouponName = "";

    private String CouponCode = "";
    private String Type = "";
    private double Value;
    private String Validity = "";

    private ArrayList<Locations> Locations = new ArrayList<com.ncr.aymcoupon.data.Locations>();

    private ArrayList<Products> Products = new ArrayList<com.ncr.aymcoupon.data.Products>();


    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public boolean isUsed() {
        return Used;
    }

    public void setUsed(boolean used) {
        Used = used;
    }

    public String getUsedDate() {
        return UsedDate;
    }

    public void setUsedDate(String usedDate) {
        UsedDate = usedDate;
    }

    public String getCouponName() {
        return CouponName;
    }

    public void setCouponName(String couponName) {
        CouponName = couponName;
    }

    public String getCouponCode() {
        return CouponCode;
    }

    public void setCouponCode(String couponCode) {
        CouponCode = couponCode;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }

    public String getValidity() {
        return Validity;
    }

    public void setValidity(String validity) {
        Validity = validity;
    }

    public ArrayList<com.ncr.aymcoupon.data.Locations> getLocations() {
        return Locations;
    }

    public ArrayList<com.ncr.aymcoupon.data.Products> getProducts() {
        return Products;
    }

    public String getCouponPromotionName() {
        return CouponPromotionName;
    }

    public void setCouponPromotionName(String couponPromotionName) {
        CouponPromotionName = couponPromotionName;
    }

    public String getCouponPromotionCode() {
        return CouponPromotionCode;
    }

    public void setCouponPromotionCode(String couponPromotionCode) {
        CouponPromotionCode = couponPromotionCode;
    }

    public String getCouponExpiry() {
        return CouponExpiry;
    }

    public void setCouponExpiry(String couponExpiry) {
        CouponExpiry = couponExpiry;
    }
}
