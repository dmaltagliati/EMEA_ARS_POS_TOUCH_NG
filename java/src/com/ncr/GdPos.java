package com.ncr;


import ComponentVersion.ComponentVersion;
import com.ncr.ars.ArsGraphicInterface;
import com.ncr.ars.touchmenu.PosToTouchMenuInterface;
import com.ncr.common.engines.sales.BaseSalesEngine;
import com.ncr.common.utilities.AutoCommandManager;
import com.ncr.ecommerce.ECommerceManager;
import com.ncr.eft.EftPluginManager;
import com.ncr.gpe.PosGPE;
import com.ncr.gui.*;
import com.ncr.gui.executor.ExecutorCompletitionService;
import com.ncr.gui.executor.ExecutorServiceFactory;
import com.ncr.ssco.communication.entities.TableElement;
import com.ncr.ssco.communication.manager.SscoPosManager;
import com.ncr.zatca.ZatcaManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static com.ncr.FmtIo.editKey;
import static com.ncr.Table.dpt;
import static com.ncr.Table.lDPT;


public class GdPos extends JPanel implements Runnable, Graphical, ActionListener, AdjustmentListener, MouseWheelListener {
    public static Cursor CURSOR = null;
    public static final Cursor HIDDEN_CURSOR = java.awt.Toolkit.getDefaultToolkit()
            .createCustomCursor(java.awt.Toolkit.getDefaultToolkit().createImage(new byte[]{}), new Point(0, 0), "");
    public static ArsGraphicInterface arsGraphicInterface = null;
    private static final Logger logger = Logger.getLogger(GdPos.class);
    public KeyListener modalKeyListener = new ModalKeyListener();
    public KeyListener eventKeyListener = new EventKeyListener();
    public ModalMainThread modalMainThread = new ModalMainThread();
    public WaitThread waitThread = new WaitThread();
    public SplashThread splashThread = new SplashThread();
    public boolean waitThreadForced = false;

    public JScrollPane jScrollPane;
    public JScrollPane jScrollPane2Screen;
    public JournalTable journalTable;
    public JournalTable journalTable2Screen;
    public GdLabel totalArticle2Screen = new GdLabel("");
    public GdLabel total2Screen = new GdLabel("");
    public GdLabel journalPicture;
    public GdLabel journalPicture2Screen;
    public JPanel panelDetail;
    private Object lock = new Object();
    public ExecutorCompletitionService executorCompletionService = ExecutorServiceFactory.getExecutorService(); // FLM-SCANNER#A
    // MMS-THREAD#D END


    public GdPos() {
        tick = new Thread(this);
        tick.setName("IdleLoop");
    }
    public JFrame frame;

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public Modal modal;
    public ConIo input = Action.input;
    public Motor event = Action.event;
    public Thread tick = new Thread(event, event.project + ":ticker");
    public int idleState;
    public Button idle = new Button() {
        public Container getParent() {
            return modal; /* null in main frame */
        }
    };
    public Border pnlCard = new Border(0); /* 0 = reg+store/ckr, 1 = bonuspoints */
    public Border pnlView = new Border(0); /* 0 = journal, 1 = customer/versions */
    public Border pnlList = new Border(0); /* 0 = news+journal, 1 = item chooser */
    public Border pnlRoll = new Border(0); /* 0 = legacy journal, 1 = trans view */

    static boolean isIdle = false;

    public boolean isIdle() {
        return isIdle;
    }

    public EventQueue queue = getToolkit().getSystemEventQueue();
    public ArrayList innerList = new ArrayList();
    TransView transViewPanel = new TransView();
    public CidIo cid;
    public GdLabel kbrd;
    public static DynakeyGroup dyna;
    public static GdPos panel;
    static boolean dialog_active = false;
    public Mnemo mnemo = new Mnemo();

    public GdLabel dspArea[] = new GdLabel[8];
    GdLabel msgArea[] = new GdLabel[2];
    GdLabel pntArea[] = new GdLabel[5];
    GdLabel sinArea[] = new GdLabel[2];
    public GdLabel stsArea[] = new GdLabel[6];
    GdLabel cusArea[] = new GdLabel[8];

	GdElJrn journal = null;
    GdLabel picture = new GdLabel(null, 0);
    GdLabel sticker = new GdLabel("   ", GdLabel.STYLE_RAISED);
    GdLabel versionArea[] = new GdLabel[3];
    GdLabel trxCard = new GdLabel(null, 0);
    GdLabel mntlabeladd = null;
    GdLabel mntlabelchg = null;
    GdLabel mntlabeldel = null;
    public KeyPadDialog keyPadDialog = null;
    public ArrowsDialog arrowsDialog = null;
    public BottomBarDialog bottomBarDialog = null;
    public SmokePanel smokePanel = null;
    public WaitPanel waitPanel = null;
    public StatusPanel statusPanel = null;
    public SplashPanel splashPanel = null;
    GdTView trxView = null;
    GdLabel labelVersion = null;
    GdLabel labelNegozio = null;
    GdLabel labelCassa = null;
    GdLabel labelCassiere = null;
    GdLabel labelEpts = null;
    GdLabel labelUpb = null;



    private SscoPosManager posManager;


    private void sscoInitialize() {
        posManager = SscoPosManager.getInstance();
        posManager.initialize(idle, queue, editKey(Struc.ctl.reg_nbr, 3));

        for (int index = 1; index < dpt.key.length; index++) {
            lDPT.read(index, lDPT.LOCAL);
            if (!editKey(lDPT.key, 4).startsWith("*")) {
                posManager.getDepartmentsTable().add(new TableElement(lDPT.text, editKey(lDPT.key, 4)));
            }
        }
    }

    public void refreshModals() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (bottomBarDialog != null) {
                    bottomBarDialog.showMyself();
                    // bottomBarDialog.repaint();
                }

                if (smokePanel != null && smokePanel.isVisible()) {
                    if (keyPadDialog != null) {
                        keyPadDialog.hideMyself();
                    }
                    if (arrowsDialog != null) {
                        arrowsDialog.hideMyself();
                    }
                    return;
                } else if (waitPanel != null && waitPanel.isVisible()) {
                    if (keyPadDialog != null) {
                        keyPadDialog.hideMyself();
                    }
                    if (arrowsDialog != null) {
                        arrowsDialog.hideMyself();
                    }
                    return;
                }

                if (splashPanel != null && splashPanel.isVisible()) {
                    if (keyPadDialog != null) {
                        keyPadDialog.hideMyself();
                    }
                    if (arrowsDialog != null) {
                        arrowsDialog.hideMyself();
                    }
                    if (bottomBarDialog != null) {
                        bottomBarDialog.hideMyself();
                    }
                    return;
                }

