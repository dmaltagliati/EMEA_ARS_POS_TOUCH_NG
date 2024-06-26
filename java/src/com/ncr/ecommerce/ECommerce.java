package com.ncr.ecommerce;

import com.ncr.Action;
import com.ncr.GdRegis;
import com.ncr.Mnemo;
import com.ncr.ecommerce.data.TenderProperties;
import com.ncr.gui.SelDlg;
import lombok.Getter;
import org.apache.log4j.Logger;
import com.ncr.GdPos;

import java.io.*;
import java.util.*;

public class ECommerce extends Action {
    private static final Logger logger = Logger.getLogger(ECommerce.class);

    private static boolean isAutomaticAmazonItem = false;
    private static boolean isAutomaticVoidAmazonItem = false;
    @Getter
    private static boolean alreadyAmzCommCalc = false;
    private static int amtItem = 0;
    private static final String CHARGE_FILENAME = "conf/charge.properties";
    private static final Properties prop = new Properties();
    @Getter
    private static final Map<Integer, TenderProperties> tenderProperties = new HashMap<Integer, TenderProperties>();
    @Getter
    private static final HashMap<Integer, Integer> amtMaxMap = new HashMap<Integer, Integer>();
    @Getter
    private static final HashMap<Integer, String> instashopChoiceMap = new HashMap<Integer, String>();
    @Getter
    private static final HashMap<Integer, String> ddqCodeMap = new HashMap<Integer, String>();
    @Getter
    private static final TreeMap<Integer, String> cardTypeMap = new TreeMap<Integer, String>();
    @Getter
    private static HashMap<String, Integer> automaticTndOnline = new HashMap<String, Integer>();
    @Getter
    private static HashMap<Integer, String> secondCopyDelivery = new HashMap<Integer, String>();
    @Getter
    private static HashMap<Integer, String> secondCopyDeliveryEnable = new HashMap<Integer, String>();   //qui second copy
    @Getter
    private static HashMap<Integer, Integer> automaticTndCard = new HashMap<Integer, Integer>();
    @Getter
    private static HashMap<Integer, Integer> automaticTndCash = new HashMap<Integer, Integer>();
    private static HashMap<Integer, Vector> choicePaymentMap = new HashMap<Integer, Vector>();
    @Getter
    private static HashMap<String, String> cardNumberMap = new HashMap<String, String>();
    @Getter
    private static HashMap<String, String> authCodeMap = new HashMap<String, String>();
    @Getter
    private static String instashopChoice = "0";
    @Getter
    private static String instashopChoiceType = "";
    private static Vector vetKeyChoices = new Vector();
    @Getter
    private static long eanAutomaticItm = 0;
    @Getter
    private static int tndInstashop = 0;
    @Getter
    private static String account = "";
    @Getter
    private static String transactionResumed = "";
    private static boolean isFinalizeInstashop = false;
    private static boolean isResumeInstashop = false;
    @Getter
    private static int cardTypeTnd = 0;
    @Getter
    private static String cardTypeDesc = "";
    @Getter
    private static int numberTraResume = 0;
    @Getter
    private static String eanItemComm = "";
    @Getter
    private static ArrayList amountTndInstashop = new ArrayList();
    @Getter
    private static ArrayList tenderInstashopUsed = new ArrayList();
    @Getter
    private static long totalInstashop = 0;
    @Getter
    private static boolean continueInstaPayment = false;
    private static boolean isPartialInstaPayment = false;
    private static long amtTenderResumeInstashop = 0;
    @Getter
    private static int currentIndexTnd = -1;
    @Getter
    private static boolean printCardNumber = false;
    @Getter
    private static boolean printAuthCode = false;
    private static boolean isReturnInstashop = false;
    @Getter
    private static String accountInstashop = "";
    private static final int INPUT_TRANS_NUM = 0;
    private static final int PAYMENT = 1;
    private static final String TRANSACTIONS_FILE = "data//transactionsInstashop.txt";

