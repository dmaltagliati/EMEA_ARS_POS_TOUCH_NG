package com.ncr.loyalty.aym.data;

import com.google.gson.annotations.SerializedName;

import java.util.*;

public class EnrollmentRequest extends PostRequest {
    private String programCode;
    private String countryCode;
    @SerializedName("DOB")
    private Date dob;
    private String enrollmentLocationCode;
    private String idTypeCode;
    private String accountIdValue;
    private boolean isActive;
    private Date enrollmentDate;
    private List<Variable> variables;

    public EnrollmentRequest() {
    }

    public EnrollmentRequest(String programCode, String countryCode, Date dob, String enrollmentLocationCode, String idTypeCode, String accountIdValue, boolean isActive, Date enrollmentDate) {
        this.programCode = programCode;
        this.countryCode = countryCode;
        this.dob = dob;
        this.enrollmentLocationCode = enrollmentLocationCode;
        this.idTypeCode = idTypeCode;
        this.accountIdValue = accountIdValue;
        this.isActive = isActive;
        this.enrollmentDate = enrollmentDate;
        this.variables = new ArrayList<Variable>();
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getEnrollmentLocationCode() {
        return enrollmentLocationCode;
    }

    public void setEnrollmentLocationCode(String enrollmentLocationCode) {
        this.enrollmentLocationCode = enrollmentLocationCode;
    }

    public String getIdTypeCode() {
        return idTypeCode;
    }

    public void setIdTypeCode(String idTypeCode) {
        this.idTypeCode = idTypeCode;
    }

    public String getAccountIdValue() {
        return accountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        this.accountIdValue = accountIdValue;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }
}
