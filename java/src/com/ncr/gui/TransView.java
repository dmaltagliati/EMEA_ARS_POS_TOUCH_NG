package com.ncr.gui;

import com.ncr.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;

/**
 * This class is the main Driver Class for the scrollable view of sales transaction
 *
 * @author bs230003
 */
public class TransView extends JPanel implements AdjustmentListener {

	private static final long serialVersionUID = -4017721240945858927L;

	public static Color colorMenu = Color.getColor("COLOR_MENU", SystemColor.menu);
	public static Color colorMenuText = Color.getColor("COLOR_MENUTEXT", SystemColor.menuText);
	public static Color colorWindow = Color.getColor("COLOR_WINDOW", SystemColor.window);
	public static Color colorWindowText = Color.getColor("COLOR_WINDOWTEXT", SystemColor.windowText);
	public static Color colorControl = Color.getColor("COLOR_CONTROL", SystemColor.control);
	public static Color colorControlText = Color.getColor("COLOR_CONTROLTEXT", SystemColor.controlText);
	public static Color colorActiveCaption = Color.getColor("COLOR_ACTIVECAPTION", SystemColor.activeCaption);
	public static Color colorActiveCaptionText = Color.getColor("COLOR_ACTIVECAPTIONTEXT",
			SystemColor.activeCaptionText);
	public static Color colorInactiveCaption = Color.getColor("COLOR_INACTIVECAPTION", SystemColor.inactiveCaption);
	public static Color colorInactiveCaptionText = Color.getColor("COLOR_INACTIVECAPTIONTEXT",
			SystemColor.inactiveCaptionText);
	public static Color colorScrollbar = Color.getColor("COLOR_SCROLLBAR", SystemColor.scrollbar);

	public static int DATA_DISPLAY_LINES = 11;
	public static int DATA_DISPLAY_COLS = 56;
	public static int SCROLL_BAR_WIDTH = 20;
	public static boolean SELECTION_MODE = true;

	protected static TransDataProvider dataProvider = null;

	private TransHeaderView hdrView = null;
	private TransDataView transDataView = null;
	private Scrollbar sbar = new Scrollbar(Scrollbar.VERTICAL);
	private int localDataFrom = -1;
	private boolean isScrollbarEnabled = true;
	private boolean dblLineMode = false;
	private TransViewCid cidView = null; // View used in CID; creating this
											// component is trivial, based on
											// CID environment variable

	/**
	 * Sets up the TransView Control
	 */
	public TransView() {
		super();
		BorderLayout bl = new BorderLayout();

		setLayout(bl);
	}

	/**
	 * Initialize with option of scrollbar.
	 *
	 * @param font
	 *            Font size
	 */
	public void init(Font font) {
		init(font, true);
	}

	/**
	 * Initialize with option of scrollbar.
	 *
	 * @param font
	 *            Font size
	 * @param scrollBar
	 *            with / without scrollbar
	 */
	private void init(Font font, boolean scrollBar) {
		setFont(font);
		initGUI(scrollBar);
		initCidView();
	}

	public static TransDataProvider getTransDataProvider() {
		if (dataProvider == null) {
			// UtilLog4j.logInformation(this.getClass(), "Created new TransDataProvider ..");
			dataProvider = new TransDataProvider();
		}
		return dataProvider;
	}

	/**
	 * Initialize the GUI for the TransView Panel
	 */
	public void initGUI(boolean scrollbar) {
		dataProvider = getTransDataProvider();
		this.isScrollbarEnabled = scrollbar;
		dblLineMode = (Struc.options[Struc.O_ElJrn] & 0x01) > 0;
		hdrView = new TransHeaderView(scrollbar);
		if (!dblLineMode) {
			if (scrollbar) {
				transDataView = new TransDataView(dataProvider);
			} else {
				transDataView = new TransDataView(dataProvider, scrollbar, !SELECTION_MODE);
			}
			if (scrollbar) {
				this.add(transDataView, BorderLayout.WEST);
				this.add(sbar, BorderLayout.CENTER);
			} else {
				this.add(transDataView, BorderLayout.CENTER);
			}
			this.add(hdrView, BorderLayout.NORTH);
			int max = dataProvider.getTotalLines();

			sbar.setValues(max - DATA_DISPLAY_LINES, DATA_DISPLAY_LINES, 0, max);
		} else {
			initGUI(scrollbar, true);
		}
		transDataView.setSelectionColor(TransView.colorActiveCaption);
		transDataView.setSelectionTextColor(TransView.colorActiveCaptionText);
		transDataView.setBackground(TransView.colorWindow);
		transDataView.setTextColor(TransView.colorWindowText);

		transDataView.setDissabledSelectColor(TransView.colorInactiveCaption);
		transDataView.setVoidedTextColor(Color.red); // fixed
		transDataView.setVoidedSelectColor(Color.red); // fixed

		hdrView.setFont(this.getFont());
		transDataView.setFont(this.getFont());
		sbar.addAdjustmentListener(this);
		sbar.setBlockIncrement(DATA_DISPLAY_LINES);
		sbar.setBackground(colorScrollbar);
		transDataView.setTransViewScrollBar(this.sbar);
		this.setVisible(true);
	}

