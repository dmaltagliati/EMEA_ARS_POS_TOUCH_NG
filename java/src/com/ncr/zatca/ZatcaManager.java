package com.ncr.zatca;

import com.ncr.*;
import com.ncr.common.data.TerminalInfo;
import com.ncr.common.utilities.CardReader;
import com.ncr.common.utilities.QrCodeGenerator;
import com.ncr.common.utilities.ReferenceManager;
import com.ncr.ssco.communication.manager.SscoPosManager;
import com.ncr.zatca.data.*;
import com.ncr.zatca.greencore.data.b2b.sap.B2bCustomer;
import com.ncr.zatca.greencore.data.b2b.sap.B2bError;
import com.ncr.zatca.greencore.data.b2b.sap.B2bResponse;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Properties;

import static com.ncr.zatca.ZatcaPlugin.*;

public class ZatcaManager extends Action {
    private static final Logger logger = Logger.getLogger(ZatcaManager.class);
    private final static String ZATCA_PLUGINS_PROPERTIES = "conf/zatca-plugins.properties";
    private final static String ZATCA_PROPERTIES = "conf/zatca.properties";
    private final static String QR_TYPE = "qr.type";
    private final static String QR_IMAGE_WIDTH = "qr.image.width";
    private final static String QR_IMAGE_HEIGHT = "qr.image.height";
    private final static String QR_IMAGE_PATH = "qr.image.path";
    private final static String QR_IMAGE_TYPE = "qr.image.type";
    private final static String ENABLE_ERRROR_PRINT = "error.print.enable";
    private final static String ENABLE_SELLER_PRINT = "seller-info.print.enable";
    private static final String ZATCA_TENDER_TYPE = "tender-type";
    private static final String B2B_RECEIPT = "b2b.receipt.";
    private static final String SERVICE_NAME = "service-name";
    public final static String RBS = "rbs.RbsClientPlugin";
    public final static String GREENCORE = "greencore.LocalAgentPlugin";
    public final static String SAP_B2B = "greencore.B2bSapPlugin";
    private static final String PROMOTION = "Promotion";
    private static final String DISCOUNT = "Discount";
    private static ZatcaManager instance = null;
    private Properties props = new Properties();
    private ZatcaPlugin zatcaPlugin;
    private ZatcaPlugin zatcaB2bPlugin;
    private TerminalInfo terminalInfo;
    private boolean enabled;
    private boolean b2bEnabled;
    private InvoiceData invoice;
    private ZatcaStatus currentStatus;
    private boolean b2bTransaction;
    private B2bCustomer b2bCustomer;
    private ZatcaB2bStatus b2bStatus;
    public static final BigDecimal decimals = BigDecimal.valueOf(Math.pow(10, tnd[0].dec));

    private ZatcaManager() {
    }

