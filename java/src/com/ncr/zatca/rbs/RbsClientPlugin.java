package com.ncr.zatca.rbs;

import com.google.gson.*;
import com.ncr.common.data.TerminalInfo;
import com.ncr.zatca.ZatcaManager;
import com.ncr.zatca.ZatcaPlugin;
import com.ncr.zatca.data.*;
import com.ncr.zatca.greencore.data.b2b.sap.B2bCustomer;
import com.ncr.zatca.greencore.data.b2b.sap.B2bResponse;
import com.ncr.zatca.rbs.data.*;
import com.ncr.zatca.utils.UnirestWrapper;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

public class RbsClientPlugin implements ZatcaPlugin {
    private static final Logger logger = Logger.getLogger(RbsClientPlugin.class);
    private static final String TENDER_HEADER = "tender.";
    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private Properties props;

    @Override
    public void init(Properties props, TerminalInfo terminalInfo) {
        this.props = props;

        //TODO: Invoke RBS client init
    }

    @Override
    public void startOfDay(TerminalInfo terminalInfo) {
        //TODO: Invoke RBS client start of day
    }

    @Override
    public void endOfDay(TerminalInfo terminalInfo) {
        //TODO: Invoke RBS client end of day
    }

    private ZatcaStatus buildBasicStatus() {
        ZatcaStatus zatcaStatus = new ZatcaStatus();
        zatcaStatus.getSellerInfo().put(SELLER_NAME, props.getProperty(SI_SELLER_NAME, ""));
        zatcaStatus.getSellerInfo().put(SELLER_ADDITIONAL_NAME, props.getProperty(SI_SELLER_ADDITIONAL_NAME, ""));
        zatcaStatus.getSellerInfo().put(VAT_REGISTRATION_NO, props.getProperty(SI_VAT_REGISTRATION_ID, ""));
        return zatcaStatus;
    }

    @Override
    public ZatcaStatus getStatus(TerminalInfo terminalInfo) {
        logger.debug("Enter");
        String endpoint = props.getProperty(URL, "http://localhost/") + props.getProperty(GET_STATUS_URI, "checkstatus");
        JsonElement jsonElement = UnirestWrapper.getInstance().getRequest(endpoint, props);
        RbsReply rbsReply = gson.fromJson(jsonElement, RbsReply.class);
        ZatcaStatus zatcaStatus = buildBasicStatus();
        if (rbsReply != null) {
            zatcaStatus.setStatus(rbsReply.isHasError() ? StatusType.UNKNOWN : StatusType.OPEN);
            zatcaStatus.setErrorCode(rbsReply.isHasError() ? ZatcaStatus.ERROR : ZatcaStatus.OK);
            zatcaStatus.setErrorDescription(rbsReply.getErrorDesc());
        } else {
            zatcaStatus.setStatus(StatusType.UNKNOWN);
            zatcaStatus.setErrorCode(ZatcaStatus.OFFLINE);
        }
        logger.debug("Exit: " + zatcaStatus);
        return zatcaStatus;
    }

    @Override
    public ZatcaStatus getQR(InvoiceData invoiceData, TerminalInfo terminalInfo) {
        logger.debug("Enter");
        ZatcaStatus zatcaStatus = getStatus(terminalInfo);

//        if (zatcaStatus.isOk()) {
            RbsInvoiceData rbsInvoiceData = convertInvoice(invoiceData);
            String endpoint = props.getProperty(URL, "http://localhost/") + props.getProperty(GET_QR_URI, "getQr");
            logger.debug("Zatca Transaction: " + gson.toJson(rbsInvoiceData));
            JsonElement jsonElement = UnirestWrapper.getInstance().postRequest(endpoint, gson.toJson(rbsInvoiceData), props);
            RbsReply rbsReply = gson.fromJson(jsonElement, RbsReply.class);
            if (rbsReply != null) {
                zatcaStatus.setStatus(rbsReply.isHasError() ? StatusType.UNKNOWN : StatusType.OPEN);
                zatcaStatus.setErrorCode(rbsReply.isHasError() ? ZatcaStatus.ERROR : ZatcaStatus.OK);
                zatcaStatus.setErrorDescription(rbsReply.getErrorDesc());
                zatcaStatus.setQr(rbsReply.getResult());
                logger.debug("Zatca Status: " + zatcaStatus);
            } else {
                zatcaStatus.setStatus(StatusType.UNKNOWN);
                zatcaStatus.setErrorCode(ZatcaStatus.OFFLINE);
            }
//        }
        logger.debug("Exit: " + zatcaStatus);
        return zatcaStatus;
    }

