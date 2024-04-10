package com.ncr.loyalty;

import com.ncr.common.data.TerminalInfo;
import com.ncr.loyalty.aym.data.Variable;
import com.ncr.loyalty.data.PhoneCode;
import com.ncr.loyalty.transaction.*;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;

public class BasicLoyaltyService extends LoyaltyService {
    private static final Logger logger = Logger.getLogger(BasicLoyaltyService.class);
    private static BasicLoyaltyService instance = null;

    public static BasicLoyaltyService getInstance() {
        if (instance == null) {
            instance = new BasicLoyaltyService();
        }
        return instance;
    }

    private BasicLoyaltyService() {
        super();
    }

    @Override
    public void startTransaction(String uniqueId, LoyaltyCustomer loyaltyCustomer, TerminalInfo terminalInfo, boolean returnTransaction, boolean enrollment) {
    }

    @Override
    public LoyaltyData endTransaction(boolean canceled) {
        return null;
    }

    @Override
    public boolean isLoyaltyTransaction() {
        return false;
    }

    @Override
    public void addItem(Item item) {
    }

    @Override
    public void addTender(Tender tender) {
    }

    @Override
    public void addVat(Vat vat) {
    }

    @Override
    public void removeItem(Item item) {
    }

    @Override
    public void updateTransactionTotal(BigDecimal amount) {
    }

    @Override
    public LoyaltyData customerIdentification(String accountIdValue, String idType) {
        return null;
    }

    @Override
    public LoyaltyData customerEnrollment(String accountIdValue, String idType, TerminalInfo terminalInfo) {
        return null;
    }

    @Override
    public LoyaltyData rewardsRedemption(BigDecimal amount) {
        return null;
    }

    @Override
    public List<PhoneCode> getCountryCodes() {
        return null;
    }

    @Override
    public Transaction getLoyaltyTransaction() {
        return null;
    }

    @Override
    public boolean isCustomerMaskEnabled() {
        return false;
    }

    @Override
    public void run() {
    }

    @Override
    public void setVariable(String variable, String value) {
    }

    @Override
    public String getVariable(String variable) {
        return null;
    }

    @Override
    public List<Variable> getTransactionVariables() {
        return null;
    }
}