                if (bottomBarDialog != null && bottomBarDialog.isOpened()) {
                    if (keyPadDialog != null) {
                        keyPadDialog.hideMyself();
                    }
                    if (arrowsDialog != null) {
                        arrowsDialog.hideMyself();
                    }
                } else {
                    if (keyPadDialog != null) {
                        keyPadDialog.showMyself();
                    }

                    if (GdPos.dyna != null) {
                        if (panel.modal == null) {
                            GdPos.dyna.setEnabled(true);
                        } else {
                            GdPos.dyna.setEnabled(false);
                        }
                        if (arrowsDialog != null) {
                            if (GdPos.dyna.getChooser().isVisible()) {
                                arrowsDialog.hideMyself();
                            } else {
                                arrowsDialog.showMyself();
                            }
                        }
                    }
                }
            }
        });
    }

    public void updateInfoLabel() {
        if (labelVersion != null) {
            labelVersion.setText(PosVersion.getVersion() + " " + PosVersion.getBuild());
        }
        if (labelNegozio != null) {
            if (Struc.ctl != null && Struc.ctl.sto_nbr > 0) {
                labelNegozio.setText(LinIo.editNum(Struc.ctl.sto_nbr, 4));
                labelNegozio.setVisible(true);
            } else {
                labelNegozio.setText("");
                labelNegozio.setVisible(false);
            }
        }
        if (labelCassa != null) {
            if (Struc.ctl != null && Struc.ctl.reg_nbr > 0) {
                labelCassa.setText(LinIo.editKey(Struc.ctl.reg_nbr, 3));
                labelCassa.setVisible(true);
            } else {
                labelCassa.setText("");
                labelCassa.setVisible(false);
            }
        }
        if (labelCassiere != null) {
            if (Struc.ctl != null && Struc.ctl.ckr_nbr > 0) {
                labelCassiere.setText(LinIo.editNum(Struc.ctl.ckr_nbr, 3));
                labelCassiere.setVisible(true);
            } else {
                labelCassiere.setText("");
                labelCassiere.setVisible(false);
            }
        }
    }

    private static final String TM_XML_FILENAME = "conf" + File.separatorChar + "touchmenu.xml";
    public PosToTouchMenuInterface touchMenu = null;

    public void run() {

        while (true) {
            // FLM-SCANNER#D BEGIN
            // if (idleState == 0) {
            // Action.idle();
            // // idleState--;
            // // postAction("TICK");
            // }
            // FLM-SCANNER#D END

            setName(this.getClass().getName());
            // FLM-SCANNER#A BEGIN
            if (GdPos.panel.modal != null && GdPos.panel.modal.idleLoopThread.isAlive()) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    UtilLog4j.logInformation(this.getClass(), "Interrupted with modal shown!");
                }
                continue;
            }

            synchronized (lock) {
                IdleThread idle = new IdleThread(lock);
                GdPos.panel.executorCompletionService.submit(idle);
                UtilLog4j.logDebug(this.getClass(), "Action.Idle is been queued by executor");
                try {
                    lock.wait();
                    UtilLog4j.logDebug(this.getClass(), "Action.Idle is been executed");
                } catch (InterruptedException e) {
                    UtilLog4j.logDebug(this.getClass(), "Interrupted!");
                    break;
                }
                try {
                    if (panel.frame.getTitle().equals("JUNIT")) {
                        Thread.sleep(10);
                    } else {
                        Thread.sleep(250);
                    }
                } catch (InterruptedException e) {
                    UtilLog4j.logInformation(this.getClass(), "Interrupted!");
                    break;
                }
            }
            // FLM-SCANNER#A END
        }
    }

    protected void paintComponent(Graphics g) {
        if ((BufferedImage) ArsXmlParser.getInstance().getPanelElement("GdPos", "Image") != null) {
            g.drawImage((BufferedImage) ArsXmlParser.getInstance().getPanelElement("GdPos", "Image"), 0, 0, getWidth(),
                    getHeight(), null);
        }

        super.paintComponent(g);
    }

    void conioInit(Dimension d) {

        int ind = 0;

        try {
            splashPanel = new SplashPanel();
            add(splashPanel);
        } catch (Exception exception) {
            UtilLog4j.logError(this.getClass(), "Errore nel caricamento dello SplashPanel");
        }

        try {
            smokePanel = new SmokePanel();
            add(smokePanel);
        } catch (Exception exception) {
            UtilLog4j.logError(this.getClass(), "Errore nel caricamento dello SmokePanel");
        }

        try {
            waitPanel = new WaitPanel();
            add(waitPanel);
        } catch (Exception exception) {
            UtilLog4j.logError(this.getClass(), "Errore nel caricamento del WaitPanel");
        }

        // La Dynakey non verr� caricata nel caso in cui non sia presente la
        // Entry nell'xml
        if (ConIo.hasDyna()) {
            try {
                Object object = ArsXmlParser.getInstance().getPanelElement("Dynakey", "Bounds");

                if (object != null) {
                    dyna = new DynakeyGroup();
                    if (dyna != null) {
                        add(dyna);
                        add(dyna.getChooser());
                    }
                }
            } catch (Exception exception) {
                UtilLog4j.logError(this.getClass(), "Errore nel caricamento della Dynakey");
                exception.printStackTrace();
            }
        }
        ConIo.dyna = dyna;

        journal = new GdElJrn(42, 6);
        journal.rows = 6;

        cid = new CidIo(frame);
        transViewPanel.init(cid.getFont());

        dspArea[0] = new GdLabel("");
        dspArea[1] = new GdLabel("");
        dspArea[2] = new GdLabel("");
        dspArea[3] = new GdLabel("");
        dspArea[4] = new GdLabel("");
        dspArea[5] = new GdLabel("");
        dspArea[6] = new GdLabel("");
        dspArea[7] = new GdLabel("");

        msgArea[0] = new GdLabel("              ", GdLabel.STYLE_RAISED);
        msgArea[1] = new GdLabel(null, GdLabel.STYLE_RAISED);

        sinArea[0] = new GdLabel(null, GdLabel.STYLE_RAISED);
        sinArea[1] = new GdLabel(null, GdLabel.STYLE_RAISED);

        stsArea[0] = new GdLabel(null, GdLabel.STYLE_NONE);
        stsArea[1] = new GdLabel("SRV000", GdLabel.STYLE_NONE);
        stsArea[2] = new GdLabel("[---]", GdLabel.STYLE_NONE);
        stsArea[3] = new GdLabel("autho", GdLabel.STYLE_NONE);
        stsArea[4] = new GdLabel(" ----- ", GdLabel.STYLE_NONE);
        stsArea[5] = new GdLabel(" ----- ", GdLabel.STYLE_NONE);

        versionArea[0] = new GdLabel(null, GdLabel.STYLE_WINDOW);
        versionArea[1] = new GdLabel(null, GdLabel.STYLE_WINDOW);
        versionArea[2] = new GdLabel(null, GdLabel.STYLE_WINDOW);

        for (ind = 0; ind < dspArea.length; ind++) {
            dspArea[ind].setName("dspArea" + ind);
        }
        for (ind = 0; ind < pntArea.length; ind++) {
            pntArea[ind] = new GdLabel(" ", GdLabel.STYLE_STATUS);
            pntArea[ind].setName("pntArea" + ind);
        }
        for (ind = 0; ind < 2; ind++) {
            msgArea[ind].setName("msgArea" + ind);
            sinArea[ind].setName("sinArea" + ind);
        }

        for (ind = 0; ind < stsArea.length; ind++) {
            stsArea[ind].setName("stsArea" + ind);
            stsArea[ind].setEnabled(false);
        }

        stsArea[0].setHorizontalAlignment(JLabel.LEFT);
        versionArea[0].setName("versionAreaTitle");
        versionArea[0].setForeground(Color.white);
        versionArea[0].setText(PosVersion.getName());
        versionArea[0].setHorizontalAlignment(JLabel.LEFT);
        versionArea[1].setName("versionAreaBuild");
        versionArea[1].setForeground(Color.white);
        versionArea[1].setText(PosVersion.getVersion());
        versionArea[1].setHorizontalAlignment(JLabel.LEFT);
        versionArea[2].setName("versionAreaRevision");
        versionArea[2].setForeground(Color.white);
        versionArea[2].setText(PosVersion.getBuild());
        versionArea[2].setHorizontalAlignment(JLabel.LEFT);
        sticker.setName("sticker");
        picture.setName("picture");

        for (ind = 0; ind < cusArea.length; ind++) {
            if (ind < 4) {
                cusArea[ind] = new GdLabel(null, GdLabel.STYLE_RAISED);
            } else {
                cusArea[ind] = new GdLabel(null, GdLabel.STYLE_STATUS);
            }
            cusArea[ind].setName("cusArea" + ind);
        }

        journal.setName("journal");
        journal.setBackground(Color.white);

        setLayout(null);

        Map labelsMap = (Map) ArsXmlParser.getInstance().getPanelElement("GdPos", "Label");

        labelVersion = (GdLabel) labelsMap.get("Versione");
        if (labelVersion != null) {
            add(labelVersion);
        }
        labelNegozio = (GdLabel) labelsMap.get("Negozio");
        if (labelNegozio != null) {
            add(labelNegozio);
        }
        labelCassa = (GdLabel) labelsMap.get("Cassa");
        if (labelCassa != null) {
            add(labelCassa);
        }
        labelCassiere = (GdLabel) labelsMap.get("Cassiere");
        if (labelCassiere != null) {
            add(labelCassiere);
        }
        labelUpb = (GdLabel) labelsMap.get("Upb");
        if (labelUpb != null) {
            add(labelUpb);
        }
        labelEpts = (GdLabel) labelsMap.get("Epts");
        if (labelEpts != null) {
            add(labelEpts);
        }


        updateInfoLabel();


        dspArea[0] = (GdLabel) labelsMap.get("DateTotal");
        dspArea[1] = (GdLabel) labelsMap.get("DisplayLineTop");
        dspArea[2] = (GdLabel) labelsMap.get("DisplayLineBottom");
        dspArea[3] = (GdLabel) labelsMap.get("Terminale");
        dspArea[4] = (GdLabel) labelsMap.get("Operatore");
        dspArea[5] = (GdLabel) labelsMap.get("DateTime");
        dspArea[6] = (GdLabel) labelsMap.get("Items");
        dspArea[7] = (GdLabel) labelsMap.get("Totale");

        if (existSecondScreen(1)) {
            try {
                totalArticle2Screen = (GdLabel) ((Map) ArsXmlParser.getInstance().getPanelElement("SecondPanel",
                        "Label")).get("Items");

                total2Screen = (GdLabel) ((Map) ArsXmlParser.getInstance().getPanelElement("SecondPanel", "Label"))
                        .get("Totale");
            } catch (Exception e) {
                UtilLog4j.logError(this.getClass(), "errore nel caricamento del SecondPanel", e);
            }
        }
        for (int i = 0; i < dspArea.length; i++) {
            if (dspArea[i] == null) {
                dspArea[i] = new GdLabel("");
            }
            add(dspArea[i]);
        }

        stsArea[0] = (GdLabel) labelsMap.get("Variazioni");
        try {
            stsArea[0].setLayout(new GridLayout(3, 1));
            mntlabeladd = new GdLabel("A00000", GdLabel.STYLE_STATUS);
            mntlabelchg = new GdLabel("C00000", GdLabel.STYLE_STATUS);
            mntlabeldel = new GdLabel("D00000", GdLabel.STYLE_STATUS);
            mntlabeladd.setHorizontalAlignment(stsArea[0].getHorizontalAlignment());
            mntlabelchg.setHorizontalAlignment(stsArea[0].getHorizontalAlignment());
            mntlabeldel.setHorizontalAlignment(stsArea[0].getHorizontalAlignment());
            mntlabeladd.setFont(stsArea[0].getFont());
            mntlabelchg.setFont(stsArea[0].getFont());
            mntlabeldel.setFont(stsArea[0].getFont());
            stsArea[0].add(mntlabeladd);
            stsArea[0].add(mntlabelchg);
            stsArea[0].add(mntlabeldel);
        } catch (Exception exception) {
        }

        stsArea[1] = (GdLabel) labelsMap.get("Server");
        stsArea[2] = (GdLabel) labelsMap.get("KeyLock");
        stsArea[3] = (GdLabel) labelsMap.get("Autho");
        stsArea[4] = (GdLabel) labelsMap.get("EptsUpb");
        stsArea[5] = (GdLabel) labelsMap.get("PinPad");

        for (int i = 0; i < stsArea.length; i++) {
            if (stsArea[i] == null) {
                stsArea[i] = new GdLabel("");
            }
            stsArea[i].setEnabled(false);
            add(stsArea[i]);
        }

        if (labelsMap.get("VariazioniAggiunte") != null && labelsMap.get("VariazioniModificate") != null
                && labelsMap.get("VariazioniEliminate") != null) {

            mntlabeladd = (GdLabel) labelsMap.get("VariazioniAggiunte");
            if (mntlabeladd == null) {
                mntlabeladd = new GdLabel("A00000");
            }
            mntlabeladd.setVisible(false);
            add(mntlabeladd);

            mntlabelchg = (GdLabel) labelsMap.get("VariazioniModificate");
            if (mntlabelchg == null) {
                mntlabelchg = new GdLabel("C00000");
            }
            mntlabelchg.setVisible(false);
            add(mntlabelchg);

            mntlabeldel = (GdLabel) labelsMap.get("VariazioniEliminate");
            if (mntlabeldel == null) {
                mntlabeldel = new GdLabel("D00000");
            }
            mntlabeldel.setVisible(false);
            add(mntlabeldel);
        }

        Map tablesMap = (Map) ArsXmlParser.getInstance().getPanelElement("GdPos", "Table");

        jScrollPane = (JScrollPane) tablesMap.get("Journal");
        if (jScrollPane != null) {
            journalTable = (JournalTable) jScrollPane.getViewport().getView();
            journalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // MMS-R10 FIX Selezione di una sola riga
            jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            journalTable.setBorder(null);
            jScrollPane.setBorder(null);
            add(jScrollPane);

            journalPicture = (GdLabel) labelsMap.get("JournalPicture");
            if (journalPicture == null) {
                journalPicture = new GdLabel("");
            }
            jScrollPane.add(journalPicture);

            GdPos.panel.journalTable.setActiveModel(JournalTable.MODEL_OLD);
            if (GdPos.panel.journalTable.supportModel(JournalTable.MODEL_NEW)) {
                GdPos.panel.journalTable.setActiveModel(JournalTable.MODEL_NEW);
            }

        }

        // if (existSecondScreen(1)) {
        // try {
        // jScrollPane2Screen = (JScrollPane) ((Map) ArsXmlParser.getInstance().getPanelElement("SecondPanel",
        // "Table")).get("Journal");
        // if (jScrollPane2Screen != null) {
        // journalTable2Screen = (JournalTable) jScrollPane2Screen.getViewport().getView();
        // jScrollPane2Screen.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // jScrollPane2Screen.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // jScrollPane2Screen.setBorder(null);
        // journalTable2Screen.setBorder(null);
        // journalPicture2Screen = (GdLabel) ((Map) ArsXmlParser.getInstance().getPanelElement("SecondPanel",
        // "Label")).get("JournalPicture");
        // if (journalPicture2Screen == null) {
        // journalPicture2Screen = new GdLabel("");
        // }
        // jScrollPane2Screen.add(journalPicture2Screen);
        // GdPos.panel.journalTable2Screen.setActiveModel(JournalTable.MODEL_OLD);
        // if (GdPos.panel.journalTable2Screen.supportModel(JournalTable.MODEL_NEW)) {
        // GdPos.panel.journalTable2Screen.setActiveModel(JournalTable.MODEL_NEW);
        // }
        // // secondf.add(jScrollPane2);
        // secondf.setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("SecondPanel", "Bounds"));
        //
        // // secondf.getContentPane().add(jScrollPane2);
        // secondf.add(jScrollPane2Screen);
        // JPanel secondPanel = new SecondPanel();
        //
        // showOnScreen(1, secondf);
        // secondPanel.add(totalArticle2Screen);
        // secondPanel.add(total2Screen);
        //
        // JPanel panel = new SecondPanelRight();
        // GdLabel pictureLabelSecondPanel = new GdLabel(null, 0);
        // Map labelsMapSecondPanelDx = (Map) ArsXmlParser.getInstance().getPanelElement("SecondPanelRight",
        // "Label");
        // pictureLabelSecondPanel = (GdLabel) labelsMapSecondPanelDx.get("SecondPanelRightLabel");
        // if (pictureLabelSecondPanel == null) {
        // pictureLabelSecondPanel = new GdLabel("");
        // }
        //
        // // picture.setImage(picture.getBufferedImage());
        // // File f = new File(pictureLabelSecondPanel.getImageText());
        // // pictureLabelSecondPanel.setImage("gif",f.getName());
        // panel.add(pictureLabelSecondPanel);
        // secondPanel.add(panel);
        //
        // secondf.add(secondPanel);
        // // secondTotale.setLocation(163, 576);
        // // secondTotale.setSize(160,32);
        // // secondf.getContentPane().add(secondTotale);
        // secondf.setVisible(true);
        // }
        // } catch (Exception exception) {
        // UtilLog4j.logError(this.getClass(), "Errore nel caricamento del secondPanel",exception);
        //
        // }
        //
        // }
        try {
            panelDetail = new JPanel();
            panelDetail.setLayout(null);
            panelDetail.setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("ItemDetail", "Bounds"));
            panelDetail.setOpaque(
                    ((Boolean) ArsXmlParser.getInstance().getPanelElement("ItemDetail", "Opaque")).booleanValue());
            panelDetail
                    .setBackground((Color) ArsXmlParser.getInstance().getPanelElement("ItemDetail", "BackgroundColor"));
            add(panelDetail);
        } catch (Exception exception) {
            UtilLog4j.logError(this.getClass(), "Errore nel caricamento del Pannello di Dettaglio Articolo");
            panelDetail = null;
        }

        picture = (GdLabel) labelsMap.get("Picture");
        if (picture == null) {
            picture = new GdLabel("");
        }
        add(picture);

        try {
            statusPanel = new StatusPanel();
            add(statusPanel);
        } catch (Exception exception) {
            UtilLog4j.logError(this.getClass(), "Errore nel caricamento dell'AuthoPanel");
        }

        // if (TouchMenuParameters.getInstance().isPriceVerifier()) {
        // // ---------------------------------------------------------------
        // // Il PriceVerifier non necessita di:
        // // - smokePanel
        // // - keyPadDialog
        // // - arrowsDialog
        // // - bottomBarDialog
        // // ---------------------------------------------------------------
        // } else {

        try {
            keyPadDialog = new KeyPadDialog();
        } catch (Exception exception) {
            UtilLog4j.logError(this.getClass(), "Errore nel caricamento del KeyPad");
        }

        try {
            arrowsDialog = new ArrowsDialog();
        } catch (Exception exception) {
            UtilLog4j.logError(this.getClass(), "Errore nel caricamento dei tasti del Journal");
        }

        try {
            bottomBarDialog = new BottomBarDialog();
        } catch (Exception exception) {
            UtilLog4j.logError(this.getClass(), "Errore nel caricamento della Barra di stato.");
        }
        // }

        for (ind = 0; ind < dspArea.length; ind++) {
            dspArea[ind].setName("dspArea" + ind);
        }
        for (ind = 0; ind < stsArea.length; ind++) {
            stsArea[ind].setName("stsArea" + ind);
        }

        Action.showHeader(false);
        if (((ConIo.optAuth = Struc.options[Struc.O_Autho]) & 2) < 2) {
            GdPos.panel.dspStatus(2, null, true, false);
        }
        Struc.dspLine.init(PosVersion.getName());

        kbrd = dspArea[2];
        kbrd.addKeyListener(eventKeyListener);
        UtilLog4j.logInformation(this.getClass(), kbrd.getName() + " has focus listener");
        kbrd.addFocusListener(new FocusAdapter() {
            // TODO: COMMENTARE QUESTA PARTE PER USARE SCANNER
            public void focusLost(FocusEvent e) {
                if (System.getProperty("DISABLEHACKFOCUS", "false").equalsIgnoreCase("false")) {
                    UtilLog4j.logInformation(this.getClass(), kbrd.getName() + " lost the focus");
                    if (panel.modal == null) {
                        UtilLog4j.logInformation(this.getClass(), kbrd.getName() + " requesting the focus");
                        kbrd.requestFocus();
                    }
                }
            }
        });

        setKeyListeners();

        idle.addActionListener(this);
        stsArea[2].addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                int step = (e.getModifiers() & InputEvent.BUTTON1_MASK) > 0 ? -1 : 1;

                if (!DevIo.hasKeylock()) {
                    Action.input.keyLock(ConIo.posLock + step);
                }
            }
        });

    }

    public void startUp() {
        AutoCommandManager.getInstance().initialize(idle, queue);
    }

    public void postAction(String cmd) {
        //EventQueue queue = getToolkit().getSystemEventQueue();
        queue.postEvent(new ActionEvent(idle, ActionEvent.ACTION_PERFORMED, cmd));
    }

    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();

        // MMS-TOUCHMENU#A BEGIN
        isIdle = false;
        // MMS-TOUCHMENU#A END

        if (!cmd.startsWith("SELECT")) {
            UtilLog4j.logInformation(this.getClass(), "cmd = " + cmd);
        } else {
            UtilLog4j.logDebug(this.getClass(), "cmd = " + cmd);
        }

        if (cmd.startsWith("AUTO")) {

            int code = Integer.parseInt(cmd.substring(4), 16);
            UtilLog4j.logInformation(this.getClass(), "Accepting " + code);
            code = Action.input.accept(code);
            if (code >= 0) {
                code = GdPos.panel.eventMain(code);
                if (code >= 0) {
                    GdPos.panel.eventStop(code);
                }
            }
        }
        if (cmd.startsWith("CODE")) {
            // FLM-THREADS#A BEGIN
            ActionThread actionThread = new ActionThread(e);
            GdPos.panel.executorCompletionService.submit(actionThread);
            // FLM-THREADS#A END
        }

        // EMEA-00046-DSA#A BEG

        // MMS-JUNIT-20190917#A END
        if (cmd.startsWith("LIST")) {
            ListThread listThread = new ListThread();
            listThread.setActionEvent(e); // MMS-JUNIT
            listThread.setCommand(cmd);
            listThread.setSelected(Action.input.sel);
            // listThread.start(); // FLM-THREADS#D
            GdPos.panel.executorCompletionService.submit(listThread); // FLM-THREADS#A
        }
        // EMEA-00046-DSA#A END
        if (cmd.startsWith("LCK")) {
            KeyLockThread keyLockThread = new KeyLockThread(e);
            if (GdPos.panel.modal != null) {
                keyLockThread.start();
            } else {
                GdPos.panel.executorCompletionService.submit(keyLockThread);
            }
        }
        if (cmd.startsWith("KEYB")) {

            int keyCodeMod = Integer.parseInt(cmd.substring(4, 5));
            int keyCodeFun = Integer.parseInt(cmd.substring(5, 6));
            int keyCode = Integer.parseInt(cmd.substring(6));
            char keyChar = (keyCodeFun == 0 ? (char) keyCode : KeyEvent.CHAR_UNDEFINED);
            KeyEvent k = new KeyEvent(GdPos.panel.modal == null ? GdPos.panel.kbrd : GdPos.panel.modal.getKbrd(),
                    KeyEvent.KEY_PRESSED, System.currentTimeMillis(), keyCodeMod, keyCode, keyChar);
            UtilLog4j.logInformation(this.getClass(), "KeyEvent: " + k);
            if (GdPos.panel.modal != null) {
                GdPos.panel.modalMainThread = new ModalMainThread();
                GdPos.panel.modalMainThread.addEvent(k);
                GdPos.panel.modalMainThread.start();
            } else {
                EventMainThread eventMainThread = new EventMainThread(k);
                GdPos.panel.executorCompletionService.submit(eventMainThread);
            }

        }
        // MMS-SCANNER#A BEGIN
        if (cmd.startsWith("RDX")) {
            UtilLog4j.logInformation(this.getClass(), "Scanner thread start");
            ScannerDecodeDataThread scannerDecodeDataThread = new ScannerDecodeDataThread(e);
            if (GdPos.panel.modal != null && GdPos.panel.modal.isEnableScanner()) {
                scannerDecodeDataThread.start();
            } else {
                GdPos.panel.executorCompletionService.submit((Thread) scannerDecodeDataThread);
            }
            return;
        }
        // MMS-SCANNER#A END
        // MMS-TOUCHMENU#A BEGIN
        if (cmd.startsWith("PLL")) {
            int code = Action.input.label(cmd);

            if (modal == null) {
                eventMain(code);
            } else {
                modal.modalMain(code);
            }
        }
        // MMS-TOUCHMENU#A END
        if (cmd.startsWith("RDR")) {
            // FLM-SCANNER#A BEGIN
            ScannerThread scannerThread = new ScannerThread(e);
            if (GdPos.panel.modal != null && GdPos.panel.modal.isEnableScanner()) {
                scannerThread.start();
            } else {
                GdPos.panel.executorCompletionService.submit((Thread) scannerThread);
            }
            return;
            // FLM-SCANNER#A END

        }
        if (cmd.startsWith("MSR")) {
            int code = Action.input.track(cmd);

            UtilLog4j.logInformation(this.getClass(), "MSR code " + code + ": " + cmd);

            if (code < 0) {
                return;
            }
            if (modal == null) {
                eventMain(code);
            } else {
                modal.modalMain(code);
            }
        }

        if (cmd.startsWith("WEPTS")) {
            if (modal == null) {
                int code = Action.input.track(cmd);

                if (code >= 0) {
                    eventMain(code);
                }
            }
        }
        if (cmd.startsWith("SCA")) {
            if (DevIo.scale.state < 1) {
                return;
            }
            if (cmd.charAt(3) == '1') {
                Action.input.reset(cmd.substring(4));
                innerVoice(ConIo.ENTER);
                DevIo.scale.state = 0;
            }
            if (cmd.charAt(3) == '4') {
                String weight = cmd.charAt(4) == '4' ? cmd.substring(5) : "";

                Action.input.reset(FmtIo.leftFill(weight, Action.input.msk, '0')); // EMEA-00040-GQU#A
                // DevIo.wghControl(4); //EMEA-00040-GQU#D
            }
        }

    }

    void eventInit() {
        Action.initialize();
        eventMain(-1);
        if (Action.input.dky >= 0) {
            dyna.setVisible(true);
        }
        UtilLog4j.logInformation(this.getClass(), kbrd + " requestFocus()");
        kbrd.requestFocus();
        tick.start();


        if ((ConIo.optAuth & 2) > 0) {
            Action.input.lck &= 0xF0;
            Action.input.lck |= Struc.ctl.ckr_nbr < 800 && (Action.input.lck & 0x10) == 0 ? 1 : 4;

        }
        // MMS-R10#A END

    }

    // MMS-TOUCHMENU#A BEGIN
    public int eventExecute(int actionCode) {

        Action.input.error = 0;
        Action.input.key = actionCode;

        // Simulo l'invio
        if ((actionCode = Action.input.accept(actionCode)) >= 0) {
            actionCode = GdPos.panel.eventMain(actionCode);
            if (actionCode >= 0) {
                GdPos.panel.eventStop(actionCode);
            }
        }

        return Action.input.error;

    }

    // MMS-TOUCHMENU#A END

    public void eventStop(int sts) {
        UtilLog4j.logFatal(this.getClass(), "Entering eventStop(int sts) = " + sts);
    }

    void eventDyna(int state) {
        if (dyna == null) {
            return;
        }

        int ind = 0, len = dyna.getComponentCount();

        state = Action.get_state(state);
        // EMEA-00046-DSA#A BEG
        // UtilLog4j.logInformation(this.getClass(), "input.sel="+ input.sel);
        if (Action.input.sel > 0) {
            if (state == 3) {
                if (dyna.getSubstate() == Action.input.sel) {
                    return;
                }
                dyna.setSubstate(Action.input.sel);
                dyna.setState(state);
                for (ind = 0; ind < len; Action.showDynaTch(dyna.key(ind++))) {
                    ;
                }
                dyna.select(0);
                return;
            }
        }
        if ((Action.input.sel = 0) < dyna.getSubstate()) {
            dyna.setSubstate(0);
            Action.input.dky = -1;
        }
        // EMEA-00046-DSA#A END
        if (Action.input.dky != state) {
            dyna.setState(Action.input.dky = state);
            while (ind < len) {
                Action.showDynakey(dyna.key(ind++));
            }
        } else {
            if (Action.event.base == Action.event.last) {

                return;
            }
        }
        int code[] = ConIo.dynas[state], line;
        boolean visible[] = new boolean[len];

        for (line = Action.event.next(Action.event.base); Action.event.key > 0; line = Action.event.next(line)) {
            for (ind = 0; ind < len; ind++) {
                visible[ind] |= Action.event.menu == 0 ? Action.event.key == code[ind] : Action.event.min == ind + 1;
                // BAS-FIX-NOTETS-MMS#A BEGIN
                if (code[ind] == ConIo.NOTES) {
                    visible[ind] = true;
                }
                // BAS-FIX-NOTETS-MMS#A END
            }
        }
        Action.event.last = Action.event.read(Action.event.base);
        for (ind = 0; ind < len; ind++) {
            Dynakey d = dyna.key(ind);
            boolean b = visible[ind] || code[ind] != 0;

            if (d.isEnabled() ^ b) {
                d.setEnabled(b);
            }
        }

    }


	public int eventMain(int sts) {
		// MMS-R10#A BEGIN
		return eventMain(sts, Action.input.key);
	}

	public int eventMain(int sts, int key) {

		Action.input.key = key; // MMS-R10#A
		int line = Action.event.base, dyna = Action.input.dky, err = 0;

		UtilLog4j.logInformation(this.getClass(), "Called with " + sts + " and Looking for 0x"
				+ Integer.toHexString(Action.input.key) + " | Current EventTable Position: " + Action.event.pb);

		if (sts == 0) {
			if (dyna == Action.get_state(Action.event.lck & 0x0F)) {
				dyna = 0;
			}
			if (Action.input.key == ConIo.CLEAR) {


				if (dyna > 0) {
					Action.event.setNxt(line);
				} else {
					if (Action.event.menu > 0) {
						Action.event.setNxt(Action.event.menu);
					} else {
						sts--;
					}
				}
			} else {
				sts--;
			}

			while (sts < 0) {
				line = Action.event.next(line);
				if (Action.input.key > 0xF000) {
					err = (Action.input.key - 0xF000);
					Action.input.key = 0xF000;
				}
				if (Action.event.key == 0) {
					sts = 5;
					if (Action.input.isEmpty()) {
						if (Action.input.key > 0x13 && Action.input.key < 0x16) {
							if (Action.event.menu < 1) {
								dyna = Action.input.key - 0x13;
							}
							Action.input.key = ConIo.CLEAR;
						} else {
							sts = Action.spec(sts);
						}
					}
					break;
				}
				if (Action.event.menu > 0) {
					if (Action.input.isEmpty()) {
						if (Action.input.key == Action.event.min + 0x80) {
							Action.input.reset(Integer.toString(Action.event.key));
							Action.input.key = ConIo.ENTER;
						}
					}
					if (Action.input.num < 1 || Action.input.key != ConIo.ENTER) {
						continue;
					}
					if (Action.event.key != Integer.parseInt(Action.input.pb)) {
						continue;
					}
					if ((sts = Action.input.adjust(0)) > 0) {
						break;
					}
					sts++;
					if ((Action.event.lck & Action.input.lck) != 0) {
						Action.input.num = 0;
						sts = Action.group[Action.event.act / 10].exec();
						if (Action.event.listNxt != 0 || Action.event.listAlt != 0) {
							Action.event.setNxt(Action.event.listNxt);
							Action.event.setAlt(Action.event.listAlt);
						}
						if (sts == 0) {
							UtilLog4j.logInformation(this.getClass(), "Ok (status " + sts + ")");
						} else if (sts > 0) {
							UtilLog4j.logInformation(this.getClass(), "Error (status " + sts + ")");
						} else {
							UtilLog4j.logInformation(this.getClass(), "Continue (status " + sts + ")");
						}
						if (Action.event.getNxt() < 0) {
							return sts;
						}
					}
					break;
				}
				if (Action.event.key != Action.input.key) {
					continue;
				}
				if (err > 0) {
					Action.event.spc = err;
					err = 0;
				}
				if (!Action.input.isEmpty()) {
					if (Action.event.max == 0) {
						continue;
					}
				} else {
					if (Action.event.min > 0) {
						continue;
					}
				}
				if ((Action.event.lck & 0x80) > 0) {
					if (Action.event.spc != Integer.parseInt(Action.input.pb)) {
						continue;
					}
				}
				if ((sts = Action.input.adjust(Action.event.dec)) > 0) {
					break;
				}
				sts++;

				// MMS-R10
				boolean supervisorOk = false;

				if (GdPos.panel.keyPadDialog != null) { // Se � il Pos Touch?
					UtilLog4j.logInformation(this.getClass(), Action.event.lck + " & " + Action.input.lck);
					if ((Action.event.lck & Action.input.lck) == 0) {
						if (GdSigns.askForSupervisor(1) == 0) {
							supervisorOk = true;
						} else {
							return -1;
						}
					}
				}
				// MMS-R10

				if (((Action.event.lck & Action.input.lck) != 0) || (Action.input.key == 0xF000) || supervisorOk) {
					sts++;
					if (Action.event.max >= Action.input.num) {
						sts++;
						if (Action.event.min <= Action.input.num) {
							try {
								sts = Action.group[Action.event.act / 10].exec();
								if (Action.event.listNxt != 0 || Action.event.listAlt != 0) {
									Action.event.setNxt(Action.event.listNxt);
									Action.event.setAlt(Action.event.listAlt);
								}
								if (sts == 0) {
									UtilLog4j.logInformation(this.getClass(), "Ok (status " + sts + ")");
								} else if (sts > 0) {
									UtilLog4j.logInformation(this.getClass(), "Error (status " + sts + ")");
								} else {
									UtilLog4j.logInformation(this.getClass(), "Continue (status " + sts + ")");
								}
								// MMS-WATER#A BEGIN
								Action.input.error = sts;
								// MMS-WATER#A END
							} catch (Exception ex) {
								UtilLog4j.logInformation(this.getClass(), Action.event.pb);
								UtilLog4j.logError(this.getClass(), "EXCEPTION OCCURRED", ex);
								ex.printStackTrace();
								sts = 143;
							}
							// MMS-WATER#A BEGIN
							Action.input.error = sts;
							if (Action.event.getNxt() < 0) {
								return sts;
							}
							// MMS-WATER#A END
						}
					}
				}
			}
		}

		if (sts > 0) {
			// MMS-WATER#A BEGIN
			Action.input.error = sts;
			// MMS-WATER#A END
			line = Action.event.base;
			if (Action.input.key != ConIo.CLEAR) {
				UtilLog4j.logInformation(this.getClass(), "EVENTTABLE " + Action.event.pb);

				clearLink(sts);
			}
		} else {
			if (Action.event.menu == 0) {
				Action.event.menu = Action.event.base;
			}
			line = Action.event.getNxt();
			dyna = 0;
		}
		Action.event.base = Action.event.read(line);
		if (Action.event.lck < 0xF0) {
			UtilLog4j.logInformation(this.getClass(), "Setting menu to zero");
			UtilLog4j.logInformation(this.getClass(), Action.event.pb);
			Action.event.menu = 0;
			Struc.dspLine.show(1);
			Action.showShort("POS", Action.event.spc);
		} else {
			Action.event.lck &= 0x0F;
			if (Action.event.act > 0) {
				display(1, GdPos.panel.mnemo.getMenu(Action.event.act));
			}
			Action.showShort("MNU", Action.event.spc);
		}
		if ((Action.event.lck & 0x80) > 0) {
			if (Action.event.min > 0 && Struc.oplLine.peek(19) == ' ') {
				Action.event.lck ^= 0x80;
			} else {
				Struc.oplLine.show(2);
			}
		}
		if (Action.event.getAlt() > 0 || Action.event.min < 1) {
			Action.input.prompt = GdPos.panel.mnemo.getText(Action.event.getAlt());
		}
		Action.input.init(Action.event.lck, Action.event.max, Action.event.min, Action.event.dec);
		if (ConIo.hasDyna()) {
			eventDyna(dyna > 0 ? dyna : Action.event.lck & 0x0F);
		}
		dspPicture(Struc.dspBmap);

		return -1;
	}

    public void adjustmentValueChanged(AdjustmentEvent e) {
        ElJrn.view(false);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == e.WHEEL_UNIT_SCROLL) {
            int ind = e.getWheelRotation();
            while (ind++ < 0)
                ElJrn.roll(KeyEvent.VK_UP);
            while (--ind > 0)
                ElJrn.roll(KeyEvent.VK_DOWN);
        }
    }


    //originali
