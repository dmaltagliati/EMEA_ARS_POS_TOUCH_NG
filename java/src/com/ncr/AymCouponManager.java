package com.ncr;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.ncr.aymcoupon.AymCouponService;
import com.ncr.aymcoupon.AymCouponSettings;
import com.ncr.aymcoupon.data.Coupon;
import com.ncr.aymcoupon.data.CouponError;
import com.ncr.aymcoupon.data.CouponUsed;
import com.ncr.aymcoupon.data.Products;
import com.ncr.loyalty.aym.AymLoyaltyService;
import com.ncr.loyalty.aym.data.ErrorResponse;
import com.ncr.loyalty.aym.data.InnerResponse;
import com.ncr.loyalty.aym.data.PostRequest;
import com.ncr.loyalty.transaction.Transaction;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.util.*;

public class AymCouponManager extends Action {
    private static final Logger logger = Logger.getLogger(AymCouponManager.class);

    public static final String PROGRAM_CODE = AymCouponSettings.getInstance().getProgramCode();
    public static final String LOCATION_CODE = AymCouponSettings.getInstance().getLocationCode();
    public static final int TIMEOUT = AymCouponSettings.getInstance().getTimeOut();

    public  static final String COUPON_SUFFIX = "_CP";
    public static final String TYPE_PERCENT = "Percentage";
    private static AymCouponManager instance;
    private static String token = "";
    private static Transaction transaction;

    public static AymCouponManager getInstance(){
        if (instance == null){
            instance = new AymCouponManager();
        }

        token = AymLoyaltyService.getInstance().getToken().getAccessToken();
        transaction = AymLoyaltyService.getInstance().getLoyaltyTransaction();
        return instance;
    }

    public void testFetch(){
        String baseUrl = AymCouponSettings.getInstance().fetchCouponEndPoint();

        String[] queries = {"AccountIdValue=Test_User_2"};

        HttpResponse<String> response = AymCouponService.getInstance().get(baseUrl, TIMEOUT, token, queries);

        List<Coupon> testlistcoupon = new ArrayList<Coupon>();

        if (response!= null && response.getStatus() == HttpStatus.SC_OK) {
            JsonElement jsonResponse = new JsonParser().parse(response.getBody());
            Coupon[] enums = AymCouponService.getInstance().getGsonGeneric().fromJson(jsonResponse, Coupon[].class);
            testlistcoupon =  Arrays.asList(AymCouponService.getInstance().getGsonGeneric().fromJson(jsonResponse, Coupon[].class));

            logger.info("ciao");
        }
    }

    public void testValidate(String coupon){
        String baseUrl = AymCouponSettings.getInstance().validateCouponEndPoint();

        String couponCode = coupon;

        String[]queries = {"CouponCode=" + couponCode.trim(), "LocationCode="+LOCATION_CODE, "ProgramCode="+PROGRAM_CODE};

        HttpResponse<String> response = AymCouponService.getInstance().get(baseUrl, TIMEOUT, token, queries);

        if (response!= null && response.getStatus() == HttpStatus.SC_OK) {
            JsonElement jsonResponse = new JsonParser().parse(response.getBody());
            Coupon couponJson = AymCouponService.getInstance().getGsonGeneric().fromJson(jsonResponse, Coupon.class);
            logger.info("ciao");
        }
    }

    private List<Coupon> listCoupon = new ArrayList<Coupon>();
    private List<Coupon> listCouponPassed = new ArrayList<Coupon>();

    public List<Coupon> getListCoupon() {
        return listCoupon;
    }

    public List<Coupon> getListCouponPassed() {
        return listCouponPassed;
    }

