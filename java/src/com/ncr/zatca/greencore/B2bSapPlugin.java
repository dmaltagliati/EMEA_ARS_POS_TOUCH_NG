package com.ncr.zatca.greencore;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ncr.GdPsh;
import com.ncr.Struc;
import com.ncr.common.data.TerminalInfo;
import com.ncr.common.data.nextgen.Transaction;
import com.ncr.loyalty.aym.data.PostRequest;
import com.ncr.loyalty.transaction.LoyaltyError;
import com.ncr.zatca.ZatcaManager;
import com.ncr.zatca.data.InvoiceData;
import com.ncr.zatca.data.ZatcaB2bStatus;
import com.ncr.zatca.greencore.data.TaxScheme;
import com.ncr.zatca.greencore.data.b2b.*;
import com.ncr.zatca.greencore.data.b2b.sap.B2bCustomer;
import com.ncr.zatca.greencore.data.b2b.sap.B2bError;
import com.ncr.zatca.greencore.data.b2b.sap.B2bResponse;
import com.ncr.zatca.greencore.data.b2b.status.B2bStatusReply;
import com.ncr.zatca.utils.UnirestWrapper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class B2bSapPlugin extends LocalAgentPlugin {
    private static final Logger logger = Logger.getLogger(B2bSapPlugin.class);
    private static final String B2B_SENT_INVOICE = "Invoice sent";
    private static final String B2B_OFFLINE = "SAP Offline";
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private static final String B2B_CUSTOMER_URL = "b2b.customer.url";
    private static final String B2B_INVOICE_URL = "b2b.invoice.url";
    private static final String B2B_SAP_ID = "b2b.sap-client.id";
    public static final String CUSTOMER_FIELD = "CUSTOMER";
    private static final String B2B_FAIL_DIR = "b2b/fail";
    private static final String B2B_SUCCESS_DIR = "b2b/success";
    private static final String INVOICE_HEADER = "invoice_";

    @Override
    public void init(Properties props, TerminalInfo terminalInfo) {
        logger.debug("Enter");
        setProps(props);
    }

    @Override
    public String getName() {
        return ZatcaManager.SAP_B2B;
    }

    @Override
    public B2bResponse getCustomerInfo(String customerId) {
        logger.debug("Enter");
        String endpoint = getProps().getProperty(B2B_CUSTOMER_URL, "http://localhost/");
        endpoint = endpoint + "?sap-client=" + getProps().getProperty(B2B_SAP_ID, "100") + "&customer=" + customerId;
        JsonElement jsonElement = UnirestWrapper.getInstance().getRequest(endpoint, getProps());
        B2bResponse b2bResponse = null;
        if (jsonElement != null) {
            if (jsonElement.getAsJsonObject().has(CUSTOMER_FIELD)) {
                b2bResponse = gson.fromJson(jsonElement, B2bCustomer.class);
            } else {
                b2bResponse = gson.fromJson(jsonElement, B2bError.class);
            }
        }
        logger.debug("Exit: " + b2bResponse);
        return b2bResponse;
    }

    @Override
    public ZatcaB2bStatus sendInvoice(InvoiceData invoiceData, B2bCustomer b2bCustomer, TerminalInfo terminalInfo) {
        ZatcaB2bStatus zatcaB2bStatus = new ZatcaB2bStatus();
        B2bInvoice b2bInvoice = (B2bInvoice) convertInvoice(invoiceData, true);
        b2bInvoice.setAccountingBuyerParty(convertCustomer(b2bCustomer));
        b2bInvoice.setUniqueTransactionId(GdPsh.getInstance().getUniqueTransactionId());
        b2bInvoice.setCashierId(String.valueOf(Struc.ctl.ckr_nbr));
        b2bInvoice.setCustomerId(b2bCustomer.getCustomer());
        String endpoint = getProps().getProperty(B2B_INVOICE_URL, "http://localhost/");
        endpoint = endpoint + "?_register=" + terminalInfo.getRegisterId() + "&_store=" + terminalInfo.getStoreId() + "&_cashier=" + terminalInfo.getCashierId();
        String jsonString = gson.toJson(b2bInvoice);
        logger.debug("Zatca B2B Transaction: " + jsonString);
        JsonElement jsonElement = UnirestWrapper.getInstance().postRequest(endpoint, jsonString, getProps());
        B2bStatusReply b2bStatusReply = gson.fromJson(jsonElement, B2bStatusReply.class);
        if (b2bStatusReply != null) {
            zatcaB2bStatus.setOk(true);
            zatcaB2bStatus.setMessage(B2B_SENT_INVOICE);
            //TODO: Status returned from SAP not specified
            logger.debug("Zatca Status: " + zatcaB2bStatus);
        } else {
            zatcaB2bStatus.setMessage(B2B_OFFLINE);
            logger.debug("Offline Zatca Status: " + zatcaB2bStatus);
        }
        serializeInvoice(b2bInvoice, !zatcaB2bStatus.isOk());
        logger.debug("Exit: " + zatcaB2bStatus);
        return zatcaB2bStatus;
    }

    private boolean serializeInvoice(B2bInvoice invoice, boolean error) {
        File directory = error ? new File(B2B_FAIL_DIR) : new File(B2B_SUCCESS_DIR);

        try {
            if (!directory.exists()) directory.mkdirs();
        } catch (SecurityException e) {
            logger.error("Error creating dir: ", e);
            return false;
        }

        String filename = INVOICE_HEADER + invoice.getUniqueTransactionId() + ".json";
        FileWriter writer = null;
        try {
            writer = new FileWriter(directory + "/" + filename);
            writer.write(gson.toJson(invoice));
        } catch (IOException e) {
            logger.error("Error writing file: ", e);
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Error closing file: ", e);
            }
        }
        return true;
    }

    private AccountingBuyerParty convertCustomer(B2bCustomer b2bCustomer) {
        AccountingBuyerParty accountingBuyerParty = new AccountingBuyerParty();
        Party party = new Party();
        party.setPartyTaxScheme(new PartyTaxScheme(b2bCustomer.getVatRegistrationNumber(), TaxScheme.VAT));

        PostalAddress postalAddress = new PostalAddress();
        postalAddress.setPostalZone(b2bCustomer.getAddressZip());
        postalAddress.setCityName(b2bCustomer.getAddressCity());
        postalAddress.setCountry(new Country(b2bCustomer.getAddressCountryCode()));
        postalAddress.setBuildingNumber("");    //TODO: Check if it is needed and where to get it from
        postalAddress.setStreetName(b2bCustomer.getAddressStreet());
        postalAddress.setCitySubdivisionName("");    //TODO: Check if it is needed and where to get it from
        party.setPostalAddress(postalAddress);

        party.setPartyLegalEntity(new PartyLegalEntity(b2bCustomer.getArabicName() + " | " + b2bCustomer.getEnglishName()));
        accountingBuyerParty.setParty(party);
        return accountingBuyerParty;
    }
}
