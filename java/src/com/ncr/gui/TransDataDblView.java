package com.ncr.gui;

/**
 * java
 *
 * Created on 13. Oktober 2005, 14:21
 *
 * Paints the datalines and also handles different coloured marking + handling
 * of scrolling + navigation
 */

import com.ncr.TransDataProvider;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseEvent;

/**
 * The view class that is responsible for painting the data lines in the transview canvas control This gets 3 lines from the DataProvider object but render them as 6 lines. Each line is splitted into
 * 2 lines while painted to this Canvas.
 *
 * @author bs230003
 */
public class TransDataDblView extends TransDataView {

	private static final long serialVersionUID = 6609916157295782738L;
	protected String orgLines[] = null; // original lines read from file
	protected int NORMAL_TEXT_LEN = 32; // upto the quantity field
	protected int BOLD_TEXT_LEN = 20;

	/**
	 * Constructor that accepts the DataProvider object
	 *
	 * @param dp
	 */
	public TransDataDblView(TransDataProvider dp) {
		super();
		dataProvider = dp;
		TransDataProvider.DATA_DISPLAY_LINES = DATA_DISPLAY_LINES; // UtilLog4j.logInformation(this.getClass(), "DATA_DISPLAY_LINES="
																	// +
																	// DATA_DISPLAY_LINES
																	// );
		this.addMouseListener(new MouseHandler());
		if (getTotalLines() > DATA_DISPLAY_LINES / 2) {
			dataFrom = getTotalLines() - DATA_DISPLAY_LINES / 2 + 1;
		} else {
			dataFrom = 1;
		}
		readAndSetData();
		dataSelIndex = getTotalLines() - 1;
		screenSelIndex = paintLines.length - 1;
	}

	public TransDataDblView(TransDataProvider dp, boolean isWithScrollbar, boolean isWithSelection) {
		this(dp);
		this.isSelectionEnabled = isWithSelection;
		this.isScrollbarEnabled = isWithScrollbar;
	}

	/**
	 * Ask for the size from the panel which is card layout size
	 */
	public Dimension getPreferredSize() {
		Dimension d = getParent().getSize();
		int height = d.height - d.height / 12; // 11 lines with border 3,3,3,3
												// inset
		int width = d.width;

		d = new Dimension(width - TransView.SCROLL_BAR_WIDTH, height);
		return d;
	}

	/**
	 * Break the original lines into two lines, 0-32 ; 8 spaces + 33-57
	 */
	public void setPaintableLines(String[] lines) {
		orgLines = lines;
		int size = lines.length * 2;

		paintLines = new String[size];
		int k = 0;

		for (int i = 0; i < lines.length; i++) {
			paintLines[k++] = lines[i].substring(0, NORMAL_TEXT_LEN);
			if (paintLines[k - 1].startsWith(" --")) {
				paintLines[k++] = " --------------------------------";
			} else if (paintLines[k - 1].startsWith(" >")) {
				paintLines[k++] = lines[i].substring(BOLD_TEXT_LEN);
			} else {
				paintLines[k++] = "        " + lines[i].substring(NORMAL_TEXT_LEN, DATA_LINE_LEN + 1);
			}
		}
	}

	private void initFonts() {
		if (font == null) {
			font = getFont();
			font = new Font(font.getName(), font.getStyle(), font.getSize() * 7 / 4);
			fontDbl = new Font(font.getName(), font.getStyle(), font.getSize() * 4 / 3);
			// bouncer.init (this, FmtIo.localFile ("gif", "BOUNCE.GIF"));
			this.bgColor = getBackground();
		}
	}

	/**
	 * Sets the value to scroll bar
	 *
	 * @param newVal
	 */
	public void setScrollbarValue(int newVal) {
		if (parentScroll.isEnabled()) {
			int max = getTotalLines();

			if (newVal == max - 1) {
				newVal = max;
			}
			if (max > DATA_DISPLAY_LINES / 2) {
				parentScroll.setValue(newVal);
			}
		}
	}

	/**
	 * This method gives the correct data from DataProvider to the Viewer object from a given index
	 *
	 * @param df
	 */
	public void readAndSetData(int df) {
		setPaintableLines(dataProvider.getLines(df, viewOperatorMode));
	}

