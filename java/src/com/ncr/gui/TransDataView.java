package com.ncr.gui;

/**
 * TransDataView.java
 *
 * Created on 13. Oktober 2005, 14:21
 *
 */

import com.ncr.TransDataProvider;
import com.ncr.UtilLog4j;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class repersents the DataView in the Canvas
 *
 * @author bs230003
 */
public class TransDataView extends Canvas {

	private static final long serialVersionUID = 6609916157295782737L;

	public int DATA_LINE_LEN = TransDataProvider.DATA_LINE_LEN;
	public int DATA_DISPLAY_LINES = TransView.DATA_DISPLAY_LINES;
	static boolean multiColor = true;

	protected int x = 0;
	protected String paintLines[] = null;
	protected Font font = null;
	protected Font fontDbl = null;
	protected Color bgColor = Color.white;
	protected Color textColor = Color.black;
	protected Color selectionColor = Color.blue;
	protected Color dissabledSelectColor = Color.gray;
	protected Color selectionTextColor = Color.white;
	protected Color voidedSelectedTextColor = Color.red;
	protected Color voidedTextColor = Color.red;
	protected int yVal = 0;
	protected int mSelIndex = -1;
	protected int RAISED_PIXELS = 3;
	protected boolean showLines = false;
	protected Image gif = null;
	protected Scrollbar parentScroll = null; // Handle for setting scroll value
												// when mouse is used
	protected TransDataProvider dataProvider = null;
	protected boolean isSelectionEnabled = true;
	protected boolean isScrollbarEnabled = true;
	protected boolean raised = false; // base rectangular area..
	protected boolean viewOperatorMode = true;

	protected boolean GRID = false;
	protected int[] la = { 20, 31, 40, 51 }; // start index of columns

	// package level variables
	public boolean mouseClicked = false;
	public boolean scrollEvent = false;
	public int[] selIndex = { 0 }; // 1 based selection
	public int dataSelIndex = 0; // varies from 0 to n ( n = lines in file)
	int screenSelIndex = 0; // varies from 0 - 10
	int dataFrom = 0; // varies from 1 - 11

	/**
	 * Default Constructor
	 */
	protected TransDataView() {
	}

	;

	/**
	 * Constructor that accepts the DataProvider object
	 *
	 * @param dp
	 *            : Object of TransDataProvider
	 */

	public TransDataView(TransDataProvider dp) {
		setName("transDataView");
		this.dataProvider = dp;
		multiColor = dataProvider.isMultiColor();
		this.addMouseListener(new MouseHandler());
		if (getTotalLines() > DATA_DISPLAY_LINES) {
			dataFrom = getTotalLines() - DATA_DISPLAY_LINES + 1;
		} else {
			dataFrom = 1;
		}
		readAndSetData();
		dataSelIndex = getTotalLines() - 1;
		screenSelIndex = paintLines.length - 1;
	}

	public TransDataView(TransDataProvider dp, boolean isWithScrollbar, boolean isWithSelection) {
		this(dp);
		this.isSelectionEnabled = isWithSelection;
		this.isScrollbarEnabled = isWithScrollbar;
	}

	// Holds only reference to scroll bar
	public void setTransViewScrollBar(Scrollbar bar) {
		this.parentScroll = bar;
	}

	public void setScrollbarValue(int newVal) {
		if (parentScroll.isEnabled()) {
			int max = getTotalLines();

			if (newVal == max - 1) {
				newVal = max;
			}
			if (max > DATA_DISPLAY_LINES) {
				parentScroll.setValue(newVal);
			}
		}
	}

	// /**
	// * This method is called from the framework to refresh the contents of
	// screen.
	// */
	// public void update(Graphics g) {
	// paint(g);
	// }

	public void setPaintableLines(String[] lines) {
		paintLines = lines;
	}

	public void printLines() {
		String s = "";
		for (int i = 0; i < paintLines.length; i++) {
			s += paintLines[i] + " ,";
		}
		UtilLog4j.logInformation(this.getClass(), "s=" + s);
	}

