package com.ncr;

public class TouchMenuThread extends Thread {
    private long endTime = 0;

    public TouchMenuThread() {
        setName("TouchMenuThread");
    }

    public void run() {

        UtilLog4j.logInformation(this.getClass(), "start timer...");
        boolean toshow = false;

        while (true) {

            while (System.currentTimeMillis() < endTime) {
                toshow = true;
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                }
            }

            if (toshow) {
                toshow = false;
                UtilLog4j.logInformation(this.getClass(), "show panelMain");

                if (GdPos.panel.touchMenu != null) {
                    GdPos.panel.touchMenu.loadMenu("panelMain");
                    GdPos.panel.touchMenu.reset();
                    GdPos.panel.touchMenu.setPriceVerifierVisible(false);
                }
            }

            try {
                Thread.sleep(250);
            } catch (Exception ex) {
            }

        }

    }

    public void start(long wait) {
        // Se wait � 0 non faccio partire il thread
        if (wait == 0) {
            return;
        }
        // Se endTime � zero significa che � la prima chiamata al thread e
        // quindi lo faccio partire
        if (endTime == 0) {
            start();
        }
        endTime = System.currentTimeMillis() + wait;
    }

}
