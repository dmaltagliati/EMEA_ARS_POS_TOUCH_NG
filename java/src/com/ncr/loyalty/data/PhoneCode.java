package com.ncr.loyalty.data;

/**
 * Created by Administrator on 02/12/17.
 */
public class PhoneCode {
    private String country = "";
    private String phoneCode = "";
    private int rangeMin = 0;
    private int rangeMax = 0;
    private boolean trimLeadingZeroes = false;

    public PhoneCode(String country, String phoneCode, int rangeMin, int rangeMax, boolean trimLeadingZeroes) {
        this.country = country;
        this.phoneCode = phoneCode;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.trimLeadingZeroes = trimLeadingZeroes;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public int getRangeMin() {
        return rangeMin;
    }

    public void setRangeMin(int rangeMin) {
        this.rangeMin = rangeMin;
    }

    public int getRangeMax() {
        return rangeMax;
    }

    public void setRangeMax(int rangeMax) {
        this.rangeMax = rangeMax;
    }

    public boolean isTrimLeadingZeroes() {
        return trimLeadingZeroes;
    }

    public void setTrimLeadingZeroes(boolean trimLeadingZeroes) {
        this.trimLeadingZeroes = trimLeadingZeroes;
    }

    @Override
    public String toString() {
        return "PhoneCode{" +
                "country='" + country + '\'' +
                ", phoneCode='" + phoneCode + '\'' +
                ", rangeMin=" + rangeMin +
                ", rangeMax=" + rangeMax +
                ", trimLeadingZeroes=" + trimLeadingZeroes +
                '}';
    }
}
