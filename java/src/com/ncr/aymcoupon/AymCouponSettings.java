package com.ncr.aymcoupon;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.Properties;

public class AymCouponSettings {

    private static final Logger logger = Logger.getLogger(AymCouponSettings.class);
    private static final String AYMCOUPON_PROPERTIES = "conf/loyalty.properties";

    private Properties props = new Properties();

    private static AymCouponSettings instance;

    public static AymCouponSettings getInstance() {
        if (instance == null) {
            instance = new AymCouponSettings();
        }

        instance.load();

        return instance;
    }


    public void load() {
        try {
            props.load(new FileInputStream(AYMCOUPON_PROPERTIES));
            baseUrl = props.getProperty("url", "");
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    private String baseUrl;

    public boolean isEnable() {
        return Boolean.parseBoolean(props.getProperty("aymCoupon.enabled", "false"));
    }

    public String fetchCouponEndPoint(){
        return baseUrl + props.getProperty("aymCoupon.fetchCouponEndpoint", "/coupon");
    }

    public String validateCouponEndPoint(){
        return baseUrl + props.getProperty("aymCoupon.validateCouponEndpoint", "/validateCoupon");
    }

    public String useCouponEndPoint(){
        return baseUrl + props.getProperty("aymCoupon.useCouponEndpoint", "/coupon");
    }

    public int getTimeOut(){
        return Integer.valueOf(props.getProperty("aymCoupon.timeout", "5000"));
    }

    public String getLocationCode(){
        return props.getProperty("location-code", "111");
    }

    public String getProgramCode(){
        return props.getProperty("loyalty-program-code", "BHCL");
    }

    public String getCouponPattern(){
        return props.getProperty("aymCoupon.pattern", "^981[0-9]{10}$").trim();
    }

    public String getOfflineCode(){
        return props.getProperty("offline.code", "0000").trim();
    }

    public String getOfflineStatus(){
        return props.getProperty("offline.status", "OFFLINE").trim();
    }

}


