package com.ncr.zatca.data;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.ncr.zatca.ZatcaPlugin.*;

@Data
public class ZatcaStatus {
    public static final String OK = "0";
    public static final String ERROR = "1";
    public static final String OFFLINE = "999";

    private String errorCode = "";
    private String errorDescription = "";
    private Map<String, String> sellerInfo = new HashMap<String, String>();
    private StatusType status;
    private String invoiceHash;
    private String qr;

    public boolean isOk() {
        return OK.equals(errorCode);
    }
}