    private RbsInvoiceData convertInvoice(InvoiceData invoiceData) {
        RbsInvoiceData rbsInvoiceData = new RbsInvoiceData();
        rbsInvoiceData.setInvoiceID(invoiceData.getInvoiceId());
        rbsInvoiceData.setInvoiceType(invoiceData.getType().getValue());
        rbsInvoiceData.setInvoiceDateTimeIssue(new Date());
        if (invoiceData.getInvoiceReferenceId() != null) {
            rbsInvoiceData.setInvoiceRef(invoiceData.getInvoiceReferenceId());
            String invoiceDate = invoiceData.getInvoiceReferenceDate();
            rbsInvoiceData.setInvoiceRefDate(invoiceDate.substring(0, 4) + "-" + invoiceDate.substring(4, 6) + "-" + invoiceDate.substring(6));
        }
        rbsInvoiceData.setInvoiceTotalAmountWithVat(invoiceData.getNetAmount().add(invoiceData.getVatAmount()));
        rbsInvoiceData.setInvoiceTotalVatAmount(invoiceData.getVatAmount());
        rbsInvoiceData.setInvoiceTotalAmountWithOutVat(rbsInvoiceData.getInvoiceTotalAmountWithVat()
                .subtract(rbsInvoiceData.getInvoiceTotalVatAmount()));
        rbsInvoiceData.setDocumentDiscount(new RbsInvoiceDiscount(
                invoiceData.getInvoiceSumOfDiscounts().intValue() > 0,
                invoiceData.getInvoiceSumOfDiscounts()));
        for (InvoiceTender tender : invoiceData.getTenders()) {
            RbsInvoiceTender rbsInvoiceTender = convertTender(tender);
            rbsInvoiceData.getTenders().add(rbsInvoiceTender);
        }
        for (InvoiceItem invoiceItem : invoiceData.getItems()) {
            RbsInvoiceItem rbsInvoiceItem = convertItem(invoiceItem);
            rbsInvoiceData.getInvoiceItemList().add(rbsInvoiceItem);
        }
        return rbsInvoiceData;
    }

    private RbsInvoiceItem convertItem(InvoiceItem invoiceItem) {
        RbsInvoiceItem rbsInvoiceItem = new RbsInvoiceItem();
        rbsInvoiceItem.setItemID("" + invoiceItem.getId());
        rbsInvoiceItem.setItemTitle(invoiceItem.getText());
        rbsInvoiceItem.setItemDescription(invoiceItem.getAdditionalText());
        rbsInvoiceItem.setItemQuantity(invoiceItem.getQuantity());
        rbsInvoiceItem.setItemTaxAmount(invoiceItem.getInvoiceTax().getAmount());
        rbsInvoiceItem.setItemPrice(invoiceItem.getAmount().subtract(invoiceItem.getInvoiceTax().getAmount()));
        rbsInvoiceItem.setItemUnit(invoiceItem.getMeasureUnit());
        RbsInvoiceDiscount rbsInvoiceDiscount = new RbsInvoiceDiscount(false, BigDecimal.ZERO);
        for (InvoiceDiscount invoiceDiscount : invoiceItem.getDiscounts()) {
            rbsInvoiceDiscount.setDiscountAmount(rbsInvoiceDiscount.getDiscountAmount().add(invoiceDiscount.getAmount()));
        }
        rbsInvoiceDiscount.setDiscountApplicable(!rbsInvoiceDiscount.getDiscountAmount().equals(BigDecimal.ZERO));
        rbsInvoiceItem.setItemDiscount(rbsInvoiceDiscount);
        return rbsInvoiceItem;
    }

    private RbsInvoiceTender convertTender(InvoiceTender tender) {
        RbsInvoiceTender rbsInvoiceTender = new RbsInvoiceTender();
        rbsInvoiceTender.setType(tender.getDescription());
        rbsInvoiceTender.setAmount(tender.getAmount());
        rbsInvoiceTender.getAdditionalInfos().addAll(tender.getAdditionalInfos());
        return rbsInvoiceTender;
    }

    @Override
    public String getName() {
        return ZatcaManager.RBS;
    }

    @Override
    public B2bResponse getCustomerInfo(String customerId) {
        return null;
    }

    @Override
    public ZatcaB2bStatus sendInvoice(InvoiceData invoice, B2bCustomer b2bCustomer, TerminalInfo terminalInfo) {
        return null;
    }
}
