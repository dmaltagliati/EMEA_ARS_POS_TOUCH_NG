package com.ncr.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.FmtIo;
import com.ncr.GdPos;

// EMEA-02021-GQU#A BEGIN
public class TchDlg extends Modal {

    /**
     *
     */
    private static final long serialVersionUID = 6398607238980337459L;
    int cols = 5, rows = 4; // EMEA-00046-DSA#A
    // int cols = 6, rows = 4;//EMEA-00046-DSA#D
    public GdLabel area = new GdLabel(null, 0);

    public TchDlg(String title) {
        super(title);
        setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("TchDlg", "Bounds"));

        getContentPane().add(area, BorderLayout.CENTER);
        area.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
                    return;
                }
                Dimension d = getSize();
                int x = e.getX() / ((d.width + cols - 1) / cols);
                int y = e.getY() / ((d.height + rows - 1) / rows);

                if (x == cols || y == rows) {
                    return;
                }
                GdPos.playSound("touch/click.wav");
                if (Action.input.isEmpty()) {
                    Action.input.reset(FmtIo.editNum(x + y * cols + 1, 2));
                    Action.input.key = getCode();
                    // modalMain(0);
					press(getCode());
                } else {
                    modalMain(2);
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                area.setImage(null, null);
            }
        });
    }
}