package com.ncr.common.engines.sales;

public interface SalesEngineInterface {
    void init();

    int startTransaction();
    int transactionStarted();

    int endTransaction();
    int transactionEnded();
}
