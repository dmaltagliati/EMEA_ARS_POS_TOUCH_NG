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

        Iterator keyEventListIterator = keyEventList.iterator();
        while (keyEventListIterator.hasNext()) {
            KeyEvent e = (KeyEvent) keyEventListIterator.next();

            int code = Action.input.keyBoard(e);

            UtilLog4j.logInformation(this.getClass(), "[" + KeyEvent.getKeyText(e.getKeyCode()) + "]" + "[0x"
                    + Integer.toHexString(Action.input.key) + "]" + "input.pb = " + Action.input.pb);

            if (GdPos.panel.modal != null) {

                if (e.getWhen() > 0) {
                    if (code >= 0) {
                        GdPos.panel.modal.modalMain(code);
                    }
                } else {
                    GdPos.panel.modal.modalMain(-1);
                }
            }
        }

        GdPos.panel.interruptWaitTread();
        if (GdPos.panel.modal == null) {
            if (GdPos.panel.executorCompletionService.poll() != null) { // FLM-THREADS#A
                GdPos.panel.startWaitTread();
            }
        }
        UtilLog4j.logInformation(this.getClass(), "Ended!");
    }
}
