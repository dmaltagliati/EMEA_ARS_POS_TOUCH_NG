package com.ncr.zatca.greencore;

import com.google.gson.*;
import com.greencore.model.*;
import com.ncr.Action;
import com.ncr.restclient.HttpClientFactory;
import com.ncr.restclient.IHttpClient;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class LocalAgentOldPlugin extends Action {

    private static LocalAgentOldPlugin instance = null;
    public static String QR_CODE_IMAGE_PATH;
    public static String QR_CODE_IMAGE_TYPE;
    public static String QR_CODE_IMAGE_NAME;
    public static int QR_Height;
    public static int QR_Width;

    private static final String LA_PROPERTIES = "conf/localAgent.properties";
    private static final Logger logger = Logger.getLogger(LocalAgentOldPlugin.class);
    private Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    private Properties props = new Properties();
    private Boolean isInitialized = false;
    private String store;
    private String register;
    private String sync;
    private List<String> jrn;
    private List<String> dataCollect;
    private TransactionDocument trn;
    public List<TaxValue> taxValues;
    private static String currencySymbol;
    private static boolean laZatca = false;
    private static ResponseTrnDocumentExpand responseQR;
    private static int printQrType = 0;
    private static boolean printQRError = false;

    private static MultivaluedMap<String, String> params;

    public static LocalAgentOldPlugin getInstance() {
        if (instance == null) {
            instance = new LocalAgentOldPlugin();
        }
        return instance;
    }

    private LocalAgentOldPlugin() {
        taxValues = new ArrayList<TaxValue>();
        params = new MultivaluedMapImpl();
        trn = new TransactionDocument();
        loadProperties();
        setStore(props.getProperty("pos.store"));
        setRegister(props.getProperty("pos.register"));
        sync = props.getProperty("asynchronous");
        try {
            printQrType = Integer.parseInt(props.getProperty("printQR.type"));
            printQRError = Boolean.parseBoolean(props.getProperty("printQR.error.flag"));
            if (printQrType == 1) {
                QR_Height = Integer.parseInt(props.getProperty("la.imgQR.height"));
                QR_Width = Integer.parseInt(props.getProperty("la.imgQR.width"));
            }
            if (printQrType == 0) {
                QR_Height = Integer.parseInt(props.getProperty("printQR.height"));
                QR_Width = Integer.parseInt(props.getProperty("printQR.width"));
            }
            QR_CODE_IMAGE_TYPE = props.getProperty("la.imgQR.type");
            QR_CODE_IMAGE_NAME = props.getProperty("la.imgQR.name");
            QR_CODE_IMAGE_PATH = new File(props.getProperty("la.imgQR.path"),
                    QR_CODE_IMAGE_NAME.concat(".").concat(QR_CODE_IMAGE_TYPE)).getPath();
            currencySymbol = tnd[0].symbol;
            laZatca = Boolean.parseBoolean(props.getProperty("la.zatca"));
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        //initialize();
    }

    static String formatDateLA(String date) {
        String reformattedStr = "";
        SimpleDateFormat fromUser = new SimpleDateFormat("yyMMdd");
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            reformattedStr = myFormat.format(fromUser.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reformattedStr;
    }

    static String formatTime(String time) {
        String returnedtime = "";
        String[] timeSplited = new String[3];
        if (time.length() >= 6) {
            timeSplited[0] = time.substring(0, 2);
            timeSplited[1] = time.substring(2, 4);
            timeSplited[2] = time.substring(4, 6);
            returnedtime = StringUtils.join(timeSplited, ":");
        }
        return returnedtime;
    }

    // TODO: GetQR()
    public ResponseTrnDocumentExpand getQr() {
        responseQR = new ResponseTrnDocumentExpand();
        responseQR.setReturnCode(Response.UNRECHEABLE_SERVER);
        String url = props.getProperty("url") + props.getProperty("api.getqr");
        String json = "";
        String qr = "";
        create();
        /*if (!isInitialized) {
            isInitialized();
        }*/
        initialize();
        if (isInitialized) {
            IHttpClient client = new HttpClientFactory().getClient(props, url);
            //MultivaluedMap<String, String> params = new MultivaluedMapImpl();
            /*if (store != null) params.add("_store", store);
            if (register != null) params.add("_register", register);*/
            //SetTaxTotal
            try {
                json = gson.toJson(trn);
                logger.debug("json request" + json.toString());
                String response = client.post(params, json).getEntity(String.class);
                logger.debug("Response String : " + response);
                if (response != null) {
                    responseQR = gson.fromJson(response, ResponseTrnDocumentExpand.class);
                    if (responseQR.getQr() != null && responseQR.getQr().length() > 0) {
                        qr = responseQR.getQr();
                        // generateQRCodeImage(qr, 500, 500);
                    }
                }
           /* } catch (IOException e) {
                logger.error("Error: " + e.getMessage());
            } catch (WriterException e) {
                logger.error("Error: " + e.getMessage());*/
            } catch (Exception e) {
                logger.error("Error: " + e.getMessage());
            }
            logger.debug("Response - Exit " + responseQR);
        }

        return responseQR;

    }

    private void setTaxTotalField() {
        if(taxValues==null || taxValues.size()>0){

        }
        for (int i = 0; i < taxValues.size(); i++) {
            //logger.debug("taxvalue="+taxValues.get(i).getTaxamount()+ "tax="+taxValues.get(i).getPercentuale());
            TaxAmount taxAmount = new TaxAmount();
            //taxAmount.setValue(BigDecimal.valueOf(taxValues.get(i).taxamount).divide(new BigDecimal(100)));
            taxAmount.setValue(BigDecimal.valueOf(removeDecimals(taxValues.get(i).getTaxamount())));
            taxAmount.setCurrencyId(currencySymbol);
            TaxTotal taxtot = new TaxTotal();
            taxtot.setTaxAmount(taxAmount);
            trn.addTaxTotalItem(taxtot);
        }
    }

    public static int getPrintQrType() {
        return printQrType;
    }

    public static void setPrintQrType(int printQrType) {
        LocalAgentOldPlugin.printQrType = printQrType;
    }

    public static Boolean getPrintQRError() {
        return printQRError;
    }

    public static void setPrintQRError(Boolean printQRError) {
        LocalAgentOldPlugin.printQRError = printQRError;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        String propsStoreSt = store;
        int propsStore;
        try {
            propsStore = Integer.parseInt(propsStoreSt);
            this.store = propsStoreSt;
        } catch (Exception e) {
            propsStore = 0;
        }
        if (propsStore == 0) {
            this.store = String.valueOf(ctl.sto_nbr);
        }
        params.add("_store", this.store);
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        String propsRegSt = register;
        int propsReg;
        try {
            propsReg = Integer.parseInt(propsRegSt);
            this.register = propsRegSt;
        } catch (Exception e) {
            propsReg = 0;
        }
        if (propsReg == 0) {
            this.register = String.valueOf(ctl.reg_nbr);
        }
        params.add("_register", this.register);

    }

    // TODO: Create TransactionDocumentObject from POS DATA
    public String addTransactionDocument(TransactionDocument trn) {
        //public String getReceiptQRCode(String store, String register) {
        //TransactionDocument trnOut = new TransactionDocument();
        ResponseTrnDocument res = new ResponseTrnDocument();
        String qr = "";
        // TODO: Call Initialize
        if (!isInitialized) {
            initialize();
        }
        // TODO: Send Data to LocalAgent
        if (isInitialized) {
            IHttpClient client = new HttpClientFactory().getClient(props);
            MultivaluedMap<String, String> params = new MultivaluedMapImpl();
            if (store != null) params.add("_store", store);
            if (register != null) params.add("_register", register);
            try {
                String json = gson.toJson(trn);
                String response = client.post(params, json).getEntity(String.class);
                logger.debug("Response: " + response);
                if (response != null) {
                    JsonObject jsonObject = (new JsonParser()).parse(response).getAsJsonObject();
                    res = gson.fromJson(jsonObject, ResponseTrnDocument.class);
                    if (res.getQr() != null && res.getQr().length() > 0) {
                        qr = res.getQr();
                    }
                }
            } catch (Exception e) {
                logger.error("Error: ", e);
            }
            logger.debug("Response - Exit " + qr);
        }
        return qr;
    }

    public Boolean initialize() {
        String url = props.getProperty("url") + props.getProperty("api.initialize");
        IHttpClient client = new HttpClientFactory().getClient(props, url);
        Response result = new Response();
        //MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        /*if (Integer.parseInt(store) == 0 || store == null)
            store = String.valueOf(ctl.sto_nbr);
        if (Integer.parseInt(register) == 0 || register == null)
            register = String.valueOf(ctl.reg_nbr);*/
        /*params.add("_store", store);
        params.add("_register", register);*/
        // if (store != null) params.add("_store", store);
        //if (register != null) params.add("_register", register);
        if (sync != null) params.add("_sync", sync);
        try {
            String response = client.post(params).getEntity(String.class);
            logger.debug("Response: " + response);
            if (response != null) {
                result = gson.fromJson(response, Response.class);

                if (result.getReturnCode() == 0)
                    isInitialized = true;
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            isInitialized = false;
        }
        return isInitialized;
    }

    public List<String> getJrn() {
        return jrn;
    }

    public void setJrn(List<String> jrn) {
        this.jrn = jrn;
    }

    public List<String> getDataCollect() {
        return dataCollect;
    }

    public void setDataCollect(List<String> dataCollect) {
        this.dataCollect = dataCollect;
    }

    public List<TaxValue> getTaxValues() {
        return taxValues;
    }

    public void setTaxValues(List<TaxValue> taxValues) {
        if (taxValues != null)
            this.taxValues = taxValues;
    }

    public static ResponseTrnDocumentExpand getResponseQR() {
        return responseQR;
    }

    public static void setResponseQR(ResponseTrnDocumentExpand responseQR) {
        LocalAgentOldPlugin.responseQR = responseQR;
    }

    public TransactionDocument getTrn() {
        return trn;
    }

    public void setTrn(TransactionDocument trn) {
        this.trn = trn;
    }

    public boolean isLaZatca() {
        return laZatca;
    }

    public void setLaZatca(boolean laZatca) {
        this.laZatca = laZatca;
    }

    private void loadProperties() {
        try {
            props.load(new FileInputStream(LA_PROPERTIES));
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    public TransactionDocument create() {
        String currencySymbol = tnd[0].symbol;
        String currencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        ValueWithCurrency vwc = new ValueWithCurrency();
        vwc.setCurrencyId((currencySymbol));
        List<String> linesS = new ArrayList<String>();
        List<String> linesC = new ArrayList<String>();
        List<String> linesV = new ArrayList<String>();
        List<String> linesW = new ArrayList<String>();
        List<String> linesT = new ArrayList<String>();
        List<String> linesF = new ArrayList<String>();
        List<String> linesH = new ArrayList<String>();
        List<String> linesU = new ArrayList<String>();

        BufferedReader reader;
        /*try {
            reader = new BufferedReader(new FileReader(
                    "data/S_TRALA" + REG + ".DAT"));
            String line = reader.readLine();
            while (line != null) {
                char type = line.charAt(32);
                line = reader.readLine();
                switch (type) {
                    case 'S':
                        linesS.add(line);
                        break;
                    case 'C':
                        linesC.add(line);
                        break;
                    case 'V':
                        linesV.add(line);
                        break;
                    case 'W':
                        linesW.add(line);
                        break;
                    case 'U':
                        linesU.add(line);
                        break;
                    case 'T':
                        linesT.add(line);
                        break;
                    case 'F':
                        linesF.add(line);
                        break;
                    case 'H':
                        linesH.add(line);
                        break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        logger.debug("CTL.TRAN= "+ctl.tran);
        logger.debug("CTL.Date= "+ctl.date);
        logger.debug("CTL.Time= "+ctl.time);
        logger.debug("formatDateLA gives = "+formatDateLA(String.valueOf(ctl.date)));
        trn.setId(String.valueOf(ctl.tran));
        trn.setIssueDate(formatDateLA(String.valueOf(ctl.date)));
        trn.setIssueTime(formatTime(String.valueOf(editNum(ctl.time,6))));
        //Create InvoiceLine
        List<Line> invoiceLines = new ArrayList<Line>();
        for (int i = 0; i < linesS.size(); i++) {
            Line line = new Line();
            String l = linesS.get(i);
            BasicValue bv = new BasicValue();
            //bv.setValue(Long.parseLong(l.substring(64, 68)) / 10);
            bv.setValue(BigDecimal.valueOf(Long.parseLong(l.substring(64, 68)) / 10));
            line.setInvoicedQuantity(bv);
            line.setId(String.valueOf(i));
            Price price = new Price();
            ValueWithScheme vws = new ValueWithScheme();
            vws.setSchemeId(currencySymbol);
            //vws.setValue();
            price.setPriceAmount(vws);
        }

        //LegalMonetaryTotal
        LegalMonetaryTotal lmtb = new LegalMonetaryTotal();
        //LegalMonetaryTotal/TaxExclusiveAmount

        vwc.setValue(BigDecimal.valueOf(removeDecimals(tra.amt - tra.dsc_amt)));
        lmtb.setTaxExclusiveAmount(vwc);

        //LegalMonetaryTotal/TaxInclusiveAmount
        vwc.setValue(BigDecimal.valueOf(removeDecimals(tra.amt)));
        lmtb.setTaxInclusiveAmount(vwc);
        trn.setLegalMonetaryTotal(lmtb);

        //Invoice Type Code
        ValueName vn = new ValueName();
        vn.setValue(props.getProperty("type.subtype.taxInvoice"));
        vn.setName(props.getProperty("type.subtype.invoice"));
        if ((tra.spf1 & M_TRRTRN) > 0) {
            vn.setName(props.getProperty("type.debitNote"));
        }
        if ((tra.spf1 & M_TRVOID) > 0) {
            vn.setName(props.getProperty("type.creditNote"));
        }
        //Currency
        trn.setDocumentCurrencyCode(currencyCode);
        trn.setTaxCurrencyCode(currencyCode);
        setTaxTotalField();
        return trn;
    }

    public double addDecimals(double amt) {
        return amt * Math.pow(10, tnd[0].dec);
    }

    // TODO: Method removeDecimals  Math.pow(10, tnd[0].dec)
    // Input :amt
    // OutPut:amt
    public double removeDecimals(double amt) {
        return amt / Math.pow(10, tnd[0].dec);
    }

    public static class TaxValue {
        private long taxamount;
        private int percentuale;
        private String VatDesc;

        public TaxValue(long taxamount, int percentuale, String vatDesc) {
            this.taxamount = taxamount;
            this.percentuale = percentuale;
            VatDesc = vatDesc;
        }

        public long getTaxamount() {
            return taxamount;
        }

        public void setTaxamount(long taxamount) {
            this.taxamount = taxamount;
        }

        public int getPercentuale() {
            return percentuale;
        }

        public void setPercentuale(int percentuale) {
            this.percentuale = percentuale;
        }

        public String getVatDesc() {
            return VatDesc;
        }

        public void setVatDesc(String vatDesc) {
            VatDesc = vatDesc;
        }
    }

}