	/**
	 * Ask for the size from the panel which is card layout size
	 */
	public Dimension getPreferredSize() {
		Dimension d = getParent().getSize();
		int height = d.height - d.height / 12; // 11 lines with 3,3,3,3 inset
		int width = d.width;

		d = new Dimension(width - TransView.SCROLL_BAR_WIDTH, height);
		return d;
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
	 * Operator mode : See all the tags if defined in Copt Parameter
	 *
	 * @param val
	 *            : true ; means Operator mode : false : CID view mode
	 */
	public void setViewOperatorMode(boolean val) {
		viewOperatorMode = val;
	}

	/**
	 * This method gives the correct data from DataProvider to the Viewer object
	 */
	public void readAndSetData() {
		if (mouseClicked) {
			return;
		}
		if (dataFrom > getTotalLines() - DATA_DISPLAY_LINES) {
			dataFrom = getTotalLines() - DATA_DISPLAY_LINES + 1;
		}
		if (dataFrom <= 0) {
			dataFrom = 1;
		}
		setPaintableLines(dataProvider.getLines(dataFrom, viewOperatorMode));

	}

	/**
	 * Draws the lines at the four diagonal corners of the rectangle
	 *
	 * @param g
	 *            Graphics object
	 * @param dim
	 *            view's dimension
	 */
	protected void drawEdgeLines(Graphics g, Dimension dim) {
		if (RAISED_PIXELS > 0) {
			int pc = RAISED_PIXELS - 1;

			g.setColor(Color.gray);
			g.drawLine(0, 0, pc, pc);
			g.setColor(Color.white);
			g.drawLine(dim.width - pc, pc, dim.width, 0);
			g.setColor(Color.white);
			g.drawLine(0, dim.height, pc, dim.height - pc);
			g.setColor(Color.gray);
			g.drawLine(dim.width - pc, dim.height - pc, dim.width, dim.height);
		}
	}

	protected void drawGrid(Graphics g, Dimension d, int fh) {
		if (!GRID) {
			return;
		}
		for (int i = 1; i <= DATA_LINE_LEN; i++) {
			g.setColor(Color.lightGray);
			g.drawLine(RAISED_PIXELS, i * fh + RAISED_PIXELS, d.width, i * fh + RAISED_PIXELS);
		}

		/*
		 * int cw = getFontMetrics(font).charWidth(' '); int tx = this.x = (d.width - cw*(DATA_LINE_LEN-2) )/2; if (getSize().width > 514) tx = tx - 2; // dirty trick but works if (getSize().width <
		 * 514) tx = tx + 2; for (int i=0; i<la.length; i++) g.drawLine (tx + (cw*la[i]), RAISED_PIXELS, tx+(cw*la[i]), d.height);
		 */
	}

	/**
	 * Main method that does painiting of GUI control and checks when to diplay image only & when to do the data and header painting
	 */
	public synchronized void paint(Graphics g) {
		initFont();
		Dimension dim = getSize();

		// bouncer.hide ();
		RAISED_PIXELS = (dim.height % DATA_DISPLAY_LINES) / 2;
		int width = dim.width - (2 * RAISED_PIXELS), height = dim.height - (2 * RAISED_PIXELS);
		int fh = height / DATA_DISPLAY_LINES;

		if (paintLines.length == 0) {
			g.setColor(Color.gray);
			int w = dim.width, h = dim.height;

			for (int k = 0; k < RAISED_PIXELS; k++) {
				g.draw3DRect(k, k, w - (k << 1) - 1, h - (k << 1) - 1, raised);
			}
			drawEdgeLines(g, dim);
			g.setColor(bgColor);
			g.fillRect(RAISED_PIXELS, RAISED_PIXELS, width, height);
			drawGrid(g, dim, fh);
			return;
		}
		paintDataLines(fh, g);
		if (!dataProvider.isTransFinished()) {
			if (isSelectionEnabled) {
				paintSelection(g, fh, width, height);
			}
			// else paintDissabledSelection(g, fh, width, height);
		} else {
			if (isSelectionEnabled) {
				paintDissabledSelection(g, fh, width, height);
			}
		}
		drawGrid(g, dim, fh);
	}

	/**
	 * Initialize the Fonts at the first call to paint method.
	 */
	private void initFont() {
		if (font == null) {
			font = getFont();
			fontDbl = new Font(font.getName(), font.getStyle(), font.getSize() * 4 / 3);
			// bouncer.init (this, FmtIo.localFile ("gif", "BOUNCE.GIF"));
			this.bgColor = getBackground();
		}
	}

	/**
	 * Paints the data lines read from the TransDataProvider
	 *
	 * @param fh
	 *            font height in pixel, calculated from font_size
	 * @param g
	 *            Graphics object
	 */
	public void paintDataLines(int fh, Graphics g) {

		Dimension dim = getSize();

		g.setColor(Color.gray);
		int w = dim.width, h = dim.height;

		for (int k = 0; k < RAISED_PIXELS; k++) {
			g.draw3DRect(k, k, w - (k << 1) - 1, h - (k << 1) - 1, raised);
		}
		drawEdgeLines(g, dim);
		int width = dim.width - (RAISED_PIXELS * 2);
		int cw = getFontMetrics(font).charWidth(' ');

		this.x = (width - cw * (DATA_LINE_LEN - 2)) / 2;
		int fy = RAISED_PIXELS;
		int ty = fh - (fh / 3) + RAISED_PIXELS;
		int i = 0;
		int fx = RAISED_PIXELS;

		for (; i < paintLines.length; i++) {
			g.setColor(bgColor);
			g.fillRect(fx, fy, width, fh);
			g.setColor(textColor);
			String tmp = paintLines[i];

			if (tmp.charAt(1) == '>') {
				g.setFont(fontDbl);
				tmp = tmp.substring(2, DATA_LINE_LEN);
				g.drawString(tmp, x, ty + 2); // +2 do not touch the upper limit
												// of disp. area
			} else {
				g.setFont(font);
				g.setColor(getTextColor(tmp));
				tmp = tmp.substring(0, DATA_LINE_LEN);
				g.drawString(tmp.substring(1), this.x, ty);
			}
			ty += fh;
			fy += fh;
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
	 * @param width
	 * @param height
	 */

	public void paintSelection(Graphics g, int fh, int width, int height) {
		if (paintLines.length == 0) {
			return;
		}
		if (screenSelIndex > DATA_DISPLAY_LINES - 1) {
			return;
		}
		if (screenSelIndex >= paintLines.length) {
			screenSelIndex = paintLines.length - 1;
		}
		if (screenSelIndex < 0) {
			return;
		}
		int sy = 0, sw = width;
		int sx = RAISED_PIXELS;

		if (screenSelIndex < 0) {
			screenSelIndex = 0;
		}
		String selLine = paintLines[screenSelIndex];
		int cc = Character.getNumericValue(selLine.charAt(DATA_LINE_LEN)); // color
																			// code

		cc = cc == -1 ? 0 : cc;
		// int max = getTotalLines();
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
		sy = screenSelIndex * fh;
		g.fillRect(sx, sy + RAISED_PIXELS, sw, fh);
		g.setColor(selectionTextColor);
		String tmp = paintLines[screenSelIndex];
		int margin = fh / 3 - RAISED_PIXELS;

		if (tmp.charAt(1) == '>') {
			tmp = tmp.substring(2, DATA_LINE_LEN);
			g.setFont(fontDbl);
			g.drawString(tmp, x, sy + fh - margin + 2);
		} else {
			g.setColor(getSelectedTextColor(tmp));
			tmp = tmp.substring(0, DATA_LINE_LEN);
			int sty = sy + fh - margin;

			g.setFont(font);
			g.drawString(tmp.substring(1), x, sty);
		}
		int ts = 1;
		int sc = getSubRecordsCount();

		if (selLine.charAt(DATA_LINE_LEN) != ' ') {
			for (int sci = 1; sci <= sc; sci++) {
				int dsi = screenSelIndex + sci;

				if (dsi >= DATA_DISPLAY_LINES) {
					continue;
				}
				g.setColor(dissabledSelectColor);
				g.fillRect(sx, sy + (sci * fh) + RAISED_PIXELS, sw, fh);
				tmp = paintLines[dsi];
				g.setColor(getSelectedTextColor(tmp));
				int _sy = 0;

				if (tmp.charAt(1) == '>') {
					g.setFont(fontDbl);
					tmp = tmp.substring(2, DATA_LINE_LEN);
					_sy = sy + (sci * fh) + fh - 1;
					g.drawString(tmp, x, _sy);
				} else {
					g.setFont(font);
					_sy = sy + (sci * fh) + fh - margin;
					tmp = tmp.substring(0, DATA_LINE_LEN);
					g.drawString(tmp.substring(1), x, _sy);
				}
				ts++;
			}
		}
		int si = 0;

		selIndex = new int[ts];
		// capture all the real selected indexes in the file (1-value based)
		while (si < ts) {
			selIndex[si] = dataFrom + screenSelIndex + si;
			si++;
		} // UtilLog4j.logInformation(this.getClass(), " ---- " );
	}

	protected int getSubRecordsCount() {
		int rec = 0;

		for (int i = screenSelIndex + 1; i < paintLines.length; i++) {
			String next = paintLines[i];

			rec++;
			if (next.charAt(DATA_LINE_LEN) != ' ') {
				rec--;
				break;
			}
		}
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

		if (paintLines.length < screenSelIndex) {
			screenSelIndex = paintLines.length - 1;
		}
		if (screenSelIndex < 0) {
			return;
		}
		g.setColor(dissabledSelectColor);
		sy = screenSelIndex * fh + (height % DATA_DISPLAY_LINES);
		g.fillRect(sx, sy + RAISED_PIXELS, sw, fh);
		String tmp = paintLines[screenSelIndex];
		int margin = fh / 3 - RAISED_PIXELS;

		if (tmp.charAt(1) == '>') {
			g.setFont(fontDbl);
			g.setColor(getSelectedTextColor(tmp));
			tmp = "" + tmp.substring(2, DATA_LINE_LEN);
			g.drawString(tmp, x, sy + fh - margin + 2);
		} else {
			g.setFont(font);
			g.setColor(getSelectedTextColor(tmp));
			tmp = tmp.substring(0, DATA_LINE_LEN);
			int sty = sy + fh - margin;

			g.drawString(tmp.substring(1), x, sty);
		}
	}

	protected int getColorFlag(char ch) {
		ch = (ch == ' ' ? '0' : ch);
		return Character.getNumericValue(ch);
	}

	protected Color getSelectedTextColor(String line) {
		Color c = selectionTextColor;
		char ch = ' ';

		if (multiColor) {
			ch = line.charAt(52);
			if (ch != ' ' && ch != '-' && ch != TransDataProvider.ITEM_QTY_MARK) { // voided
																					// /
																					// EC
																					// lines
				c = Color.white;
			}
			int cc = getColorFlag(line.charAt(DATA_LINE_LEN));

			if ((cc & 6) > 0) { // -ve sales line
				c = voidedSelectedTextColor.brighter(); // c = Color.white;
			}
		}
		return c;
	}

	protected Color getSelectedTextColor(String line, boolean dissabledMode) {
		Color c = selectionTextColor;
		char ch = ' ';

		if (multiColor) {
			ch = line.charAt(52);
			if (ch != ' ' && ch != '-' && ch != TransDataProvider.ITEM_QTY_MARK) { // voided
																					// /
																					// EC
																					// lines
				c = selectionTextColor;
			}
			int cc = getColorFlag(line.charAt(DATA_LINE_LEN));

			if ((cc & 6) > 0) { // -ve sales line
				c = voidedSelectedTextColor;
			}
		}
		return c;
	}

	protected Color getTextColor(String line) {
		Color c = textColor;
		char ch = ' ';

		if (multiColor) {
			ch = line.charAt(52);
			if (ch != ' ' && ch != '-' && ch != TransDataProvider.ITEM_QTY_MARK) { // voided
																					// /
																					// EC
																					// lines
				c = dissabledSelectColor;
			}
			int cc = getColorFlag(line.charAt(DATA_LINE_LEN));

			if ((cc & 6) > 0) { // -ve sales line
				c = voidedTextColor;
			} else if (cc == 0) {
				c = dissabledSelectColor;
			}
		}
		return c;
	}

	/**
	 * Total Lines in the Files
	 */
	public int getTotalLines() {
		return dataProvider.getTotalLines();
	}

	public String[] getPaintedLines() {
		return paintLines;
	}

	/**
	 * Over-write the values in the file at certain location index
	 */
	public void voidItem(boolean allItems) {
		dataProvider.overWrite(selIndex, TransDataProvider.LINE_ITEM_VOID, allItems);
		repaint();
	}

	/**
	 * Over-write the values in the file at certain location index
	 */
	public void errorCorrectItem() {
		if (isScrollbarEnabled) {
			dataProvider.overWrite(selIndex, TransDataProvider.ERROR_CORRECT, false);
			repaint();
		}
	}

	public int getNextItemLine() {
		int i = 0;

		for (; i < paintLines.length; i++) {
			char ch = paintLines[i].charAt(56);

			if (ch != ' ') {
				UtilLog4j.logInformation(this.getClass(), ch + " " + i);
				break;
			}
		}
		return i;
	}

	// /////////////////////// Key Functions STARTS
	// /////////////////////////////
	public void keyDown() {
		int max = getTotalLines();

		if (dataFrom + screenSelIndex > max) {
			return;
		}
		dataSelIndex = dataFrom + screenSelIndex;
		if (dataSelIndex < max) {
			dataSelIndex++;
			if (screenSelIndex <= DATA_DISPLAY_LINES) {
				screenSelIndex++;
			}
			if (screenSelIndex >= DATA_DISPLAY_LINES) {
				screenSelIndex = DATA_DISPLAY_LINES - 1;
				dataFrom++;
			}
			readAndSetData();
			repaint();
		}
		parentScroll.setValue(dataSelIndex);
	}

	public void keyUp() {
		dataSelIndex = dataFrom + screenSelIndex - 1; // UtilLog4j.logInformation(this.getClass(), "dataSel="
														// + dataSelIndex );
		if (dataSelIndex == 0) {
			return;
		}
		if (dataSelIndex >= 0) {
			dataSelIndex--;
			if (screenSelIndex >= 0) {
				screenSelIndex--;
			}
			if (screenSelIndex < 0) {
				screenSelIndex = 0;
				dataFrom--;
			}
			readAndSetData();
			repaint();
		} // UtilLog4j.logInformation(this.getClass(), dataSelIndex);
		parentScroll.setValue(dataSelIndex < 1 ? 0 : dataSelIndex);
	}

	public int keyHome() {
		dataSelIndex = 0;
		screenSelIndex = 0;
		dataFrom = 1;
		parentScroll.setValue(0);
		repaint();
		readAndSetData();
		return 0;
	}

	public int keyEnd() {
		int max = dataProvider.getTotalLines();

		screenSelIndex = DATA_DISPLAY_LINES - 1;
		dataSelIndex = max - 1; // 0 based index
		dataFrom = max - DATA_DISPLAY_LINES + 1;
		parentScroll.setValue(parentScroll.getMaximum());
		readAndSetData();
		repaint();
		return 0;
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
		int max = getTotalLines();

		dataFrom += DATA_DISPLAY_LINES;
		if (dataFrom >= max - DATA_DISPLAY_LINES) {
			dataFrom = max - DATA_DISPLAY_LINES + 1;
			parentScroll.setValue(dataFrom + screenSelIndex);
		}
		dataSelIndex = dataFrom + screenSelIndex - 1;
		readAndSetData();
		return 0;
	}

	public int scrollBlockDecrement() {
		dataFrom -= DATA_DISPLAY_LINES;
		if (dataFrom <= 1) {
			parentScroll.setValue(screenSelIndex);
		} else {
			dataSelIndex -= DATA_DISPLAY_LINES;
		}
		readAndSetData();
		dataSelIndex = dataFrom + screenSelIndex - 1;
		return 0;
	}

	public int scrollTrack(AdjustmentEvent ae) {
		int max = getTotalLines();

		if (ae.getValue() < 5) {
			dataFrom = 1;
			parentScroll.setValue(screenSelIndex);
		} else if (ae.getValue() > max - 5) {
			dataFrom = max - DATA_DISPLAY_LINES + 1;
			parentScroll.setValue(dataFrom + screenSelIndex);
		} else {
			dataFrom = ae.getValue() - 5;
		}
		readAndSetData();
		dataSelIndex = dataFrom + screenSelIndex - 1;
		return 0;
	}

	// ////////////////// Scroll Bar Function ////////////////////////

	// //////////////////// Page Up / Page Down

	/**
	 * Action on PageUp key is pressed
	 *
	 * @return 0
	 */
	public int keyPageUp() {
		int max = getTotalLines();

		if (max >= DATA_DISPLAY_LINES && dataFrom > 1) {
			dataFrom -= DATA_DISPLAY_LINES;
			dataSelIndex = dataFrom + screenSelIndex - 1;
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

		if (dataFrom <= max - DATA_DISPLAY_LINES) {
			dataFrom += DATA_DISPLAY_LINES;
			dataSelIndex = dataFrom + screenSelIndex - 1;
			parentScroll.setValue(parentScroll.getValue() + DATA_DISPLAY_LINES);
			readAndSetData();
			repaint();
		}
		return 0;
	}

	public void checkOnScroll(int localDataFrom) {
		if (scrollEvent && screenSelIndex == DATA_DISPLAY_LINES) { // start from
																	// bottom
			dataFrom = localDataFrom - DATA_DISPLAY_LINES;
			dataSelIndex = dataFrom + DATA_DISPLAY_LINES - 1;
		} else if (scrollEvent && screenSelIndex == -1) { // start from top
			dataFrom = localDataFrom + 1;
			dataSelIndex = dataFrom - 1;
		}
	}

	// ////////////////////Page Up / Page Down /////////////////////////

	public void setSelectionColor(Color newColor) {
		this.selectionColor = newColor;
	}

	public void setSelectionTextColor(Color newColor) {
		this.selectionTextColor = newColor;
	}

	public void setBgColor(Color newColor) {
		this.bgColor = newColor;
	}

	public void setTextColor(Color newColor) {
		this.textColor = newColor;
	}

	public void setDissabledSelectColor(Color c) {
		dissabledSelectColor = c;
	}

	public void setVoidedSelectColor(Color c) {
		voidedSelectedTextColor = c;
	}

	public void setVoidedTextColor(Color c) {
		voidedTextColor = c;
	}

	/**
	 * Returns the array of selected texts values
	 *
	 * @return array of string objects selected
	 */
	public String[] getSelectedTexts() {
		String[] ret = new String[1];

		if (selIndex[0] > 0) {
			ret[0] = dataProvider.getSelectedItemText(selIndex[0]);
		}
		return ret;
	}

	/**
	 * Return the line at asked position. values 0..10, if value is there otherwise null
	 *
	 * @param position
	 * @return returns the string at position
	 */
	public String getText(int position) {
		if (paintLines != null && paintLines.length > position && paintLines[position] != null) {
			return paintLines[position];
		} else {
			return null;
		}
	}

	public void updateView() {
		mouseClicked = false;
		int max = dataProvider.getTotalLines();

		if (max > DATA_DISPLAY_LINES) {
			parentScroll.setEnabled(true);
		}
		dataSelIndex = max - 1;
		if (max < DATA_DISPLAY_LINES) {
			screenSelIndex = max - 1;
		} else {
			screenSelIndex = DATA_DISPLAY_LINES - 1;
		}
		if (max > DATA_DISPLAY_LINES) {
			parentScroll.setValues(max, DATA_DISPLAY_LINES, 0, max + DATA_DISPLAY_LINES);
		} else {
			parentScroll.setValues(max, DATA_DISPLAY_LINES, 0, DATA_DISPLAY_LINES);
		}
		dataFrom = max - DATA_DISPLAY_LINES + 1;
		readAndSetData();
		repaint();
	}

	protected void handleMouseEvent(MouseEvent me) {
		yVal = me.getY();
		int lh = (getSize().height / DATA_DISPLAY_LINES);

		mSelIndex = (yVal / lh);
		if (mSelIndex >= getTotalLines() - 1) {
			mSelIndex = getTotalLines() - 1;
		}
		dataSelIndex = dataFrom + mSelIndex - 1;
		screenSelIndex = mSelIndex;
		mouseClicked = true;
		if (me.getClickCount() > 1) {
			dataProvider.showLastTrans();
			dataSelIndex = dataProvider.getTotalLines();
			dataFrom = dataSelIndex - DATA_DISPLAY_LINES + 1;
		}
		readAndSetData(dataFrom);
		setScrollbarValue(dataSelIndex);
		repaint();
	}

	public void stop() {
	}

	// ///////////////////////////////////////////////// Mouse Event Handler
	// ///////////////////
	/**
	 * MouseHandler for the view area of the data; capture the mouse click events for selecting a line.
	 *
	 * @author bs230003
	 */
	class MouseHandler extends MouseAdapter {
		public MouseHandler() {
		}

		public void mouseReleased(MouseEvent me) {
			handleMouseEvent(me);
		}

	}
} // end of TransDataView class