	/**
	 * This method gives the correct data from DataProvider to the Viewer object
	 */
	public void readAndSetData() {
		if (mouseClicked) {
			return;
		}
		if (dataFrom > getTotalLines() - DATA_DISPLAY_LINES / 2) {
			dataFrom = getTotalLines() - DATA_DISPLAY_LINES / 2 + 1;
		}
		if (dataFrom <= 0) {
			dataFrom = 1;
		}
		setPaintableLines(dataProvider.getLines(dataFrom, viewOperatorMode));
	}

	protected void drawGrid(Graphics g, Dimension d, int fh) {
		if (!GRID) {
			return;
		}
		// int _la [] = {21, 17, 28};
		// int cw = getFontMetrics(font).charWidth(' ');
		// int tx = this.x = (d.width - cw*(NORMAL_TEXT_LEN+1) )/2;
		// //UtilLog4j.logInformation(this.getClass(), tx + ", cw=" + cw + ",fh=" + fh );
		for (int i = 1; i <= DATA_DISPLAY_LINES; i++) {
			g.setColor(Color.lightGray);
			if (i % 2 == 0) {
				g.drawLine(RAISED_PIXELS, i * fh + RAISED_PIXELS - 2, d.width, i * fh + RAISED_PIXELS - 2);
				g.drawLine(RAISED_PIXELS, i * fh + RAISED_PIXELS - 1, d.width, i * fh + RAISED_PIXELS - 1);
				// g.drawLine (tx+(cw*_la[1]), RAISED_PIXELS+(fh*(i-1)),
				// tx+(cw*_la[1]), RAISED_PIXELS+fh*i);
				// g.drawLine (tx+(cw*_la[2]), RAISED_PIXELS+(fh*(i-1)),
				// tx+(cw*_la[2]), RAISED_PIXELS+fh*i);
			}
			// else g.drawLine (tx+(cw*_la[0]), RAISED_PIXELS+(fh*(i-1)),
			// tx+(cw*_la[0]), RAISED_PIXELS+fh*i);
		}
	}

	/**
	 * Main method that does painiting of GUI control and checks when to diplay image only & when to do the data and header painting
	 */
	public synchronized void paint(Graphics g) {
		initFonts();
		Dimension dim = getSize();

		RAISED_PIXELS = (dim.height % DATA_DISPLAY_LINES) / 2;
		if (RAISED_PIXELS <= 1) {
			RAISED_PIXELS = 5;
		}
		int width = dim.width - (2 * RAISED_PIXELS), height = dim.height - (2 * RAISED_PIXELS);
		int fh = height / DATA_DISPLAY_LINES;

		if (paintLines.length == 0) {
			int k = 0;

			g.setColor(Color.gray);
			int w = dim.width, h = dim.height;

			for (; k < RAISED_PIXELS; k++) {
				g.draw3DRect(k, k, w - (k << 1) - 1, h - (k << 1) - 1, raised);
			}
			drawEdgeLines(g, dim);
			g.setColor(bgColor);
			g.fillRect(RAISED_PIXELS, RAISED_PIXELS, width, height);
			drawGrid(g, dim, fh);
			return;
		}
		paintDataLines(fh, g);
		drawGrid(g, dim, fh);
		if (!dataProvider.isTransFinished()) {
			if (isSelectionEnabled) {
				paintSelection(g, fh, width, height);
			}
		} else {
			if (isSelectionEnabled) {
				paintDissabledSelection(g, fh, width, height);
			}
		}

	}

	/**
	 * Draw the data lines are empty line when there are less data lines than the default size of the maximum lines
	 *
	 * @param fh
	 *            font height
	 * @param g
	 *            Graphics object
	 */
	public void paintDataLines(int fh, Graphics g) {
		Dimension dim = getSize();

		g.setColor(Color.gray);
		int i = 0;
		int w = dim.width, h = dim.height;

		for (; i < RAISED_PIXELS; i++) {
			g.draw3DRect(i, i, w - (i << 1) - 1, h - (i << 1) - 1, raised);
		}
		drawEdgeLines(g, dim);
		int width = dim.width - (RAISED_PIXELS * 2);
		int cw = getFontMetrics(font).charWidth(' ');

		this.x = (width - cw * (NORMAL_TEXT_LEN + 1)) / 2;
		int fx, fy;

		fx = fy = RAISED_PIXELS;
		int ty = fh - (fh / 3) + RAISED_PIXELS;

		for (i = 0; i < paintLines.length; i++) {
			if (i >= DATA_DISPLAY_LINES) {
				break;
			}
			if (paintLines[i] == null) {
				break;
			}
			g.setColor(bgColor);
			g.fillRect(fx, fy, width, fh); // UtilLog4j.logInformation(this.getClass(), i + " " + fx +
											// "," + fy + " dim=" + dim + " " +
											// paintLines.length);
			g.setColor(textColor);
			String tmp = paintLines[i];
			char tmpc = orgLines[i / 2].charAt(1);

			if (tmpc == '>') {
				g.setFont(fontDbl);
				tmp = " " + tmp.substring(2, BOLD_TEXT_LEN + 2);
				g.drawString(tmp, x, ty + 2); // +2 do not touch the upper limit
												// of disp. area
			} else {
				g.setFont(font);
				g.setColor(getTextColor(orgLines[i / 2]));
				g.drawString(" " + tmp.substring(1, NORMAL_TEXT_LEN), this.x, ty);
			}
			fy += fh;
			ty += fh;
		}
		for (i = paintLines.length - 1; i < DATA_DISPLAY_LINES - 1; i++) {
			g.setColor(bgColor);
			g.fillRect(fx, fy, width, fh);
			fy += fh;
		}
	}