	/**
	 * Initialize TransView in double-Line mode
	 *
	 * @param scrollbar
	 * @param dblLineMode
	 */
	private void initGUI(boolean scrollbar, boolean dblLineMode) {
		hdrView = new TransHeaderView(scrollbar);
		if (scrollbar) {
			transDataView = new TransDataDblView(dataProvider);
		} else {
			transDataView = new TransDataDblView(dataProvider, scrollbar, !SELECTION_MODE);
		}
		transDataView.DATA_DISPLAY_LINES = 8;
		if (scrollbar) {
			this.add(transDataView, BorderLayout.WEST);
			this.add(sbar, BorderLayout.CENTER);
		} else {
			this.add(transDataView, BorderLayout.CENTER);
		}
		int max = dataProvider.getTotalLines();

		if (max > DATA_DISPLAY_LINES / 2) {
			max *= 2;
		}
		sbar.setValues(max, DATA_DISPLAY_LINES, 0, max + DATA_DISPLAY_LINES);
		sbar.setUnitIncrement(2);
	}

	public Dimension getPreferredSize() {
		// UtilLog4j.logInformation(this.getClass(), getParent().getSize());
		return new Dimension(0, 0); // accepts the size of the panel from the
									// first card of layout manager
	}

	/**
	 * Initialize the Customer Information Display's TransView Control. (No Navigation / user interactions permitted)
	 */
	public void initCidView() {
		cidView = new TransViewCid();
		cidView.initGUI(dataProvider);
		if (GdPos.panel.cid.width > 0) {
			GdPos.panel.cid.add(cidView, BorderLayout.CENTER);
		}
	}

	/**
	 * Scroll bar actions are triggered here and are direct to TransDataView class to perform the logic
	 */
	public void adjustmentValueChanged(AdjustmentEvent ae) {
		if (!isVisible()) {
			return;
		}
		if (!isScrollbarEnabled) {
			return;
		}
		if (!transDataView.scrollEvent) {
			localDataFrom = transDataView.selIndex[0];
		}
		if (transDataView.mouseClicked) {
			localDataFrom = transDataView.selIndex[0];
		}
		transDataView.scrollEvent = true;
		transDataView.mouseClicked = false;
		switch (ae.getAdjustmentType()) {
		case AdjustmentEvent.UNIT_DECREMENT:
			transDataView.scrollUnitDecrement();
			break;

		case AdjustmentEvent.UNIT_INCREMENT:
			transDataView.scrollUnitIncrement();
			break;

		case AdjustmentEvent.BLOCK_DECREMENT:
			transDataView.scrollBlockDecrement();
			break;

		case AdjustmentEvent.BLOCK_INCREMENT:
			transDataView.scrollBlockIncrement();
			break;

		case AdjustmentEvent.TRACK:
			transDataView.scrollTrack(ae);
			break;
		}
		transDataView.repaint();
	}

	/**
	 * KeyEvents actions are performed here.
	 *
	 * @param vkey
	 *            code
	 * @return 0 if success 7 otherwise
	 */
	public int scroll(int vkey) {
		// UtilLog4j.logInformation(this.getClass(), "this=" + this.isVisible() + " dataView=" +
		// transDataView.isVisible() + " hdrView=" + hdrView.isVisible() );
		if (!this.isVisible()) {
			return 0;
		}
		if (!isScrollbarEnabled) {
			return 0;
		}
		transDataView.mouseClicked = false;
		transDataView.checkOnScroll(localDataFrom);
		transDataView.scrollEvent = false;
		localDataFrom = -1;
		switch (vkey) {
		case KeyEvent.VK_DOWN:
			keyDown();
			break;

		case KeyEvent.VK_UP:
			keyUp();
			break;

		case KeyEvent.VK_PAGE_DOWN:
			keyPageDown();
			break;

		case KeyEvent.VK_PAGE_UP:
			keyPageUp();
			break;

		case KeyEvent.VK_HOME:
			keyHome();
			break;

		case KeyEvent.VK_END:
			keyEnd();
			break;

		default:
			return 7;
		}
		if (cidView != null) {
			cidView.scroll(vkey);
		}
		return 0;
	}

