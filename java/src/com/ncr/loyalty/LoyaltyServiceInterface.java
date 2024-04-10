package com.ncr.loyalty;

import com.ncr.common.data.TerminalInfo;
import com.ncr.loyalty.aym.data.Variable;
import com.ncr.loyalty.data.PhoneCode;
import com.ncr.loyalty.transaction.*;

import java.math.BigDecimal;
import java.util.List;

public interface LoyaltyServiceInterface {
    String ROUNDING_VARIABLE = "Rounding";
    String DONATION_VARIABLE = "Donation";
    String SERIAL_NO_VARIABLE = "SerialNo";
    String CHANGE_VARIABLE = "Change";

    boolean isLoyaltyEnabled();
    boolean initialize(int decimals);
    void startTransaction(String uniqueId, LoyaltyCustomer loyaltyCustomer, TerminalInfo terminalInfo, boolean returnTransaction, boolean enrollment);
    boolean isLoyaltyTransaction();
    LoyaltyData endTransaction(boolean canceled);
    LoyaltyData customerIdentification(String accountIdValue, String idType);
    LoyaltyData customerEnrollment(String accountIdValue, String idType, TerminalInfo terminalInfo);
    LoyaltyData rewardsRedemption(BigDecimal amount);
    List<PhoneCode> getCountryCodes();
    void addItem(Item item);
    void removeItem(Item item);
    void addTender(Tender tender);
    void addVat(Vat vat);
    void updateTransactionTotal(BigDecimal amount);
    Transaction getLoyaltyTransaction();
    boolean isCustomerMaskEnabled();
    boolean isActive();
    void setActive(boolean active);
    boolean isOffline();
    void setOffline(boolean offline);
    void setVariable(String variable, String value);
    String getVariable(String variable);
    List<Variable> getTransactionVariables();
    String getBranch();
    void setBranch(String branch);
}
