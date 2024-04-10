package com.ncr.gui;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import com.ncr.ArsXmlParser;

public class WghDlg extends Modal {

    /**
     *
     */
    private static final long serialVersionUID = -4709616269602982520L;
    int mode;
    GdLabel area = new GdLabel(null, GdLabel.STYLE_STATUS);

    public WghDlg(String title, int cmd) {
        super("PoS Scale");
        setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("WghDlg", "Bounds"));

        Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("WghDlg", "Label");

        GdLabel info = (GdLabel) labelsMap.get("Info");

        if (info == null) {
            info = new GdLabel("");
        }
        info.setText(title);

        getContentPane().add(info, BorderLayout.SOUTH);
        getContentPane().add(area, BorderLayout.CENTER);
        area.setImage("gif", "SCALE.GIF");
        this.mode = cmd;
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                area.setImage(null, null);
            }
        });
    }
}
