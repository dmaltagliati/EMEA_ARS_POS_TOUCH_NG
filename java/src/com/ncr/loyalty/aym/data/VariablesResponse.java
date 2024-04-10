package com.ncr.loyalty.aym.data;

import java.util.ArrayList;
import java.util.List;

public class VariablesResponse {
    private String accountIdValue;
    private String programCode;
    private List<Variable> variables;

    public VariablesResponse() {
        variables = new ArrayList<Variable>();
    }

    public VariablesResponse(String accountIdValue, String programCode, List<Variable> variables) {
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
        return "VariablesResponse{" +
                "accountIdValue='" + accountIdValue + '\'' +
                ", programCode='" + programCode + '\'' +
                ", variables=" + variables +
                '}';
    }
}
