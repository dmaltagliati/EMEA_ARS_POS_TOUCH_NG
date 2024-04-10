package com.ncr.zatca.greencore;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ncr.common.data.TerminalInfo;
import com.ncr.zatca.ZatcaManager;
import com.ncr.zatca.ZatcaPlugin;
import com.ncr.zatca.data.*;
import com.ncr.zatca.greencore.data.InvoiceType;
import com.ncr.zatca.greencore.data.*;
import com.ncr.zatca.greencore.data.b2b.B2bInvoice;
import com.ncr.zatca.greencore.data.b2b.B2bInvoiceLine;
import com.ncr.zatca.greencore.data.b2b.sap.B2bCustomer;
import com.ncr.zatca.greencore.data.b2b.sap.B2bResponse;
import com.ncr.zatca.greencore.data.status.StatusReply;
import com.ncr.zatca.utils.UnirestWrapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ncr.Struc.tnd;

public class LocalAgentPlugin implements ZatcaPlugin {
    private static final Logger logger = Logger.getLogger(LocalAgentPlugin.class);
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    @Getter @Setter
    private Properties props;
    private static final String TAX_ID = "tax-id.";
    private static final String TAX_EXEMPTION_REASON = "tax-exemption-reason.";
    private static final String TAX_EXEMPTION_RC = "tax-exemption-reason-code.";
    private static final String INVOICE_NAME = "invoice-name.";
    private static final String INVOICE_VALUE = "invoice-value.";
    private static final String UNIT_CODE = "unit-code.";
    private static final String INSTRUCTION_NOTE = "instruction-note";
    private static final String DISCOUNT = "discount";
    private static final String ROUNDING = "rounding";
    private static final String STANDARD = "Miscellaneous";
    private static final String STANDARD_RC = "ABK";
    private static final int POSITIVE = 1;
    private static final int NEGATIVE = -1;
    public static final BigDecimal decimals = BigDecimal.valueOf(Math.pow(10, tnd[0].dec));

    @Override
    public void init(Properties props, TerminalInfo terminalInfo) {
        logger.debug("Enter");
        this.props = props;

        String endpoint = props.getProperty(URL, "http://localhost/") + props.getProperty(INIT_URI, "initialize");
        endpoint = endpoint + "?_register=" + terminalInfo.getRegisterId() + "&_store=" + terminalInfo.getStoreId() + "&_sync=true";
        UnirestWrapper.getInstance().postRequest(endpoint, "", props);
    }

    @Override
    public void startOfDay(TerminalInfo terminalInfo) {
        //TODO: Add Start of Day activities
    }

    @Override
    public void endOfDay(TerminalInfo terminalInfo) {
        //TODO: Add End of Day activities
    }

    @Override
    public ZatcaStatus getStatus(TerminalInfo terminalInfo) {
        logger.debug("Enter");
        String endpoint = props.getProperty(URL, "http://localhost/") + props.getProperty(GET_STATUS_URI, "getStatus");
        endpoint = endpoint + "?_register=" + terminalInfo.getRegisterId() + "&_store=" + terminalInfo.getStoreId();
        JsonElement jsonElement = UnirestWrapper.getInstance().getRequest(endpoint, props);
        StatusReply statusReply = gson.fromJson(jsonElement, StatusReply.class);
        ZatcaStatus zatcaStatus = new ZatcaStatus();
        if (statusReply != null) {
            convertStatus(zatcaStatus, statusReply);
        } else {
            offlineStatus(zatcaStatus);
        }
        logger.debug("Exit: " + zatcaStatus);
        return zatcaStatus;
    }

    private void offlineStatus(ZatcaStatus zatcaStatus) {
        zatcaStatus.setStatus(StatusType.UNKNOWN);
        zatcaStatus.setErrorCode(ZatcaStatus.OFFLINE);
        zatcaStatus.getSellerInfo().put(SELLER_NAME, props.getProperty(SI_SELLER_NAME, ""));
        zatcaStatus.getSellerInfo().put(SELLER_ADDITIONAL_NAME, props.getProperty(SI_SELLER_ADDITIONAL_NAME, ""));
        zatcaStatus.getSellerInfo().put(VAT_REGISTRATION_NO, props.getProperty(SI_VAT_REGISTRATION_ID, ""));
    }