	/**
	 * This method contains business logic for the controlling of the selection of the an item. When item has one associated discount, it is also shown as sub item with the main item.
	 *
	 * @param g
	 *            Reference to graphics object
	 * @param fh
	 *            Font height in pixels
	 * @param width
	 *            Width of view
	 * @param height
	 *            Height of view
	 */
	public void paintSelection(Graphics g, int fh, int width, int height) {
		if (paintLines.length == 0) {
			return;
		}
		if (screenSelIndex > DATA_DISPLAY_LINES) {
			return;
		}
		if (screenSelIndex < 0) {
			return;
		}
		if (screenSelIndex >= paintLines.length) {
			screenSelIndex = paintLines.length - 1;
		}
		if (screenSelIndex < 0) {
			screenSelIndex = 0;
		}
		screenSelIndex = screenSelIndex - screenSelIndex % 2;
		int tsi = screenSelIndex;
		int sx = 0, sy = 0;

		sx = RAISED_PIXELS;
		sy = tsi * fh + (height % DATA_DISPLAY_LINES);
		String selLine = orgLines[tsi / 2];
		// int cc = Character.getNumericValue(selLine.charAt (DATA_LINE_LEN));
		// // color code
		int cc = Character.getNumericValue(selLine.charAt(DATA_LINE_LEN)); // color
																			// code

		cc = cc == -1 ? 0 : cc;
		g.setColor(dissabledSelectColor);
		if ((cc & 0x03) > 0) {
			g.setColor(selectionColor);
		}
		if (dataProvider.isTender()) {
			g.setColor(dissabledSelectColor);
		}
		if ((cc & 0x07) > 0) {
			g.setColor(selectionColor);
		}
		if (dataProvider.getState() == 0) {
			g.setColor(dissabledSelectColor);
		}

		g.fillRect(sx, sy + RAISED_PIXELS, width, fh * 2);
		g.setColor(selectionTextColor);
		String tmp = paintLines[tsi];
		int margin = fh / 3 - RAISED_PIXELS;

		if (selLine.charAt(1) == '>') {
			g.setFont(fontDbl);
			tmp = " " + tmp.substring(2, BOLD_TEXT_LEN + 2);
			g.drawString(tmp, x, sy + fh - margin + 2);
			tmp = paintLines[tsi + 1];
			g.drawString(" " + tmp.substring(2, BOLD_TEXT_LEN + 2), x, sy + fh + fh - margin + 2);
		} else {
			g.setFont(font);
			tmp = " " + tmp.substring(1, NORMAL_TEXT_LEN);
			g.setColor(getSelectedTextColor(orgLines[tsi / 2]));
			int sty = sy + fh - margin;

			g.drawString(tmp, x, sty);
			tmp = paintLines[tsi + 1];
			g.drawString(tmp.substring(0, NORMAL_TEXT_LEN), x, sty + fh);
		}
		int ts = 1;
		int sc = getSubRecordsCount();

		if (sc > 0 && selLine.charAt(DATA_LINE_LEN) != ' ') {
			for (int sci = 1; sci <= sc * 2; sci++) {
				int dsi = screenSelIndex + sci + 1; // UtilLog4j.logInformation(this.getClass(), "dsi="
													// + dsi + " " + sc );

				if (dsi >= DATA_DISPLAY_LINES) {
					continue;
				}
				g.setColor(dissabledSelectColor);
				g.fillRect(sx, sy + fh + (sci * fh) + RAISED_PIXELS, width, fh);
				tmp = paintLines[dsi];
				g.setColor(getSelectedTextColor(orgLines[dsi / 2]));
				int _sy = 0;

				if (tmp.charAt(1) == '>') {
					g.setFont(fontDbl);
					tmp = " " + tmp.substring(2, BOLD_TEXT_LEN + 2);
					_sy = sy + fh + (sci * fh) - 1;
					g.drawString(tmp, x, _sy);
				} else {
					g.setFont(font);
					_sy = sy + fh + (sci * fh) - margin;
					tmp = " " + tmp.substring(1, NORMAL_TEXT_LEN);
					g.drawString(tmp, x, _sy + fh);
				}
				ts++;
			}
		}
		int si = 0;

		selIndex = new int[ts];
		// capture all the real selected indexes in the file (1-value based)
		// System.err.println ("**screenSelIndex=" + screenSelIndex + ",
		// dataSelIndex=" + dataSelIndex + ", dataFrom=" + dataFrom + ",[si,ei]=
		// [" + startSelIndex + "," + endSelIndex + "]" );
		while (si < ts) {
			selIndex[si] = dataFrom + tsi / 2; // System.err.println
												// (selIndex[si] +" => " +
												// dataProvider.getSelectedItemText(selIndex[si]));
			si++;
		} // UtilLog4j.logInformation(this.getClass(), " ---- " );
	}

