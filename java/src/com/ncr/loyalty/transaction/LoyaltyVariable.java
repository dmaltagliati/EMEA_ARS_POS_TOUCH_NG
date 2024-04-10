package com.ncr.loyalty.transaction;

public class LoyaltyVariable {
    private String name;
    private String value;
    private boolean updated = false;

    public LoyaltyVariable(String name, String value, boolean updated) {
        this.name = name;
        this.value = value;
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "LoyaltyVariable{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", updated=" + updated +
                '}';
    }
}
