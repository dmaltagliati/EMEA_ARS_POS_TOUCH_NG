package com.ncr.ecommerce;

import com.google.gson.*;
import com.ncr.*;
import com.ncr.common.data.AdditionalInfo;
import com.ncr.ecommerce.data.*;
import com.ncr.gui.ClrDlg;
import com.ncr.gui.SelDlg;
import com.ncr.restclient.HttpClientFactory;
import com.ncr.restclient.IHttpClient;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import lombok.Getter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MultivaluedMap;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

import static com.ncr.GdRegis.set_tra_top;

public class ECommerceManager extends Action {
    private static final String ECOMMERCE = "Ecommerce";
    private static final String READY = "Ready";
    private static final String CANCELED = "Canceled";
    private static final String SUSPENDED = "Suspended";
    private static final String ITEM_NOT_FOUND = "Item not found";
    public static final int SUCCESS = 0;
    public static final int ITEM_ERROR = 101;
    public static final int PRINTER_ERROR = 102;
    public static final int PAYMENT_ERROR = 103;
    public static final int OVERFLOW_ERROR = 105;

    private static ECommerceManager instance = null;
    private static final Logger logger = Logger.getLogger(ECommerceManager.class);
    private static final String ECOMMERCE_PROPERTIES = "conf/ecommerce.properties";
    private static final String ENABLED = "enabled";
    private static final String TENDER = "tender.";
    private static final String TENDER_DEFAULT = "tender.Default";
    private static final String PRINTERCOPIESNUMBER = "printedCopiesNumber";
    private static final String SHOW_POPUP = "showPopupOnPOS";
    private static final String FINALIZE = "finalize";
    private static final String LEGACYBEHAVIORTENDER = "legacyBehaviorTender";
    private static final String NOT_SOLD_WARNING = "notSoldWarning";
    private static final String GETBASKETBYSTOREID = "GetBasketbyStoreID";
    private static final String PROVIDERID = "provider-id";
    private static final String TSC_CALCULATE = "TscCalculateSellingPrice";
    private static final String CAN_APPLY_DSC = "canApplyDsc";  //NODSC-CGA#A
    private Boolean basketRetrieved = false;

    private Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    private final Properties props = new Properties();
    private Boolean transactionStarted = false;
    @Getter
    private Basket basket;
    private int indexPrinterLine = 0;
    @Getter
    private boolean enabled = false;
    private boolean isTransactionBasket = false; //ECOMMERCE-CGA#A
    @Getter
    private String authCode = "";
    @Getter
    private String cardNum = "";
    private static long deltaTimer = 0;
    private static long timer = System.currentTimeMillis();
    private long specialAmount = 0;

    public static ECommerceManager getInstance() {
        if (instance == null) {
            instance = new ECommerceManager();
        }
        return instance;
    }

    private ECommerceManager() {
        loadProperties();
    }