	protected int getSubRecordsCount() {
		int rec = 0;

		for (int i = screenSelIndex / 2 + 1; i < orgLines.length; i++) {
			String next = orgLines[i];

			rec++; // UtilLog4j.logInformation(this.getClass(), rec + " " + next );
			if (next.charAt(DATA_LINE_LEN) != ' ') {
				rec--;
				break;
			}
		}
		// UtilLog4j.logInformation(this.getClass(), "rec=" + rec );
		return rec < 0 ? 0 : rec;
	}

	/**
	 * Paints the selection in gray color
	 *
	 * @param g
	 * @param fh
	 * @param width
	 * @param height
	 */
	private void paintDissabledSelection(Graphics g, int fh, int width, int height) {
		if (paintLines.length == 0) {
			return;
		}
		if (screenSelIndex > 10) {
			return;
		}
		int sy = 0, sw = width;
		int sx = RAISED_PIXELS;

		if (screenSelIndex >= paintLines.length) {
			screenSelIndex = paintLines.length - 1;
		}
		if (screenSelIndex < 0) {
			return;
		}
		screenSelIndex = screenSelIndex - screenSelIndex % 2;
		int tsi = screenSelIndex; // = startSelIndex;

		if (tsi >= paintLines.length) {
			return;
		}
		sy = tsi * fh + (height % DATA_DISPLAY_LINES);
		g.setColor(dissabledSelectColor);
		g.fillRect(sx, sy + RAISED_PIXELS, sw, fh * 2);
		g.setColor(getSelectedTextColor(orgLines[tsi / 2], true));
		String tmp = paintLines[tsi];
		int margin = fh / 3 - RAISED_PIXELS;

		if (orgLines[tsi / 2].charAt(1) == '>') {
			g.setFont(fontDbl);
			tmp = " " + tmp.substring(2, BOLD_TEXT_LEN + 2);
			g.drawString(tmp, x, sy + fh - margin + 2);
			tmp = paintLines[tsi + 1];
			g.drawString(" " + tmp.substring(2, BOLD_TEXT_LEN + 2), x, sy + fh + fh - margin + 2);
		} else {
			g.setFont(font);
			tmp = " " + tmp.substring(1, NORMAL_TEXT_LEN);
			int sty = sy + fh - margin;

			g.drawString(tmp, x, sty);
			tmp = paintLines[tsi + 1];
			g.drawString(tmp.substring(0, NORMAL_TEXT_LEN), x, sty + fh);
		}
	}

	// /////////////////////// Key Functions STARTS
	// /////////////////////////////
	public void keyDown() {
		int max = getTotalLines();

		dataSelIndex = dataFrom + screenSelIndex / 2; // -1;
		if (dataSelIndex < max) {
			if (screenSelIndex <= DATA_DISPLAY_LINES) {
				screenSelIndex += 2;
			}
			if (screenSelIndex >= DATA_DISPLAY_LINES) {
				screenSelIndex = DATA_DISPLAY_LINES - 2;
				dataFrom++;
			}
			readAndSetData();
			repaint();
		} // UtilLog4j.logInformation(this.getClass(), "keyDn : dataSelIndex=" + dataSelIndex + ",
			// screenSelIndex="+screenSelIndex + " scroll=" +
			// parentScroll.getValue());
		if (dataSelIndex + 1 == max) {
			parentScroll.setValue(parentScroll.getMaximum());
		} else {
			parentScroll.setValue((dataSelIndex + 1) * 2);
		}
	}

