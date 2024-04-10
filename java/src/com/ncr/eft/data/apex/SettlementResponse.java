package com.ncr.eft.data.apex;

import com.google.gson.annotations.SerializedName;

public class SettlementResponse {
    private String code;
    private String description;
    @SerializedName("isApproved")
    private boolean approved;
    private String jsonData;

    public SettlementResponse() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public String toString() {
        return "SettlementResponse{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", approved=" + approved +
                ", jsonData='" + jsonData + '\'' +
                '}';
    }
}
