package com.ncr.eft.data;

import java.util.HashMap;
import java.util.Map;

public enum ComStopBits {
    None(0),
    One(1),
    Two(2),
    OnePointFive(3);

    private static final Map<Integer, ComStopBits> BY_LABEL = new HashMap<Integer, ComStopBits>();

    static {
        for (ComStopBits e: values()) {
            BY_LABEL.put(e.value, e);
        }
    }

    public final int value;

    private ComStopBits(int value) {
        this.value = value;
    }

    public static ComStopBits valueOfLabel(int value) {
        return BY_LABEL.get(value);
    }
}