	public void keyUp() {
		dataSelIndex = dataFrom + screenSelIndex / 2 - 1;
		if (dataSelIndex == 0) {
			return;
		}
		if (dataSelIndex > 0) {
			dataSelIndex--;
			if (screenSelIndex >= 0) {
				screenSelIndex -= 2;
			}
			if (screenSelIndex < 0) {
				screenSelIndex = 0;
				dataFrom--;
			}
			readAndSetData();
			repaint();
		} // UtilLog4j.logInformation(this.getClass(), "keyUp : dataSelIndex=" + dataSelIndex + ",
			// screenSelIndex="+screenSelIndex + " scroll=" +
			// parentScroll.getValue());
		parentScroll.setValue(dataSelIndex * 2);
	}

	// /////////////////////// Key Functions ENDS /////////////////////////////

	// ////////////////// Scroll Bar Function ////////////////////////

	public int scrollUnitDecrement() {
		keyUp();
		return 0;
	}

	public int scrollUnitIncrement() {
		keyDown();
		return 0;
	}

	public int scrollBlockIncrement() {
		int max = getTotalLines(), dataBlock = DATA_DISPLAY_LINES / 2;

		dataFrom += dataBlock;
		if (dataFrom > max - dataBlock) {
			dataFrom = max - dataBlock + 1;
			parentScroll.setValue(dataFrom * 2 + screenSelIndex);
		}
		dataSelIndex = dataFrom + dataBlock - 1;
		readAndSetData();
		return 0;
	}

	public int scrollBlockDecrement() {
		int dataBlock = DATA_DISPLAY_LINES / 2;

		dataFrom -= dataBlock;
		if (dataFrom <= 1) {
			dataFrom = 1;
			parentScroll.setValue(screenSelIndex);
		}
		readAndSetData(); // UtilLog4j.logInformation(this.getClass(), dataFrom + " Blk Dec:" +
							// parentScroll.getValue());
		dataSelIndex = dataFrom + dataBlock - 1;
		return 0;
	}

	/**
	 * The index of the selection is roughly estimated !
	 */
	public int scrollTrack(AdjustmentEvent ae) {
		int max = getTotalLines(), trackVal = ae.getValue() / 2, dataBlock = DATA_DISPLAY_LINES / 2;

		if (trackVal < dataBlock) {
			dataFrom = 1;
			parentScroll.setValue(screenSelIndex);
			return 0;
		} else if (trackVal > max - dataBlock) {
			dataFrom = max - dataBlock + 1;
			parentScroll.setValue(dataFrom * 2 + screenSelIndex);
		} else {
			dataFrom = trackVal - dataBlock;
		}
		readAndSetData();
		// parentScroll.setValue(dataFrom * 2 + dataBlock / 2 );
		dataSelIndex = dataFrom + dataBlock / 2 - 1; // UtilLog4j.logInformation(this.getClass(), "TRACK
														// " + trackVal + "
														// dataSelIndex=" +
														// dataSelIndex);
		return 0;
	}

	// ////////////////// Scroll Bar Function ////////////////////////

	// //////////// Key Up/Down/Home/End Functions ///////////////////

	/**
	 * Action of Key Home is pressed
	 *
	 * @return 0
	 */
	public int keyHome() {
		dataSelIndex = 0;
		screenSelIndex = 0;
		dataFrom = 1;
		parentScroll.setValue(0);
		repaint();
		readAndSetData();
		return 0;
	}

	/**
	 * Action of Key End is pressed
	 *
	 * @return 0
	 */
	public int keyEnd() {
		int max = getTotalLines();

		screenSelIndex = DATA_DISPLAY_LINES - 2;
		dataSelIndex = max - 1; // 0 based index
		dataFrom = max - DATA_DISPLAY_LINES / 2 + 1;
		parentScroll.setValue(parentScroll.getMaximum());
		readAndSetData();
		repaint();
		return 0;
	}

	/**
	 * Action on PageUp key is pressed
	 *
	 * @return 0
	 */
	public int keyPageUp() {
		int max = getTotalLines();

		if (max >= DATA_DISPLAY_LINES && dataFrom > 1) {
			dataSelIndex -= DATA_DISPLAY_LINES / 2;
			dataFrom -= DATA_DISPLAY_LINES / 2;
			parentScroll.setValue(parentScroll.getValue() - DATA_DISPLAY_LINES);
			readAndSetData();
			repaint();
		}
		return 0;
	}