    public static void loadChargeFile() {
        logger.debug("Enter");

        try {
            try {
                prop.load(new FileInputStream(CHARGE_FILENAME));

                for (Object element : prop.keySet()) {
                    String key = element.toString();

                    if (key.startsWith("commission")) {
                        int tnd = Integer.parseInt(key.split("\\.")[1]);
                        String[] tokens = prop.getProperty(key, "100,$,0").split(",");
                        tenderProperties.put(tnd, new TenderProperties(tokens[0], tokens[1].charAt(0), Integer.parseInt(tokens[2])));
                    } else if (key.startsWith("choice")) {  //INSTASHOP-SELL-CGA#A BEG
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        vetKeyChoices.add(key);
                        logger.info("key " + key + " : tender = " + tnd);
                    } else if (key.startsWith("tnd.S.")) {
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        int value = Integer.parseInt(prop.getProperty(key));
                        automaticTndCash.put(tnd, value);
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + value);
                    } else if (key.startsWith("tnd.SC.")) {
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        int value = Integer.parseInt(prop.getProperty(key));
                        automaticTndCard.put(tnd, value);
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + value);
                    } else if (key.startsWith("tnd.F.") || key.startsWith("tnd.FC.")) {
                        String type = key.split("\\.")[1];
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        int value = Integer.parseInt(prop.getProperty(key));
                        automaticTndOnline.put(type + "." + tnd, value);
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + value);
                        //INSTASHOP-MULTITENDER BEG
                    } else if (key.startsWith("cardNum.FC.") || key.startsWith("cardNum.SC.")) {
                        String type = key.split("\\.")[1];
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        String value = prop.getProperty(key);
                        cardNumberMap.put(type + "." + tnd, value);
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + value);
                    } else if (key.startsWith("authCode.FC.") || key.startsWith("authCode.SC.")) {
                        String type = key.split("\\.")[1];
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        String value = prop.getProperty(key);
                        authCodeMap.put(type + "." + tnd, value);
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + value);
                    } else if (key.startsWith("cardNum.print")) {
                        String value = prop.getProperty(key, "false");
                        printCardNumber = value.trim().equals("true");
                    } else if (key.startsWith("authCode.print")) {
                        String value = prop.getProperty(key, "false");
                        printAuthCode = value.trim().equals("true");
                    } //INSTASHOP-MULTITENDER END
                    else if (key.startsWith("secondcopy.delivery")) {
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        secondCopyDelivery.put(tnd, prop.getProperty(key));
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + prop.getProperty(key));
                    } else if (key.startsWith("secondcopy.enable")) {    //qui second copy
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        secondCopyDeliveryEnable.put(tnd, prop.getProperty(key));
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + prop.getProperty(key));
                    } else if (key.startsWith("amt.max")) {
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        int value = Integer.parseInt(prop.getProperty(key));
                        amtMaxMap.put(tnd, value);
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + value);
                    } else if (key.startsWith("ddq.code")) {
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        String value = prop.getProperty(key);
                        ddqCodeMap.put(tnd, value);
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + value);
                    } else if (key.startsWith("card.type")) {
                        int tnd = Integer.parseInt(key.split("\\.")[2]);
                        String value = prop.getProperty(key);
                        cardTypeMap.put(tnd, value);
                        logger.info("key " + key + " : tender = " + tnd + ", value = " + value);
                    }
                }
            } catch (Exception e) {
                throw new Exception("missing or malformed parameter in file " + CHARGE_FILENAME);
            }
        } catch (Exception e) {
            logger.error("EXCEPTION: " + e.getMessage());
        }

        orderListChoices();  //INSTASHOP-SELL-CGA#A
        logger.debug("EXIT loadChargeFile");
    }

    public int action0(int spec) {
        logger.debug("Enter. spec: " + spec);

        switch (spec) {
            case INPUT_TRANS_NUM:
                dspLine.init(Mnemo.getMenu(100));

                isFinalizeInstashop = true;
                isResumeInstashop = true;
                break;
            case PAYMENT:
                dspLine.init(' ');

                isFinalizeInstashop = false;
                isResumeInstashop = false;
                break;
        }

        logger.debug("Exit. Returning 0");
        return 0;
    }

    public static boolean isFinalizeInstashop() {
        return isFinalizeInstashop;
    }

    public static void setIsFinalizeInstashop(boolean isFinalizeInstashop) {
        ECommerce.isFinalizeInstashop = isFinalizeInstashop;
    }

    public static boolean isResumeInstashop() {
        return isResumeInstashop;
    }

    public static void setIsResumeInstashop(boolean isResumeInstashop) {
        ECommerce.isResumeInstashop = isResumeInstashop;
    }

    public int action1(int spec) {
        logger.debug("Enter");

        int sts = 0;

        if ((sts = sc_checks(2, 6)) > 0)
            return sts;

        prtLine.init(' ').type(2);
        prtLine.init("####################").type(2);
        prtLine.init(' ').type(2);

        prtLine.init(Mnemo.getMenu(107)).type(2);
        prtLine.init(' ').type(2);

        try {
            File instanshopFile = new File(TRANSACTIONS_FILE);

            if (!instanshopFile.exists()) {
                prtLine.init("NO INSTASHOP TRANS. TO FINALIZE").type(2);
            } else {
                BufferedReader br = new BufferedReader(new FileReader(instanshopFile));
                String line = "";

                while ((line = br.readLine()) != null) {
                    String numberTrans = line.split("_")[0].trim();
                    logger.info("numberTrans: " + numberTrans);

                    String tendersUsed = line.split("_")[3].trim();
                    String[] tndList = tendersUsed.split(",");
                    logger.info("tenderUsed: " + tendersUsed);

                    String amountTenders = line.split("_")[4].trim();
                    String[] amountList = amountTenders.split(",");
                    logger.info("amountTenders: " + amountTenders);

                    String operatorInstashop = line.split("_")[6].trim();
                    logger.info("operatorInstashop: " + operatorInstashop);

                    String amountTransaction = line.split("_")[7].trim();
                    logger.info("amountTransaction: " + amountTransaction);

                    prtLine.init("OPERATOR NUMBER: " + operatorInstashop).type(2);
                    prtLine.init("TRANSACTION NUMBER: " + numberTrans).type(2);
                    for (int i = 0; i < tndList.length; i++) {
                        prtLine.init("TENDER NUMBER: " + tnd[Integer.parseInt(tndList[i])].tx20.trim() + " (" + tndList[i] + ")").type(2);
                        prtLine.init("TENDER AMOUNT: " + editMoney(0, Long.parseLong(amountList[i]))).type(2);
                    }

                    prtLine.init("TRANSACTION AMOUNT: " + editMoney(0, Long.parseLong(amountTransaction))).type(2);
                    prtLine.init(' ').type(2);
                    prtLine.init(' ').type(2);
                }

                br.close();
            }
        } catch (Exception e) {

        }

        prtLine.init(' ').type(2);
        prtLine.init("####################").type(2);
        prtLine.init(' ').type(2);

        GdRegis.hdr_print();

        logger.debug("Exit. Returning 0");
        return 0;
    }

    private static void orderListChoices() {
        logger.debug("Enter");

        Collections.sort(vetKeyChoices);

        for (Object element : vetKeyChoices) {
            String key = element.toString();
            int tnd = Integer.parseInt(key.split("\\.")[2]);

            String choices = instashopChoiceMap.get(tnd) != null ? instashopChoiceMap.get(tnd) + prop.getProperty(key) + "," : prop.getProperty(key) + ",";
            instashopChoiceMap.put(tnd, choices);
        }

        logger.debug("Exit");
    }

    public static void setInstashopChoiceType(String instashopChoiceType) {
        ECommerce.instashopChoiceType = instashopChoiceType;
    }

    public static void setInstashopChoice(String instashopChoice) {
        ECommerce.instashopChoice = instashopChoice;
    }

    public static void setAccount(String acnt) {
        account = acnt;
    }

    public static void setTndInstashop(int tndInstashop) {
        ECommerce.tndInstashop = tndInstashop;
    }

    public static int getAmtAutomaticItem() {
        return amtItem;
    }

    public static void setCardTypeTnd(int cardTypeTnd) {
        ECommerce.cardTypeTnd = cardTypeTnd;
    }

    public static void setCardTypeDesc(String cardTypeDesc) {
        ECommerce.cardTypeDesc = cardTypeDesc;
    }

    public static void setTransactionResumed(String transactionResumed) {
        ECommerce.transactionResumed = transactionResumed;
    }

    private static int calculateAmtAutomaticItem() {
        logger.debug("ENTER calculateAmtAutomaticItem");

        amtItem = tenderProperties.get(itm.tnd).getCommissionValue();  //default value

        if (tenderProperties.get(itm.tnd).getCommissionType() == TenderProperties.PERCENTAGE) {  //perc
            amtItem = (int) ((amtItem / 100) * tra.amt) / 100;
        }

        alreadyAmzCommCalc = true;
        logger.info("tra.amt: " + tra.amt);
        logger.debug("EXIT calculateAmtAutomaticItem - return: " + amtItem);
        return amtItem;
    }

    public static boolean isAutomaticAmazonItem() {
        return isAutomaticAmazonItem;
    }

    public static void resetAutomaticAmazonItem() {
        isAutomaticAmazonItem = false;
    }

    public static boolean isAutomaticVoidAmazonItem() {
        return isAutomaticVoidAmazonItem;
    }

    public static void resetAutomaticVoidAmazonItem() {
        isAutomaticVoidAmazonItem = false;
    }

    public static int getAmtItem() {
        return amtItem;
    }

    public static int automaticSaleItem() {
        logger.debug("Enter");

        eanAutomaticItm = 0;
        int sts = 0;

        try {
            if (ECommerce.getAmtMaxMap().get(itm.tnd) != null) {
                if (tra.amt > ECommerce.getAmtMaxMap().get(itm.tnd)) {
                    return 0;
                }
            }

            calculateAmtAutomaticItem();

            int nxt = event.nxt;
            event.spc = input.msk = 0;

            isAutomaticAmazonItem = true;
            input.prompt = "";
            input.qrcode = "";

            input.reset(ECommerce.tenderProperties.get(itm.tnd).getCommissionItem());

            String tmpDspLine = dspLine.toString();
            sts = group[5].action2(0);
            dspLine.init(tmpDspLine);

            event.nxt = nxt;
            isAutomaticAmazonItem = false;
        } catch (Exception e) {

        }

        logger.debug("Exit. Returning: " + sts);
        return sts;
    }

    public static int automaticVoidItem() {
        logger.debug("Enter");

        int nxt = event.nxt;
        event.spc = input.msk = 0;

        isAutomaticAmazonItem = true;
        isAutomaticVoidAmazonItem = true;
        input.prompt = "";

        input.reset(String.valueOf(eanAutomaticItm));

        String tmpDspLine = dspLine.toString();

        int sts = group[5].action2(0);
        dspLine.init(tmpDspLine);

        event.nxt = nxt;
        isAutomaticAmazonItem = false;
        isAutomaticVoidAmazonItem = false;

        logger.debug("Exit. Returning: " + sts);
        return sts;
    }

    public static void chooseCardType() {
        logger.debug("Enter");

        input.init(0x00, 1, 1, 0);

        Set keys = ECommerce.getCardTypeMap().keySet();

        SelDlg dlg = new SelDlg(Mnemo.getText(22));

        int i = 1;
        List<String> tndList = new ArrayList<String>();
        List<String> descList = new ArrayList<String>();
        for (Object key : keys) {
            tndList.add(key.toString());
            String typeCard = ECommerce.getCardTypeMap().get(key);
            descList.add(typeCard);
            dlg.add(9, editNum(i, 1), " " + typeCard);
            i++;
        }
        dlg.show("MNU");

        ECommerce.setCardTypeTnd(Integer.parseInt(tndList.get(Integer.parseInt(dlg.input.pb) - 1)));
        ECommerce.setCardTypeDesc(descList.get(Integer.parseInt(dlg.input.pb) - 1));

        logger.debug("Exit");
    }

    public static boolean handleInstashopPayment() {
        logger.debug("Enter");

        SelDlg dlg = new SelDlg(Mnemo.getText(22));
        ArrayList<String> choiceList = new ArrayList<String>();
        ArrayList<String> suspendList = new ArrayList<String>();

        String InstashopValue = ECommerce.getInstashopChoiceMap().get(itm.tnd);

        for (String s : InstashopValue.split(",")) {
            choiceList.add(s.split(";")[0]);
            suspendList.add(s.split(";")[1]);
        }

        if (ECommerceManager.getInstance().getBasket() == null) {  //INSTASHOP-MULTITENDER ADD
            input.prompt = Mnemo.getText(13);
            input.init(0x00, 1, 1, 0);
            GdPos.panel.display(1, Mnemo.getMenu(103));

            for (int i = 0; i < choiceList.size(); i++) {
                dlg.add(9, editNum(i + 1, 1), " " + choiceList.get(i));
            }

            dlg.show("MNU");
        }

        try {
            int choiceIndex = 1;

            if (ECommerceManager.getInstance().getBasket() == null) {   //INSTASHOP-MULTITENDER ADD
                choiceIndex = Integer.parseInt(dlg.input.pb) - 1;
                ECommerce.setInstashopChoice(dlg.input.pb);
                ECommerce.setInstashopChoiceType(suspendList.get(choiceIndex));
            } else {
                ECommerce.setInstashopChoice(String.valueOf(choiceIndex));
                ECommerce.setInstashopChoiceType(suspendList.get(choiceIndex - 1));
            }

            if (suspendList.get(choiceIndex).trim().startsWith("S")) {
                logger.info("Exit. Returning true");
                return true;
            }
        } catch (Exception e) {
            logger.error("Error: " + e);

            if (dlg.input.pb.isEmpty()) {
                GdPos.panel.clearLink(Mnemo.getInfo(8), 1);
                return handleInstashopPayment();
            }
        }

        logger.debug("Exit. Returning false");
        return false;
    }

    public static void resetAlreadyAmzCommCalc() {
        logger.debug("resetAlreadyAmzCommCalc");
        alreadyAmzCommCalc = false;
    }

    public static void writeInstashopSuspend() {
        logger.debug("Enter");
        FileWriter instanshopSuspend = null;
        BufferedWriter bw = null;

        try {
            instanshopSuspend = new FileWriter(TRANSACTIONS_FILE, true);
            bw = new BufferedWriter(instanshopSuspend);
            logger.info("ctl.tran: " + ctl.tran);

            String line = leftFill(ctl.tran + "", 4, '0') + " _ " + ECommerce.getEanAutomaticItm() + " _ " + ECommerce.getInstashopChoice()
                    + " _ " + itm.tnd + " _ " + itm.amt + " _ " + itm.number + " _ " + ctl.ckr_nbr + " _ " + tra.amt + "\r\n";
            bw.write(line);
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            try {
                bw.close();
                instanshopSuspend.close();
            } catch (Exception ec) {
                logger.error("Error closing file: ", ec);
            }
        }

        logger.debug("Exit");
    }

    public static int searchInstashopSuspend(String transaction) {
        logger.debug("Enter. transaction: " + transaction);

        String line = "";
        BufferedReader br = null;

        try {
            File fileSuspend = new File(TRANSACTIONS_FILE);
            br = new BufferedReader(new FileReader(fileSuspend));

            while ((line = br.readLine()) != null) {
                if (line.split("_")[0].trim().startsWith(transaction)) {
                    numberTraResume = Integer.parseInt(transaction);
                    logger.info("numberTraResume: " + numberTraResume);
                    setTransactionResumed(String.valueOf(numberTraResume));

                    eanItemComm = line.split("_")[1].trim();
                    logger.info("eanItemComm: " + eanItemComm);

                    //INSTASHOP-MULTITENDER BEG
                    String tendersUsed = line.split("_")[3].trim();
                    for (String tndElement : tendersUsed.split(",")) {
                        tenderInstashopUsed.add(tndElement.trim());
                    }
                    // tenderInstashopUsed = Integer.parseInt(line.split("_")[3].trim());
                    logger.info("tendersUsed: " + tendersUsed);

                    String amountsTnd = line.split("_")[4].trim();
                    for (String amtUsed : amountsTnd.split(",")) {
                        amountTndInstashop.add(amtUsed.trim());
                    }
                    //amountInstashop = Long.parseLong(line.split("_")[4].trim());
                    logger.info("amountsTnd: " + amountsTnd);
                    //INSTASHOP-MULTITENDER END

                    accountInstashop = line.split("_")[5].trim();
                    logger.info("accountInstashop: " + accountInstashop);
                    setAccount(accountInstashop);

                    totalInstashop = Long.parseLong(line.split("_")[7].trim());  //INSTASHOP-MULTITENDER 
                    logger.info("totalInstashop: " + totalInstashop);

                    logger.info("EXIT searchInstashopSuspend, return 0");
                    return 0;
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            try {
                br.close();
            } catch (Exception ec) {
                logger.error("Error closing file: ", ec);
            }
        }

        logger.debug("Exit. Return -1");
        return -1;
    }

    public static boolean checkFileInstashop() {
        logger.debug("Enter");
        BufferedReader br = null;

        try {
            File instanshopFile = new File(TRANSACTIONS_FILE);

            if (!instanshopFile.exists()) {
                logger.info("Exit. Returning false");
                return ctl.block = false;
            }
            br = new BufferedReader(new FileReader(instanshopFile));

            if (br.readLine() != null) {
                ctl.block = true;
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            try {
                br.close();
            } catch (Exception ec) {
                logger.error("Error closing file: ", ec);
            }
        }

        logger.debug("Exit. Returning: " + ctl.block);
        return ctl.block;
    }

    public static void handleInstashopResume(int transaction) {
        logger.debug("Enter. transaction " + transaction);
        FileWriter fw = null;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(TRANSACTIONS_FILE));
            StringBuilder sb = new StringBuilder();
            String line = "";

            while ((line = br.readLine()) != null) {
                if (!line.split("_")[0].trim().startsWith(String.valueOf(leftFill(transaction + "", 4, '0')))) {
                    logger.info("rewrite the line");
                    sb.append(line).append("\n");
                }
            }

            fw = new FileWriter(new File(TRANSACTIONS_FILE));
            logger.info("file writer");

            fw.write(sb.toString());
            logger.info("write all");
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            try {
                br.close();
                fw.close();
            } catch (Exception ec) {
                logger.error("Error closing file: ", ec);
            }
        }

        logger.debug("Exit");
    }

    public static void addTenderTransInstashop(String line) {
        logger.error("ENTER addTenderTransInstashop");

        //FILEERROR-CGA#A BEG
        //FileOutputStream fileOut = null;
        FileWriter fileOut = null;
        BufferedWriter bw = null;

        removeLastTransaction();
        //FILEERROR-CGA#A END

        try {
            if (!line.isEmpty()) {
                String[] fiels = line.split("_");
                String newLine = "";

                for (int i = 0; i <= 7; i++) {
                    newLine += fiels[i];

                    if (i == 3) {
                        newLine += "," + itm.tnd;
                    } else if (i == 4) {
                        newLine += "," + itm.amt;
                    }

                    if (i != 7) {
                        newLine += "_";
                    } else {
                        newLine += "\r\n";
                    }
                }
                //FILEERROR-CGA#A BEG
                logger.error("newLIne: " + newLine);

                fileOut = new FileWriter(TRANSACTIONS_FILE, true);
                bw = new BufferedWriter(fileOut);

                bw.write(newLine);
                logger.error("adding line");
                //fileOut = new FileOutputStream(TRANSACTIONS_FILE);
                //fileOut.write(newLine.getBytes());
                //FILEERROR-CGA#A END
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            try {
                bw.close(); //FILEERROR-CGA#A
                fileOut.close();
            } catch (Exception ec) {
                logger.error("Error closing file: ", ec);
            }
        }

        logger.error("EXIT addTenderTransInstashop");
    }

    //FILEERROR-CGA#A BEG
    public static void removeLastTransaction() {
        logger.error("ENTER removeLastTransaction");

        File originFile = null;
        File newFile = null;

        BufferedReader br = null;
        BufferedWriter bw = null;

        String line = "";

        try {
            originFile = new File(TRANSACTIONS_FILE);
            newFile = new File(TRANSACTIONS_FILE + ".txt");

            br = new BufferedReader(new FileReader(originFile));
            bw = new BufferedWriter(new FileWriter(newFile));

            while ((line = br.readLine()) != null) {
                logger.info("line: " + line);
                if (!line.split("_")[0].trim().startsWith(String.valueOf(leftFill(ctl.tran + "", 4, '0')))) {
                    bw.write(line + "\r\n");
                    logger.info("write line in the new file: " + line);
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            try {
                br.close();
                bw.close();
                originFile.delete();
                newFile.renameTo(new File(TRANSACTIONS_FILE));
            } catch (Exception ec) {
                logger.error("Error closing file: ", ec);
            }
        }

        logger.error("EXIT removeLastTransaction");
    }
    //FILEERROR-CGA#A END

    public static String searchTransInstashop() {
        String line = "";
        String read = "";
        BufferedReader file = null;
        try {
            file = new BufferedReader(new FileReader(TRANSACTIONS_FILE));

            while ((line = file.readLine()) != null) {
                if (line.split("_")[0].trim().equals(String.valueOf(leftFill(ctl.tran + "", 4, '0')))) {
                    read = line;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            try {
                file.close();
            } catch (Exception ec) {
                logger.error("Error closing file: ", ec);
            }
        }

        return read;
    }

    public static void resetInstashop() {
        logger.debug("Enter");

        eanItemComm = "";
        tenderInstashopUsed.clear();
        amountTndInstashop.clear();
        numberTraResume = 0;
        accountInstashop = "";
        ECommerce.setIsFinalizeInstashop(false);
        ECommerce.setIsResumeInstashop(false);
        ECommerce.setIsReturnInstashop(false);
        continueInstaPayment = false;
        isPartialInstaPayment = false;
        totalInstashop = 0;
        amtTenderResumeInstashop = 0;
        currentIndexTnd = -1;

        logger.debug("Exit");
    }

    public static void printInstashopReport() {
        try {
            File instanshopFile = new File(TRANSACTIONS_FILE);

            if (instanshopFile.exists()) {
                prtLine.init(' ').type(2);
                prtLine.init("****Instashop Transactions**************").book(2);

                BufferedReader br = new BufferedReader(new FileReader(instanshopFile));
                String line = "";
                long totalInstashop = 0;

                while ((line = br.readLine()) != null) {
                    String numberTrans = line.split("_")[0].trim();
                    long amountInstashop = Long.parseLong(line.split("_")[7].trim());
                    String operatorInstashop = line.split("_")[6].trim();

                    if (ctl.ckr_nbr == Integer.parseInt(operatorInstashop)) {
                        totalInstashop += amountInstashop;
                        String lineTransaction = "Transaction " + leftFill(numberTrans, 8, ' ') + leftFill(editMoney(0, amountInstashop), 20, ' ');
                        prtLine.init(lineTransaction).book(2);
                    }
                }

                String lineTotal = "Instashop Total " + leftFill(editMoney(0, totalInstashop), 24, ' ');
                prtLine.init(lineTotal).book(2);

                prtLine.init(' ').type(2);
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    public static void setContinueInstaPayment(boolean continueInstaPayment) {
        ECommerce.continueInstaPayment = continueInstaPayment;
    }

    public static boolean isPartialInstaPayment() {
        return isPartialInstaPayment;
    }

    public static void setIsPartialInstaPayment(boolean isPartialInstaPayment) {
        ECommerce.isPartialInstaPayment = isPartialInstaPayment;
    }

    public static void setTotalInstashop(long totalInstashop) {
        ECommerce.totalInstashop = totalInstashop;
    }

    public static void setCurrentIndexTnd(int currentIndexTnd) {
        ECommerce.currentIndexTnd += currentIndexTnd;
    }

    public static boolean isReturnInstashop() {
        return isReturnInstashop;
    }

    public static void setIsReturnInstashop(boolean isReturnInstashop) {
        ECommerce.isReturnInstashop = isReturnInstashop;
    }
}