    public void fetch(String accountIdValue){
        logger.info("ENTER fetch, accountIdValue : " + accountIdValue);

        String baseUrl = AymCouponSettings.getInstance().fetchCouponEndPoint();

        String[] queries = {"AccountIdValue=" + accountIdValue};

        HttpResponse<String> response = AymCouponService.getInstance().get(baseUrl, TIMEOUT, token, queries);

        if (response!= null && response.getStatus() == HttpStatus.SC_OK) {
            JsonElement jsonResponse = new JsonParser().parse(response.getBody());

            try {
                listCoupon = Arrays.asList(AymCouponService.getInstance().getGsonGeneric().fromJson(jsonResponse, Coupon[].class));
            }catch (Exception e){
                listCoupon = new ArrayList<Coupon>();
                logger.info("failed to fetch listCoupon. e: " + e.getMessage());
            }
        }else{
            listCoupon = new ArrayList<Coupon>();
            logger.info("failed to fetch listCoupon.");
        }
        logger.info("EXIT fetch");
    }

    public int validate(String coupon){
        logger.info("ENTER Validate, coupon( " + coupon + " )");
        int sts = 0;

        if (!getListCoupon().isEmpty()) {
            String baseUrl = AymCouponSettings.getInstance().validateCouponEndPoint();

            String couponCode = coupon;

            String[] queries = {"CouponCode=" + couponCode.trim(), "LocationCode=" + LOCATION_CODE, "ProgramCode=" + PROGRAM_CODE};

            HttpResponse<String> response = AymCouponService.getInstance().get(baseUrl, TIMEOUT, token, queries);

            if (response != null && response.getStatus() == HttpStatus.SC_OK) {
                JsonElement jsonResponse = new JsonParser().parse(response.getBody());
                Coupon couponJson = AymCouponService.getInstance().getGsonGeneric().fromJson(jsonResponse, Coupon.class);
                printCopuon(couponJson);
                getListCouponPassed().add(couponJson);
                logger.info("success coupon validation.");
            } else {
                Coupon couponError = new CouponError("9999", "failed to validate", false);
                logger.info("failed to validate coupon : " + coupon);
            }
        }else{
            sts = 7; //list coupon empty.
            logger.info("listCoupon is empty. return 7");
        }
        logger.info("EXIT Validate()");
        return sts;
    }

    public void sendUsed(String accountIdValue){
        logger.info("ENTER sendUsed");

        if (accountIdValue == null){
            logger.info("EXIT sendUsed. Not a loyalty transaction.");
            return;
        }

        Iterator<Coupon> usedCoupon_ITR =  getListCouponPassed().iterator();
        Coupon tmpCoupon = new Coupon();
        CouponUsed couponUsed = new CouponUsed();
        Gson couponUsedJson = new Gson();
        String strCouponUsed = "";
        String baseUrl = AymCouponSettings.getInstance().useCouponEndPoint();
        String transactionUnique = transaction.getUniqueId();
        int suffixIndex = 0;


        while(usedCoupon_ITR.hasNext()){
            tmpCoupon = usedCoupon_ITR.next();

            String offlineCode = AymCouponSettings.getInstance().getOfflineCode();
            String offlineStatus = AymCouponSettings.getInstance().getOfflineStatus();

            if (tmpCoupon.isUsed()){
                transactionUnique = transaction.getUniqueId() + "C" + suffixIndex++;
                couponUsed = new CouponUsed(accountIdValue, tmpCoupon.getCouponCode(), transactionUnique);
                strCouponUsed = couponUsedJson.toJson(couponUsed);
                logger.info("used coupon to send : " + strCouponUsed);

                HttpResponse<String> response = AymCouponService.getInstance().post(baseUrl, TIMEOUT, token, strCouponUsed,"");

                if (response != null && response.getStatus() == HttpStatus.SC_OK) {
                    logger.info("Successful coupon used sent.");
                }else if (response == null) {
                    JsonElement jsonResponse = AymLoyaltyService.getInstance().getGsonGeneric().toJsonTree(new ErrorResponse(new InnerResponse(offlineCode,offlineStatus, false)), ErrorResponse.class);
                    AymLoyaltyService.getInstance().checkOffline(transactionUnique + COUPON_SUFFIX, couponUsed, jsonResponse);
                }else{
                    logger.info("Failure to send the used coupon.");

                }
            }
        }


        logger.info("EXIT sendUsed");
    }



