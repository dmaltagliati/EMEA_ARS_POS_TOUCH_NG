package com.ncr.gui;

import com.ncr.*;

import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;

public class ModalMainThread extends Thread {
    private static long id = 0;
    // private volatile KeyEvent e = null; //MMS-JUNIT-20190917#D
    private volatile LinkedList keyEventList = new LinkedList(); // MMS-JUNIT-20190917#A

    public ModalMainThread() {
        super();
        setName("ModalMainThread-" + id++);

    }

    public void addEvent(KeyEvent e) {
        // this.e = e; //MMS-JUNIT-20190917#D
        // MMS-JUNIT-20190917#A BEGIN
        UtilLog4j.logInformation(this.getClass(), "" + e);
        keyEventList.add(e);
        // MMS-JUNIT-20190917#A END
    }

    public synchronized void run() {

        GdPos.panel.startWaitTread();

        UtilLog4j.logInformation(this.getClass(), "Started!");

        GdPos.panel.interruptWaitTread();
        if (GdPos.panel.modal == null) {
            if (GdPos.panel.executorCompletionService.poll() != null) { // FLM-THREADS#A
                GdPos.panel.startWaitTread();
            }
        }
        UtilLog4j.logInformation(this.getClass(), "Ended!");
    }

}
