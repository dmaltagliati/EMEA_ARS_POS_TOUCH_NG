package com.ncr.common.engines.sales.plugins;

import com.ncr.common.data.PluginActionStatus;
import com.ncr.common.data.nextgen.Article;
import com.ncr.common.data.nextgen.Tender;
import com.ncr.common.data.nextgen.Transaction;

import java.util.Properties;

public interface SalesPlugin {
    String BASIC_SALES_PLUGIN = "BASIC_SALES";
    String TRANSACTION_STORE_PLUGIN = "TRANSACTION_STORE";

    String START_TRANSACTION_ENABLED = "start-transaction.enabled";
    String TRANSACTION_STARTED_ENABLED = "transaction-started.enabled";
    String END_TRANSACTION_ENABLED = "end-transaction.enabled";
    String TRANSACTION_ENDED_ENABLED = "transaction-ended.enabled";

    String getId();
    void init(Properties props);

    PluginActionStatus recordItem(Transaction transaction, Article article);
    PluginActionStatus itemRecorded(Transaction transaction, Article article);

    PluginActionStatus recordTender(Transaction transaction, Tender tender);
    PluginActionStatus tenderRecorded(Transaction transaction, Tender tender);

    PluginActionStatus startTransaction(Transaction transaction);
    PluginActionStatus transactionStarted(Transaction transaction);

    PluginActionStatus endTransaction(Transaction transaction);
    PluginActionStatus transactionEnded(Transaction transaction);
}