//	void eventInit() {
//		Thread.currentThread().setName(event.project + ":dispatch");
//		Action.init(this);
//		event.main(-1);
//		frame.requestFocus();
//		tick.start();
//	}
//
//	public void eventStop(int sts) {
//		Action.stop();
//		try {
//			tick.interrupt();
//			tick.join();
//		} catch (InterruptedException e) {
//			System.out.println(e);
//		}
//		journal.stop();
//		System.exit(sts);
//	}


	public int clearLink(int sts) {
		return clearLink(sts, 1);
	}


	public int clearLink(int sts, int mode) {
		UtilLog4j.logInformation(this.getClass(), "sts=" + sts + "; mode=" + mode);

		return clearLink(Mnemo.getInfo(sts), mode);
	}


	public int clearLink(Vector v, int type) {
		String msg = "<html><p>";
		Iterator iterator = v.iterator();

		while (iterator.hasNext()) {
			msg += iterator.next() + "<br>";
		}
		msg += "</p></html>";
		return clearLink(msg, type);
	}

	public synchronized int clearLink(String msg, int type) {
		UtilLog4j.logInformation(this.getClass(), "<<<" + msg + ">>>");

		//ECOMMERCE-MSOUK#A BEG
        if (ECommerceManager.getInstance().hidePopup()) {
            logger.info("Not showing popup " + msg);
            return 0;
        }
        //ECOMMERCE-MSOUK#A END

		String sav = dspArea[1].getText();

		DevIo.alert(0);
		DevIo.oplSignal(15, 1);
		// DevIo.oplDisplay(line, msg);

		Action.input.init(0x80, 0, 0, type);

		try {
			Modal dlg = DialogFactory.create(DialogFactory.ERROR_DIALOG, msg, type); // MMS-PRINTER-ERROR-RECOVERY#A
			dlg.show("CLR", false, false);

//			// Solo per self, ma dovrebbe andare anceh su cassa trandizionale.
//			// alla chiusura di una modale, faccio ripartire il thread del clessidrone
//			// se nell'executor � presente almeno un thread (ipoteticamente
//			// l'EventMainThread che ha causato l'apertura della modale.
//			if (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService()) {
//				if (GdPos.panel.executorCompletionService.poll() != null) {
//					GdPos.panel.startWaitTread();
//				}
//			}

			Struc.dspLine.init(' ');


			if ((dlg.getCode() < 0) && (Action.input.key == 0)) {
				dlg.setCode(1);
				Action.input.key = ConIo.CLEAR;
			}
			if (Struc.ctl.ckr_nbr != 0) {
				Action.input.reset("");
			}
			return dlg.getCode();
		} catch (Exception e) {
			UtilLog4j.logError(this.getClass(), e.getMessage());
		}

		return -1;
	}

