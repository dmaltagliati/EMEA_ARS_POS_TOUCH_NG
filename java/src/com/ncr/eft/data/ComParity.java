package com.ncr.eft.data;

import java.util.HashMap;
import java.util.Map;

public enum ComParity {
    None(0),
    Odd(1),
    Even(2),
    Mark(3),
    Space(4);

    private static final Map<Integer, ComParity> BY_LABEL = new HashMap<Integer, ComParity>();

    static {
        for (ComParity e: values()) {
            BY_LABEL.put(e.value, e);
        }
    }

    public final int value;

    private ComParity(int value) {
        this.value = value;
    }

    public static ComParity valueOfLabel(int value) {
        return BY_LABEL.get(value);
    }
}
