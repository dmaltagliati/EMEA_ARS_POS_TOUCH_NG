package com.ncr.gui;

import java.awt.*;

public class Dynakey extends Picture {
    int nbr;
    String txt2;
    Rectangle rect = new Rectangle(0, 0, 200, 75);
    com.ncr.gui.DynakeyGroup dyna;

    public Dynakey(com.ncr.gui.DynakeyGroup d) {
        this.dyna = d;
        d.keys[nbr = d.getComponentCount()] = this;
        ;
        setName("dynaKey" + nbr);
        rect.y += nbr * rect.height;
    }

    public String getText2() {
        return txt2;
    }

    public void setEnabled(boolean state) {
        image = state ? dyna.image : null;
        ground = dyna.ground;
        super.setEnabled(state);
    }

    public void setText(int align, String text, String txt2) {
        this.align = align;
        this.text = text;
        this.txt2 = txt2;
    }

    public void paint(Graphics g) {
        Dimension d = getSize();

        if (image != null && dyna.downKey != nbr) {
            if ((checkImage(image, this) & ALLBITS + FRAMEBITS) == 0) return;
            g.drawImage(image, 0, 0, d.width, d.height, rect.x, rect.y,
                    rect.x + rect.width, rect.y + rect.height, null);
        } else if (ground != null) ground.paintOn(this, g);
        if (text == null) return;
        if (!isEnabled()) return;
        int high = (d.height - 15) / 5;
        int x = d.width / 20, y = high * 3 + nbr + nbr;
        x += x * align;
        if (txt2 != null) {
            g.drawString(txt2, x, y + high);
            y -= high;
        } else if (text.length() < 9) {
            y += 4;
            g.setFont(dyna.dble);
        }
        g.drawString(text, x, y);
    }
}

