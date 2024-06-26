package com.ncr.common.utilities;

import com.ncr.Basis;
import com.ncr.Mnemo;
import com.ncr.gui.ModDlg;
import org.apache.log4j.Logger;
import com.ncr.GdPos;
public class CardReader extends Basis {
    private static final Logger logger = Logger.getLogger(CardReader.class);

    public static boolean readCard(String hdr, int txt, int min, int max, int msk, int dec) {
        for (int sts; ; GdPos.panel.clearLink(Mnemo.getInfo(sts), 1)) {
            ModDlg dlg = new ModDlg(hdr);
            dlg.block = false;
            dlg.input.prompt = Mnemo.getText(txt);
            oplToggle(2, hdr);
            input.reset("");
            input.init(0x00, max, msk, dec);
            dlg.show("NBR");
            oplToggle(0, null);

            if (input.key == 0)
                input.key = input.CLEAR;
            if (input.key == input.CLEAR) {
                dspLine.init("").show(1);

                return false;
            }
            if (input.num < 1 || (input.key != input.ENTER && input.key != input.SCANNER))
                sts = 5;
            else
                sts = input.adjust(input.pnt);
            if (sts != 0)
                continue;
            if (input.num >= min)
                return true;
            sts = 3;
        }
    }
}
