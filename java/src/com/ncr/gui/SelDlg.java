package com.ncr.gui;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.ConIo;
import com.ncr.DevIo;
import com.ncr.FmtIo;
import com.ncr.GdPos;
import com.ncr.Struc;
import com.ncr.UtilLog4j;

public class SelDlg extends Modal {

    public boolean sorted = false;
    /**
     *
     */
    private static final long serialVersionUID = -6961151483718781786L;
    // Timer timeout = null;
    // long lTimeout = 0;
    private boolean echo = true; // MMS-VALASSIS-MOBILE#A

    public JList list;
    public JScrollPane jScrollPane;
    public DefaultListModel defaultListModel;

    public SelDlg(String title) {
        super(title);
        Init(title, 0, true);
    }

    private void Init(String title, long timeout, boolean echo) {
        this.echo = echo;

        getContentPane().setLayout(null);
        if (GdPos.arsGraphicInterface != null) {
            GdPos.arsGraphicInterface.setUndecorated(this,
                    ((Boolean) ArsXmlParser.getInstance().getPanelElement("SelDlg", "Undecorated")).booleanValue());
        }

        setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("SelDlg", "Bounds"));
        getRootPane()
                .setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement("SelDlg", "Opaque")).booleanValue());
        getContentPane().setBackground((Color) ArsXmlParser.getInstance().getPanelElement("SelDlg", "BackgroundColor"));

        Map listsMap = (Map) ArsXmlParser.getInstance().getPanelElement("SelDlg", "List");

        jScrollPane = (JScrollPane) listsMap.get("List");
        if (jScrollPane != null) {
            list = (JList) jScrollPane.getViewport().getView();
            jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            getContentPane().add(jScrollPane);

            defaultListModel = new DefaultListModel();
            list.setModel(defaultListModel);

            ListSelectionListener[] listSelectionListeners = (ListSelectionListener[]) list
                    .getListeners(ListSelectionListener.class);
            for (int i = 0; i < listSelectionListeners.length; i++) {
                list.removeListSelectionListener(listSelectionListeners[i]);
            }

            list.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    int index = ((JList) listSelectionEvent.getSource()).getSelectedIndex();
                    if (index >= 0) {
                        GdPos.playSound("touch/click.wav");
                        itemEcho(index);
                    }
                }
            });

        }

    }

    public void show(String sin, boolean enableScanner) {
        try {
            super.show(sin, false, enableScanner);
        } catch (Exception e) {
            UtilLog4j.logError(this.getClass(), e.getMessage());
        }
    }

    public void show(String sin) {
        try {
            super.show(sin, false, false);
        } catch (Exception e) {
            UtilLog4j.logError(this.getClass(), e.getMessage());
        }
    }

    public void modalMain(int sts) {
        if (Action.input.key == 0x2f2f) {
            Action.input.reset("");
            return;
        }
        int ind = list.getSelectedIndex();

        if (Action.input.key == ConIo.CLEAR) {
            if (ind >= 0) {
                sts = 0;
            }
        }
        if (Action.input.key == ConIo.NORTH) {
            if (ind < 0) {
                ind = defaultListModel.getSize();
            }
            if (--ind >= 0) {
                list.setSelectedIndex(ind);
                list.ensureIndexIsVisible(ind);
            }
            // itemEcho(--ind);
            return;
        }
        if (Action.input.key == ConIo.SOUTH) {
            if (++ind < defaultListModel.getSize()) {
                list.setSelectedIndex(ind);
                list.ensureIndexIsVisible(ind);
                // itemEcho(ind);
            }
            return;
        }
        super.modalMain(sts);

    }

    // MMS-ECOUPONING#A BEGIN
    public void add(int pos, String key, Object object) {
        UtilLog4j.logInformation(this.getClass(), "pos=" + pos + "; key=" + key + "; object=" + object);
        String s;

        if (key.length() != 0) {
            s = FmtIo.editTxt(key, pos) + ".";
        } else {
            s = FmtIo.editTxt(key, pos);
        }

        defaultListModel.addElement(s + " Elemento non visualizzabile");

    }
    // MMS-ECOUPONING#A END

    public void add(int pos, String key, String text) {
        UtilLog4j.logInformation(this.getClass(), "pos=" + pos + "; key=" + key + "; text=" + text);
        String s;

        if (key.length() != 0) { // EMEA-02017-SBE#C
            s = FmtIo.editTxt(key, pos) + ".";
        } else {
            s = FmtIo.editTxt(key, pos);
        }
        defaultListModel.addElement(text == null ? s : s + text);

    }

    public void itemEcho(int index) {
        // if (index >= 0) {
        // list.setSelectedIndex(index);
        // list.ensureIndexIsVisible(index);
        // }

        if (!echo) {
            return;
        }

        String s = ((String) list.getSelectedValue()).trim();

        index = s.indexOf('.');
        Action.input.reset((index >= 0) ? s.substring(0, index) : "00");
        DevIo.oplDisplay(0, s.substring(index + 1).trim());

    }

    public void setEcho(boolean echo) {
        this.echo = echo;
    }

}
