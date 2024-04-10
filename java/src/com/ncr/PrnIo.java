package com.ncr;

import com.ncr.ecommerce.ECommerceManager;
import com.ncr.ssco.communication.manager.SscoPosManager;
import jpos.CashDrawer;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import org.apache.log4j.Logger;

import java.io.File;

public class PrnIo extends PosIo implements POSPrinterConst {
    int recColumns = 42, jrnColumns = 42, slpColumns = 66;
    int recCompressed = 0;
    private static final Logger logger = Logger.getLogger(PrnIo.class);
    POSPrinter prn1;
    CashDrawer drw1, drw2;
    File logo = localFile("bmp", "P_REGELO.BMP");

    public PrnIo(Device dev) {
        try {
            prn1 = new POSPrinter();
            if (dev.version > 0) {
                jposOpen("PosPrinter.1", prn1, true);
                if (codePage > 0)
                    prn1.setCharacterSet(codePage);
            }
        } catch (JposException je) {
            je.printStackTrace();
            logConsole(0, prn1.getClass().getName(), je.getMessage());
            if (je.getErrorCode() != JPOS_E_ILLEGAL)
                gui.eventStop(255);
        }
        try {
            drw1 = new CashDrawer();
            jposOpen("CashDrawer.1", drw1, false);
        } catch (JposException je) {
            jposError(je, drw1);
        }
        try {
            drw2 = new CashDrawer();
            jposOpen("CashDrawer.2", drw2, false);
        } catch (JposException je) {
            jposError(je, drw2);
        }
    }

    public void stop() {
        jposClose(prn1);
        jposClose(drw1);
        jposClose(drw2);
    }

    public int init(int id) {
        try {
            prn1.setMapMode(PTR_MM_METRIC);
            if (prn1.getCapJrnPresent()) {
                id |= 0x01;
                if ((jrnColumns = prn1.getJrnLineChars()) < 40)
                    prn1.setJrnLineChars(jrnColumns = 40);
                prn1.setJrnLineSpacing(425);
            }
            if (prn1.getCapRecPresent()) {
                id |= 0x02;
                if ((recColumns = prn1.getRecLineChars()) < 40)
                    prn1.setRecLineChars(recColumns = 40);
                prn1.setRecLineSpacing(425);
            }
            if (prn1.getCapSlpPresent()) {
                id |= 0x04;
                if ((slpColumns = prn1.getSlpLineChars()) > 66)
                    prn1.setSlpLineChars(slpColumns = 66);
                prn1.setSlpLineSpacing(425);
            }
            if (prn1.getCapRecPapercut())
                id |= 0x80;
        } catch (JposException je) {
            error(je, true);
        }
        try {
            if (logo.exists()) {
                setQuality(PTR_S_RECEIPT, true);
                prn1.setBitmap(1, PTR_S_RECEIPT, logo.getPath(), PTR_BM_ASIS, PTR_BM_CENTER);
                setQuality(PTR_S_RECEIPT, false);
            }
        } catch (JposException je) {
            logConsole(0, prn1.getClass().getName(), je.getMessage());
            if (je.getErrorCodeExtended() == JPOS_EPTR_BADFORMAT)
                logConsole(0, prn1.getClass().getName(), "WRONG LOGO FORMAT: " + logo.getPath());
            if (je.getErrorCodeExtended() == JPOS_EPTR_TOOBIG)
                logConsole(0, prn1.getClass().getName(), "WRONG LOGO SIZE: " + logo.getPath());
        }
        return id;
    }

    public void error(JposException je, boolean abort) {
        jposError(je, prn1);
        gui.clearLink(Mnemo.getInfo(17), abort ? 4 : 1);
        if (abort)
            gui.eventStop(255);
    }

    public void write(int dev, String data) {
        while (true) {
            try {
                logger.debug("Printing in dev: " + dev + " data [" + data + "]");
                prn1.printNormal(dev, data);
                break;
            } catch (JposException je) {
                logger.warn("Jpos error: " + je.getMessage());
                //ECOMMERCE-SSAM#A BEG
                if (ECommerceManager.getInstance().abortTransaction()) {
                    break;
                }
                //ECOMMERCE-SSAM#A END
                error(je, false);
                if (je.getErrorCodeExtended() == JPOS_EPTR_SLP_EMPTY) {
                    DevIo.slpInsert(0);
                    if (DevIo.mfptr.state < 0)
                        break;
                }
            }
        }
    }

    public void bitmap(String name) {
        while (true) {
            try {
                setQuality(PTR_S_RECEIPT, true);
                prn1.printBitmap(PTR_S_RECEIPT, name, PTR_BM_ASIS, PTR_BM_CENTER);
                setQuality(PTR_S_RECEIPT, false);
                break;
            } catch (JposException je) {
                logger.warn("Jpos error: " + je.getMessage());
                if (ECommerceManager.getInstance().abortTransaction()) {
                    break;
                }
                error(je, false);
            }
        }
    }

    public boolean paperState(int dev) {
        boolean state = false;

        try {
            if (dev == PTR_S_JOURNAL)
                state = prn1.getJrnNearEnd();
            if (dev == PTR_S_RECEIPT)
                state = prn1.getRecNearEnd();
        } catch (JposException je) {
        }
        return state;
    }

    public boolean tillState() {
        boolean state = false;

        try {
            state |= drw1.getDrawerOpened();
        } catch (JposException je) {
        }
        try {
            state |= drw2.getDrawerOpened();
        } catch (JposException je) {
        }
        return state;
    }

