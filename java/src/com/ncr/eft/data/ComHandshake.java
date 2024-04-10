package com.ncr.eft.data;

import java.util.HashMap;
import java.util.Map;

public enum ComHandshake {
    None(0),
    XOnXOff(1),
    RequestToSend(2),
    RequestToSendXOnXOff(3);

    private static final Map<Integer, ComHandshake> BY_LABEL = new HashMap<Integer, ComHandshake>();

    static {
        for (ComHandshake e: values()) {
            BY_LABEL.put(e.value, e);
        }
    }

    public final int value;

    private ComHandshake(int value) {
        this.value = value;
    }

    public static ComHandshake valueOfLabel(int value) {
        return BY_LABEL.get(value);
    }
}