    private void loadProperties() {
        try {
            props.load(new FileInputStream(ECOMMERCE_PROPERTIES));
            enabled = Boolean.parseBoolean(props.getProperty(ENABLED, "false"));
            SpecialItemsManager.getInstance().init(props);
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    public boolean updateBasket(Basket basket) {
        logger.debug("Enter");
        boolean result = false;

        try {
            if (basket != null) {
                IHttpClient client = new HttpClientFactory().getClient(props);
                MultivaluedMap<String, String> params = new MultivaluedMapImpl();
                params.add("fromSource", "POS");

                gson = new GsonBuilder().registerTypeAdapter(Basket.class, new BasketSerializer()).create();   //ECOMMERCE-SSAM#A //PORTING-SPINNEYS-ECOMMERCE-CGA#A
                String response = client.post(params, gson.toJson(basket).toString()).getEntity(String.class);

                result = true;
                gson = null;   //ECOMMERCE-SSAM#A //PORTING-SPINNEYS-ECOMMERCE-CGA#A
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }

        logger.debug("Exit - result: " + result);
        return result;
    }

    public boolean updateExtraItem(Item item) {
        logger.debug("Enter");

        List<Item> items = null;
        boolean result = false;

        if (basket != null
                && (basket.getTenders() == null || basket.getTenders().isEmpty())) {   //ECOMMERCE-SSAM#A //PORTING-TSC-ECOMMERCE-CGA#A
            // Checked if not item void
            if ((itm.spf1 & M_VOID) == 0) {
                if (basket.getItems().size() == (basket.getSoldItems().size() + basket.getNotSoldItems().size())) {
                    if (basket.getExtraItems() != null && !basket.getExtraItems().isEmpty()) {
                        items = basket.getExtraItems();
                        items.add(item);
                    } else {
                        items = new ArrayList<Item>();
                        items.add(item);
                    }
                    basket.setExtraItems(items);
                    result = true;
                }
            }
        }

        logger.debug("Exit - result: " + result);
        return result;
    }

    public void savePrinterInfo(String data) {
        if (isEnabled()) {
            if (transactionStarted && basket != null) {
                String prnOldInfo = basket.getReceipt() != null ? basket.getReceipt() : "";
                String prnInfo = prnOldInfo.concat(data);
                basket.setReceipt(prnInfo);
            }
        }
    }

    public Boolean mustPrinterInfo() {
        return !isEnabled();
    }

    public String addPrinterInfo() {
        if (isEnabled()) {
            return props.getProperty("printedCopies." + indexPrinterLine + ".Text");
        }
        return "";
    }

    public void updateItemsVoided(Itemdata item) {
        logger.debug("Enter");
        Item itemToMove = null;

        if (isEnabled()) {
            if (basket != null) {
                // Checked if item void
                if ((item.spf1 & M_VOID) > 0) {
                    for (Item itm : basket.getSoldItems()) {
                        if (itm.getBarcode().trim().compareTo(item.number.trim()) == 0) {
                            itemToMove = itm;
                            break;
                        }
                    }

                    logger.debug("Item voided move from getSoldItems to getNotSoldItems list");

                    if (itemToMove != null) {
                        basket.getSoldItems().remove(itemToMove);
                        basket.getNotSoldItems().add(itemToMove);
                    }
                }
            }
        }
        logger.debug("Exit");
    }

    public int action0(int spec) {
        int sts = 0;

        switch (spec) {
            case 1:
                if (isEnabled()) {
                    checkForNewBasket(editKey(ctl.ckr_nbr, 4), editKey(ctl.reg_nbr, 3), input.pb.trim());  //ECOMMERCE-SSAM#A  //PORTING-SPINNEYS-ECOMMERCE-CGA#A
                }
                break;
            case 2:
                getBasket(null, null, input.pb.substring(8).trim(), null);
                break;
        }
        if (basket != null && basket.getItems() != null) {
            sts = basketExplode();
        }
        return sts;
    }

    public int action1(int spec) {
        int sts = 0;
        enabled = !enabled;

        try {
            //Reloading the S_PLURCD.DAT file if is necessary...
            if (!enabled) {
                lRCD.open(null, "S_PLU" + "RCD" + ".DAT", 0);
            } else {
                if (lRCD != null) {
                    lRCD.close();
                }
            }
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
            sts = -1;
        }

        dspLine.init(Mnemo.getMenu(enabled ? 117 : 118)).show(1);
        return sts;
    }

    private int basketExplode() {
        int sts = 0;
        logger.debug("Enter");
        startBasketTransaction();
        sellBasketItems();
        endBasketTransaction();
        logger.debug("Exit - sts: " + sts);
        return sts;
    }

    private void startBasketTransaction() {
        logger.debug("Enter");

        if (Basket.RETURN.equals(basket.getType())) tra.spf1 = M_TRRTRN;
        basket.reset();
        transactionStarted = true;
        tra.eCommerce = basket.getSource().trim().equals(ECOMMERCE) ? 1 : 2;

        if (!tra.isActive()) set_tra_top();
        printBasketHeader();

        int amt = basket.getTotalAmount() != null ? basket.getTotalAmount().multiply(new BigDecimal(Math.pow(10, tnd[0].dec))).intValue() : 0;

        Itmdc.IDC_write('b', sc_value(tra.spf1), 0, basket.getBasketID(), basket.getItems().size(), amt);
        if (!basket.getCustomerID().trim().isEmpty()) {  //ECOMMERCE-SSAM#A  //PORTING-SPINNEYS-ECOMMERCE-CGA#A
            Action.input.reset(basket.getCustomerID());
            int sts = group[2].action1(0);
            if (sts > 0) {
                logger.debug("CustomerID is not valid - sts: " + sts);
                tra.stat = 1;
            }
        }
        logger.debug("Exit");
    }

    public void printBasketHeader() {
        if (props.getProperty(basket.getSource() + ".Header1") != null) {
            String text = props.getProperty(basket.getSource() + ".Header1");
            prtTitle(text);
            tra.vItems.addElement('X', new Itemdata(text));
        }

        if (props.getProperty(basket.getSource() + ".Header2") != null &&
                props.getProperty(basket.getSource() + ".Header3") != null) {

            String text = rightFill(props.getProperty(basket.getSource() + ".Header" + (basket.RETURN.equals(basket.getType()) ? "3" : "2")), 20, ' ');
            text = text.concat(basket.getBasketID());
            prtLine.init(text).book(3);
            TView.append(' ', 0x00, text, "", "", "", "");
            tra.vItems.addElement('X', new Itemdata(text));
        }
    }

    private void endBasketTransaction() {
        logger.debug("Enter");
        basketRetrieved = false;

        // If there is almost one NoSoldItem in the basket then transaction must be abort...
        if (!basket.getNotSoldItems().isEmpty()) {
            if (!basket.getSource().trim().equals(ECOMMERCE)) {
                //TODO To send a DataNeeded with list of not sold item...
                SelDlg dlg = new SelDlg(Mnemo.getText(109));
                int i = 1;
                for (Item item: basket.getNotSoldItems()) {
                    if (item.getDescription().isEmpty()) {
                        dlg.add(9, editNum(i, 1), " " + item.getBarcode() + " " + ITEM_NOT_FOUND);
                    }
                    else dlg.add(9, editNum(i, 1), " " + item.getBarcode() + " " + item.getDescription());
                    i++;
                }

                dlg.show("PLU");
                return;
            }
            transactionStarted = false;

            basket.setStatus(CANCELED);
            basket.setErrorCode(ITEM_ERROR);
            Action.input.lck = 0xFF;
            group[3].action5(M_CANCEL);
            logger.debug("Transaction state canceled");

        } else {
            Action.input.reset(String.valueOf(Math.abs(tra.bal)));
            group[3].action3(0);

            // Barcode formatting
            String barcode = editNum(ctl.date, 4) + editKey(ctl.reg_nbr, 3) + editNum(ctl.tran, 4);
            basket.setBarcodeID(barcode);
            basket.setTransactionID(GdPsh.getInstance().getUniqueTransactionId());
            basket.setEarnedLoyaltyPoints(tra.pnt);
            basket.setTotalAmount(removeDecimals(new BigDecimal(tra.bal)));

            if (basket.getVersion() >= 1.3) {
                payWithTenderSpinneys();
                resetDataTender(); //ECOMMERCE-SUSPEND-CGA#A
            } else {
                payWithTenderTsc();
            }
        }
        specialAmount = 0;
        logger.debug("Exit");
    }

    private void sellBasketItems() {
        logger.debug("Enter");

        for (Item item : basket.getItems()) {
            int result = 0;

            if (SpecialItemsManager.getInstance().getSpecialItem(item) == null) {
                result = sellItem(item);
                item.setDescription(itm.ecommerceInfo.getDescription());
                itm.ecommerceInfo.setDescription("");
                if (result > 0) {
                    basket.update(item, true);
                } else {
                    // Update price with possible DP active...
                    int coefficient = 1000;
                    Item updItem = new Item(item.getCode(), new BigDecimal(pit.amt + pit.crd).divide(new BigDecimal(coefficient)), item.getQty(), item.getUnitPrice(), item.getBarcode());
                    basket.update(updItem, false);
                }
            } else {
                specialAmount += item.getPrice().multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec))).intValue();
                item.setDescription(itm.text);
                basket.update(item, false);
            }
        }

        if (Boolean.parseBoolean(props.getProperty(NOT_SOLD_WARNING, "false")) && !basket.getNotSoldItems().isEmpty()) {
            GdPos.panel.clearLink(Mnemo.getInfo(136), 1);
        }

        logger.debug("Exit");
    }

