package com.ncr.loyalty.transaction;

import java.util.HashMap;
import java.util.Map;

public class LoyaltyCustomer extends LoyaltyData {
    private String accountIdValue = "";
    private String firstName = "";
    private String lastName = "";
    private String suffix = "";
    private String card = "";
    private String mobile = "";
    private LoyaltyPoints points;
    private Map<String, LoyaltyVariable> variables;
    private boolean offline;

    public LoyaltyCustomer(String accountIdValue) {
        super();
        this.accountIdValue = accountIdValue;
        points = new LoyaltyPoints();
        variables = new HashMap<String, LoyaltyVariable>();
    }

    public String getAccountIdValue() {
        return accountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        this.accountIdValue = accountIdValue;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public LoyaltyPoints getPoints() {
        return points;
    }

    public void setPoints(LoyaltyPoints points) {
        this.points = points;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public Map<String, LoyaltyVariable> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, LoyaltyVariable> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "LoyaltyCustomer{" +
                "accountIdValue='" + accountIdValue + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", suffix='" + suffix + '\'' +
                ", card='" + card + '\'' +
                ", mobile='" + mobile + '\'' +
                ", points=" + points +
                ", variables=" + variables +
                ", offline=" + offline +
                '}';
    }
}
