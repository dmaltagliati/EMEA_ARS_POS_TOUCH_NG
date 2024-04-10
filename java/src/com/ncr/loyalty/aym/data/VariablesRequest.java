package com.ncr.loyalty.aym.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VariablesRequest extends PostRequest {
    private String accountIdValue;
    private String programCode;
    private List<Variable> variables;

    public VariablesRequest() {
        variables = new ArrayList<Variable>();
    }

    public VariablesRequest(String accountIdValue, String programCode, List<Variable> variables) {
        this.accountIdValue = accountIdValue;
        this.programCode = programCode;
        this.variables = variables;
    }

    public String getAccountIdValue() {
        return accountIdValue;
    }

    public void setAccountIdValue(String accountIdValue) {
        this.accountIdValue = accountIdValue;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "VariablesRequest{" +
                "accountIdValue='" + accountIdValue + '\'' +
                ", programCode='" + programCode + '\'' +
                ", variables=" + variables +
                '}';
    }
}