    private void convertStatus(ZatcaStatus zatcaStatus, StatusReply statusReply) {
        zatcaStatus.setStatus(StatusType.getValue(statusReply.getStatus()));
        zatcaStatus.setErrorCode("" + statusReply.getReturnCode());
        zatcaStatus.setErrorDescription(statusReply.getErrorDescription());
        zatcaStatus.setQr(statusReply.getQr());
        zatcaStatus.setInvoiceHash(statusReply.getInvoiceHash());
        zatcaStatus.getSellerInfo().put(SELLER_NAME, statusReply.getSellerName1());
        zatcaStatus.getSellerInfo().put(SELLER_ADDITIONAL_NAME, statusReply.getSellerName2());
        zatcaStatus.getSellerInfo().put(VAT_REGISTRATION_NO, statusReply.getVatRegistrationNo());
    }

    @Override
    public ZatcaStatus getQR(InvoiceData invoiceData, TerminalInfo terminalInfo) {
        logger.debug("Enter");
        ZatcaStatus zatcaStatus = getStatus(terminalInfo);
        if (zatcaStatus.getStatus().equals(StatusType.NOT_INITIALIZED)) {
            init(props, terminalInfo);
        }

        Invoice invoice = convertInvoice(invoiceData, false);
        String endpoint = props.getProperty(URL, "http://localhost/") + props.getProperty(GET_QR_URI, "getQr");
        endpoint = endpoint + "?_register=" + terminalInfo.getRegisterId() + "&_store=" + terminalInfo.getStoreId() + "&_cashier=" + terminalInfo.getCashierId();
        String jsonString = gson.toJson(invoice);
        logger.debug("Zatca Transaction: " + jsonString);
        JsonElement jsonElement = UnirestWrapper.getInstance().postRequest(endpoint, jsonString, props);
        StatusReply statusReply = gson.fromJson(jsonElement, StatusReply.class);
        if (statusReply != null) {
            convertStatus(zatcaStatus, statusReply);
            logger.debug("Zatca Status: " + zatcaStatus);
        } else {
            offlineStatus(zatcaStatus);
            logger.debug("Offline Zatca Status: " + zatcaStatus);
        }
        logger.debug("Exit: " + zatcaStatus);
        return zatcaStatus;
    }

