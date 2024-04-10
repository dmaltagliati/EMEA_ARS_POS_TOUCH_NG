package com.ncr.loyalty;

import com.ncr.common.data.TerminalInfo;
import com.ncr.loyalty.aym.AymLoyaltyService;
import com.ncr.loyalty.transaction.*;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Properties;

public abstract class LoyaltyService implements LoyaltyServiceInterface, Runnable {
    private static final Logger logger = Logger.getLogger(LoyaltyService.class);
    public static final String PROPERTIES = "conf/loyalty.properties";
    public static final String AYM = "AYM";
    public static final String PROMO_HEADER = "Promo_";
    public static final String BRANCH_PROP = "branch.code";
    private static Properties props = new Properties();
    private int decimals = 2;
    private boolean active = false;
    private boolean offline = false;
    private String branch;

    public LoyaltyService() {}

    private static void loadProperties() {
        logger.debug("Enter");
        try {
            props.load(new FileInputStream(PROPERTIES));
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("Exit");
    }

    public boolean isLoyaltyEnabled() {
        logger.debug("Enter");
        boolean property = true;
        try {
            property = props.getProperty("enabled", "false").equals("true");
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("Exit returning " + property);
        return property;
    }

    @Override
    public boolean initialize(int decimals) {
        this.decimals = decimals;
        return true;
    }

    public static LoyaltyServiceInterface getService() {
        loadProperties();
        String serviceName = props.getProperty("type", "");
        logger.debug("Loyalty Service: " + serviceName);
        if (AYM.equals(serviceName)) {
            return AymLoyaltyService.getInstance();
        }
        return BasicLoyaltyService.getInstance();
    }

    @Override
    public abstract void startTransaction(String uniqueId, LoyaltyCustomer loyaltyCustomer, TerminalInfo terminalInfo, boolean returnTransaction, boolean enrollment);

    @Override
    public abstract LoyaltyData endTransaction(boolean canceled);

    @Override
    public abstract boolean isLoyaltyTransaction();

    @Override
    public abstract void addItem(Item item);

    @Override
    public abstract void addTender(Tender tender);

    @Override
    public abstract void addVat(Vat vat);

    @Override
    public abstract void removeItem(Item item);

    @Override
    public abstract void updateTransactionTotal(BigDecimal amount);

    @Override
    public abstract LoyaltyData customerIdentification(String accountIdValue, String idType);

    @Override
    public abstract LoyaltyData customerEnrollment(String accountIdValue, String idType, TerminalInfo terminalInfo);

    @Override
    public abstract LoyaltyData rewardsRedemption(BigDecimal amount);

    @Override
    public abstract Transaction getLoyaltyTransaction();

    @Override
    public abstract boolean isCustomerMaskEnabled();

    public abstract void run();

    public Properties getProps() {
        return props;
    }

    public int getDecimals() {
        return decimals;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    @Override
    public abstract void setVariable(String variable, String value);

    public abstract String getVariable(String variable);

    @Override
    public String getBranch() {
        return props.getProperty(BRANCH_PROP,"10");
    }

    @Override
    public void setBranch(String branch) {
        this.branch = branch;
    }
}
