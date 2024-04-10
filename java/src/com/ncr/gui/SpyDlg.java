package com.ncr.gui;

import com.ncr.GdElJrn;
import com.ncr.notes.Notes;

import java.awt.*;
import java.awt.event.*;

public class SpyDlg extends Modal implements AdjustmentListener, MouseWheelListener {
    public GdElJrn area = new GdElJrn(42, 11);

    public SpyDlg(String title) {
        super(title);
        Panel info = new Panel(new BorderLayout());
        info.add(Border.around(area, -3), BorderLayout.WEST);
        info.add(area.bar, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);
        area.bar.addAdjustmentListener(this);
        addMouseWheelListener(this);
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                Notes.watch(0);
            }
        });
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        Notes.watch(area.bar.getValue() + 1);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == e.WHEEL_UNIT_SCROLL) {
            int ind = e.getWheelRotation();
            while (ind++ < 0)
                scroll(KeyEvent.VK_UP);
            while (--ind > 0)
                scroll(KeyEvent.VK_DOWN);
        }
    }

    public void scroll(int vkey) {
        if (area.scroll(vkey))
            adjustmentValueChanged(null);
    }

    public void modalMain(int sts) {
        if (sts > 0)
            return;
        if (input.key == input.NORTH)
            scroll(KeyEvent.VK_UP);
        if (input.key == input.SOUTH)
            scroll(KeyEvent.VK_DOWN);
        if (input.key == input.ENTER)
            scroll(KeyEvent.VK_END);
        if (input.key == 0)
            input.key = input.CLEAR;
        if (input.key == input.CLEAR)
            super.modalMain(sts);
    }

    public void add(String data) {
        int ind = 0;

        while (ind < area.rows - 1)
            area.list[ind] = area.list[++ind];
        area.list[ind] = data;
    }
}
