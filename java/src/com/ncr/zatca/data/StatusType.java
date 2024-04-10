package com.ncr.zatca.data;

public enum StatusType {
    NOT_INITIALIZED ("notInitialized"),
    INITIALIZED ("initialized"),
    OPEN ("open"),
    UNKNOWN ("unknown");

    private String value;

    StatusType(String value) {
        this.value = value;
    }

    public boolean equals(String value) {
        return this.value.equals(value);
    }

    public String getValue() {
        return value;
    }

    public static StatusType getValue(String value) {
        if ("notInitialized".equals(value)) return StatusType.NOT_INITIALIZED;
        if ("initialized".equals(value)) return StatusType.INITIALIZED;
        if ("open".equals(value)) return StatusType.OPEN;
        return StatusType.UNKNOWN;
    }
}
