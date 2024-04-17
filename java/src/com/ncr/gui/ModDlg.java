package com.ncr.gui;

import java.awt.*;

public class ModDlg extends Modal {
    public GdLabel line[] = new GdLabel[3];

    public ModDlg(String name) {
        super("PoS Enter Data");
        Panel info = new Panel(new GridLayout(0, 1));
        //info.setFont(panel.font40);
        info.add(Border.around(line[0] = new GdLabel(null, GdLabel.STYLE_RAISED), -3));
        info.add(line[1] = new GdLabel(name, GdLabel.STYLE_STATUS));
        info.add(Border.around(line[2] = new GdLabel(null, GdLabel.STYLE_RAISED), -3));
        add(info, BorderLayout.CENTER);
    }
}