//    public int clearLink(int index, int type) {
//        String msg = Mnemo.getInfo(index);
//        return clearLink(msg, type);
//    }
//
//    public int clearLink(String msg, int type) {
//        int line = type >> 4;
//
//        //ECOMMERCE-MSOUK#A BEG
//        if (ECommerceManager.getInstance().hidePopup()) {
//            logger.info("Not showing popup " + msg);
//            return 0;
//        }
//        //ECOMMERCE-MSOUK#A END
//
//        DevIo.alert(0);
//        if (!Thread.currentThread().getName().endsWith("dispatch")) {
//            DevIo.oplDisplay(1, msg);
//            dspStatus(0, msg, true, true);
//            return 0;
//        }
//        if (event.idle > 0)
//            return 0;
//        if (input.key == 0x4f4f) {
//            DevIo.setAlerted(input.label.charAt(3) & 3);
//        }
//        DevIo.oplSignal(15, 1);
//        Action.oplToggle(line & 3, msg);
//        ClrDlg dlg = new ClrDlg(msg, type & 7);
//        if ((type & 0x80) > 0)
//            dlg.input = new ConIo(20);
//        dlg.input.init(0x80, 0, 0, 0);
//        dlg.show("CLR");
//        Action.oplToggle(line & 1, null);
//        DevIo.oplSignal(15, 0);
//        return dlg.code;
//    }

    public void innerVoice(int action) {
        UtilLog4j.logInformation(this.getClass(), "action = " + Integer.toHexString(action));
        AWTEvent e;

        if (modal != null) {
            e = new WindowEvent(modal, WindowEvent.WINDOW_CLOSING);
        } else {
            e = new ActionEvent(idle, ActionEvent.ACTION_PERFORMED, "CODE" + Integer.toHexString(action));
            GdPos.panel.innerList.add(e);
        }
        GdPos.panel.queue.postEvent(e);

    }

    void sequentialInnerVoice(int action) {
        UtilLog4j.logInformation(this.getClass(), "action = " + Integer.toHexString(action));
        AWTEvent e;

        if (modal != null) {
            e = new WindowEvent(modal, WindowEvent.WINDOW_CLOSING);
            GdPos.panel.queue.postEvent(e);
            while (true) {
                if (modal == null)
                    return;
            }
        } else {
            if (action > 0) {
                action = Action.input.accept(action);
                if (action >= 0) {
                    action = GdPos.panel.eventMain(action);
                    if (action >= 0) {
                        GdPos.panel.eventStop(action);
                    }
                }
            }
        }
    }

    public void display(int line, String data) {

        UtilLog4j.logInformation(this.getClass(), line + ", " + data);
        // MMS-TOUCHMENU#A BEGIN
        if (line == 1 || line == 2) {
            if (panel.touchMenu != null) {
                panel.touchMenu.setDisplayLine(line, data);
            }
        }
        // MMS-TOUCHMENU#A END
        if (line == 1) {
            GdPos.panel.updateDiplayTopText(data);
        } else if (line == 2) {
            GdPos.panel.updateDiplayBottomText(data);
        }
        if (line > 9) {
            Action.cusDisplay(line - 10, data);
            return;
        }
        // if (line == 3) {
        // ((CardLayout) pnlCard.getLayout()).first(pnlCard);
        // }
        dspArea[line].setText(data);
        if (line == 0) {
            // if (Struc.tra.mode >= Struc.M_GROSS) { //MMS-MANTIS-17114#D
            if (Struc.tra.mode >= Struc.M_GROSS || Struc.tra.code == 9 || Struc.tra.code == 11) { // MMS-MANTIS-17114#A
                GdPos.panel.updateTotalText("Totale: " + Action.editMoney(0, Struc.tra.bal).trim()); // MMS-LOTTERY-VAR1#A
                if (touchMenu != null && TouchMenuParameters.getInstance().isSelfService()) {
                    touchMenu.setTotal(Struc.tra.bal);
                }
            }
        }
        if (line > 0 && line < 3) {
            DevIo.oplDisplay(line - 1, data);
        }

    }

    public void dspNotes(int line, String data) {
        if (line < 1)
            msgArea[line].setAlerted(data != null);
        msgArea[line].setText(data);
    }

    public void dspShort(int line, String data) {
        sinArea[line].setText(data);
    }

	void dspJournal(boolean append) {
		jrnPicture(null);
		journal.view(append);
	}

    public void dspShopper(int line, String data) {
        if (line == 0)
            pnlView.toFront(1); /* first card = shopper */
        cusArea[line].setText(data);
    }

	public void jrnPicture(String name) {
		journal.setPicture(name);
		journal.setPicture2Screen(name);

	}

