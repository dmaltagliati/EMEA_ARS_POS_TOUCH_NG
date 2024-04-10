package com.ncr.ecommerce;

import org.apache.log4j.BasicConfigurator;

public class Test {

    public static void main(String args[]) {
        BasicConfigurator.configure();
        //ECommerceManager.getInstance().checkForNewBasket("001", null);  //PORTING-SPINNEYS-ECOMMERCE-CGA#D
        ECommerceManager.getInstance().checkForNewBasket("001", "101", null); //ECOMMERCE-SSAM#A  //PORTING-SPINNEYS-ECOMMERCE-CGA#A
    }
}