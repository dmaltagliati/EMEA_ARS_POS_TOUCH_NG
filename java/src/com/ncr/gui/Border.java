package com.ncr.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Border extends Panel {
    public int size;
    public boolean raised;
    public Ground ground = null;
    public static Color color = Color.getColor("COLOR_CONTROLSHADOW", SystemColor.controlShadow);

    public static Border around(Component c, int size) {
        Border border = new Border(size);
        border.add(c);
        border.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Component c = ((Border) e.getComponent()).getComponent(0);
                if (c.isEnabled())
                    c.dispatchEvent(e);
            }
        });
        return border;
    }

    public Border(int size) {
        super(new GridLayout(0, 1));
        raised = size > 0;
        this.size = raised ? size : 0 - size;
    }

    public Insets getInsets() {
        return new Insets(size, size, size, size);
    }

    public void toFront(int ind) {
        while (!getComponent(ind).isVisible())
            ((CardLayout) getLayout()).next(this);
    }

    public void paint(Graphics g) {
        if (ground == null) {
            int ind = -1;
            Dimension d = getSize();
            g.setColor(color);
            while (++ind < size)
                g.draw3DRect(ind, ind, --d.width - ind, --d.height - ind, raised);
            if (ind-- > 0) {
                g.drawLine(0, 0, ind, ind);
                g.drawLine(d.width, d.height, d.width + ind, d.height + ind);
            }
        } else
            ground.paintOn(this, g);
        super.paint(g);
    }
}
