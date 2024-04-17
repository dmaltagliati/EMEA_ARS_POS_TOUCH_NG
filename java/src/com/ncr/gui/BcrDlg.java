package com.ncr.gui;

import java.awt.*;

public class BcrDlg extends Modal {
    int cols = 5;
    Panel area = new Panel(new GridLayout(0, cols, 1, 1));
    public GdLabel[] info = new GdLabel[2];
    Border pnl;
    GdLabel lbl;

    public BcrDlg(String title) {
        super(title);
        for (int ind = 0; ind < info.length; ind++) {
            info[ind] = new GdLabel("", GdLabel.STYLE_HEADER);
            //info[ind].setFont(panel.font20);
            info[ind].setEnabled(false);
        }
        add(Border.around(info[0], -4), BorderLayout.NORTH);
        add(area, BorderLayout.CENTER);
        add(Border.around(info[1], -4), BorderLayout.SOUTH);
        bounds = panel.getParent();
        if (bounds != panel.frame)
            bounds = panel;
    }

    public void add(String text, String name) {
        area.add(pnl = new Border(-4));
        pnl.setLayout(new BorderLayout());
        pnl.add(lbl = new GdLabel("", GdLabel.STYLE_RAISED), BorderLayout.CENTER);
        //lbl.setPicture(name);
        lbl.setEnabled(false);
        lbl.setText(text);
        pnl.add(lbl = new GdLabel("", GdLabel.STYLE_HEADER), BorderLayout.SOUTH);
        lbl.setEnabled(false);
        //lbl.setFont(panel.font40);
    }

    public void setText(int ind, String text, boolean alert) {
        if (ind >= area.getComponentCount())
            return;
        pnl = (Border) area.getComponent(ind);
        lbl = (GdLabel) pnl.getComponent(1);
        if (text.equals(lbl.getText()))
            return;
        lbl.setAlerted(alert);
        lbl.setText(text);
    }
}