    public void printCopuon(Coupon coupon){
        prtLine.init("");
        prtLine.onto(0, rightFill(coupon.getCouponName(), 20, ' '));
        //prtLine.onto(0, editTxt(coupon.getCouponName(), 20));
        prtLine.book(3);
    }
    public boolean isCoupon(String coupon){
        logger.info("ENTER isCoupon( " + coupon + " )");
        boolean flag = false;

        String pattern = AymCouponSettings.getInstance().getCouponPattern();
        if (pattern.length() > 0) {
            if (coupon.matches(pattern)) {
                flag = true;
            }
        }

        logger.info("EXIT isCoupon - " + flag);
        return flag;
    }

    public boolean isCouponPresent(String coupon){
        logger.info("ENTER isCouponPresent( " + coupon + " )");
        boolean flag = false;
        Coupon tmpCoupon;

        Iterator<Coupon> listCoupon_ITR = getListCoupon().iterator();
        while(listCoupon_ITR.hasNext()){
            tmpCoupon = listCoupon_ITR.next();

            if (tmpCoupon.getCouponCode().equals(coupon)){
                flag = true;
                break;
            }
        }

        logger.info("EXIT isCouponPresent() " + flag) ;
        return flag;
    }

    public void generateDiscount(){
        logger.info("ENTER generateDiscount" );

        Itemdata tmpItem = new Itemdata();
        Coupon tmpCoupon = new Coupon();
        long discountValue = 0;

        for(Map.Entry<String, Itemdata> elemento : Promo.sellItemMap.entrySet()){
            tmpItem = elemento.getValue();

            tmpCoupon = findCoupon(tmpItem.number);
            if (tmpCoupon.getId() > 0) {

                tmpCoupon.setUsed(true);
                discountValue = (long)(tmpCoupon.getValue() * 100);

                if (tmpCoupon.getType().trim().equals(TYPE_PERCENT)) {
                    logger.info("generate percent discount. discountValue: " + discountValue);
                    Promo.manualRewardDiscoutPercent(0, tmpCoupon.getCouponName(), discountValue, 90, tmpItem.index + 1, "", 1);
                }else{
                    logger.info("generate value discount. discountValue: " + discountValue);
                    Promo.manualRewardDiscoutAmount(0, tmpCoupon.getCouponName(), discountValue, 90, tmpItem.index + 1, "");
                }
            }
        }

        logger.info("EXIT generateDiscount" );
    }

    private Coupon findCoupon(String item){
        logger.info("ENTER findCoupon. item: " + item);
        Iterator<Coupon> couponITR = getListCouponPassed().iterator();

        Coupon tmpCoupon = new Coupon();
        Products tmpEan = new Products();

        boolean found = false;

        while(couponITR.hasNext()){
            tmpCoupon = couponITR.next();

            if (tmpCoupon.isUsed()){
                logger.info("Coupon : " + tmpCoupon.getCouponCode() + " already used");
                continue;
            }

            Iterator<Products> skuITR = tmpCoupon.getProducts().iterator();

            while(skuITR.hasNext()){
                tmpEan = skuITR.next();
                if (tmpEan.getSKU().trim().equals(item.trim())){
                    found = true;
                    break;
                }
            }
        }

        if (!found){
            tmpCoupon = new Coupon();
        }

        logger.info("EXIT find. Coupon.getCouponCode() : " + tmpCoupon.getCouponCode() + " - found: " + found);
        return tmpCoupon;
    }

    public void reset(){
        listCoupon = new ArrayList<Coupon>();
        listCouponPassed = new ArrayList<Coupon>();
    }
}