    private int sellItem(Item item) {
        logger.debug("Enter");

        int sts = 0;
        input.prompt = "";
        input.reset(item.getBarcode().trim());

        itm = new Itemdata();

        if (item.getUnitPrice() != null) {
            itm.ecommerceInfo.setUnitPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec))).intValue());
        }

        if (item.getPrice() != null) {
            itm.ecommerceInfo.setPrice(item.getPrice().multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec))).intValue());
        }

        if ((sts = group[5].action2(0)) == 0) {
            if (event.spc == 0) {
                //TODO: Check if this is needed
                sts = group[5].action7(1);
            }
        }

        logger.debug("Exit - sts: " + sts);
        return sts;
    }

    public void endOfTransaction() {
        logger.debug("endOfTransaction - Enter");
        logger.debug("tra.mode: " + tra.mode);

        if (SALESTRN_MODES.contains(tra.mode)) {
            printNotSoldList();
            basket.setTerminalID(editKey(ctl.reg_nbr, 3));
            if (basket.getType().trim().equals(Basket.PREPROCESS) && tra.mode != M_CANCEL) {
                basket.setStatus(SUSPENDED);
            }
            else if (tra.mode == M_GROSS) basket.setStatus(READY);
            else if (tra.mode == M_CANCEL) basket.setStatus(CANCELED);
            else if (tra.mode == M_SUSPND) basket.setStatus(SUSPENDED);

            logger.debug("basket.getStatus(): " + basket.getStatus());
            if (basket.getStatus() == READY) {   //ECOMMERCE-SSAM#A //PORTING-SPINNEYS-ECOMMERCE-CGA#A
                int numPrinterCopies = Integer.parseInt(props.getProperty(PRINTERCOPIESNUMBER));
                for (indexPrinterLine = 1; indexPrinterLine <= numPrinterCopies; indexPrinterLine++) {
                    ElJrn.second_cpy(2, ctl.tran, 1);
                }
            }

            basket.setEarnedLoyaltyPoints(tra.pnt);
            updateBasket(basket);
            transactionStarted = false;
            basketRetrieved = false;
            input.reset("");
        }

        logger.debug("endOfTransaction - Exit");
    }

    public boolean abortTransaction() {
        logger.debug("aborting... enabled: " + isEnabled() + " basket: " + basket + " tra.mode: " + tra.mode);
        if (!isEnabled() || basket == null) {
            logger.debug("abort trx false");

            return false;
        }

        if (tra.mode == M_CANCEL) return true;

        tra.mode = M_CANCEL;
        basket.setErrorCode(PRINTER_ERROR);
        basket.setStatus(CANCELED);  //PRINTERR-CGA#A
        group[3].action5(M_CANCEL);
        logger.debug("abort trx true");
        return true;
    }

    private void printNotSoldList() {
        if (!basket.getNotSoldItems().isEmpty()) {
            for (Item item : basket.getNotSoldItems()) {
                logger.debug("Not sold item: " + item.getBarcode());
                if (props.getProperty(basket.getSource() + ".Header4") != null) {
                    Struc.prtLine.init(props.getProperty(basket.getSource() + ".Header4")).onto(21, item.getBarcode()).type(2);   //ECOMMERCE-SSAM#A //PORTING-SPINNEYS-ECOMMERCE-CGA#A
                }
            }

            GdRegis.set_trailer();
            Struc.prtLine.type(2);
            GdRegis.hdr_print();
        }
    }

    private void prtTitle(String text) {
        dspLine.init(text);
        prtDwide(ELJRN + 3, text);
        TView.append('>', 0x00, text, "", "", "", "");
    }

    public void sendHeartBeatMessage() {
        int errorCode = 0;
        deltaTimer += (System.currentTimeMillis() - timer);
        timer = System.currentTimeMillis();

        try {
            if (isEnabled() && Struc.ctl.ckr_nbr != 0) {
                if (GdPos.panel.modal != null) {
                    if (GdPos.panel.modal instanceof ClrDlg) {
                        ClrDlg dlg = (ClrDlg) GdPos.panel.modal;
                        if (dlg.getTitle().toUpperCase(Locale.ENGLISH).contains("PRINTER")) errorCode = 102;
                        //if (dlg.info.text.toUpperCase(Locale.ENGLISH).contains("PRINTER")) errorCode = 102;
                    }
                }

                if (deltaTimer > Long.parseLong(props.getProperty("heartBeatSignal", "5000"))) {
                    deltaTimer = 0;
                    HeartBeatSender.getInstance().send(editKey(ctl.reg_nbr, 3), errorCode);
                }
            }
        } catch (Exception ex) {
            logger.error("Error: ", ex);
        }
    }

    public boolean isLegacyBehavior() {
        String[] values = props.getProperty("legacyBehavior", "").split(";");

        if (!basket.getProviderID().trim().isEmpty()) {
            return ArrayUtils.contains(values, basket.getProviderID());
        }
        return false;
    }

    public BigDecimal addDecimals(BigDecimal amt) {
        return amt.multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec)));
    }

    public BigDecimal removeDecimals(BigDecimal amt) {
        return amt.divide(BigDecimal.valueOf(Math.pow(10, tnd[0].dec)));
    }

    private boolean getBasket(String storeID, String terminal, String id, String providerId) {
        logger.debug("storeID: " + storeID + " terminal: " + terminal + " id: " + id + " providerId: " + providerId);
        boolean status = false;
        IHttpClient client = new HttpClientFactory().getClient(props);
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("type", "Sale");

        if (storeID != null && Boolean.parseBoolean(props.getProperty(GETBASKETBYSTOREID, "false"))) {
            params.add("StoreId", storeID);
        }
        if (terminal != null) params.add("TerminalId", terminal);
        if (id != null) params.add("BasketId", id);
        if (providerId != null) params.add("ProviderID",providerId);
        transactionStarted = false;

        try {
            String response = client.get(params).getEntity(String.class);
            //TEST: String response = "{\"version\":2.0,\"BasketID\":\"7305859622689\",\"Source\":\"Ecommerce\",\"ProviderID\":\"Instashop\",\"Status\":\"Processing\",\"StoreID\":\"0087\",\"TerminalID\":\"005\",\"SecurityCheck\":false,\"CustomerID\":\"\",\"Type\":\"Sale\",\"Receipt\":\"\",\"TotalAmount\":0.0,\"EarnedLoyaltyPoints\":0,\"TransactionId\":null,\"BarcodeId\":null,\"Items\":[{\"Code\":\"258070051037\",\"Qty\":6,\"UnitPrice\":42.0,\"Barcode\":\"258070051037\",\"Price\":51.03},{\"Code\":\"29589180\",\"Qty\":1,\"UnitPrice\":1.0,\"Barcode\":\"29589180\",\"Price\":1.0},{\"Code\":\"234\",\"Qty\":1,\"UnitPrice\":7.0,\"Barcode\":\"234\",\"Price\":7.0}],\"SoldItems\":[],\"NotSoldItems\":[],\"ExtraItems\":[],\"OriginBasketId\":null,\"Tenders\":[{\"Type\":\"InstaOnline\",\"Amount\":59.03,\"AdditionalInfo\":[]}],\"TenderId\":null,\"ErrorCode\":0}";
            //TEST: props.setProperty(ENABLED, "false");
            logger.debug("Response: " + response);
            if (response != null) {
                gson = new GsonBuilder().registerTypeAdapter(Basket.class, new BasketDeserializer()).create();
                JsonObject jsonObject = (new JsonParser()).parse(response).getAsJsonObject();
                basket = gson.fromJson(jsonObject, Basket.class);
                gson = null;
                basketRetrieved = true;
                status = true;
            }
        } catch (Exception e) {
            basket = null;
            basketRetrieved = false;
            logger.error("Error: " + e.getMessage());
        }

        return status;
    }

    public boolean checkForNewBasket(String storeID, String terminal, String id) {
        logger.debug("Enter");

        boolean result = false;
        if (Boolean.parseBoolean(props.getProperty(ENABLED, "false"))) {
            transactionStarted = false;
            String providerId = props.getProperty(PROVIDERID, null);
            result = getBasket(storeID, terminal, id, providerId);
        }

        logger.debug("Exit - result: " + result);
        return result;
    }

    public boolean isTransactionBasket() {
        return isTransactionBasket;
    }

    public void setTransactionBasket(boolean transactionBasket) {
        isTransactionBasket = transactionBasket;
    }

    private void payWithTenderSpinneys() {
        logger.debug("Enter");

        int sts = 0;
        tra.bal += specialAmount;
        itm.amt = itm.pos = tra.bal;

        if (basket.getType().trim().equals(Basket.PREPROCESS)) {
            tra.mode = M_SUSPND;
        }

        if (basket.getTenders() != null && !basket.getTenders().isEmpty()) {
            for (int index = 0; index < basket.getTenders().size(); index++) {
                resetDataTender(); //ECOMMERCE-SUSPEND-CGA#A
                itm.amt = itm.pos = tra.bal;

                String tenderType = props.getProperty(TENDER + basket.getTenders().get(index).getType().trim());
                if (tenderType != null && !tenderType.isEmpty()) {
                    logger.debug("Valid TenderID found: " + tenderType);
                    itm.tnd = Integer.parseInt(tenderType);

                    if (basket.getTenders().get(index).getAmount().compareTo(new BigDecimal(0)) > 0 &&
                            basket.getTenders().get(index).getAmount().multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec))).intValue() <= (tra.amt + tra.dsc_amt)) {

                        logger.debug("The value of itm.amt will be the current amount tender");
                        itm.amt = itm.pos = basket.getTenders().get(index).getAmount().multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec))).intValue();
                    }

                    logger.debug("itm.amt: " + itm.amt);

                    if (basket.getTenders().get(index).getAdditionalInfo() != null
                            && !basket.getTenders().get(index).getAdditionalInfo().isEmpty()) {
                        handleTenderAdditionalInfo(index);
                    }
                    sts = group[7].action2(0);

                    if (sts > 0) {
                        logger.debug("Error occurred into tender payment step: " + sts);
                    } else {
                        itm.number = basket.getBasketID();

                        if (itm.amt > 0) {
                            input.reset(String.valueOf(itm.amt));
                        }

                        if ((index + 1) == basket.getTenders().size()) {
                            logger.debug("Last tender");
                            if (tra.bal > itm.amt) {
                                logger.debug("The balance is greater than the last amount tender");
                                tra.tnd = 0;
                                handleCancelStatus(OVERFLOW_ERROR);

                                logger.debug("Basket status is CANCEL: " + sts);
                                return;
                            }
                        }

                        isTransactionBasket = true;
                        sts = group[7].action3(-1);
                        isTransactionBasket = false;
                    }
                }

                // Setting errorcode only case basket sale...
                if ((sts > 0 && !Basket.RETURN.equals(basket.getType()))) {
                    handleCancelStatus(PAYMENT_ERROR);
                    logger.debug("Basket status is CANCEL: " + sts);
                    return;
                }

                logger.debug("sts: " + sts);
                logger.debug("Payment step complete with success: ");
                basket.setErrorCode(SUCCESS);

                if (tra.amt == 0) {
                    logger.debug("Fully paid transaction ");
                    break;
                }
            }
        } else if (Boolean.parseBoolean(props.getProperty(FINALIZE, "false"))) {
            logger.debug("Using default TenderType configured");
            itm.tnd = Integer.parseInt(props.getProperty(TENDER_DEFAULT));

            if (isLegacyBehavior()) {
                sts = group[7].action2(0);
                if (sts > 0) {
                    logger.debug("Error occurred into commission payment step: " + sts);
                }
            }

            sts = group[7].action3(0);
            if (sts > 0 && !Basket.RETURN.equals(basket.getType())) {
                basket.setErrorCode(103);
                logger.debug("Error occurred into payment step: " + sts);
                return;
            }

            basket.setErrorCode(SUCCESS);
            endOfTransaction();
        }
        logger.debug("Exit");
    }

    private void handleCancelStatus(int codeError) {
        logger.debug("Enter");

        input.reset(String.valueOf(tra.bal));
        basket.setErrorCode(codeError);

        // Calling Abort on POS...
        Action.input.lck = 0xFF;
        int success = group[3].action5(M_CANCEL);
        logger.debug("gdTrans.action5 returns: " + success);

        transactionStarted = false;
        logger.debug("Exit");
    }

    private void handleTenderAdditionalInfo(int index) {
        try {
            List<AdditionalInfo> lstAdditionalInfo = basket.getTenders().get(index).getAdditionalInfo();
            for (int i = 0; i < lstAdditionalInfo.size(); i++) {
                AdditionalInfo addInfo = lstAdditionalInfo.get(i);

                if (addInfo.getKey().equals("type")) {
                    itm.tnd = eftPluginManager.getEftTender(addInfo.getValue(), itm.tnd);
                }
                else if (addInfo.getKey().equals("card-number")) {
                    cardNum = addInfo.getValue();
                }
                else if (addInfo.getKey().equals("auth-code")) {
                    authCode = addInfo.getValue();
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    private void resetDataTender() {
        cardNum = "";
        authCode = "";
    }

    private void payWithTenderTsc() {
        logger.debug("Enter");

        itm.amt = itm.pos = tra.bal;
        if (basket.getTenderId() != null && !basket.getTenderId().trim().isEmpty()) {
            itm.tnd = Integer.parseInt(basket.getTenderId().trim());
        } else if (basket.getTenders() != null
                && props.getProperty(TENDER + basket.getTenders().get(0).toString().trim()) != null) {
            logger.debug("payWithDefaultTenderTsc - Valid TenderType found");
            itm.tnd = Integer.parseInt(props.getProperty(TENDER + basket.getTenders().get(0).toString().trim()));
        } else if (Boolean.parseBoolean(props.getProperty(FINALIZE, "false"))) {
            logger.debug("payWithDefaultTenderTsc - Using default TenderType configured");
            itm.tnd = Integer.parseInt(props.getProperty(TENDER_DEFAULT));
        }

        int sts = group[7].action3(0);

        if (sts > 0 && !Basket.RETURN.equals(basket.getType())) {
            tra.mode = M_CANCEL;
            basket.setErrorCode(PAYMENT_ERROR);
            endOfTransaction();

            Action.input.lck = 0xFF;
            group[3].action5(M_CANCEL);

            logger.debug("Error occurred into payment step - sts: " + sts);
        } else basket.setErrorCode(SUCCESS);

        logger.debug("Exit");
    }

    public void cancelBasket() {
        tra.mode = M_CANCEL;
        basketRetrieved = false;
        basket.setStatus(CANCELED);
        updateBasket(basket);
    }

    public Boolean isBasketRetrieved() {
        return basketRetrieved;
    }

    public boolean isTransactionStarted() {
        return transactionStarted;
    }

    public boolean hidePopup() {
        logger.debug("CHR_NBR : " + Struc.ctl.ckr_nbr);
        return (isEnabled() && !Boolean.parseBoolean(props.getProperty(SHOW_POPUP, "true")) && Struc.ctl.ckr_nbr > 0);
    }

    public boolean noDscOnBasket() {
        logger.debug("basket: " + basket);
        return (!Boolean.parseBoolean(props.getProperty(CAN_APPLY_DSC, "true")) && basket != null && isTransactionStarted());
    }
}