	/**
	 * Action on Key PageDown is pressed
	 *
	 * @return 0
	 */
	public int keyPageDown() {
		int max = getTotalLines();

		if (dataFrom <= max - DATA_DISPLAY_LINES / 2) {
			dataSelIndex += DATA_DISPLAY_LINES / 2;
			dataFrom += DATA_DISPLAY_LINES / 2;
			parentScroll.setValue(parentScroll.getValue() + DATA_DISPLAY_LINES);
			readAndSetData();
			repaint();
		}
		return 0;
	}

	// //////////// Key Up/Down/Home/End Functions ///////////////////
	/**
	 * This method adjust the counters when different controlls are used in navigation (key/mouse/scroll)
	 */
	public void checkOnScroll(int localDataFrom) {
		if (scrollEvent && screenSelIndex >= DATA_DISPLAY_LINES) { // start from
																	// bottom
			dataFrom = localDataFrom - DATA_DISPLAY_LINES / 2 - 1;
			dataSelIndex = dataFrom + DATA_DISPLAY_LINES - 2;
		} else if (scrollEvent && screenSelIndex < 0) { // start from top
			dataFrom = localDataFrom + 1;
			dataSelIndex = dataFrom - 1;
		}
		if (scrollEvent) { // when scroll bar movement was previous
			// UtilLog4j.logInformation(this.getClass(), "Last dataFrom before scroll=" + localDataFrom
			// + " dataSelIndex=" + transDataView.dataSelIndex);
			setScrollbarValue(dataFrom + screenSelIndex / 2 - 1);
		}
	}

	/**
	 * Return the single selected text value
	 *
	 * @return text at selection
	 */
	public String getSelectedText() {
		if (screenSelIndex < paintLines.length) {
			return paintLines[screenSelIndex];
		} else {
			return null;
		}
	}

	/**
	 * Returns the array of selected texts values
	 *
	 * @return array of strings when multi-selection
	 */
	public String[] getSelectedTexts() {
		String[] ret = new String[1];

		if (selIndex[0] > 0) {
			ret[0] = dataProvider.getSelectedItemText(selIndex[0]);
		}
		return ret;
	}

	/**
	 * When new item entry is arrived.
	 */
	public void updateView() {
		mouseClicked = false;
		int max = dataProvider.getTotalLines();

		if (max * 2 > DATA_DISPLAY_LINES) {
			parentScroll.setEnabled(true);
		}
		dataSelIndex = max - 1;
		if (max < DATA_DISPLAY_LINES / 2) {
			screenSelIndex = max + 2;
		} else {
			screenSelIndex = DATA_DISPLAY_LINES;
		}
		if (max * 2 > DATA_DISPLAY_LINES) {
			parentScroll.setValues(max * 2, DATA_DISPLAY_LINES, 0, max * 2 + DATA_DISPLAY_LINES);
		} else {
			parentScroll.setValues(max, DATA_DISPLAY_LINES, 0, DATA_DISPLAY_LINES);
		}
		dataFrom = max - DATA_DISPLAY_LINES / 2 + 1;
		readAndSetData();
		repaint();
	}

	protected void handleMouseEvent(MouseEvent me) {
		int lh = (getSize().height / DATA_DISPLAY_LINES);
		int mSelIndex = (me.getY() / lh) / 2;

		if (dataFrom < 1) {
			dataSelIndex = mSelIndex - 1;
		} else {
			dataSelIndex = dataFrom + mSelIndex - 1;
		}
		screenSelIndex = mSelIndex * 2;
		mouseClicked = true;
		if (me.getClickCount() > 1) {
			dataProvider.showLastTrans();
			dataSelIndex = getTotalLines();
			dataFrom = dataSelIndex - DATA_DISPLAY_LINES / 2 + 1;
			// UtilLog4j.logInformation(this.getClass(), "datafrom=" + dataFrom + " dataSelIndex=" +
			// dataSelIndex) ;
		}
		setScrollbarValue((dataSelIndex + 1) * 2);
		// UtilLog4j.logInformation(this.getClass(), "Mouse Sel=" + mSelIndex + " dataSelIndex=" +
		// dataSelIndex) ;
		readAndSetData(dataFrom);
		repaint();
	}

} // end of class
