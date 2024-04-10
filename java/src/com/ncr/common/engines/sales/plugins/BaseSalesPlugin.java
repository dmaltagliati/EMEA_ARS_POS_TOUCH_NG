package com.ncr.common.engines.sales.plugins;

import com.ncr.common.data.PluginActionStatus;
import com.ncr.common.data.PluginActionStatusType;
import com.ncr.common.data.nextgen.Article;
import com.ncr.common.data.nextgen.Tender;
import com.ncr.common.data.nextgen.Transaction;
import org.apache.log4j.Logger;

import java.util.Properties;

public class BaseSalesPlugin implements SalesPlugin {
    private static final Logger logger = Logger.getLogger(BaseSalesPlugin.class);
    private Properties props = new Properties();

    @Override
    public String getId() {
        return SalesPlugin.TRANSACTION_STORE_PLUGIN;
    }

    @Override
    public void init(Properties props) {
        this.props = props;
    }

    @Override
    public PluginActionStatus recordItem(Transaction transaction, Article article) {
        return null;
    }

    @Override
    public PluginActionStatus itemRecorded(Transaction transaction, Article article) {
        return null;
    }

    @Override
    public PluginActionStatus recordTender(Transaction transaction, Tender tender) {
        return null;
    }

    @Override
    public PluginActionStatus tenderRecorded(Transaction transaction, Tender tender) {
        return null;
    }

    @Override
    public PluginActionStatus startTransaction(Transaction transaction) {
        if ("false".equals(props.getProperty(this.getClass().getSimpleName() + "." + SalesPlugin.START_TRANSACTION_ENABLED, "true"))) {
            logger.debug("Action disabled for this plugin");
            return PluginActionStatus.builder().status(PluginActionStatusType.Disabled).description("Start Transaction Disabled").plugin(this.getClass().getName()).build();
        }

        return startTransactionPayload(transaction);
    }

    protected PluginActionStatus startTransactionPayload(Transaction transaction) {
        //TODO: Activities at start of transaction
        return PluginActionStatus.builder().status(PluginActionStatusType.Ok).description("Start Transaction OK").plugin(this.getClass().getName()).build();
    }

    @Override
    public PluginActionStatus transactionStarted(Transaction transaction) {
        if ("false".equals(props.getProperty(this.getClass().getSimpleName() + "." + SalesPlugin.TRANSACTION_STARTED_ENABLED, "true"))) {
            logger.debug("Action disabled for this plugin");
            return PluginActionStatus.builder().status(PluginActionStatusType.Disabled).description("Transaction Started Disabled").plugin(this.getClass().getName()).build();
        }

        return transactionStartedPayload(transaction);
    }

    protected PluginActionStatus transactionStartedPayload(Transaction transaction) {
        //TODO: Activities at the end of start of transaction
        return PluginActionStatus.builder().status(PluginActionStatusType.Ok).description("Transaction Started OK").plugin(this.getClass().getName()).build();
    }

    @Override
    public PluginActionStatus endTransaction(Transaction transaction) {
        if ("false".equals(props.getProperty(this.getClass().getSimpleName() + "." + SalesPlugin.END_TRANSACTION_ENABLED, "true"))) {
            logger.debug("Action disabled for this plugin");
            return PluginActionStatus.builder().status(PluginActionStatusType.Disabled).description("End Transaction disabled").plugin(this.getClass().getName()).build();
        }

        return endTransactionPayload(transaction);
    }

    protected PluginActionStatus endTransactionPayload(Transaction transaction) {
        //TODO: Activities at beginning of end of transaction
        return PluginActionStatus.builder().status(PluginActionStatusType.Ok).description("End Transaction OK").plugin(this.getClass().getName()).build();
    }

    @Override
    public PluginActionStatus transactionEnded(Transaction transaction) {
        if ("false".equals(props.getProperty(this.getClass().getSimpleName() + "." + SalesPlugin.TRANSACTION_ENDED_ENABLED, "true"))) {
            logger.debug("Action disabled for this plugin");
            return PluginActionStatus.builder().status(PluginActionStatusType.Disabled).description("Transaction Ended disabled").plugin(this.getClass().getName()).build();
        }

        return transactionEndedPayload(transaction);
    }

    protected PluginActionStatus transactionEndedPayload(Transaction transaction) {
        //TODO: Activities at end of end of transaction
        return PluginActionStatus.builder().status(PluginActionStatusType.Ok).description("Transaction Ended OK").plugin(this.getClass().getName()).build();
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }
}