    public void waitIdle() {
        return;
    }

    public void label(int dev, int type, String nbr) {
        logger.info("ENTER label type: " + type + " - nbr: " + nbr);
        lfeed(dev, 1);
        while (true) {
            try {
                if (dev == PTR_S_RECEIPT)
                    if (!prn1.getCapRecBarCode()) {
                        logger.info("EXIT label 1");
                        return;
                    }
                if (dev == PTR_S_SLIP)
                    if (!prn1.getCapSlpBarCode()) {
                        logger.info("EXIT label 2");
                        return;
                    }
                if (type == PTR_BCS_Code39) {
                    prn1.printBarCode(dev, ipcBase32(nbr), type, 1275, 5200, PTR_BC_CENTER, PTR_BC_TEXT_NONE);
                    write(dev, dwide("\u001b|cAA" + nbr + '\n'));
                } else if (type == PTR_BCS_QRCODE) {
                    prn1.printBarCode(dev, nbr, type, 150, 150, PTR_BC_CENTER, PTR_BC_TEXT_NONE);
                } else {
                    logger.info("PRINT BARCODE type: " + type);
                    prn1.printBarCode(dev, nbr, type, 1275, 5200, PTR_BC_CENTER, PTR_BC_TEXT_BELOW);
                }
                break;
            } catch (JposException je) {
                logger.info("label exception. : " + je.getMessage());
                error(je, false);
            }
        }
    }

    public void labelQR(int dev, int type, String nbr, int width, int height) {
        logger.info("ENTER label QR type: " + type + " - nbr: " + nbr);
        lfeed(dev, 1);
        while (true) {
            try {
                if (dev == PTR_S_RECEIPT)
                    if (!prn1.getCapRecBarCode()) {
                        logger.info("EXIT label 1");
                        return;
                    }
                if (dev == PTR_S_SLIP)
                    if (!prn1.getCapSlpBarCode()) {
                        logger.info("EXIT label 2");
                        return;
                    }
                if (type == PTR_BCS_Code39) {
                    prn1.printBarCode(dev, ipcBase32(nbr), type, 1275, 5200, PTR_BC_CENTER, PTR_BC_TEXT_NONE);
                    write(dev, dwide("\u001b|cAA" + nbr + '\n'));
                } else if (type == PTR_BCS_QRCODE) {
                    //prn1.printBarCode(dev, nbr, type, 150, 150, PTR_BC_CENTER, PTR_BC_TEXT_NONE);
                    prn1.printBarCode(dev, nbr, type, width, height, PTR_BC_CENTER, PTR_BC_TEXT_NONE);
                } else {
                    logger.info("PRINT BARCODE type: " + type);
                    prn1.printBarCode(dev, nbr, type, 1275, 5200, PTR_BC_CENTER, PTR_BC_TEXT_BELOW);
                }
                break;
            } catch (JposException je) {
                logger.info("label exception. : " + je.getMessage());
                error(je, false);
            }
        }
    }

    public void ldata(int dev, String data, StringBuffer sb) {
        int cols = dev > 1 ? recColumns : jrnColumns;

        data = jposOemCode(data);
        if (dev == PTR_S_SLIP) {
            for (cols = slpColumns - data.length(); cols-- > 0; sb.append(' '))
                ;
            if (data.charAt(1) != '>')
                sb.append(data);
            else
                sb.append(' ').append(dwide(data.substring(2, 22)));
            return;
        }
        if (cols == 40) {
            if (data.charAt(1) != '>')
                sb.append(data.substring(1, 41));
            else
                sb.append(dwide(data.substring(2, 22)));
            return;
        }
        if (recCompressed == 0)
            for (cols = cols - 42 >> 1; cols-- > 0; sb.append(' '))
                ;
        if (data.charAt(1) != '>')
            sb.append(data);
        else
            sb.append(' ').append(dwide(data.substring(2, 22)));
    }

    public void lfeed(int dev, int lfs) {
        if (lfs > 0)
            write(dev, "\u001b|" + lfs + "lF");
    }

    public void pulse(int nbr) {
        CashDrawer co = nbr == 0 ? drw1 : drw2;
        if (!jposActive(co))
            return;
        try {
            co.openDrawer();
        } catch (JposException je) {
            jposError(je, co);
        }
    }

    public void knife(int msk) {
        if ((msk & 0x40) == 0) {
            try {
                lfeed(PTR_S_RECEIPT, prn1.getRecLinesToPaperCut());
            } catch (JposException je) {
            }
        }
        if ((msk & 0x80) != 0) {
            write(2, "\u001b|75P");
        }
    }

    public void setPitch(int chars) {
        if (!jposActive(prn1))
            return;
        try {
            prn1.setRecLineChars(recCompressed = chars);
        } catch (JposException je) {
            error(je, true);
        }
    }

    public void setQuality(int dev, boolean high) {
        try {
            if (dev == PTR_S_RECEIPT)
                prn1.setRecLetterQuality(high);
            if (dev == PTR_S_JOURNAL)
                prn1.setJrnLetterQuality(high);
            if (dev == PTR_S_SLIP)
                prn1.setSlpLetterQuality(high);
        } catch (JposException je) {
            error(je, true);
        }
    }

    public String dwide(String data) {
        String dwide = "\u001b|2C" + data;
        if (SscoPosManager.getInstance().isEnabled()) dwide += "\n";
        return dwide;
    }

    public void logo(int dev, boolean top) {
        String position = top ? "1" : "2";
        write(dev, "\u001b|" + position + "B");
    }
}