    public static ZatcaManager getInstance() {
        if (instance == null)
            instance = new ZatcaManager();

        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isB2bEnabled() {
        return b2bEnabled;
    }

    public void setB2bEnabled(boolean enabled) {
        this.b2bEnabled = enabled;
    }

    public void init() {
        logger.debug("Enter");
        try {
            FileInputStream input = new FileInputStream(new File(ZATCA_PROPERTIES));
            logger.debug("Loading properties");;
            props.load(new InputStreamReader(input, Charset.forName("UTF-8")));
            enabled = "true".equals(props.getProperty("enabled", "false"));
            logger.debug("Enabled: " + enabled);
            zatcaPlugin = loadZatcaPluginsProperties();
            logger.debug("Plugin: " + zatcaPlugin);
            if (zatcaPlugin == null) setEnabled(false);
            b2bEnabled = "true".equals(props.getProperty("b2b.enabled", "false"));
            logger.debug("B2B Enabled: " + b2bEnabled);
            zatcaB2bPlugin = loadZatcaPluginsProperties("b2b");
            logger.debug("B2B Plugin: " + zatcaB2bPlugin);
            if (zatcaPlugin == null) setB2bEnabled(false);
        } catch (Exception e) {
            logger.error("Error loading Zatca properties", e);
        }
        logger.debug("Initializing Plugin Manager");
        terminalInfo = new TerminalInfo(
                editNum(ctl.sto_nbr, 4),
                editKey(ctl.reg_nbr, 3),
                editNum(ctl.ckr_nbr, 3),
                null);
        currentStatus = new ZatcaStatus();
        if (isEnabled()) zatcaPlugin.init(props, terminalInfo);
        if (isB2bEnabled()) zatcaB2bPlugin.init(props, terminalInfo);
    }

    public int action0(int spec) {
        logger.debug("Enter");
        int sts = 0;
        String[] script = {"net.exe", "/c", "sc", "start", props.getProperty(SERVICE_NAME, "GC Local Agent Service")};
        try {
            Process process = Runtime.getRuntime().exec(script);
        } catch (Exception e) {
            logger.error("Error starting Local Agent: ", e);
        }
        logger.debug("Exit - sts: " + sts);
        return sts;
    }

    public int action1(int spec) {
        logger.debug("Enter - B2B toggle function");
        int sts = 0;
        if (!isB2bEnabled()) return 7;
        if (!b2bTransaction) {
            dspLine.init(Mnemo.getMenu(133)).show(1);
            boolean result = CardReader.readCard(Mnemo.getMenu(133), 112, 8, 12, 0, 0);
            logger.debug("result insert customer card: " + result);

            if (!result) {
                b2bTransaction = true; // Set to false later on
            } else {
                String customerId = input.pb;
                dspLine.init(Mnemo.getText(81)).show(1);
                B2bResponse b2bResponse = zatcaB2bPlugin.getCustomerInfo(customerId);
                if (b2bResponse instanceof B2bCustomer) {
                    logger.debug("Received B2B Customer info");
                    if (!tra.isActive()) GdRegis.set_tra_top();
                    b2bCustomer = (B2bCustomer) b2bResponse;
                    dspLine.init(Mnemo.getText(113)).onto(5, b2bCustomer.getVatRegistrationNumber()).show(1);
                } else {
                    logger.debug("Error in fetching B2B customer");
                    B2bError b2bError = (B2bError) b2bResponse;
                    if (b2bError != null) dspLine.init(b2bError.getReason()).show(1);
                    return 7;
                }
            }
        } else {
            logger.debug("B2B disabled");
            dspLine.init(Mnemo.getMenu(134)).show(1);
        }
        b2bTransaction = !b2bTransaction;
        logger.debug("Exit - sts: " + sts + " b2b: " + b2bTransaction);
        return sts;
    }

    public void zatcaStatus() {
        if (!isEnabled()) return;
        String data = isNotInitialized() ?  "ZAT DIS" : isOffline() ? "ZAT OFF" : "ZATCA";
        panel.dspStatus(5, data, !isNotInitialized(), isOffline());
    }

    public boolean isOffline() {
        return StatusType.UNKNOWN.equals(currentStatus.getStatus());
    }

    public boolean isNotInitialized() {
        return StatusType.NOT_INITIALIZED.equals(currentStatus.getStatus());
    }

    public void checkHealth() {
        logger.debug("Enter");
        if (!isEnabled()) return;
        terminalInfo = new TerminalInfo(
                editNum(ctl.sto_nbr, 4),
                editKey(ctl.reg_nbr, 3),
                editNum(ctl.ckr_nbr, 3),
                null);

        while (true) {
            logger.debug("Checking status");
            currentStatus = zatcaPlugin.getStatus(terminalInfo);
            logger.debug("Showing status");
            zatcaStatus();
            if (SscoPosManager.getInstance().isEnabled()) {
                SscoPosManager.getInstance().sendDataNeeded("ZatcaMessage");
                break;
            } else {
                if (StatusType.UNKNOWN.equals(currentStatus.getStatus())) {
                    if (panel.clearLink(Mnemo.getInfo(160), 5) == 2) break;
                } else if (StatusType.NOT_INITIALIZED.equals(currentStatus.getStatus())) {
                    if (panel.clearLink(Mnemo.getInfo(161), 5) == 2) break;
                    zatcaPlugin.init(props, terminalInfo);
                } else break;
            }
        }
        logger.debug("Exit");
    }

    private boolean isSaleTransaction() {
        return isEnabled() && tra.mode == M_GROSS;
    }

    private boolean isSaleTransactionWithItems() {
        return isSaleTransaction() && tra.cnt != 0;
    }

    public void startInvoice() {
        invoice = new InvoiceData();
        if (!b2bTransaction) b2bCustomer = null;
    }

    public void endInvoice(InvoiceType invoiceType) {
        logger.debug("Enter");
        if (!isSaleTransactionWithItems()) return;
        logger.debug("Filling invoice info");
        if (b2bTransaction && invoiceType == InvoiceType.SIMPLIFIED_INVOICE) invoiceType = InvoiceType.INVOICE;
        fillInvoice(invoiceType);
        if (b2bTransaction) {
            logger.debug("B2B - Sending the invoice");
            sendB2bInvoice();
            tra.special.add(Transact.B2B_INVOICE);
        } else {
            logger.debug("Adding transaction log");
            addTransactionLog();
            logger.debug("Getting QR");
            getQrCode();
            tra.special.add(Transact.SIMPLIFIED_INVOICE);
        }
    }

    private void fillInvoice(InvoiceType invoiceType) {
        invoice.setInvoiceId(editNum(ctl.sto_nbr, 4) + editKey(ctl.reg_nbr, 3) + editNum(ctl.tran, 4));
        invoice.setType(invoiceType);
        if (invoiceType != InvoiceType.SIMPLIFIED_INVOICE && invoiceType != InvoiceType.INVOICE) {
            invoice.setInvoiceReferenceId(tra.getReferenceNumber());
            invoice.setInvoiceReferenceDate(tra.getReferenceDate());
        }
        long amount = Math.abs(tra.amt);

        for (int ind = 0; ind < tra.vItems.size(); ind++) {
            itm = tra.vItems.getElement(ind);
            switch (itm.id) {
                case 'S':
                    InvoiceItem invoiceItem = createItem(itm);
                    invoice.getItems().add(invoiceItem);
                    break;
                case 'T':
                    if (amount <= 0) continue;
                    InvoiceTender invoiceTender = createTender(itm);
                    if (Math.abs(itm.amt) > amount) invoiceTender.setAmount(new BigDecimal(amount).divide(decimals).abs());
                    invoice.getTenders().add(invoiceTender);
                    amount -= Math.abs(itm.amt);
                    break;
            }
        }
        for (int ind = 0; ind < vat.length; ind++) {
            int rec = reg.find(7, 11 + ind);
            if (rec < 1) continue;
            Sales sls = reg.sales[rec - 1][0];
            if (sls.isZero()) continue;
            InvoiceTax invoiceTax = new InvoiceTax();
            invoiceTax.setId(ind);
            invoiceTax.setAmount(new BigDecimal(roundBy(sls.total * vat[ind].rate * 10 / (1000 + vat[ind].rate), 10))
                    .divide(new BigDecimal(Math.pow(10, tnd[0].dec))).abs());
            invoiceTax.setGross(new BigDecimal(sls.total).divide(decimals).abs());
            invoiceTax.setText(vat[ind].text);
            invoiceTax.setRate(new BigDecimal(vat[ind].rate).divide(BigDecimal.TEN));
            invoice.getTaxes().add(invoiceTax);
        }
    }

    private InvoiceTender createTender(Itemdata itm) {
        InvoiceTender invoiceTender = new InvoiceTender();
        invoiceTender.setId(itm.index + 1);
        invoiceTender.setAmount(new BigDecimal(itm.amt).divide(decimals).abs());
        String[] tokens = mapTenderType(itm).split(";");
        invoiceTender.setDescription(tokens[0]);
        invoiceTender.setType(tokens[1]);
        invoiceTender.getAdditionalInfos().addAll(itm.getAdditionalInfos());
        return invoiceTender;
    }

    private String mapTenderType(Itemdata itm) {
        String type = props.getProperty(ZATCA_TENDER_TYPE + "." + tnd[itm.tnd].getType());
        if (type == null) type = props.getProperty(ZATCA_TENDER_TYPE + "." + itm.tnd, "10;Payable");
        return type;
    }

    private InvoiceItem createItem(Itemdata itm) {
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setId(itm.index + 1);
        invoiceItem.setNumber(itm.number);
        invoiceItem.setNegative(itm.amt < 0);
        invoiceItem.setAmount(new BigDecimal(itm.amt).divide(decimals).abs());
        invoiceItem.setPrice(new BigDecimal(itm.price).divide(decimals));
        invoiceItem.setText(itm.text);
        invoiceItem.setAdditionalText(itm.eXline);
        invoiceItem.setQuantity(itm.prm > 0 ? new BigDecimal(itm.dec).divide(new BigDecimal(1000)) : new BigDecimal(itm.qty));
        invoiceItem.setWeightItem(itm.prm > 0);
        invoiceItem.setMeasureUnit(itm.ptyp);
        if (itm.crd != 0) {
            InvoiceDiscount invoiceDiscount = new InvoiceDiscount(PROMOTION, new BigDecimal(-itm.crd).divide(decimals).abs());
            invoiceItem.getDiscounts().add(invoiceDiscount);
        }
        if (itm.dsc != 0) {
            InvoiceDiscount invoiceDiscount = new InvoiceDiscount(DISCOUNT, new BigDecimal(-itm.dsc).divide(decimals).abs());
            invoiceItem.getDiscounts().add(invoiceDiscount);
        }
        if (itm.mmDiscount != 0) {
            InvoiceDiscount invoiceDiscount = new InvoiceDiscount(DISCOUNT, new BigDecimal(-itm.mmDiscount).divide(decimals).abs());
            invoiceItem.getDiscounts().add(invoiceDiscount);
        }

        long tax = roundBy(itm.amt * vat[itm.vat].rate * 10 / (1000 + vat[itm.vat].rate), 10);
        InvoiceTax invoiceTax = new InvoiceTax();
        invoiceTax.setId(itm.vat);
        invoiceTax.setAmount(new BigDecimal(tax).divide(decimals).abs());
        invoiceTax.setGross(new BigDecimal(itm.amt).divide(decimals).abs());
        invoiceTax.setText(vat[itm.vat].text);
        invoiceTax.setRate(new BigDecimal(vat[itm.vat].rate).divide(BigDecimal.TEN));
        invoiceItem.setInvoiceTax(invoiceTax);

        return invoiceItem;
    }

    public void addJournal(String line) {
        if (!isSaleTransaction()) return;
        if (tra.mode == M_GROSS) invoice.getJournal().add(line);
    }

    public void addTransactionLog() {
        if (!isSaleTransaction()) return;
        int nbr = 0;
        for (int rec = 0; lTRA.read(++rec) > 0;) {
            char type = lTRA.pb.charAt(32);
            if (type == 'C')
                if (lTRA.pb.charAt(35) == '9')
                    continue;
            lIDC.onto(0, lTRA.scan(28)).push(editNum(++nbr, 3));
            lIDC.push(lTRA.skip(3).scan(3)).push(editNum(tra.mode, 1));
            lIDC.push(lTRA.pb.substring(++lTRA.index));
            invoice.getTransactionLog().add(lIDC.pb);
        }
    }

    private void sendB2bInvoice() {
        if (!isSaleTransaction()) return;

        b2bStatus = zatcaB2bPlugin.sendInvoice(invoice, b2bCustomer, terminalInfo);
    }

    public int insertReferenceInfo() {
        int sts = 0;
        if (!isEnabled()) return 0;
        if ((sts = ReferenceManager.getInstance().insertReferenceNumber()) >= 0) return sts;
        return ReferenceManager.getInstance().insertReferenceDate();
    }

    public ZatcaStatus getStatus() {
        if (!isEnabled()) return null;
        ZatcaStatus zatcaStatus = zatcaPlugin.getStatus(terminalInfo);
        zatcaStatus();
        logger.debug("Zatca Status:" + zatcaStatus);
        return zatcaStatus;
    }

    private ZatcaPlugin loadZatcaPluginsProperties() {
        return loadZatcaPluginsProperties("");
    }

    private ZatcaPlugin loadZatcaPluginsProperties(String type) {
        String packageName = ZatcaManager.class.getPackage().getName();
        Properties zatcaPluginProperties = new Properties();
        ZatcaPlugin zatcaPlugin = null;

        logger.debug("Reading Zatca plugins properties");
        try {
            zatcaPluginProperties.load(new FileInputStream(ZATCA_PLUGINS_PROPERTIES));

            for (Object key : zatcaPluginProperties.keySet()) {
                String className = (String) key;
                if (className.startsWith(type + "-")) {
                    logger.debug("Type: " + type);
                    className = className.substring(type.length() + 1);
                } else if (!type.isEmpty() || className.contains("-")) continue;
                logger.debug("Plugin name: [" + className + "]");
                if (zatcaPluginProperties.getProperty((String) key, "false").equals("true")) {
                    Class<ZatcaPlugin> zatcaPluginClass = (Class<ZatcaPlugin>) Class.forName(packageName + "." + className);
                    zatcaPlugin = zatcaPluginClass.newInstance();
                    logger.info("Plugin " + className + " enabled");
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error creating plugin", e);
        }
        return zatcaPlugin;
    }

    public void finalization() {
        if (b2bTransaction) {
            printB2bInfo();
            b2BIdc(b2bStatus == null || !b2bStatus.isOk());
            if (b2bStatus != null && b2bStatus.isOk()) {
                logger.debug("B2B ok");
            } else {
                logger.debug("B2B error");
                panel.clearLink(b2bStatus.getMessage(), 1);
            }
            b2bTransaction = false;
            b2bCustomer = null;
            b2bStatus = null;
        } else {
            printQr();
        }
    }

    private void b2BIdc(boolean error) {
        Itmdc.IDC_write('y', error ? 1 : 0, 9, b2bCustomer.getVatRegistrationNumber(), 0, 0);
    }

    private void printB2bInfo() {
        logger.debug("Enter");
        for (int index = 0; index <= 9; index++) {
            String line = props.getProperty(B2B_RECEIPT + index, "");
            logger.debug("B2B line: " + line);
            if (!line.isEmpty()) prtLine.init(line).type(3);
        }
        logger.debug("Exit");
    }

    public void printQr() {
        logger.debug("Enter");
        if (!isSaleTransactionWithItems()) return;
        logger.debug("Current status: " + currentStatus);
        zatcaStatus();
        if (currentStatus.isOk() && currentStatus.getQr() != null && !currentStatus.getQr().isEmpty()) {
            for (int index = 0; index < zqrc_txt.length; index++) {
                if (zqrc_txt[index] != null) {
                    logger.debug("ZQRC: " + zqrc_txt[index]);;
                    if (zqrc_txt[index].contains("@qr.bmp")) {
                        QrCodeGenerator.printQR(zqrc_txt[index], currentStatus.getQr(),
                                props.getProperty(QR_TYPE, QrCodeGenerator.PRINT_AS_BMP),
                                Integer.parseInt(props.getProperty(QR_IMAGE_WIDTH, "380")),
                                Integer.parseInt(props.getProperty(QR_IMAGE_HEIGHT, "380")),
                                props.getProperty(QR_IMAGE_PATH, "bmp"),
                                props.getProperty(QR_IMAGE_TYPE, "bmp"));
                    } else {
                        prtLine.init(zqrc_txt[index]).type(3);
                    }
                }
            }
            printSellerInfo();
        }
        logger.debug("Exit");
    }

    private void printSellerInfo() {
        if ("false".equals(props.getProperty(ENABLE_SELLER_PRINT, "false"))) return;
        prtLine.init("");
        for (String key : currentStatus.getSellerInfo().keySet()) {
            if (currentStatus.getSellerInfo().get(key).isEmpty()) continue;
            if (SELLER_NAME.equals(key)) {
                prtLine.onto(20, editTxt(currentStatus.getSellerInfo().get(key), 20));
            } else if (SELLER_ADDITIONAL_NAME.equals(key)) {
                prtLine.onto(0, editTxt(currentStatus.getSellerInfo().get(key), 20));
            }
        }
        prtLine.book(3);
    }

    private void getQrCode() {
        logger.debug("Enter");

        if (!isEnabled()) return;
        currentStatus = zatcaPlugin.getQR(invoice, terminalInfo);

        logger.debug("Is OK = " + currentStatus.isOk());
        if (!currentStatus.isOk()) {
            logger.warn("Zatca Error: " + currentStatus.getErrorDescription());
            if ("true".equals(props.getProperty(ENABLE_ERRROR_PRINT, "false"))) {
                while (true) {
                    if (SscoPosManager.getInstance().isEnabled()) {
                        SscoPosManager.getInstance().sendDataNeeded("ZatcaMessage");
                        break;
                    } else {
                        if (2 == panel.clearLink(Mnemo.getInfo(147), 0x82)) break;
                    }
                }
                prtBlockDwide(ELJRN + 3, zmsg_txt, 0, 3);
                prtLine.book(3);
            }
        } else {
            for (String key : currentStatus.getSellerInfo().keySet()) {
                if (currentStatus.getSellerInfo().get(key).isEmpty()) continue;
                if (SELLER_NAME.equals(key)) {
                    Itmdc.IDC_write('y', 0, 1, currentStatus.getSellerInfo().get(key), 0, 0);
                } else if (SELLER_ADDITIONAL_NAME.equals(key)) {
                    Itmdc.IDC_write('y', 0, 2, currentStatus.getSellerInfo().get(key), 0, 0);
                } else if (VAT_REGISTRATION_NO.equals(key)) {
                    Itmdc.IDC_write('y', 0, 0, currentStatus.getSellerInfo().get(key), 0, 0);
                }
            }
        }
        logger.debug("Exit");
    }

    public ZatcaStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ZatcaStatus currentStatus) {
        this.currentStatus = currentStatus;
    }
}
