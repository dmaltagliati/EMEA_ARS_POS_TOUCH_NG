package com.ncr.gui;

import com.ncr.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PluDlg extends Modal {
    int border = panel.font40.getSize() >> 1;
    public GdLabel key1 = new GdLabel(Action.key_txt[1], GdLabel.STYLE_RAISED);
    public BarCode labl = new BarCode();
    public GdLabel head = new GdLabel(Action.inq_line, GdLabel.STYLE_STATUS);
    public GdLabel data = new GdLabel(null, GdLabel.STYLE_WINDOW);

    public PluDlg(String title) {
        super(title);
        key1.setFont(panel.font40);
        Panel info = new Panel(new BorderLayout());
        info.add(head, BorderLayout.NORTH);
        info.add(Border.around(data, -1), BorderLayout.SOUTH);
        labl.setBackground(head.getBackground());
        add(Border.around(key1, border), BorderLayout.NORTH);
        add(labl, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);
        key1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                input.key = input.CLEAR;
                modalMain(0);
            }
        });
    }

    public void modalMain(int sts) {
        if (sts > 0)
            return;
        if (input.key == 0)
            input.key = input.CLEAR;
        if (input.key == input.CLEAR)
            super.modalMain(0);
    }
}
