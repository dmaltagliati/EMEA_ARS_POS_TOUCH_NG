package com.ncr.common.engines.sales.plugins;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ncr.common.data.PluginActionStatus;
import com.ncr.common.data.PluginActionStatusType;
import com.ncr.common.data.nextgen.Transaction;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class TransactionStorePlugin extends BaseSalesPlugin {
    private static final Logger logger = Logger.getLogger(TransactionStorePlugin.class);
    private static final String TRANSACTIONS_DIR = "transactions";
    private static final String TRANSACTION_HEADER = "tr_";
    private Gson gson = new GsonBuilder().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    @Override
    public String getId() {
        return SalesPlugin.TRANSACTION_STORE_PLUGIN;
    }

    @Override
    protected PluginActionStatus transactionEndedPayload(Transaction transaction) {
        if (serializeTransaction(transaction)) {
            return PluginActionStatus.builder().status(PluginActionStatusType.Ok).description("Transaction Ended OK").plugin(this.getClass().getName()).build();
        } else {
            return PluginActionStatus.builder().status(PluginActionStatusType.SerializationError).description("Error Serializing transaction").plugin(this.getClass().getName()).build();
        }
    }

    private boolean serializeTransaction(Transaction transaction) {
        File directory = new File(TRANSACTIONS_DIR);

        try {
            if (!directory.exists()) directory.mkdir();
        } catch (SecurityException e) {
            logger.error("Error creating dir: ", e);
            return false;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String filename = TRANSACTION_HEADER + transaction.getTerminal().getStoreId() + transaction.getTerminal().getRegisterId() + transaction.getTerminal().getTransactionNumber()
                + "_" + formatter.format(transaction.getEndTimestamp()) + ".json";
        FileWriter writer = null;
        try {
            writer = new FileWriter(TRANSACTIONS_DIR + "/" + filename);
            writer.write(gson.toJson(transaction));
        } catch (IOException e) {
            logger.error("Error writing file: ", e);
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                logger.error("Error closing file: ", e);
            }
        }
        return true;
    }
}
