package com.ncr.loyalty.aym;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.ncr.AymCouponManager;
import com.ncr.aymcoupon.AymCouponSettings;
import com.ncr.aymcoupon.data.CouponUsed;
import com.ncr.common.data.AdditionalInfo;
import com.ncr.common.data.TerminalInfo;
import com.ncr.eft.EftPluginManager;
import com.ncr.loyalty.LoyaltyService;
import com.ncr.loyalty.aym.data.*;
import com.ncr.loyalty.aym.data.TransactionItem;
import com.ncr.loyalty.aym.data.ereceipt.*;
import com.ncr.loyalty.data.PhoneCode;
import com.ncr.loyalty.transaction.*;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AymLoyaltyService extends LoyaltyService {
    private static final Logger logger = Logger.getLogger(AymLoyaltyService.class);
    private static AymLoyaltyService instance = null;
    private Gson gsonToken = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setDateFormat("MM/dd/yyyy HH:mm:ss a").create();
    private Gson gsonGeneric = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .registerTypeAdapter(Date.class, new GsonDateDeSerializer())
            .create();
    private Gson gsonPost = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String TIMEOUT = "timeout";
    public static final String URL = "url";
    public static final String ERECEIPTS_URL = "e-receipts.url";
    public static final String ERECEIPTS_TIMEOUT = "e-receipts.timeout";
    public static final String TAX_INVOICE_ID = "e-receipts.tax-invoice-id";
    public static final String PROGRAM_CODE = "loyalty-program-code";
    public static final String COUNTRY_CODE = "country-code";
    public static final String LOCATION_CODE = "location-code";
    public static final String REWARDS_CODE = "rewards-code";
    public static final String ENROLL_MOBILE = "Mobile";
    public static final String TYPE_POS_EARN = "POS_EARN";
    public static final String TYPE_POS_RETURN = "POS_EARN";
    public static final String TYPECODE_MOBILE = "Mobile_Number";
    public static final String TYPECODE_LOYALTYID = "LoyaltyID";
    public static final String TYPECODE_REDEMPTION = "REDEEM";
    public static final String ACCOUNTID_FIELD = "AccountIdValue";
    public static final String TRANSACTION_ID_FIELD = "transactionUniqueId";
    private static final String TOKEN_FILE = "token.json";
    private static final String TRANSACTIONS_EP = "/transactions";
    private static final String VARIABLES_EP = "/variableAdd";
    private static final String ENROLLMENT_EP = "/memberWithSMS";
    private static final String IDENTIFICATION_EP = "/members";
    private static final String REDEMPTION_EP = "/redeemRewards";
    private static final String CANCEL_REDEMPTION_EP = "/cancelReward";
    private static final String TOKEN_EP = "/token";
    private static final String OFFLINE_CODE = "offline.code";
    private static final String OFFLINE_STATUS = "offline.status";
    private static final String OFFLINE_PATH_FOLDER_PROCESS = "offline.working-dir";
    private static final String OFFLINE_PATH_FOLDER_SUCCESS = "offline.success-dir";
    private static final String OFFLINE_PATH_FOLDER_ERROR = "offline.error-dir";
    private static final String OFFLINE_FILENAME_MASK = "offline.filename-mask";
    private static final String OFFLINE_DELAY_THREAD = "offline.thread.delay";
    private static final String X_FUNCTIONS_KEY = "x-functions-key";
    private static final String VARIABLES_SUFFIX = "_V";
    private static final String ERECEIPTS_SUFFIX = "_ER";
    private static final String REWARD_CANCEL_SUFFIX = "_RC";

    private BearerToken token = new BearerToken();
    private Transaction transaction = new Transaction();

    public static AymLoyaltyService getInstance() {
        if (instance == null) {
            instance = new AymLoyaltyService();
        }
        return instance;
    }

    private AymLoyaltyService() {
        super();
    }


    public BearerToken getToken() {
        return token;
    }

    public LoyaltyData customerIdentification(String accountIdValue, String idType) {
        logger.debug("Enter. Account Id: " + accountIdValue + " idType: " + idType);
        LoyaltyData loyaltyData = null;
        if (!isLoyaltyEnabled()) return null;
        if (!checkToken(false)) return null;

        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0")))
                    .setConnectionRequestTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0")))
                    .setSocketTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0"))).build();
            HttpClient httpClient = HttpClients
                    .custom()
                    .setDefaultRequestConfig(config)
                    .build();
            Unirest.setHttpClient(httpClient);
            HttpResponse<String> response = Unirest.get(getProps().getProperty(URL, "http://localhost") + IDENTIFICATION_EP)
                    .header("Accept", "*/*")
                    .header("Authorization", "bearer " + token.getAccessToken())
                    .queryString("AccountIdValue", accountIdValue)
                    .queryString("ProgramCode", getProps().getProperty(PROGRAM_CODE, ""))
                    .asString();

            logger.info("Status: " + response.getStatus());
            logger.info("Body: " + response.getBody());
            if (response.getStatus() == HttpStatus.SC_OK) {
                JsonElement jsonResponse = new JsonParser().parse(response.getBody());
                loyaltyData = initLoyaltyData(jsonResponse, accountIdValue);
            } else {
                logger.warn("Error from server: " + response.getStatus());
                loyaltyData = initLoyaltyData(gsonGeneric.toJsonTree(new ErrorResponse(new InnerResponse("" + response.getStatus(), response.getStatusText(), false)), ErrorResponse.class), accountIdValue);
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            loyaltyData = initLoyaltyData(gsonGeneric.toJsonTree(new ErrorResponse(new InnerResponse(getProps().getProperty(OFFLINE_CODE, ""), getProps().getProperty(OFFLINE_STATUS, ""), false)), ErrorResponse.class), accountIdValue);
        }
        logger.debug("Exit. Loyalty data: " + loyaltyData);
        return loyaltyData;
    }


    private JsonElement postRequest(String endpoint, String serializedRequest, boolean withToken, boolean withXFunctionsKey) {
        logger.debug("Enter. Endpoint: " + endpoint + " Serialized request: " + serializedRequest);
        try {

            /*solo per debug BEG
            int timo = 50;
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timo)
                    .setConnectionRequestTimeout(timo)
                    .setSocketTimeout(timo).build();
            solo per debug BEG */

                     RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Integer.parseInt(getProps().getProperty(ERECEIPTS_TIMEOUT, "0")))
                    .setConnectionRequestTimeout(Integer.parseInt(getProps().getProperty(ERECEIPTS_TIMEOUT, "0")))
                    .setSocketTimeout(Integer.parseInt(getProps().getProperty(ERECEIPTS_TIMEOUT, "0"))).build();
            HttpClient httpClient = HttpClients
                    .custom()
                    .setDefaultRequestConfig(config)
                    .build();
            if (withXFunctionsKey) {
                httpClient = HttpClients
                        .custom()
                        .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .setDefaultRequestConfig(config)
                        .build();
            }
            Unirest.setHttpClient(httpClient);
            HttpRequestWithBody request = Unirest.post(endpoint)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*");
            if (withToken) request.header("Authorization", "bearer " + token.getAccessToken());
            if (withXFunctionsKey) request.header("x-functions-key", getProps().getProperty(X_FUNCTIONS_KEY, ""));
            HttpResponse<String> response = request.body(serializedRequest).asString();

            logger.info("Status: " + response.getStatus());
            logger.info("Body: " + response.getBody());
            if (response.getStatus() == HttpStatus.SC_OK) {
                if (response.getBody() == null || response.getBody().isEmpty()) throw new RuntimeException("Empty response");
                return new JsonParser().parse(response.getBody());
            } else {
                logger.warn("Error from server: " + response.getStatus());
                return gsonGeneric.toJsonTree(new ErrorResponse(new InnerResponse("" + response.getStatus(), response.getStatusText(), false)), ErrorResponse.class);
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            return gsonGeneric.toJsonTree(new ErrorResponse(new InnerResponse(getProps().getProperty(OFFLINE_CODE, ""), getProps().getProperty(OFFLINE_STATUS, ""), false)), ErrorResponse.class);
        }
    }

    private LoyaltyData initLoyaltyData(JsonElement jsonResponse, String accountIdValue) {
        LoyaltyCustomer loyaltyCustomer = new LoyaltyCustomer(accountIdValue);

        if (jsonResponse.getAsJsonObject().has(ACCOUNTID_FIELD)) {
            LoyaltyDataResponse loyaltyDataResponse = gsonGeneric.fromJson(jsonResponse, LoyaltyDataResponse.class);
            loyaltyCustomer.setFirstName(loyaltyDataResponse.getFirstName());
            loyaltyCustomer.setLastName(loyaltyDataResponse.getLastName());
            for (IdType type : loyaltyDataResponse.getIdTypes()) {
                if (TYPECODE_MOBILE.equals(type.getIdTypeCode())) {
                    loyaltyCustomer.setMobile(type.getAccountIdValue());
                } else {
                    loyaltyCustomer.setCard(type.getAccountIdValue());
                }
            }
            loyaltyCustomer.setPoints(new LoyaltyPoints(loyaltyDataResponse.getPointsBalance()));
            initVariables(loyaltyCustomer, loyaltyDataResponse.getVariables());
            logger.debug("Exit. Loyalty customer: " + loyaltyCustomer);
            return loyaltyCustomer;
        } else {
            LoyaltyError loyaltyError = loyaltyError(jsonResponse);
            if (loyaltyError.getResponseCode().equals(getProps().getProperty(OFFLINE_CODE, "0000"))) {
                loyaltyCustomer.setOffline(true);
                logger.debug("Exit. Offline Loyalty customer: " + loyaltyCustomer);
                return loyaltyCustomer;
            }
            logger.debug("Exit. Loyalty error: " + loyaltyError);
            return loyaltyError;
        }
    }

    private void initVariables(LoyaltyCustomer loyaltyCustomer, List<Variable> variables) {
        for (Variable variable : variables) {
            loyaltyCustomer.getVariables().put(variable.getName(), new LoyaltyVariable(variable.getName(), variable.getVariableValue(), false));
        }
    }

    private List<Variable> prepareVariablesForUpdate(LoyaltyCustomer loyaltyCustomer) {
        List<Variable> variables = new ArrayList<Variable>();
        for (LoyaltyVariable loyaltyVariable : loyaltyCustomer.getVariables().values()) {
            if (loyaltyVariable.isUpdated()) {
                Variable variable = new Variable(loyaltyVariable.getName(), loyaltyVariable.getValue());
                variables.add(variable);
            }
        }
        return variables;
    }

    private boolean updateLoyaltyTransactionData(JsonElement jsonResponse, Transaction loyaltyTransaction) {
        //TODO: Add points and other data
        return false;
    }

    private LoyaltyData initLoyaltyReward(JsonElement jsonResponse) {
        if (jsonResponse.getAsJsonObject().has(ACCOUNTID_FIELD)) {
            RedemptionResponse redemptionResponse = gsonGeneric.fromJson(jsonResponse, RedemptionResponse.class);
            Reward loyaltyReward = new Reward(
                    redemptionResponse.getAccountIdValue(),
                    redemptionResponse.getRedeemRewardsId(),
                    redemptionResponse.getTransactionUniqueReference(),
                    redemptionResponse.getValue(),
                    redemptionResponse.getPointsRedeemed());
            logger.debug("Loyalty reward: " + loyaltyReward);
            return loyaltyReward;
        } else {
            LoyaltyError loyaltyError = loyaltyError(jsonResponse);
            logger.debug("Loyalty error: " + loyaltyError);
            return loyaltyError;
        }
    }

    private LoyaltyError loyaltyError(JsonElement jsonResponse) {
        try {
            ErrorResponse errorResponse = gsonGeneric.fromJson(jsonResponse, ErrorResponse.class);
            return new LoyaltyError(
                    errorResponse.getResponse().getResponseCode(),
                    errorResponse.getResponse().getResponseMessage());
        } catch (Exception e) {
            logger.error("Error: ", e);
            return new LoyaltyError("9999", "Undefined Error");
        }
    }

    public LoyaltyData customerEnrollment(String accountIdValue, String idType, TerminalInfo terminalInfo) {
        logger.debug("Enter. Account Id: " + accountIdValue + " idType: " + idType + "Terminal Info: " + terminalInfo);
        if (!isLoyaltyEnabled()) return null;
        if (!checkToken(false)) return null;

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest(
                getProps().getProperty(PROGRAM_CODE, ""),
                getProps().getProperty(COUNTRY_CODE, ""),
                new Date(),
                getProps().getProperty(LOCATION_CODE, ""),
                idType,
                accountIdValue,
                true,
                new Date()
        );
        enrollmentRequest.getVariables().add(new Variable("Initiatedby", terminalInfo.getCashierId()));
        enrollmentRequest.getVariables().add(new Variable("TerminalId", terminalInfo.getRegisterId()));

        String serializedRequest = gsonPost.toJson(enrollmentRequest);
        JsonElement jsonResponse = postRequest(getProps().getProperty(URL, "http://localhost") + ENROLLMENT_EP, serializedRequest, true, false);
        LoyaltyData loyaltyData = initLoyaltyData(jsonResponse, accountIdValue);

        logger.debug("Exit. Loyalty data: " + loyaltyData);
        return loyaltyData;
    }

    private LoyaltyData updateCustomerTransaction(Transaction loyaltyTransaction) {
        logger.debug("Enter. Loyalty transaction: " + loyaltyTransaction);
        if (!isLoyaltyEnabled()) return null;
        if (!checkToken(false)) return null;

        TransactionRequest transactionRequest = new TransactionRequest(
                loyaltyTransaction.getLoyaltyCustomer().getAccountIdValue(),
                getProps().getProperty(PROGRAM_CODE, ""),
                getProps().getProperty(LOCATION_CODE, ""),
                loyaltyTransaction.getTransactionType(),
                loyaltyTransaction.getUniqueId(),
                loyaltyTransaction.getAmount(),
                new Date(),
                convert(loyaltyTransaction),
                transaction.getTerminalInfo().getCashierId(),
                transaction.getTerminalInfo().getRegisterId()
        );

        String serializedRequest = gsonPost.toJson(transactionRequest);
        JsonElement jsonResponse = postRequest(getProps().getProperty(URL, "http://localhost") + TRANSACTIONS_EP, serializedRequest, true, false);
        LoyaltyData loyaltyData = checkOfflineUpdateTransaction(transactionRequest, jsonResponse, loyaltyTransaction);

        logger.debug("Exit. Loyalty data: " + loyaltyData);
        return loyaltyData;
    }

    private LoyaltyData checkOfflineUpdateTransaction(TransactionRequest transactionRequest, JsonElement jsonResponse, Transaction loyaltyTransaction) {
        logger.debug("Enter");
        if (jsonResponse.getAsJsonObject().has(ACCOUNTID_FIELD)) {
            TransactionResponse transactionResponse = gsonGeneric.fromJson(jsonResponse, TransactionResponse.class);
            LoyaltyCustomer loyaltyCustomer = loyaltyTransaction.getLoyaltyCustomer();
            loyaltyCustomer.getPoints().setEarnedPoints(transactionResponse.getPointsEarned());
            loyaltyCustomer.getPoints().setBonusPoints(transactionResponse.getBonusPoints());
            loyaltyCustomer.getPoints().setBasePoints(transactionResponse.getBasePoints());

            BigDecimal redeemedPoints = BigDecimal.ZERO;
            for (Reward loyaltyReward : loyaltyTransaction.getRewards()) {
                redeemedPoints = redeemedPoints.add(loyaltyReward.getPointsRedeemed());
            }
            loyaltyCustomer.getPoints().setRedeemedPoints(redeemedPoints);
            return loyaltyCustomer;
        } else {
            LoyaltyError loyaltyError = checkOffline(loyaltyTransaction.getUniqueId(), transactionRequest, jsonResponse);
            return loyaltyError;
        }
    }

    private LoyaltyData updateCustomerVariables(Transaction loyaltyTransaction) {
        logger.debug("Enter. Loyalty transaction: " + loyaltyTransaction);
        if (!isLoyaltyEnabled()) return null;
        if (!checkToken(false)) return null;

        List<Variable> variables = prepareVariablesForUpdate(loyaltyTransaction.getLoyaltyCustomer());
        if (variables.isEmpty()) {
            logger.info("No variables to update");
            return loyaltyTransaction.getLoyaltyCustomer();
        }
        VariablesRequest variablesRequest = new VariablesRequest(
                loyaltyTransaction.getLoyaltyCustomer().getAccountIdValue(),
                getProps().getProperty(PROGRAM_CODE, ""),
                variables
        );

        String serializedRequest = gsonPost.toJson(variablesRequest);
        JsonElement jsonResponse = postRequest(getProps().getProperty(URL, "http://localhost") + VARIABLES_EP, serializedRequest, true, false);
        LoyaltyData loyaltyData = checkOfflineUpdateVariables(variablesRequest, jsonResponse, loyaltyTransaction);

        logger.debug("Exit. Loyalty data: " + loyaltyData);
        return loyaltyData;
    }

    private LoyaltyData checkOfflineUpdateVariables(VariablesRequest variablesRequest, JsonElement jsonResponse, Transaction loyaltyTransaction) {
        logger.debug("Enter");
        if (jsonResponse.getAsJsonObject().has(ACCOUNTID_FIELD)) {
            VariablesResponse variablesResponse = gsonGeneric.fromJson(jsonResponse, VariablesResponse.class);
            LoyaltyCustomer loyaltyCustomer = loyaltyTransaction.getLoyaltyCustomer();
            logger.debug("Variables successfully updated");
            return loyaltyCustomer;
        } else {
            LoyaltyError loyaltyError = checkOffline(getLoyaltyTransaction().getUniqueId() + VARIABLES_SUFFIX, variablesRequest, jsonResponse);
            return loyaltyError;
        }
    }

    private LoyaltyData electronicReceipt(Transaction loyaltyTransaction) {
        logger.debug("Enter. Loyalty transaction: " + loyaltyTransaction);
        if (!isLoyaltyEnabled()) return null;

        ElectronicReceiptRequest electronicReceiptRequest = new ElectronicReceiptRequest();
        prepareElectronicReceiptRequest(electronicReceiptRequest, loyaltyTransaction);

        String serializedRequest = gsonPost.toJson(electronicReceiptRequest);
        JsonElement jsonResponse = postRequest(getProps().getProperty(ERECEIPTS_URL, "http://localhost"), serializedRequest, false, true);
        LoyaltyData loyaltyData = checkOfflineElectronicReceipt(electronicReceiptRequest, jsonResponse, loyaltyTransaction);

        logger.debug("Exit. Loyalty data: " + loyaltyData);
        return loyaltyData;
    }

    private void prepareElectronicReceiptRequest(ElectronicReceiptRequest electronicReceiptRequest, Transaction loyaltyTransaction) {
        ElectronicReceiptTransaction electronicReceiptTransaction = new ElectronicReceiptTransaction();;
        electronicReceiptTransaction.setAccountIdValue(loyaltyTransaction.getEReceiptRecipient());
        if (loyaltyTransaction.getLoyaltyCustomer() != null) {
            logger.debug("Loyalty transaction eReceipt");
            electronicReceiptTransaction = new ElectronicReceiptLoyaltyTransaction();
            electronicReceiptTransaction.setAccountIdValue(loyaltyTransaction.getLoyaltyCustomer().getAccountIdValue());
            ((ElectronicReceiptLoyaltyTransaction)electronicReceiptTransaction).setProgramCode(
                    getProps().getProperty(PROGRAM_CODE, ""));
            ((ElectronicReceiptLoyaltyTransaction)electronicReceiptTransaction).setTransactionTypeCode(
                    loyaltyTransaction.getTransactionType());
            ((ElectronicReceiptLoyaltyTransaction)electronicReceiptTransaction).setTransactionTypeName(
                    loyaltyTransaction.getTransactionType());
             ((ElectronicReceiptLoyaltyTransaction)electronicReceiptTransaction).setBasePoints(
                    loyaltyTransaction.getLoyaltyCustomer().getPoints().getBasePoints());
            ((ElectronicReceiptLoyaltyTransaction)electronicReceiptTransaction).setBonusPoints(
                    loyaltyTransaction.getLoyaltyCustomer().getPoints().getBonusPoints());
            ((ElectronicReceiptLoyaltyTransaction)electronicReceiptTransaction).setPointsEarned(
                    loyaltyTransaction.getLoyaltyCustomer().getPoints().getEarnedPoints());
        } else {
            logger.debug("Non-loyalty transaction eReceipt");
        }
        electronicReceiptTransaction.setTransactionUniqueReference(loyaltyTransaction.getUniqueId());
        electronicReceiptTransaction.setLocationCode(getProps().getProperty(LOCATION_CODE, ""));
        electronicReceiptTransaction.setStore(loyaltyTransaction.getTerminalInfo().getStoreId());
        electronicReceiptTransaction.setInitiatedBy(loyaltyTransaction.getTerminalInfo().getCashierId());
        electronicReceiptTransaction.setTerminalId(loyaltyTransaction.getTerminalInfo().getRegisterId());
        electronicReceiptTransaction.setTimestamp(new Date());
        electronicReceiptTransaction.setValue(loyaltyTransaction.getAmount());
        electronicReceiptTransaction.setZatcaQr(loyaltyTransaction.getQrCode());
        electronicReceiptTransaction.setInvoiceId(loyaltyTransaction.getTerminalInfo().getTransactionNumber());
        electronicReceiptTransaction.setTaxInvoiceId(getProps().getProperty(TAX_INVOICE_ID, ""));
        electronicReceiptTransaction.setTransactionVariables(loyaltyTransaction.getVariables());
        electronicReceiptRequest.setTransaction(electronicReceiptTransaction);
        prepareElectronicReceiptItems(electronicReceiptTransaction, loyaltyTransaction);
        prepareElectronicReceiptTenders(electronicReceiptTransaction, loyaltyTransaction);
        prepareElectronicReceiptRewards(electronicReceiptTransaction, loyaltyTransaction);
        prepareElectronicReceiptTaxes(electronicReceiptTransaction, loyaltyTransaction);
    }

    private void prepareElectronicReceiptItems(ElectronicReceiptTransaction electronicReceiptTransaction, Transaction transaction) {
        for (Item item : transaction.getItems()) {
            TransactionProduct transactionProduct = new TransactionProduct(
                    item.getIndex(),
                    Integer.parseInt(transaction.getTerminalInfo().getTransactionNumber()),
                    item.getCode(),
                    item.getIndex(),
                    item.getDescription(),
                    item.getAdditionalDescription(),
                    item.getQuantity(),
                    item.getAmount(),
                    new Date(),
                    item.getPrice()
            );
            for (Variable variable : item.getVariables()) {
                transactionProduct.getProductVariables().add(variable);
            }
            transactionProduct.setDiscount(new Discount(item.getDiscount().getDescription(), item.getDiscount().getAmount()));
            electronicReceiptTransaction.getTransactionProductList().add(transactionProduct);
        }
    }

    private void prepareElectronicReceiptTenders(ElectronicReceiptTransaction electronicReceiptTransaction, Transaction transaction) {
        for (Tender tender : transaction.getTenders()) {
            PaymentDetail paymentDetail = new PaymentDetail(tender.getAmount(), tender.getPaymentType());
            for (AdditionalInfo additionalInfo : tender.getAdditionalInfos()) {
                if (EftPluginManager.CARD_NUMBER.equals(additionalInfo.getKey())) {
                    paymentDetail.setCardNo(additionalInfo.getValue());
                } else if (EftPluginManager.AUTHORIZATION_CODE.equals(additionalInfo.getKey())) {
                    paymentDetail.setAuth(additionalInfo.getValue());
                } else if (EftPluginManager.TERMINAL_NUMBER.equals(additionalInfo.getKey())) {
                    paymentDetail.setTerminalId(additionalInfo.getValue());
                }
            }
            electronicReceiptTransaction.getPaymentDetails().add(paymentDetail);
        }
    }

    private void prepareElectronicReceiptRewards(ElectronicReceiptTransaction electronicReceiptTransaction, Transaction transaction) {
        for (Reward reward : transaction.getRewards()) {
            //TODO: Implement also rewards. Not defined yet
            TransactionPromotion transactionPromotion = new TransactionPromotion();
            electronicReceiptTransaction.getTransactionPromotionsList().add(transactionPromotion);
        }
    }

    private void prepareElectronicReceiptTaxes(ElectronicReceiptTransaction electronicReceiptTransaction, Transaction transaction) {
        for (Vat vat : transaction.getVats()) {
            TaxDetail taxDetail = new TaxDetail(vat.getTax(), vat.getVat(), vat.getBefVat(), vat.getInclVat());
            electronicReceiptTransaction.getTaxDetails().add(taxDetail);
        }
    }

    private LoyaltyData checkOfflineElectronicReceipt(ElectronicReceiptRequest electronicReceiptRequest, JsonElement jsonResponse, Transaction loyaltyTransaction) {
        logger.debug("Enter");
        if (jsonResponse.getAsJsonObject().has(TRANSACTION_ID_FIELD)) {
            ElectronicReceiptResponse electronicReceiptResponse = gsonGeneric.fromJson(jsonResponse, ElectronicReceiptResponse.class);
            logger.debug("Variables successfully updated");
            return new LoyaltyData();
        } else {
            LoyaltyError loyaltyError = checkOffline(getLoyaltyTransaction().getUniqueId() + ERECEIPTS_SUFFIX, electronicReceiptRequest, jsonResponse);
            return loyaltyError;
        }
    }

    public LoyaltyError checkOffline(String uniqueId, PostRequest request, JsonElement jsonResponse) {
        String processPath = "";
        LoyaltyError loyaltyError = loyaltyError(jsonResponse);
        logger.debug("Loyalty error: " + loyaltyError);
        if (loyaltyError.getResponseCode().equals(getProps().getProperty(OFFLINE_CODE, "0000"))) {
            String serializedRequest = gsonPost.toJson(request);
            try {
                processPath = new File(getProps().getProperty(OFFLINE_PATH_FOLDER_PROCESS), getProps().getProperty(OFFLINE_FILENAME_MASK)
                        .concat(uniqueId)
                        .concat(".json")).getAbsolutePath();
                FileWriter writer = new FileWriter(processPath);
                writer.write(serializedRequest);
                writer.close();
            } catch (IOException e) {
                logger.error("Error fiding the path specified" + processPath);
                e.printStackTrace();
            }
        }
        return loyaltyError;
    }

    public LoyaltyData rewardsRedemption(BigDecimal amount) {
        logger.debug("Enter. Amount: " + amount);
        LoyaltyData loyaltyReward = null;
        if (!isLoyaltyEnabled()) return loyaltyReward;
        if (!checkToken(false)) return loyaltyReward;

        RedemptionRequest redemptionRequest = new RedemptionRequest(
                transaction.getLoyaltyCustomer().getAccountIdValue(),
                getProps().getProperty(REWARDS_CODE, ""),
                TYPECODE_REDEMPTION,
                transaction.getUniqueId() + "R" + (transaction.getRewards().size() + 1),
                amount,
                getProps().getProperty(LOCATION_CODE, ""),
                new Date(),
                getProps().getProperty(PROGRAM_CODE, ""),
                transaction.getTerminalInfo().getCashierId(),
                transaction.getTerminalInfo().getRegisterId()
        );

        String serializedRequest = gsonPost.toJson(redemptionRequest);
        JsonElement jsonResponse = postRequest(getProps().getProperty(URL, "http://localhost") + REDEMPTION_EP, serializedRequest, true, false);

        //DMA-CANCEL_REDEMPTION#A BEG
        ErrorResponse errorResponse = gsonGeneric.fromJson(jsonResponse, ErrorResponse.class);
        if (errorResponse.getResponse() != null && errorResponse.getResponse().getResponseCode().equals(getProps().getProperty(OFFLINE_CODE, "0000"))) {
            logger.debug("Server is Offline.");
            handleCancelRedemption( redemptionRequest.getTransactionUniqueReference(), false);
        }
        //DMA-CANCEL_REDEMPTION#A END

        loyaltyReward = initLoyaltyReward(jsonResponse);
        logger.debug("Exit. Loyalty Reward: " + loyaltyReward);
        return loyaltyReward;
    }

    //DMA-CANCEL_REDEMPTION#A BEG
    private boolean handleCancelRedemption(String transactionUnique, boolean handleOffline){
        boolean success = false;
        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0")))
                    .setConnectionRequestTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0")))
                    .setSocketTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0"))).build();
            HttpClient httpClient = HttpClients
                    .custom()
                    .setDefaultRequestConfig(config)
                    .build();
            Unirest.setHttpClient(httpClient);
            HttpResponse<String> response = Unirest.put(getProps().getProperty(URL, "http://localhost") + CANCEL_REDEMPTION_EP)
                    .header("Accept", "*/*")
                    .header("Authorization", "bearer " + token.getAccessToken())
                    .queryString("TransactionUniqueRef", transactionUnique)
                    .queryString("ProgramCode", getProps().getProperty(PROGRAM_CODE, ""))
                    .asString();

            logger.info("Status: " + response.getStatus());
            logger.info("Body: " + response.getBody());
            if (response.getStatus() == HttpStatus.SC_OK) {
                logger.info("CANCEL REWARD OK");
                success = true;
            } else {
                if (!handleOffline) {
                    writeOfflineCancelReward(transactionUnique);
                    logger.warn("Error from server: " + response.getStatus());
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }

        return success;
    }

    private void writeOfflineCancelReward(String uniqueId) {
        String processPath = "";
        logger.debug("uniqueId: " + uniqueId);

        try {
            processPath = new File(getProps().getProperty(OFFLINE_PATH_FOLDER_PROCESS), getProps().getProperty(OFFLINE_FILENAME_MASK)
                    .concat(uniqueId + REWARD_CANCEL_SUFFIX)
                    .concat(".DAT")).getAbsolutePath();
            FileWriter writer = new FileWriter(processPath);
            writer.write(uniqueId);
            writer.close();
        } catch (IOException e) {
            logger.error("Error fiding the path specified" + processPath);
            e.printStackTrace();
        }
    }
    private void handleOfflineCancelReward() {
        String moveTo = "";
        String uniqueId = "";
        boolean success = false;
        File[] listFiles = new File(getProps().getProperty(OFFLINE_PATH_FOLDER_PROCESS)).listFiles();
        if (listFiles.length > 0) {
            for (int i = 0; i < listFiles.length; i++) {
                File file = listFiles[i];
                try {
                    FileReader reader = new FileReader(file.getPath());

                    if (file.getName().contains(REWARD_CANCEL_SUFFIX)) {
                        BufferedReader br = new BufferedReader(reader);
                        uniqueId = br.readLine();
                        success = handleCancelRedemption(uniqueId, true);
                    }

                    reader.close();

                    if (success) {
                        logger.debug("Updating reward cancel done successfully");
                        moveTo = getProps().getProperty(OFFLINE_PATH_FOLDER_SUCCESS).concat(file.getName());
                        File f = new File(moveTo);
                        FileWriter fw = new FileWriter(f.getPath());
                        fw.write(uniqueId);
                        fw.close();
                        File removedFile = new File(file.getAbsolutePath().toString());
                        removedFile.delete();

                    } else {
                        logger.debug("Server is Offline.");
                    }
                } catch (Exception e) {
                    logger.error("Error ", e);
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }
    //DMA-CANCEL_REDEMPTION#A END

    private List<TransactionItem> convert(Transaction transaction) {
        List<TransactionItem> transactionItems = new ArrayList<TransactionItem>();

        for (Item item : transaction.getItems()) {
            TransactionItem transactionItem = new TransactionItem(
                    item.getCode(),
                    item.getQuantity(),
                    item.getAmount().divide(item.getQuantity() != 0 ? new BigDecimal(item.getQuantity()) : BigDecimal.ONE, 2, RoundingMode.HALF_EVEN),
                    item.getTimestamp()
            );
            transactionItems.add(transactionItem);
        }
        return transactionItems;
    }

    public boolean initialize(int decimals) {
        logger.debug("Enter");
        super.initialize(decimals);
        setActive(checkToken(true));
        if (isActive()) {
            Thread thread = new Thread(this);
            thread.start();
        }
        logger.debug("Exit. Is Active: " + isActive());
        return isActive();
    }

    @Override
    public void startTransaction(String uniqueId, LoyaltyCustomer loyaltyCustomer, TerminalInfo terminalInfo, boolean returnTransaction, boolean enrollment) {
        logger.debug("Enter");
        String transactionType = TYPE_POS_EARN;
        if (returnTransaction) {
            transactionType = TYPE_POS_RETURN;
        }

        if (loyaltyCustomer != null && enrollment) {
            setEnrollmentVariables(loyaltyCustomer);
        }
        logger.debug("Transaction type : " + transactionType);
        transaction = new Transaction(uniqueId, loyaltyCustomer, terminalInfo, transactionType);
        logger.debug("Exit");
    }

    @Override
    public LoyaltyData endTransaction(boolean canceled) {
        logger.debug("Enter");
        LoyaltyData loyaltyData = null;
        if (isLoyaltyTransaction()) {
            logger.debug("Loyalty customer");
            if (canceled) {
                loyaltyData = cancelRewards();
            } else {
                updateCustomerVariables(transaction);
                loyaltyData = updateCustomerTransaction(transaction);
            }
        } else {
            logger.debug("Non loyalty customer");
        }
        electronicReceipt(transaction);

        transaction = new Transaction();
        logger.debug("Exit. Loyalty Data: " + loyaltyData);
        return loyaltyData;
    }

    private LoyaltyData cancelRewards() {
        logger.debug("Enter");
        for (Reward loyaltyReward : transaction.getRewards()) {

        }
        logger.debug("Exit");
        return new LoyaltyData();
    }

    @Override
    public boolean isLoyaltyTransaction() {
        LoyaltyCustomer loyaltyCustomer = transaction.getLoyaltyCustomer();
        return loyaltyCustomer != null && loyaltyCustomer.getAccountIdValue() != null;
    }

    @Override
    public void addItem(Item item) {
        transaction.getItems().add(item);
    }

    @Override
    public void addTender(Tender tender) {
        transaction.getTenders().add(tender);
    }

    @Override
    public void addVat(Vat vat) {
        transaction.getVats().add(vat);
    }

    @Override
    public void removeItem(Item item) {
        for (Item transactionItem : transaction.getItems()) {
            if (transactionItem.equals(item)) {
                transaction.getItems().remove(transactionItem);
                break;
            }
        }
    }

    @Override
    public void updateTransactionTotal(BigDecimal amount) {
        transaction.setAmount(amount);
    }

    @Override
    public Transaction getLoyaltyTransaction() {
        return transaction;
    }

    @Override
    public boolean isCustomerMaskEnabled() {
        return isLoyaltyEnabled() && getProps().getProperty("customer-mask-enabled", "true").equals("true");
    }

    private boolean retrieveToken() {
        if (!isLoyaltyEnabled()) return true;
        boolean status = false;
        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0")))
                    .setConnectionRequestTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0")))
                    .setSocketTimeout(Integer.parseInt(getProps().getProperty(TIMEOUT, "0"))).build();
            HttpClient httpClient = HttpClients
                    .custom()
                    .setDefaultRequestConfig(config)
                    .build();
            Unirest.setHttpClient(httpClient);
            HttpResponse<String> response = Unirest.post(getProps().getProperty(URL, "http://localhost") + TOKEN_EP)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .field("username", getProps().getProperty(USERNAME, "dummy"))
                    .field("password", getProps().getProperty(PASSWORD, "dummy"))
                    .field("grant_type", "password")
                    .asString();

            logger.info("Status: " + response.getStatus());
            logger.info("Body: " + response.getBody());
            if (response.getStatus() == HttpStatus.SC_OK) {
                JsonElement jsonElement = new JsonParser().parse(response.getBody());

                token = gsonToken.fromJson(jsonElement, BearerToken.class);
                status = saveToken(token);
            } else {
                logger.warn("Error from server: " + response.getStatus());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            return false;
        }
        logger.debug("Exit: Status: " + status);
        return status;
    }

    private boolean checkToken(boolean fromFile) {
        logger.debug("Enter. From file: " + fromFile);
        boolean status = false;
        try {
            if (fromFile && new File(TOKEN_FILE).exists()) {
                JsonReader reader = new JsonReader(new FileReader(TOKEN_FILE));
                token = gsonToken.fromJson(reader, BearerToken.class);
            } else {
                if (fromFile) logger.debug("File Token not found");
                logger.debug("Checking token in memory");
            }
            if (isExpired(token)) {
                logger.debug("Token expired, asking new token");
                status = retrieveToken();
            } else {
                status = true;
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("Exit returning: " + status);
        return status;
    }

    private boolean isExpired(BearerToken token) {
        Date expirationDate = token.getExpires();
        return expirationDate == null || expirationDate.before(new Date());
    }

    private boolean saveToken(BearerToken token) {
        logger.debug("Enter. Token: " + token);
        FileWriter writer = null;
        boolean status = true;
        try {
            String json = gsonToken.toJson(token);
            writer = new FileWriter(TOKEN_FILE);
            writer.write(json);
        } catch (Exception e) {
            logger.error("Error: ", e);
            status = false;
        } finally {
            try {
                writer.close();
            } catch (IOException ioe) {
                logger.error("Error closing: ", ioe);
                status = false;
            }
        }
        logger.debug("Exit. Status: " + status);
        return status;
    }

    @Override
    public List<PhoneCode> getCountryCodes() {
        logger.debug("Enter");
        List<PhoneCode> phoneCodes = new ArrayList<PhoneCode>();
        try {
            for (int index = 1; index < 99; index++) {
                String value = getProps().getProperty("phone-number.country-code." + index, ";");
                if (value.length() > 1) {
                    String[] tokens = value.split(";");
                    if (tokens[1].length() > 0) {
                        phoneCodes.add(new PhoneCode(tokens[0], tokens[1],
                                Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), tokens[4].toLowerCase().equals("true")));
                    } else break;
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("Exit returning " + phoneCodes.size());
        return phoneCodes;
    }

    private void setEnrollmentVariables(LoyaltyCustomer loyaltyCustomer) {
        logger.debug("Enter");
        Map<String, LoyaltyVariable> loyaltyVariables = loyaltyCustomer.getVariables();
        for (int index = 1; index < 99; index++) {
            String value = getProps().getProperty("enrollment.promovar." + index, ";");
            if (value.length() > 1) {
                String[] tokens = value.split(";");
                if (tokens[0].length() > 0 && tokens[1].length() > 0) {
                    loyaltyVariables.put(PROMO_HEADER + tokens[0], new LoyaltyVariable(tokens[0], tokens[1], false));
                } else break;
            } else break;;
        }
        logger.debug("Exit");
    }

    @Override
    public void run() {
        logger.debug("Start checking for pending updating request due to Server Offline");
        while (true) {
            handleOfflineUpdateData();
            handleOfflineCancelReward();
            try {
                Thread.sleep(Integer.parseInt(getProps().getProperty(OFFLINE_DELAY_THREAD, "10000")));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setVariable(String variable, String value) {
        if (transaction.getLoyaltyCustomer() != null) transaction.getLoyaltyCustomer().getVariables().put(variable, new LoyaltyVariable(variable, value, true));
    }

    @Override
    public String getVariable(String name) {
        LoyaltyVariable variable = transaction.getLoyaltyCustomer() == null ? null : transaction.getLoyaltyCustomer().getVariables().get(name);
        return variable == null ? "" : variable.getValue();
    }

    @Override
    public List<Variable> getTransactionVariables() {
        return transaction.getVariables();
    }

    private void handleOfflineUpdateData() {
        ErrorResponse errorResponse;
        String moveTo = "";
        File[] listFiles = new File(getProps().getProperty(OFFLINE_PATH_FOLDER_PROCESS)).listFiles();
        if (listFiles.length > 0) {
            for (int i = 0; i < listFiles.length; i++) {
                File file = listFiles[i];
                try {
                    FileReader reader = new FileReader(file.getPath());
                    String url = URL + TRANSACTIONS_EP;
                    boolean eReceipt = false;
                    Class deserializedClass = TransactionRequest.class;

                    if (file.getName().contains(VARIABLES_SUFFIX)) {
                        url = URL + VARIABLES_EP;
                        deserializedClass = VariablesRequest.class;
                    } else if (file.getName().contains(ERECEIPTS_SUFFIX)) {
                        url = ERECEIPTS_URL;
                        deserializedClass = ElectronicReceiptRequest.class;
                        eReceipt = true;
                    } else if(file.getName().contains(AymCouponManager.COUPON_SUFFIX)){
                        url = URL + AymCouponSettings.getInstance().useCouponEndPoint();
                        deserializedClass = CouponUsed.class;
                    }
                    String jsonRequest = gsonPost.toJson(gsonGeneric.fromJson(reader, deserializedClass));
                    reader.close();
                    logger.debug("Pending request:  " + jsonRequest);
                    JsonElement jsonResponse = postRequest(getProps().getProperty(url, "http://localhost"), jsonRequest, !eReceipt, eReceipt);
                    if (jsonResponse.getAsJsonObject().has(ACCOUNTID_FIELD) || jsonResponse.getAsJsonObject().has(TRANSACTION_ID_FIELD)) {
                        logger.debug("Updating data done successfully");
                        moveTo = getProps().getProperty(OFFLINE_PATH_FOLDER_SUCCESS).concat(file.getName());
                    } else {
                        errorResponse = gsonGeneric.fromJson(jsonResponse, ErrorResponse.class);
                        if (errorResponse.getResponse().getResponseCode().equals(getProps().getProperty(OFFLINE_CODE, "0000"))) {
                            logger.debug("Server is Offline.");
                            break;
                        } else {
                            moveTo = getProps().getProperty(OFFLINE_PATH_FOLDER_ERROR).concat(file.getName());
                        }
                    }
                    File f = new File(moveTo);
                    FileWriter fw = new FileWriter(f.getPath());
                    fw.write(jsonRequest);
                    fw.close();
                    File removedFile = new File(file.getAbsolutePath().toString());
                    removedFile.delete();
                } catch (Exception e) {
                    logger.error("Error ", e);
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    public Gson getGsonGeneric() {
        return gsonGeneric;
    }

    public Gson getGsonPost() {
        return gsonPost;
    }
}