//    public void jrnPicture(String name) {
//        pnlView.toFront(0); /* first card = journal */
//        journal.setPicture(name);
//    }
	public void jrnPicture2Screen(String name) {
		if (GdPos.panel.journalTable2Screen != null) {
			GdPos.panel.journalTable2Screen.clear(JournalTable.MODEL_NEW);
		}
		journal.setPicture2Screen(name);

	}
    public void dspPicture(String fileList) {

        if (fileList == null || fileList.trim().length() == 0) {
            picture.setImage(null);
            return;
        }

        StringTokenizer stringTokenizer = new StringTokenizer(fileList, ";");

        while (stringTokenizer.hasMoreElements()) {
            String name = (String) stringTokenizer.nextElement();

            name = name.replace('*', 'X');
            picture.setImage("gif", name + ".GIF");
            if (picture.getIcon() != null) {
                break;
            }
        }

    }

    void dspPoints(String data) {
        int len = data.length();
        pnlCard.toFront(1); /* first card = points */
        for (int ind = pntArea.length; ind-- > 0; len--) {
            pntArea[ind].setText(data.substring(len - 1, len));
        }
    }

    public void dspSymbol(String data) {
        sticker.setImage("gif", "SYM_" + data + ".GIF");
        sticker.setText(sticker.getIcon() == null ? FmtIo.editTxt(data, 4) : null);
    }

    public void dspStatus(int nbr, String data, boolean enabled, boolean alerted) {

        UtilLog4j.logInformation(this.getClass(), nbr + ", " + data + ", " + enabled + ", " + alerted);

        if (nbr == 3) {
            // status("Autho", enabled);
            status("Autho", Action.input.isSupervisor());
            GdPos.panel.updateCassiere(alerted); // MAL-MANTIS-17779#A
        }

        GdLabel lbl = stsArea[nbr];
        lbl.setAlerted(alerted);
        lbl.setEnabled(enabled);
        if (nbr == 0) {
            mntlabeladd.setVisible(true);
            mntlabelchg.setVisible(true);
            mntlabeldel.setVisible(true);
            mntlabeladd.setText(data.substring(0, 6));
            mntlabelchg.setText(data.substring(7, 13));
            mntlabeldel.setText(data.substring(14, 20));
        } else {
            lbl.setAlerted(alerted);
            lbl.setEnabled(enabled);
            if (data != null) {
                lbl.setText(data);
            }
        }
        // EMEA-00046-DSA#A BEG
        if ((nbr == 0) && (data != null)) {
            if (!Struc.tra.isActive()) {
                DevIo.oplDisplay(nbr, data);
            }
        }
        // EMEA-00046-DSA#A END
    }

    // MMS-R10#D
    // STD-ENH-ASR31CID-SBE#A BEG
