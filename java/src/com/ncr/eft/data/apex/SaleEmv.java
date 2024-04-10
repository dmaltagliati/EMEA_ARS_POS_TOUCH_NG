package com.ncr.eft.data.apex;

public class SaleEmv {
    private String aid;
    private String tvr;
    private String appName;

    public SaleEmv() {
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String toString() {
        return "SaleEmv{" +
                "aid='" + aid + '\'' +
                ", tvr='" + tvr + '\'' +
                ", appName='" + appName + '\'' +
                '}';
    }
}
