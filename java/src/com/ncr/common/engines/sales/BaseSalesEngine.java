package com.ncr.common.engines.sales;

import com.ncr.Sales;
import com.ncr.common.data.PluginActionStatus;
import com.ncr.common.data.PluginActionStatusType;
import com.ncr.common.data.TerminalInfo;
import com.ncr.common.data.nextgen.Item;
import com.ncr.common.data.nextgen.Transaction;
import com.ncr.common.data.nextgen.Vat;
import com.ncr.common.engines.sales.plugins.SalesPlugin;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

import static com.ncr.FmtIo.editNum;
import static com.ncr.FmtIo.roundBy;
import static com.ncr.Struc.*;
import static com.ncr.Table.reg;

public class BaseSalesEngine implements SalesEngineInterface {
    private static BaseSalesEngine instance = null;
    private static final Logger logger = Logger.getLogger(BaseSalesEngine.class);
    private static final String SALES_ENGINE_PROPERTIES = "conf/sales-engine.properties";
    private static final String PLUGINS_HEADER = "plugins.";
    private static final String ENABLED = "enabled";
    private Transaction transaction;
    private boolean enabled;
    private Properties props = new Properties();
    private Map<String, SalesPlugin> plugins = new HashMap<String, SalesPlugin>();
    public static final BigDecimal decimals = new BigDecimal(Math.pow(10, tnd[0].dec));

    private BaseSalesEngine() {}

    public static BaseSalesEngine getInstance() {
        if (instance == null) instance = new BaseSalesEngine();
        return instance;
    }

    @Override
    public void init() {
        loadProperties();
        if (!enabled) return;
        logger.info("Initializing plugins");
        for (SalesPlugin plugin : plugins.values()) {
            plugin.init(props);
        }
    }

    private void loadProperties() {
        try {
            props.load(new FileInputStream(SALES_ENGINE_PROPERTIES));
            enabled = Boolean.parseBoolean(props.getProperty(ENABLED, "false"));
            if (enabled) loadPlugins();
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    private void loadPlugins() {
        String packageName = SalesPlugin.class.getPackage().getName();
        if (!plugins.isEmpty()) {
            logger.info("Plugins already loaded");
            return;
        }

        logger.debug("Loading plugins");
        try {
            for (Object object : props.keySet()) {
                String key = (String)object;
                if (!key.startsWith(PLUGINS_HEADER)) continue;

                String className = key.substring(PLUGINS_HEADER.length());
                logger.debug("Plugin name: [" + className + "]");
                if (props.getProperty(PLUGINS_HEADER + className, "false").equals("true")) {
                    Class<SalesPlugin> salesPluginClass = (Class<SalesPlugin>) Class.forName(packageName + "." + className);
                    SalesPlugin salesPlugin = salesPluginClass.newInstance();
                    plugins.put(salesPlugin.getId(), salesPlugin);
                    logger.info("Plugin " + className + " enabled");
                }
            }
        } catch (Exception e) {
            logger.error("Error creating plugins", e);
        }
    }

    @Override
    public int startTransaction() {
        if (!enabled) return 0;

        transaction = Transaction.builder()
                .terminal(new TerminalInfo(
                        editNum(ctl.sto_nbr, 4),
                        editNum(ctl.reg_nbr, 3),
                        editNum(ctl.ckr_nbr, 3),
                        editNum(ctl.tran, 4)))
                .startTimestamp(new Date())
                .items(new ArrayList<Item>())
                .build();

        logger.info("Plugins start transaction action");
        for (SalesPlugin plugin : plugins.values()) {
            PluginActionStatus status = plugin.startTransaction(transaction);
            if (PluginActionStatusType.Ok.getCode() < status.getStatus().getCode()) {
                logger.warn(status.getPlugin() + ": " + status.getDescription());
            }
        }
        return 0;
    }

    @Override
    public int transactionStarted() {
        if (!enabled) return 0;

        logger.info("Plugins transaction started action");
        for (SalesPlugin plugin : plugins.values()) {
            PluginActionStatus status = plugin.transactionStarted(transaction);
            if (PluginActionStatusType.Ok.getCode() < status.getStatus().getCode()) {
                logger.warn(status.getPlugin() + ": " + status.getDescription());
            }
        }
        return 0;
    }

    @Override
    public int endTransaction() {
        if (!enabled) return 0;

        buildTransactionSummary(transaction);

        logger.info("Plugins end transaction action");
        for (SalesPlugin plugin : plugins.values()) {
            PluginActionStatus status = plugin.endTransaction(transaction);
            if (PluginActionStatusType.Ok.getCode() < status.getStatus().getCode()) {
                logger.warn(status.getPlugin() + ": " + status.getDescription());
            }
        }
        return 0;
    }

    @Override
    public int transactionEnded() {
        if (!enabled) return 0;

        transaction.setEndTimestamp(new Date());
        logger.info("Plugins transaction ended action");
        for (SalesPlugin plugin : plugins.values()) {
            PluginActionStatus status = plugin.transactionEnded(transaction);
            if (PluginActionStatusType.Ok.getCode() < status.getStatus().getCode()) {
                logger.warn(status.getPlugin() + ": " + status.getDescription());
            }
        }
        return 0;
    }

    private void buildTransactionSummary(Transaction transaction) {
        buildVatSummary(transaction);
    }

    private void buildVatSummary(Transaction transaction) {
        transaction.setVats(new ArrayList<Vat>());

        for (int ind = 0; ind < vat.length; ind++) {
            int rec = reg.find(7, 11 + ind);
            if (rec < 1) continue;
            Sales sls = reg.sales[rec - 1][0];
            if (sls.isZero()) continue;
            Vat vatDetails = Vat.builder()
                    .code("" + ind)
                    .gross(new BigDecimal(sls.total).divide(decimals))
                    .amount(new BigDecimal(roundBy(sls.total * vat[ind].rate * 10 / (1000 + vat[ind].rate), 10)).divide(decimals))
                    .rate(new BigDecimal(vat[ind].rate).divide(BigDecimal.TEN))
                    .text(vat[ind].text).build();
            transaction.getVats().add(vatDetails);
        }
    }
}
