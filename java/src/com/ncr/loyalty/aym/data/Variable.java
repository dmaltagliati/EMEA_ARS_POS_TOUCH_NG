package com.ncr.loyalty.aym.data;

public class Variable {
    private String name;
    private String variableValue;

    public Variable() {
    }

    public Variable(String name, String variableValue) {
        this.name = name;
        this.variableValue = variableValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }
}