//    void jrnClear() {
//        Action.input.sel = 0;
//        cid.clear(true);
//        // STD-ENH-ASR31TRV-SBE#A BEG
//        transViewPanel.clear();
//        // STD-ENH-ASR31TRV-SBE#A END
//    }



    public void print(int station, String data) {
        if ((station & FmtIo.ELJRN) > 0)
            ElJrn.write(station, data);
        for (int dev = 8; dev > 0; dev >>= 1)
            if ((station & dev) > 0)
                DevIo.tpmPrint(dev, 0, data);
    }

    public void select(int ind) {
        if (modal == null)
            dyna.select(ind);
    }

    public void feedBack(KeyEvent e) { // System.out.println (e.paramString () + " at " + e.getWhen ());
    }
	void clearJournal() {
		journal.clear();
	}

    public static void main(final String[] args) {

        boolean showCursor = false;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("SHOWCURSOR")) {
                    showCursor = true;
                }
            }
        }

        if (!showCursor) {
            GdPos.CURSOR = GdPos.HIDDEN_CURSOR;
        }

        // System.setProperty("file.encoding", "Latin-1");
        System.out.println("file.encoding=" + System.getProperty("file.encoding"));

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                startPos(args);
            }
        });
    }

    static JFrame f; // MMS-R10#A
    static JFrame secondf;
    public JFrame touchMenuFrame;

    public static void startPos(String[] args) {

        boolean showCursor = false;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("SHOWCURSOR")) {
                    showCursor = true;
                }
            }
        }

        if (!showCursor) {
            GdPos.CURSOR = GdPos.HIDDEN_CURSOR;
        }

        ComponentVersion.writeComponentVersion("Pos Application", PosVersion.getDate(),
                PosVersion.getVersion() + " " + PosVersion.getBuild());

        ArsXmlParser.getInstance().load();

        // JFrame f; //MMS-R10#D
        String strLog4JConfigurationFilePath = "conf/AsrPos_Log4j.properties";
        String strSscoConfigurationFilePath = "conf/ArsSsco.properties";

        UtilLog4j.initialize(GdPos.class, "AsrPos", PosVersion.getName(),
                PosVersion.getVersion() + " " + PosVersion.getBuild(), PosVersion.getDate());
        UtilLog4j.logInformation(GdPos.class, "file.encoding=" + System.getProperty("file.encoding"));

        for (int i = 0; i < args.length; i++) {
            UtilLog4j.logInformation(GdPos.class, "args[" + i + "] = " + args[i]);
        }

        boolean result = true;


        try {
            Class cl = Class.forName("com.ncr.ars.ArsGraphic");

            arsGraphicInterface = (ArsGraphicInterface) cl.newInstance();
        } catch (Exception exception) {
            UtilLog4j.logError(GdPos.class, "Errore nel caricamento del Plugin ArsGraphic");
        }


        f = new JFrame(PosVersion.getName());

       // PosGPE.Init();
        if (GdPos.existSecondScreen(1)) {
            secondf = new JFrame("Second JPos++");
            if (GdPos.arsGraphicInterface != null) {
                try {
                    GdPos.arsGraphicInterface.setUndecorated(secondf,
                            ((Boolean) ArsXmlParser.getInstance().getPanelElement("SecondPanel", "Undecorated"))
                                    .booleanValue());
                } catch (Exception exception) {
                    UtilLog4j.logError(GdPos.class, "Errore nel caricamento del SecondPanel ", exception);
                }
            }

            secondf.toFront();
        }

        panel = new GdPos();
        f.getContentPane().add(panel);
        f.addWindowListener(new WindowAdapter() {

            public void windowActivated(WindowEvent e) {
                UtilLog4j.logInformation(this.getClass(), panel.kbrd + " requestFocus()");
                panel.kbrd.requestFocus();
            }

            public void windowDeiconified(WindowEvent e) {
                if (panel.keyPadDialog != null) {
                    panel.keyPadDialog.showMyself();
                }
                if (panel.arrowsDialog != null) {
                    panel.arrowsDialog.showMyself();
                }
                if (panel.bottomBarDialog != null) {
                    panel.bottomBarDialog.showMyself();
                    panel.bottomBarDialog.setVisible(false);
                    panel.bottomBarDialog.setVisible(true);

                }
            }

            public void windowIconified(WindowEvent e) {
                if (panel.keyPadDialog != null) {
                    panel.keyPadDialog.hideMyself();
                }
                if (panel.arrowsDialog != null) {
                    panel.arrowsDialog.hideMyself();
                }
                if (panel.bottomBarDialog != null) {
                    panel.bottomBarDialog.hideMyself();
                }

            }

            public void windowOpened(WindowEvent e) {
                // panel.eventInit(); //MMS-R10#D

                // MMS-R10#A BEGIN
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        while (!f.isVisible()) {
                            Thread.yield();
                        }
                        panel.eventInit();
                    }
                });
                // MMS-R10#A END
            }

            public void windowClosing(WindowEvent e) {
                panel.eventStop(1);
            }
        });

        Param.init();
		EftPluginManager.getInstance().init();
		GiftCardPluginManager.getInstance().init();
		ZatcaManager.getInstance().init();
		BaseSalesEngine.getInstance().init();
		PosGPE.Init();

        if (GdPos.arsGraphicInterface != null) {
            GdPos.arsGraphicInterface.setUndecorated(f,
                    ((Boolean) ArsXmlParser.getInstance().getPanelElement("GdPos", "Undecorated")).booleanValue());
        }

        f.setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("GdPos", "Bounds"));
        panel.setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement("GdPos", "Opaque")).booleanValue());
        panel.setBounds(f.getBounds());
        panel.setPreferredSize(f.getSize());


        // MMS-TOUCHMENU#A BEGIN
        if (TouchMenuParameters.getInstance().isSelfService()) {
            if (panel.touchMenu != null) {
                // panel.touchMenu.loadMenu("panelLogin");
                panel.touchMenu.setVisible(true);
            }
        }

        // MMS-TOUCHMENU#A END


        panel.setFrame(f);
        panel.conioInit(f.getSize());
        GdPos.panel.splashThread.start(); // MMS-R10#A

        f.toFront();
        f.setVisible(true);

        if (GdPos.panel.touchMenu != null && (TouchMenuParameters.getInstance().isSelfService()
                || TouchMenuParameters.getInstance().isPriceVerifier())) {
            panel.touchMenuFrame.toFront();
            panel.touchMenuFrame.setVisible(true);
            f.setState(JFrame.ICONIFIED);
        }

    }

	public void showOnScreen(int screen, JFrame frame) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gd = ge.getScreenDevices();
		if (screen > -1 && screen < gd.length) {
			UtilLog4j.logInformation(GdPos.class, "frame location on screen " + screen);
			frame.setLocation(gd[screen].getDefaultConfiguration().getBounds().x, frame.getY());
		}
		// else if (gd.length > 0) {
		// // frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, frame.getY());
		// } else {
		// // throw new RuntimeException("No Screens Found");
		// }
	}

    public static boolean existSecondScreen(int screen) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        if (screen > -1 && screen < gd.length) {
            UtilLog4j.logInformation(GdPos.class, "screen " + screen + " enabled");
            return true;
        }
        UtilLog4j.logInformation(GdPos.class, "screen " + screen + " disabled");
        return false;
    }

    public void updateCassiere(boolean active) {
        if (labelCassiere != null) {
            labelCassiere.setAlerted(active);
        }
    }

    // MAL-MANTIS-17779#A END
    public void updateArticlesText() {
        String text = "";
        switch (Struc.tra.cnt) {
            case 0:
                break;
            case 1:
                text = Struc.tra.cnt + " Articolo";
                break;
            default:
                text = Struc.tra.cnt + " Articoli";
                break;
        }
        GdPos.panel.dspArea[6].setText(text);
    }

    public void updateTotalText(String text) {
        GdPos.panel.dspArea[7].setText(text);
        if (existSecondScreen(1)) {
            GdPos.panel.total2Screen.setText(text);
        }
    }

    public void updateDiplayTopText(String text) {
        GdPos.panel.dspArea[1].setText(text);
    }

    public void updateDiplayBottomText(String text) {
        GdPos.panel.dspArea[2].setText(text);
    }

    public void smoke(final boolean visible) {
        if (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService()) {
            GdPos.panel.touchMenu.smokePanel(visible);
            return;
        }
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (smokePanel != null) {
                    smokePanel.setVisible(visible);
                }
                refreshModals();
            }
        });

    }
    public void status(final String id, final boolean visible) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (statusPanel != null) {
                    statusPanel.setVisible(id, visible);
                }
            }
        });

    }

    public void wait(final boolean visible) {

        if (GdPos.panel.touchMenu != null && (TouchMenuParameters.getInstance().isSelfService()
                || TouchMenuParameters.getInstance().isPriceVerifier())) {
            GdPos.panel.touchMenu.waitPanel(visible);
            return;
        }
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (waitPanel != null) {
                    waitPanel.setVisible(visible);
                    UtilLog4j.logDebug(this.getClass(), "Refreshing and Repaint: " + visible);
                    refreshModals();
                }

            }
        });
    }

    public void splash(final boolean visible) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (splashPanel != null) {
                    UtilLog4j.logInformation(this.getClass(), "" + visible);
                    splashPanel.setVisible(visible);
                }
                refreshModals();
            }
        });
    }

    public static void playBeep() {
        playSound("touch/beep.wav");
    }

    public static void playSound(final String url) {
        if (arsGraphicInterface != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    arsGraphicInterface.playSound(url);
                }
            });
        }
    }


    //WINEPTS-CGA#A BEG
    public void updateEpts(boolean active) {
        if (stsArea[5] != null) {
            stsArea[5].setText(active ? "WON" : "WOFF");
            stsArea[5].setAlerted(!active);
        }
    }
    //WINEPTS-CGA#A END


    public void setKeyListeners() {
        if (panel.modal != null) {
            try {
                if (keyPadDialog != null) {
                    keyPadDialog.addKeyListener(modalKeyListener);
                }
            } catch (Exception exception) {
            }

            try {
                if (arrowsDialog != null) {
                    arrowsDialog.addKeyListener(modalKeyListener);
                }
            } catch (Exception exception) {
            }

            try {
                if (bottomBarDialog != null) {
                    bottomBarDialog.addKeyListener(modalKeyListener);
                }
            } catch (Exception exception) {
            }

        } else {
            try {
                if (keyPadDialog != null) {
                    keyPadDialog.addKeyListener(eventKeyListener);
                }
            } catch (Exception exception) {
            }

            try {
                if (arrowsDialog != null) {
                    arrowsDialog.addKeyListener(eventKeyListener);
                }
            } catch (Exception exception) {
            }

            try {
                if (bottomBarDialog != null) {
                    bottomBarDialog.addKeyListener(eventKeyListener);
                }
            } catch (Exception exception) {
            }
        }

    }

    public void updateServerStatus(int serverStatus) {
        // EMEA-02017-SBE#A BEG
        String netstatus = "";
        if (serverStatus == 1 ) {
            netstatus = GdPos.panel.mnemo.getText(96 + serverStatus).substring(0, 6).trim();
        } else {
            netstatus = Action.netio.lanHost(NetIo.SRV);
        }
        // EMEA-02017-SBE#A END
        stsArea[1].setText(netstatus);
        stsArea[1].setEnabled(true);
        switch (serverStatus) {
            case 1:
                stsArea[1].setState("OffLine");
                break;
            case 2:
                stsArea[1].setState("OffLineReverse");
                break;
            default:
                stsArea[1].setState("Normal");
                break;
        }

    }
    public void startWaitTread() {
        if (waitThreadForced) {
            return;
        }

        synchronized (GdPos.panel.waitThread) {
            try {
                UtilLog4j.logDebug(Action.class, "Join WaitThread");
                GdPos.panel.waitThread.interrupt();
                GdPos.panel.waitThread.join();
                UtilLog4j.logDebug(Action.class, "WaitThread Died");
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                UtilLog4j.logError(Action.class, "During Join WaitThread", e1);
            }

            GdPos.panel.waitThread = new WaitThread();
            GdPos.panel.waitThread.start();
        }
    }

    public void interruptWaitTread() {
        if (waitThreadForced) {
            return;
        }
        GdPos.panel.waitThread.interrupt();
    }
}
