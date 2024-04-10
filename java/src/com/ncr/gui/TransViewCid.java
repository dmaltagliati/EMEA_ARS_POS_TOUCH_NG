package com.ncr.gui;

import com.ncr.Struc;
import com.ncr.TransDataProvider;

import java.awt.*;
import java.awt.event.AdjustmentEvent;

/**
 * TransView for customer information display. No navigation and scrollbar are enabled.
 *
 * @author bs230003
 */
public class TransViewCid extends TransView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6972679375360560433L;
	private TransHeaderView hdrView = null;
	private TransDataView transDataView = null;
	private Scrollbar sbar = new Scrollbar(Scrollbar.VERTICAL);
	private boolean dblLineMode = false;
	private Font fontCid = new Font("Monospaced", Font.PLAIN, 14);

	/**
	 * Default Constructor
	 */
	public TransViewCid() {
	}

	/**
	 * Initialize the GUI for the TransView Panel for CID
	 */
	public void initGUI(TransDataProvider dataProvider) {
		boolean scrollbarEnabled = false;

		dblLineMode = (Struc.options[Struc.O_ElJrn] & 0x01) > 0;
		if (!dblLineMode) {
			hdrView = new TransHeaderView(scrollbarEnabled, true);
			transDataView = new TransDataView(dataProvider, scrollbarEnabled, !SELECTION_MODE);
			transDataView.DATA_DISPLAY_LINES = 11;
			this.add(transDataView, BorderLayout.CENTER);
			this.add(hdrView, BorderLayout.NORTH);
			int max = dataProvider.getTotalLines();

			sbar.setValues(max - DATA_DISPLAY_LINES, DATA_DISPLAY_LINES, 0, max);
		} else {
			initGUI(scrollbarEnabled, true, dataProvider);
		}
		transDataView.setSelectionColor(TransView.colorActiveCaption);
		transDataView.setSelectionTextColor(TransView.colorActiveCaptionText);
		transDataView.setBackground(TransView.colorWindow);
		transDataView.setTextColor(TransView.colorWindowText);
		transDataView.setViewOperatorMode(false); // mode of opr mode / cid mode
		hdrView.setFont(fontCid);
		transDataView.setFont(fontCid);
		sbar.setBackground(colorScrollbar);
		transDataView.setTransViewScrollBar(this.sbar);
		this.setVisible(true);
	}

	/**
	 * Initialize TransViewCid in double-Line mode
	 *
	 * @param scrollbar
	 * @param dblLineMode
	 */
	private void initGUI(boolean scrollbar, boolean dblLineMode, TransDataProvider dataProvider) {
		hdrView = new TransHeaderView(scrollbar, true);
		transDataView = new TransDataDblView(dataProvider, scrollbar, !SELECTION_MODE);
		transDataView.DATA_DISPLAY_LINES = 8;
		this.add(transDataView, BorderLayout.CENTER);
		int max = dataProvider.getTotalLines();

		if (max > DATA_DISPLAY_LINES / 2) {
			max *= 2;
		}
		sbar.setValues(max, DATA_DISPLAY_LINES, 0, max + DATA_DISPLAY_LINES);
	}

	public Dimension getPreferredSize() {
		Dimension d = new Dimension(0, 0); // UtilLog4j.logInformation(this.getClass(), "d=" + d); //
											// 446

		return d; // accepts the size of the panel from the first card of layout
					// manager
	}

	public void update() {
		if (transDataView != null) {
			transDataView.updateView();
		}
	}

	public int scroll(int vkey) {
		// no action required
		return 0;
	}

	/**
	 * Scroll bar actions are triggered here and are direct to TransDataView class to perform the logic
	 */
	public void adjustmentValueChanged(AdjustmentEvent ae) {// no action
															// required
	}

	public void stop() { // transDataView.bouncer.exit ();
		transDataView.stop();
	}

} // end of class
