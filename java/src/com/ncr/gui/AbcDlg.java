package com.ncr.gui;

import com.ncr.ConIo;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AbcDlg extends Modal {
    int cols = 10, rows = 4;
    public Border area = new Border(-3);
    public String state = "  [Caps Lock]";
    private int mode = 0; /* 0=normal, 1=shift, 2=lock */

    public void touch(MouseEvent e, int type) {
        if ((e.getModifiers() & e.BUTTON1_MASK) == 0)
            return;
        char c = ((GdLabel) e.getComponent()).getText().charAt(0);
        KeyEvent k = new KeyEvent(kbrd, type, e.getWhen(), e.getModifiers(), KeyEvent.VK_UNDEFINED, c);
        getToolkit().getSystemEventQueue().postEvent(k);
        e.consume(); /* SUN: key events can be mouse food */
        if (type == KeyEvent.KEY_PRESSED)
            getToolkit().beep();
        else if (mode == 1)
            setMode(0);
    }

    public void setMode(int nbr) {
        String title = getTitle();
        int ind = title.indexOf(state);

        mode = nbr % 3;
        if (ind >= 0)
            title = title.substring(0, ind);
        if (mode == 2)
            title += "  [Caps Lock]";
        setTitle(title);
        for (ind = area.getComponentCount(); ind > 0; ) {
            char c = input.alpha.charAt(--ind);
            GdLabel key = (GdLabel) area.getComponent(ind);
            if (mode > 0)
                c = Character.toUpperCase(c);
            key.setText(String.valueOf(c));
        }
    }

    public void modalMain(int sts) {
        switch (input.key) {
            case ConIo.NORTH:
                mode--;
            case ConIo.SOUTH:
                setMode(mode + 2);
                input.key = 0;
                break;
            default:
                super.modalMain(sts);
        }
    }

    public AbcDlg(String title) {
        super(title);
        add(area);
        setFont(panel.font40);
        area.setLayout(new GridLayout(rows, cols, 2, 2));
        for (int y = 0; y < rows; y++)
            for (int x = 0; x < cols; x++) {
                char c = input.alpha.charAt(y * cols + x);
                GdLabel key = new GdLabel(String.valueOf(c), 0);
                area.add(key);
                key.setVisible(false);
                key.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        touch(e, KeyEvent.KEY_PRESSED);
                    }

                    public void mouseReleased(MouseEvent e) {
                        touch(e, KeyEvent.KEY_RELEASED);
                    }
                });
            }
    }
}