    public Invoice convertInvoice(InvoiceData invoiceData, boolean full) {
        invoiceData.setItems(removeNegatives(invoiceData.getItems()));
        fixTooBigDiscounts(invoiceData);
        addTaxesRoundings(invoiceData);

        Invoice invoice = invoiceData.getType() == com.ncr.zatca.data.InvoiceType.INVOICE ? new B2bInvoice() : new Invoice();
        invoice.setId(invoiceData.getInvoiceId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        invoice.setIssueDate(dateFormat.format(new Date()));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        invoice.setIssueTime(timeFormat.format(new Date()));
        invoice.setInvoiceTypeCode(convertInvoiceType(invoiceData));
        if (invoiceData.getInvoiceReferenceId() != null) {
            invoice.setBillingReference(new BillingReference(invoiceData.getInvoiceReferenceId()));
            invoice.setDelivery(new ArrayList<Delivery>());
            Delivery delivery = new Delivery();
            delivery.setActualDeliveryDate(invoiceData.getInvoiceReferenceDate().substring(0, 4)
                    + '-' + invoiceData.getInvoiceReferenceDate().substring(4, 6)
                    + '-' + invoiceData.getInvoiceReferenceDate().substring(6));
            invoice.getDelivery().add(delivery);
        }

        for (InvoiceItem invoiceItem : invoiceData.getItems()) {
            if (full) {
                B2bInvoiceLine b2bInvoiceLine = (B2bInvoiceLine) convertInvoiceLine(invoiceItem, full);
                b2bInvoiceLine.setVat(invoiceItem.getAmount()
                        .subtract(invoiceItem.getSumOfDiscounts())
                        .subtract(invoiceItem.getNetAmount())
                        .add(invoiceItem.getNetSumOfDiscounts()));
                if (invoiceItem.isWeightItem()) b2bInvoiceLine.setWeight(invoiceItem.getQuantity());
                invoice.getInvoiceLine().add(b2bInvoiceLine);
            } else {
                InvoiceLine invoiceLine = convertInvoiceLine(invoiceItem, full);
                invoice.getInvoiceLine().add(invoiceLine);
            }

        }
        TaxTotalWithSubtotals taxTotalWithSubtotals = new TaxTotalWithSubtotals();
        for (InvoiceTax invoiceTax : invoiceData.getTaxes()) {
            TaxSubTotal taxSubTotal = convertInvoiceTax(invoiceTax);
            taxTotalWithSubtotals.getTaxSubTotal().add(taxSubTotal);
        }
        taxTotalWithSubtotals.setTaxAmount(new Amount(invoiceData.getVatAmount()));
        invoice.getTaxTotal().add(taxTotalWithSubtotals);

        TaxTotal taxTotal = new TaxTotal();
        taxTotal.setTaxAmount(new Amount(invoiceData.getVatAmount()));
        invoice.getTaxTotal().add(taxTotal);

        convertInvoiceDiscounts(invoiceData, invoice);

        invoice.setLegalMonetaryTotal(convertTotals(invoiceData));

        List<String> usedCodes = new ArrayList<String>();
        for (InvoiceTender invoiceTender : invoiceData.getTenders()) {
            PaymentMeans paymentMeans = new PaymentMeans();
            paymentMeans.setPaymentMeansCode(invoiceTender.getDescription());
            if (invoiceData.getInvoiceReferenceId() != null) {
                paymentMeans.setInstructionNote(props.getProperty(INSTRUCTION_NOTE, ""));
            }
            if (!usedCodes.contains(paymentMeans.getPaymentMeansCode())) {
                invoice.getPaymentMeans().add(paymentMeans);
                usedCodes.add(paymentMeans.getPaymentMeansCode());
            }
        }
        return invoice;
    }

    private void convertInvoiceDiscounts(InvoiceData invoiceData, Invoice invoice) {
        for (InvoiceDiscount invoiceDiscount : invoiceData.getDiscounts()) {
            AllowanceCharge allowanceCharge = convertInvoiceDiscount(invoiceDiscount, invoiceData.getTaxes());
            invoice.getAllowanceCharge().add(allowanceCharge);
        }

        if (invoiceData.getDiscounts().isEmpty()) {
            InvoiceDiscount invoiceDiscount = new InvoiceDiscount(DISCOUNT, BigDecimal.ZERO);
            AllowanceCharge allowanceCharge = convertInvoiceDiscount(invoiceDiscount, invoiceData.getTaxes());
            invoice.getAllowanceCharge().add(allowanceCharge);
        }
    }

    private List<InvoiceItem> removeNegatives(List<InvoiceItem> items) {
        Map<String, List<InvoiceItem>> itemsMap = new HashMap<String, List<InvoiceItem>>();
        List<InvoiceItem> newList = new ArrayList<InvoiceItem>();

        createItemsMap(items, itemsMap);
        for (List<InvoiceItem> itemsList: itemsMap.values()) {
            newList.addAll(innerRemoveNegatives(itemsList));
        }
        return newList;
    }

    private void createItemsMap(List<InvoiceItem> items, Map<String, List<InvoiceItem>> itemsMap) {
        logger.debug("Enter");
        for (InvoiceItem invoiceItem : items) {
            if (!itemsMap.containsKey(invoiceItem.getNumber())) {
                itemsMap.put(invoiceItem.getNumber(), new ArrayList<InvoiceItem>());
            }
            itemsMap.get(invoiceItem.getNumber()).add(invoiceItem);
        }
        logger.debug("Exit");
    }

    private List<InvoiceItem> innerRemoveNegatives(List<InvoiceItem> items) {
        logger.debug("Enter");
        List<InvoiceItem> newList = new ArrayList<InvoiceItem>();
        for (int index = items.size() - 1; index >= 0; index--) {
            InvoiceItem item = items.get(index);
            if (item.isNegative()) {
                //First loop to search exactly the same quantity
                removeSameQuantity(items, index, item);
                //Second loop to remove quantities when different
                subtractQuantities(items, item, index);
            }
        }

        for (InvoiceItem invoiceItem : items) {
            if (!invoiceItem.getQuantity().equals(BigDecimal.ZERO)) {
                newList.add(invoiceItem);
            }
        }
        logger.debug("Exit. NewList size: " + newList.size());
        return newList;
    }

    private void subtractQuantities(List<InvoiceItem> items, InvoiceItem item, int index) {
        logger.debug("Enter");
        if (!item.getQuantity().equals(BigDecimal.ZERO)) {
            for (int inner = index; inner >= 0; inner--) {
                InvoiceItem innerItem = items.get(inner);
                if (!innerItem.isNegative() && !innerItem.getQuantity().equals(BigDecimal.ZERO)) {
                    if (item.getQuantity().compareTo(innerItem.getQuantity()) > 0) {
                        logger.debug("Found lower quantity: " + item.getQuantity() + " inner: " + innerItem.getQuantity());
                        item.setQuantity(item.getQuantity().subtract(innerItem.getQuantity()));
                        item.setAmount(item.getAmount().subtract(innerItem.getAmount()));
                        item.fixTax();
                        innerItem.setQuantity(BigDecimal.ZERO);
                        innerItem.setAmount(BigDecimal.ZERO);
                    } else {
                        logger.debug("Found higher or same quantity: " + item.getQuantity() + " inner: " + innerItem.getQuantity());
                        innerItem.setQuantity(innerItem.getQuantity().subtract(item.getQuantity()));
                        innerItem.setAmount(innerItem.getAmount().subtract(item.getAmount()));
                        innerItem.fixTax();
                        item.setQuantity(BigDecimal.ZERO);
                        item.setAmount(BigDecimal.ZERO);
                        break;
                    }
                }
            }
        }
        logger.debug("Exit");
    }

    private void removeSameQuantity(List<InvoiceItem> items, int index, InvoiceItem item) {
        logger.debug("Enter");
        for (int inner = index; inner >= 0; inner--) {
            InvoiceItem innerItem = items.get(inner);
            if (!innerItem.isNegative()
                    && innerItem.getNetAmount().compareTo(item.getNetAmount()) == 0
                    && innerItem.getQuantity().compareTo(item.getQuantity()) == 0) {
                logger.debug("Found same quantity: " + innerItem.getQuantity());
                innerItem.setQuantity(BigDecimal.ZERO);
                item.setQuantity(BigDecimal.ZERO);
                break;
            }
        }
        logger.debug("Exit");
    }

    private List<InvoiceItem> oldRemoveNegatives(List<InvoiceItem> items) {
        logger.debug("Enter");
        List<Integer> indexes = new ArrayList<Integer>();
        List<InvoiceItem> newList = new ArrayList<InvoiceItem>();

        for (int index = items.size() - 1; index >= 0; index--) {
            InvoiceItem item = items.get(index);
            if (item.isNegative()) {
                for (int inner = index; inner >= 0; inner--) {
                    InvoiceItem innerItem = items.get(inner);
                    if (innerItem.getNumber().equals(item.getNumber())
                            && !innerItem.isNegative()
                            && innerItem.getNetAmount().compareTo(item.getNetAmount()) == 0
                            && innerItem.getQuantity().compareTo(item.getQuantity()) == 0) {
                        indexes.add(index);
                        indexes.add(inner);
                        break;
                    }
                }
            }
        }
        for (int index = items.size() - 1; index >= 0; index--) {
            if (!indexes.contains(index)) {
                newList.add(items.get(index));
            }
        }
        logger.debug("Exit. NewList size: " + newList.size());
        return newList;
    }

    private void fixTooBigDiscounts(InvoiceData invoiceData) {
        for (InvoiceItem invoiceItem : invoiceData.getItems()) {
            BigDecimal sumOfDiscounts = invoiceItem.getNetSumOfDiscounts();

            if (sumOfDiscounts.compareTo(invoiceItem.getNetAmount()) == 1) {
                InvoiceDiscount invoiceDiscount = new InvoiceDiscount(DISCOUNT, sumOfDiscounts);
                invoiceData.getDiscounts().add(invoiceDiscount);
                invoiceItem.getDiscounts().clear();
            }
        }
    }

    private void addTaxesRoundings(InvoiceData invoiceData) {
        for (InvoiceTax invoiceTax : invoiceData.getTaxes()) {
            if (invoiceTax.getRate().compareTo(BigDecimal.ZERO) != 0)
                addTaxRounding(invoiceData, invoiceTax);
        }
    }

    private void addTaxRounding(InvoiceData invoiceData, InvoiceTax invoiceTax) {
        int sum = 0;
        for (InvoiceItem invoiceItem : invoiceData.getItems()) {
            if (invoiceItem.getInvoiceTax().getId() == invoiceTax.getId()) {
                sum = sum + invoiceItem.getNetAmount().subtract(invoiceItem.getNetSumOfDiscounts()).multiply(decimals).intValue();
            }
        }
        //TODO: Warning!!! This works only for a single VAT with rate != 0
        BigDecimal sumOfTax = invoiceTax.getTaxable().add(invoiceData.getInvoiceSumOfDiscounts(invoiceTax));
        int amountToSpread = sum - sumOfTax.multiply(decimals).intValue();

        if (amountToSpread == 0) {
            logger.debug("Nothing to spread");
        } else {
            logger.debug("Spreading. Creating a discount of: " + amountToSpread);
            invoiceData.getDiscounts().add(new InvoiceDiscount(ROUNDING, new BigDecimal(amountToSpread).divide(decimals), invoiceTax.getId()));
        }
    }

    private AllowanceChargeWithTaxCategories convertInvoiceDiscount(InvoiceDiscount invoiceDiscount, List<InvoiceTax> invoiceTaxList) {
        AllowanceChargeWithTaxCategories allowanceCharge = new AllowanceChargeWithTaxCategories();
        allowanceCharge.setAmount(new Amount(invoiceDiscount.getAmount().abs()));
        allowanceCharge.setChargeIndicator(invoiceDiscount.getAmount().compareTo(BigDecimal.ZERO) == -1);
        if (allowanceCharge.isChargeIndicator()) {
            allowanceCharge.setAllowanceChargeReasonCode(STANDARD_RC);
            allowanceCharge.setAllowanceChargeReason(STANDARD);
        } else
            allowanceCharge.setAllowanceChargeReason(invoiceDiscount.getText());
        for (InvoiceTax invoiceTax : invoiceTaxList) {
            if (invoiceDiscount.getIds().isEmpty() || invoiceDiscount.getIds().contains(invoiceTax.getId()))
                allowanceCharge.getTaxCategory().add(getTaxCategory(invoiceTax, false));
        }
        return allowanceCharge;
    }

    private TaxSubTotal convertInvoiceTax(InvoiceTax invoiceTax) {
        TaxSubTotal taxSubTotal = new TaxSubTotal();
        taxSubTotal.setTaxAmount(new Amount(invoiceTax.getAmount()));
        taxSubTotal.setTaxableAmount(new Amount(invoiceTax.getTaxable()));
        TaxCategory taxCategory = getTaxCategory(invoiceTax, true);
        taxSubTotal.setTaxCategory(taxCategory);
        return taxSubTotal;
    }

    private InvoiceLine convertInvoiceLine(InvoiceItem invoiceItem, boolean full) {
        InvoiceLine invoiceLine = full ? new B2bInvoiceLine() : new InvoiceLine();
        Item item = new Item();
        item.setName(!invoiceItem.getAdditionalText().isEmpty() ? invoiceItem.getAdditionalText() : invoiceItem.getText());
        if (full) item.setStandardItemIdentification(new StandardItemIdentification(invoiceItem.getNumber().trim()));

        TaxCategory taxCategory = getTaxCategory(invoiceItem.getInvoiceTax(), false);
        item.setClassifiedTaxCategory(taxCategory);
        invoiceLine.setItem(item);

        invoiceLine.setLineExtensionAmount(new Amount(invoiceItem.getNetAmount().subtract(invoiceItem.getNetSumOfDiscounts())));
        invoiceLine.setInvoicedQuantity(new Quantity(invoiceItem.getQuantity(),
                props.getProperty(UNIT_CODE + invoiceItem.getMeasureUnit(), invoiceItem.getMeasureUnit())));
        invoiceLine.setId(invoiceItem.getId());
        Price price = new Price();
        price.setPriceAmount(new Amount(invoiceItem.getNetPrice(4)));
        invoiceLine.setPrice(price);

        AllowanceCharge allowanceCharge = getAllowanceCharge(invoiceItem);
        invoiceLine.getAllowanceCharge().add(allowanceCharge);

        addInvoiceLineRounding(invoiceLine);

        return invoiceLine;
    }

    private static AllowanceCharge getAllowanceCharge(InvoiceItem invoiceItem) {
        BigDecimal sumOfDiscounts = invoiceItem.getNetSumOfDiscounts();
        AllowanceCharge allowanceCharge = new AllowanceCharge();
        boolean isCharge = sumOfDiscounts.compareTo(BigDecimal.ZERO) == -1;
        allowanceCharge.setChargeIndicator(isCharge);
        if (isCharge) {
            allowanceCharge.setAllowanceChargeReasonCode(STANDARD_RC);
            allowanceCharge.setAllowanceChargeReason(STANDARD);
        } else {
            allowanceCharge.setAllowanceChargeReason(DISCOUNT);
        }
        allowanceCharge.setAmount(new Amount(sumOfDiscounts.abs()));
        return allowanceCharge;
    }

    private void addInvoiceLineRounding(InvoiceLine invoiceLine) {
        BigDecimal amount = invoiceLine.getPrice().getPriceAmount().getValue().multiply(invoiceLine.getInvoicedQuantity().getValue()).setScale(2, RoundingMode.HALF_DOWN);
        BigDecimal discount = invoiceLine.getSignedSumOfAllowances();
        BigDecimal rounding = invoiceLine.getLineExtensionAmount().getValue().subtract(discount).subtract(amount);
        if (rounding.compareTo(BigDecimal.ZERO) != 0) {
            AllowanceCharge allowanceCharge = new AllowanceCharge();
            boolean isCharge = rounding.compareTo(BigDecimal.ZERO) == 1;
            allowanceCharge.setChargeIndicator(isCharge);
            allowanceCharge.setAmount(new Amount(rounding.abs()));
            if (isCharge) {
                allowanceCharge.setAllowanceChargeReasonCode(STANDARD_RC);
                allowanceCharge.setAllowanceChargeReason(STANDARD);
            } else {
                allowanceCharge.setAllowanceChargeReason(ROUNDING);
            }
            invoiceLine.getAllowanceCharge().add(allowanceCharge);
        }
    }

    private TaxCategory getTaxCategory(InvoiceTax invoiceTax, boolean full) {
        TaxCategory taxCategory = new TaxCategory();
        taxCategory.setTaxScheme(TaxScheme.VAT);
        taxCategory.setPercent(invoiceTax.getRate());
        taxCategory.setId(props.getProperty(TAX_ID + invoiceTax.getId(), ""));
        if (full) {
            String value = "";
            if ((value = props.getProperty(TAX_EXEMPTION_REASON + invoiceTax.getId())) != null)
                taxCategory.setTaxExemptionReason(value);
            if ((value = props.getProperty(TAX_EXEMPTION_RC + invoiceTax.getId())) != null)
                taxCategory.setTaxExemptionReasonCode(value);
        }
        return taxCategory;
    }

    private MonetaryTotals convertTotals(InvoiceData invoiceData) {
        MonetaryTotals monetaryTotals = new MonetaryTotals();
        monetaryTotals.setAllowanceTotalAmount(new Amount(invoiceData.getInvoiceSumOfDiscounts(POSITIVE)));
        monetaryTotals.setChargeTotalAmount(new Amount(invoiceData.getInvoiceSumOfDiscounts(NEGATIVE)));
        monetaryTotals.setLineExtensionAmount(new Amount(invoiceData.getNetSumOfItemAmounts()));
        //TODO: Not used so far
        if (invoiceData.getSumOfPrepaidTenders().compareTo(BigDecimal.ZERO) != 0) monetaryTotals.setPrepaidAmount(new Amount(invoiceData.getSumOfPrepaidTenders()));
        monetaryTotals.setPayableAmount(new Amount(invoiceData.getSumOfPayableTenders()));
        monetaryTotals.setTaxExclusiveAmount(new Amount(invoiceData.getGrossAmount().subtract(invoiceData.getVatAmount())));
        monetaryTotals.setTaxInclusiveAmount(new Amount(invoiceData.getGrossAmount()));
        return monetaryTotals;
    }

    private InvoiceType convertInvoiceType(InvoiceData invoiceData) {
        InvoiceType invoiceType = new InvoiceType();
        invoiceType.setName(props.getProperty(INVOICE_NAME + invoiceData.getType().getValue()));
        invoiceType.setValue(props.getProperty(INVOICE_VALUE + invoiceData.getType().getValue()));
        return invoiceType;
    }

    @Override
    public String getName() {
        return ZatcaManager.GREENCORE;
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
