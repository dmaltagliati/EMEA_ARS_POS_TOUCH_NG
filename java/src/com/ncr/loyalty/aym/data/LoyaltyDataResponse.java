package com.ncr.loyalty.aym.data;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoyaltyDataResponse implements LoyaltyResponseInterface {
    private int memberId;
    private String programCode;
    private String firstName;
    private String lastName;
    private String suffix;
    private String countryCode;
    @SerializedName("DOB")
    private Date dob;
    private String email;
    private String gender;
    private String enrollmentLocationCode;
    private String idTypeCode;
    private String accountIdValue;
    private boolean isActive;
    @SerializedName("PointBalance")
    private BigDecimal pointsBalance;
    private Date enrollmentDate;
    private List<IdType> idTypes;
    private List<Variable> variables;
    private String returnCode;

    public LoyaltyDataResponse() {
        idTypes = new ArrayList<IdType>();
        variables = new ArrayList<Variable>();
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public BigDecimal getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(BigDecimal pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public List<IdType> getIdTypes() {
        return idTypes;
    }

    public void setIdTypes(List<IdType> idTypes) {
        this.idTypes = idTypes;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }
}
