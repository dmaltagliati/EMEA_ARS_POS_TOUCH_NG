package com.ncr.common.data;

public enum PluginActionStatusType {
    Disabled (-1),
    Ok (0),
    GenericError (1),
    SerializationError (2),
    Unknown(9999);

    private int code;

    PluginActionStatusType(int code) {
        this.code = code;
    }

    public boolean equals(int id) {
        return code == id;
    }

    public int getCode() {
        return code;
    }
}
