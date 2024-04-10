package com.ncr.zatca.greencore.data.status;

import lombok.Data;

@Data
public class CommunicationStatus {
    private String responseSubmitInvoice;
    private String httpCode;
    private String dateTime;
    private boolean success;
    private String message;
    private ResponseBody responseBody;
}
