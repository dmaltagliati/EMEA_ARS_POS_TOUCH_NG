package com.ncr.zatca;

import com.ncr.common.data.TerminalInfo;
import com.ncr.zatca.data.InvoiceData;
import com.ncr.zatca.data.ZatcaB2bStatus;
import com.ncr.zatca.data.ZatcaStatus;
import com.ncr.zatca.greencore.data.b2b.sap.B2bCustomer;
import com.ncr.zatca.greencore.data.b2b.sap.B2bResponse;

import java.util.Properties;

public interface ZatcaPlugin {
    String SELLER_NAME = "SELLER_NAME";
    String SELLER_ADDITIONAL_NAME ="SELLER_ADDITIONAL_NAME";
    String VAT_REGISTRATION_NO = "VAT_REGISTRATION_NO";
    String URL = "url";
    String GET_QR_URI = "get-qr.uri";
    String GET_STATUS_URI = "check-status.uri";
    String INIT_URI = "init.uri";
    String SOD_URI = "start-of-day.uri";
    String EOD_URI = "end-of-day.uri";
    String SI_SELLER_NAME = "seller-info.seller-name";
    String SI_SELLER_ADDITIONAL_NAME = "seller-info.seller-additional-name";
    String SI_VAT_REGISTRATION_ID = "seller-info.vat-registration-id";

    void init(Properties props, TerminalInfo terminalInfo);
    void startOfDay(TerminalInfo terminalInfo);
    void endOfDay(TerminalInfo terminalInfo);
    ZatcaStatus getStatus(TerminalInfo terminalInfo);
    ZatcaStatus getQR(InvoiceData invoiceData, TerminalInfo terminalInfo);
    String getName();

    B2bResponse getCustomerInfo(String customerId);
    ZatcaB2bStatus sendInvoice(InvoiceData invoice, B2bCustomer b2bCustomer, TerminalInfo terminalInfo);
}
