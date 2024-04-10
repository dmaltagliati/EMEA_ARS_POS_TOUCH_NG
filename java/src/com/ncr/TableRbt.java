package com.ncr;

/*******************************************************************
 * rebate table entry
 *******************************************************************/
public class TableRbt {
    /**
     * selective itemizer
     **/
    String text;
    /**
     * max employee discount rate (1 dec assumed)
     **/
    int rate_empl;
    /**
     * max customer discount rate (1 dec assumed)
     **/
    int rate_cust;
    /**
     * max item % discount (1 dec assumed)
     **/
    int rate_item;
    /**
     * max item $ discount (1 dec assumed)
     **/
    int rate_ival;
    /**
     * discount granted
     **/
    long amt;
    /**
     * bonuspoints granted
     **/
    int pnt;
    /**
     * sales with possible discount
     **/
    Sales dsc_sls = new Sales();
    /**
     * sales with possible bonuspoints
     **/
    Sales pnt_sls = new Sales();

    /**
     * reset all accumulators for new transaction
     **/
    public void reset() {
        dsc_sls.set(0, amt = 0);
        pnt_sls.set(pnt = 0, 0);
    }
}
