package com.ncr.loyalty.aym.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class BearerToken implements LoyaltyResponseInterface {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("expires_in")
    private String expiresIn;
    private String userName;
    @SerializedName(".issued")
    private Date issued;
    @SerializedName(".expires")
    private Date expires;

    public BearerToken() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    @Override
    public String toString() {
        return "BearerToken{" +
                "accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn='" + expiresIn + '\'' +
                ", userName='" + userName + '\'' +
                ", issued=" + issued +
                ", expires=" + expires +
                '}';
    }
}
