package com.ncr;

import jpos.*;
import jpos.events.*;
import org.apache.log4j.Logger;

import java.awt.event.KeyEvent;

public class Wedge extends PosIo implements DataListener, StatusUpdateListener, ErrorListener, MSRConst {
    private static final Logger logger = Logger.getLogger(Wedge.class);
    Keylock lock;
    MSR imsr;
    Scanner scan;
    ToneIndicator tone;
    public POSKeyboard keyb;

    Wedge() {
        try {
            logger.debug("Instantiating Keylock");
            lock = new Keylock();
            lock.addStatusUpdateListener(this);
            logger.debug("Opening Keylock");
            lock.open("KeyLock.1");
            logger.debug("Enabling Keylock");
            jposEnable(lock);
            Device.postInput("LCK" + lock.getKeyPosition(), null);
        } catch (JposException je) {
            jposError(je, lock);
        }
        try {
            logger.debug("Instantiating ToneIndicator");
            tone = new ToneIndicator();
            logger.debug("Opening ToneIndicator");
            tone.open("ToneIndicator.1");
            logger.debug("Enabling ToneIndicator");
            jposEnable(tone);
        } catch (JposException je) {
            jposError(je, tone);
        }

        keyb = new POSKeyboard();
    }

    void init() {
        try {
            logger.debug("Instantiating MSR");
            imsr = new MSR();
            imsr.addDataListener(this);
            imsr.addErrorListener(this);
            logger.debug("Opening MSR");
            jposOpen("MSR.1", imsr, true);

            if (GdSaf.isEnabled()) {
                imsr.setTracksToRead(MSR_TR_1_2_3);
            } else {
                imsr.setTracksToRead (MSR_TR_2_3);
            }

            imsr.setDataEventEnabled(true);
            logger.debug("MSR active");
        } catch (JposException je) {
            jposError(je, imsr);
        }

        try {
            logger.debug("Instantiating Scanner");
            scan = new Scanner();
            scan.addDataListener(this);
            scan.addErrorListener(this);
            logger.debug("Opening Scanner");
            jposOpen("Scanner.0", scan, true);
            scan.setDecodeData(true);
            scan.setDataEventEnabled(true);
            logger.debug("Scanner active");
        } catch (JposException je) {
            jposError(je, scan);
        }
    }

    boolean filter(KeyEvent e) {
        return false;
    }

    void setEnabled(boolean state) {
        if (jposActive(scan)) {
            try {
                scan.clearInput();
                if ((RdrIo.alert & 1) > 0) {
                    RdrIo.alert ^= 1;
                    scan.directIO(508, null, null);
                } else
                    scan.setDeviceEnabled(state);
            } catch (JposException je) {
                jposError(je, scan);
            }
        }
    }

    void stop() {
        jposClose(imsr);
        jposClose(scan);
        jposClose(lock);
        jposClose(tone);
    }

    boolean kbdTone(int type) {
        try {
            tone.soundImmediate();
            return true;
        } catch (JposException je) {
            return false;
        }
    }

    boolean keyLock() {
        return jposActive(lock);
    }

    public void dataOccurred(DataEvent e) {
        logger.debug("Event from " + e.getSource().getClass());
        try {
            if (imsr.equals(e.getSource())) {
                int sts = e.getStatus();
                logger.debug("Event status " + e.getStatus());
                if ((sts & 0x0000ff) > 0)
                    Device.postInput("MSR1", imsr.getTrack1Data());
                if ((sts & 0x00ff00) > 0)
                    Device.postInput("MSR2", imsr.getTrack2Data());
                if ((sts & 0xff0000) > 0)
                    Device.postInput("MSR3", imsr.getTrack3Data());
                imsr.setDataEventEnabled(true);
            } else {
                String sdt = barcodeId(scan.getScanDataType());
                Device.postInput("RDR0" + sdt, scan.getScanDataLabel());
                scan.setDataEventEnabled(true);
            }
        } catch (JposException je) {
            jposError(je, e.getSource());
        }
    }

    public void errorOccurred(ErrorEvent e) {
        try {
            if (imsr.equals(e.getSource()))
                imsr.setDataEventEnabled(true);
            else
                scan.setDataEventEnabled(true);
        } catch (JposException je) {
            jposError(je, e.getSource());
        }
    }

    public void statusUpdateOccurred(StatusUpdateEvent e) {
        Device.postInput("LCK" + e.getStatus(), null);
    }
}