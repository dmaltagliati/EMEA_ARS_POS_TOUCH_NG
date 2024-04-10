package com.ncr.gui;

import com.ncr.Action;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClrDlg extends Modal {
    int border = panel.font40.getSize() >> 1;
    public GdLabel info = new GdLabel(null, GdLabel.STYLE_RAISED);
    public GdLabel key1 = new GdLabel(Action.key_txt[1], GdLabel.STYLE_RAISED);
    public GdLabel key2 = new GdLabel(Action.key_txt[0], GdLabel.STYLE_RAISED);

    public ClrDlg(String title, int type) {
        super("PoS Info Message");

        String ico = "CLR40";
        if ((type & 4) > 0) {
            code = input.ABORT;
            key2.setText(Action.key_txt[2]);
            if ((type & 1) < 1)
                ico = "ABORT";
        }
        setFont(panel.font40);
        setUndecorated(true);
        if (title.length() <= 20) {
            title = "      " + title;
            ico = (type & 2) > 0 ? "ENTER" : "CLEAR";
        } else
            info.setFont(panel.font60);
        info.setEnabled(false);
        //info.setPicture(ico);
        info.setText(title);
        add(info, BorderLayout.CENTER);
        add(Border.around(key1, border), BorderLayout.NORTH);
        add(Border.around(key2, border), BorderLayout.SOUTH);
        key1.setEnabled((type & 1) > 0);
        key2.setEnabled((type & 6) > 0);
        if (!key1.isEnabled())
            key1.setText(null);
        if (!key2.isEnabled())
            key2.setText(null);
        key1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                input.key = input.CLEAR;
                modalMain(0);
            }
        });
        key2.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                input.key = code;
                modalMain(0);
            }
        });
    }

    public void modalMain(int sts) {
        if (sts > 0)
            return;
        if (sts == 0) {
            if (input.key > 0)
                sts = 5;
            if (key1.isEnabled()) {
                if (input.key == 0)
                    input.key = input.CLEAR;
                else if (input.key == input.CLEAR)
                    sts = 1;
            }
            if (key2.isEnabled()) {
                if (input.key == 0)
                    input.key = code;
                else if (input.key == code)
                    sts = 2;
            }
        }
        if (sts < 3)
            super.modalMain(sts);
    }
}