	/**
	 * When keyDown key is pressed
	 */
	public void keyDown() {
		transDataView.keyDown();
	}

	/**
	 * When KeyUp key is pressed
	 */
	public void keyUp() {
		transDataView.keyUp();
	}

	/**
	 * Test for the making the void item
	 */
	public void voidItem(boolean allItems) {
		transDataView.voidItem(allItems);
		transDataView.readAndSetData();
	}

	/**
	 * Test for the making the void item
	 */
	public void errorCorrectItem() {
		transDataView.errorCorrectItem();
		transDataView.readAndSetData();
	}

	/**
	 * Action of Key Home is pressed
	 *
	 * @return 0
	 */
	public int keyHome() {
		return transDataView.keyHome();
	}

	/**
	 * Action of Key End is pressed
	 *
	 * @return 0
	 */
	public int keyEnd() {
		return transDataView.keyEnd();
	}

	/**
	 * Action on PageUp key is pressed
	 *
	 * @return 0
	 */
	public int keyPageUp() {
		return transDataView.keyPageUp();
	}

	/**
	 * Action on Key PageDown is pressed
	 *
	 * @return 0
	 */
	public int keyPageDown() {
		return transDataView.keyPageDown();
	}

	/**
	 * Method is used to update the Data View Canvas. From the external controlls
	 */
	public void update() {
		transDataView.updateView();
		if (cidView != null) {
			cidView.update();
		}
	}

	/**
	 * Method is called from the external controls to update the latest on the Data View area
	 *
	 * @param append
	 */
	public void view(boolean append) {
		if (append) {
			update();
		}
	}

	/**
	 * Handle to TransDataProvider object
	 *
	 * @return TransDataProvider object refrence
	 */
	public TransDataProvider getDataProvider() {
		return dataProvider;
	}

	/**
	 * Returns the Item data object for the void / error correct purpose This is 0 index based item in the list of Vectors Called from LineItem.voidItem() Seq ==> 3
	 *
	 * @return Itemdata object
	 */
	public Itemdata getSelectedItem() {
		Itemdata selItem = null;
		int index = transDataView.selIndex[0] - 1;

		if (transDataView.selIndex[0] - 1 >= 0) {
			selItem = dataProvider.getSelectedItem(index);
		}
		return selItem;
	}

	/**
	 * Return true if last item of the list is selected Called from GdSales.action7(); Seq => 1
	 *
	 * @return true / false
	 */
	public boolean isLastSelected() {
		if (transDataView.selIndex == null) {
			return true;
		}
		if (!this.isVisible()) {
			transDataView.selIndex[0] = transDataView.dataSelIndex = dataProvider.getTotalLines();
			return true;
		}
		if (!isScrollbarEnabled) {
			return true;
		}
		int si = transDataView.selIndex[0];
		boolean sel = false;

		sel = dataProvider.isLastItemSelected(si);
		return sel;
	}

	/**
	 * true ==> if the Item is - already voided or - first tender is pressed or, - item is avoidable - itm.cnt is negative - itm is error corrected return flag as true if any of above is present
	 * Called from LineItem.voidItem() ; Seq => 2
	 *
	 * @return true / false
	 */
	public boolean isVoided() {
		int si = transDataView.selIndex[0];
		Itemdata selItem = getSelectedItem();

		if (selItem == null) {
			return true;
		}
		boolean sel = dataProvider.isVoided(si) || dataProvider.isTender() || dataProvider.isTransFinished()
				|| selItem.cnt < 0 || (selItem.spf1 & 1) > 0;

		return sel;
	}

	/**
	 * This is called to end the transaction (Clear when drawer is closed..)
	 */
	public void clear() {
		dataProvider.reset(true);
		update();
	}

	/**
	 * Bouncer settings, activate
	 */
	void setBouncer(boolean active) {
	}

	public void stop() {
		UtilLog4j.logFatal(this.getClass(), "Entering into transViewPanel.stop()");
		UtilLog4j.logFatal(this.getClass(), "Before transDataView.stop()");
		transDataView.stop();
		UtilLog4j.logFatal(this.getClass(), "After transDataView.stop()");
		if (cidView != null) {
			UtilLog4j.logFatal(this.getClass(), "Before cidView.stop()");
			cidView.stop();
			UtilLog4j.logFatal(this.getClass(), "After cid.stop()");
		}
		UtilLog4j.logFatal(this.getClass(), "Exit from transViewPanel.stop()");
	}

} // end of class
