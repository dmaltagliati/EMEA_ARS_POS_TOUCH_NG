package com.ncr;

import com.ncr.gui.TransView;

import java.util.Vector;

/**
 * This class reads data from a list of objects are made by TransDataParser.
 *
 * @author bs230003
 */
public class TransDataProvider extends Action {

	public static int DATA_DISPLAY_LINES = TransView.DATA_DISPLAY_LINES; // lines
																			// fetched
																			// to
																			// display
	public static int LINE_SIZE = 59; // record size 59
	// | | | | |
	// 01234567890123456789012345678901234567890123456789012345
	// Paper Bag 1 0,50 0,50 BS0
	public static String DATA_LINE_SEPARATOR = "--------------------------------------------------------"; //
	public static int DATA_LINE_LEN = 56;
	public static int VOID_MARK_INDEX = 52;
	public static int ERROR_CORRECT = 1;
	public static int LINE_ITEM_VOID = 2;
	public static char ITEM_QTY_MARK = ' ';

	private Vector tdList = new Vector(10); // List of TransData objects..
	private boolean isTransFinished = false;
	private boolean isTender = false;
	private boolean debug = false;
	private int TRANS_STATE = 1;
	private boolean show = false;

	/**
	 * toggle the finished transaction view
	 */

	private String[] lastTrans = null;

	/**
	 * Default constructor
	 */
	public TransDataProvider() {
		ITEM_QTY_MARK = GdPos.panel.mnemo.getText(60).charAt(7); // X: quantity
	}

	/**
	 * Returns the array of the texts stored in vector of TransaData objects.
	 *
	 * @return : array of strings.
	 */
	private String[] getLines(int from) {
		int i = 0;

		from = from <= 0 ? 0 : --from;
		String ret[] = new String[DATA_DISPLAY_LINES];

		while (from < tdList.size() && i < DATA_DISPLAY_LINES) {
			ret[i++] = ((TransData) tdList.elementAt(from++)).text;
		}
		String rets[] = new String[i];

		for (i = 0; i < rets.length; i++) {
			rets[i] = ret[i]; // UtilLog4j.logInformation(this.getClass(), rets[i]+"#");
		}
		return rets;
	}

	/**
	 * Returns the array for strings for CID.
	 *
	 * @return : array of strings
	 */
	public String[] getLines(int from, boolean oprMode) {
		String[] tmp = getLines(from);

		for (int i = 0; i < tmp.length; i++) {
			if (oprMode) {
				tmp[i] = parseTagOption(tmp[i]);
			} else {
				tmp[i] = parseTagOption(tmp[i], true);
			}
		}
		return tmp;
	}

	/**
	 * supress the tags part based on setting in p_regpar [for CutomerInfoDislplay]
	 */
	private String parseTagOption(String in, boolean cidMode) {
		if (cidMode) { // no tags are printed
			char sa[] = in.toCharArray();

			sa[53] = sa[54] = sa[55] = ' '; // all are spaces
			in = new String(sa);
			return in;
		}
		return in;
	}

	/**
	 * supress the tags part based on setting in p_regpar
	 */
	private String parseTagOption(String in) {
		char sa[] = in.toCharArray();
		int option = getTagOption();

		if ((option & 1) > 0) { // no itemization
			sa[54] = sa[55] = ' ';
		}
		if ((option & 2) > 0) { // no VAT Code
			sa[53] = ' ';
		}
		in = new String(sa); // UtilLog4j.logInformation(this.getClass(), in);
		return in;
	}

	/**
	 * This returns true if gray/red color are used otherwise only black color is used. 1C : 0001 1111 >> 0x8=transview def; 0x4=color, 0x2=sorting ; 0x1 double line mode
	 *
	 * @return true if COPT 08 position == 1C / otherwise false when 0C
	 */
	public boolean isMultiColor() {
		return (options[O_ElJrn] & 0x04) != 0;
	}

	public void setTransFinished(boolean val) {
		isTransFinished = val;
	}

	public boolean isTransFinished() {
		return isTransFinished;
	}

	public void setTender(boolean t) {
		isTender = t;
	}

	public boolean isTender() {
		return isTender;
	}

	/**
	 * Show / Hide the Item related tags
	 */
	public int getTagOption() {
		return options[O_ItmPr];
	}

	/**
	 * Returns the index of selected line from view
	 */
	public String getSelectedItemText(int index) {
		return readData(index - 1);
	}

	public int getState() {
		return TRANS_STATE;
	}

	public int getTotalLines() {
		return tdList.size();
	}

	public void writeFile(String line) {
		if (debug) {
			UtilLog4j.logInformation(this.getClass(), "#" + line + "#");
		}
	}

	/**
	 * add Transdata object to the vector of objects
	 */
	public void addTransData(TransData _td) {
		if (TRANS_STATE == 0) {
			tdList.removeAllElements();
			lastTrans = null;
		}
		TRANS_STATE = 1;
		if (ctl.ckr_nbr > 799) {
			return;
		}
		writeFile(_td.text);
		tdList.addElement(_td);
	}

	/**
	 * String data part is append (eg. employee / sales / cashier functions)
	 *
	 * @param data
	 */
	public void append(String data) {
		addTransData(new TransData(data));
	}

	/**
	 * Append the object to the vector
	 *
	 * @param td
	 *            local made-up object
	 */
	public void append(TransData td) {
		if (td.isData) {
			append(td.text);
		} else {
			addTransData(td);
		}
	}

	/**
	 * Gets the last selected item from the transView panel
	 *
	 * @return Itemdata object
	 */
	public Itemdata getLastestSItemObject() {
		TransData tmp = null;

		for (int i = tdList.size() - 1; i >= 0; i--) {
			tmp = ((TransData) tdList.elementAt(i));
			if (tmp.type == 'S') {
				break;
			}
		}
		return tmp.getItemDataObject();
	}

	/**
	 * reset the data at end of transaction.
	 *
	 * @param yes
	 *            : if last sales to be stored locally
	 */
	public void reset(boolean yes) {
		if (yes) {
			lastTrans = new String[tdList.size()];
			for (int i = 0; i < tdList.size(); i++) {
				lastTrans[i] = readData(i);
			}
		}
		tdList.removeAllElements();
		isTender = isTransFinished = false;
		TRANS_STATE = 0;
	}

	/**
	 * Shows the details of the last transaction when mouse is double-clicked in TransView panel.
	 */
	public void showLastTrans() {
		if (TRANS_STATE == 1) {
			return;
		}
		tdList.removeAllElements();
		show = !show;
		if (!show) {
			return;
		}
		if (lastTrans != null) {
			for (int i = 0; i < lastTrans.length; tdList.addElement(new TransData(lastTrans[i++]))) {
				;
			}
		}
		TRANS_STATE = 0;

	}

	/**
	 * Checks if already Voided (checks for Void / Error correct flag / linked discoun selection)
	 *
	 * @param recNum
	 *            record number index
	 * @return true / false
	 */
	public boolean isVoided(int recNum) {
		char ch = ' ';
		boolean ret = false;
		String str = readData(recNum - 1);

		if (str != null) {
			str = str.substring(52, 57);
			ch = str.charAt(0);
			if (ch != ' ' && ch != 'X') {
				ret = true;
			}
			ch = str.charAt(4);
			if (!ret && (ch == ' ' || ch == '0')) {
				ret = true;
			}
		} // UtilLog4j.logInformation(this.getClass(), str + ",Voided Flag=" + ret);
		return ret;
	}

	/**
	 * Returns the object at the selected index
	 *
	 * @param index
	 *            : 0 based index of array list
	 * @return Itemdata object at selection index
	 */
	public Itemdata getSelectedItem(int index) {
		if (isVoided(index + 1)) {
			return null;
		}
		Itemdata id = null;

		if (tdList.size() > index) {
			TransData td = (TransData) tdList.elementAt(index);

			if (td.isData) {
				return null;
			}
			id = td.getItemDataObject().copy();
			if ((id.spf1 & M_ERRCOR) > 0) {
				return null;
			} // already error corrected
		}
		return id;
	}

	/**
	 * Reads data from the list of items
	 *
	 * @param recNum
	 * @return data read at recNum position
	 */
	private String readData(int recNum) {
		if (recNum >= tdList.size()) {
			recNum = tdList.size() - 1;
		}
		String ret = ((TransData) tdList.elementAt(recNum)).text;

		return ret;
	}

	/**
	 * True when last item is selected.
	 *
	 * @param ind
	 *            Index passed from action7
	 * @return true if last item line is sales line otherwise false
	 */
	public boolean isLastItemSelected(int ind) {
		// MMS-R10
		if (tdList.size() == 0) {
			return true;
		}
		// MMS-R10
		ind--;
		if (ind == tdList.size() - 1) {
			TransData td = (TransData) tdList.elementAt(ind);

			if (td.text.charAt(DATA_LINE_LEN) == '0' || td.text.charAt(DATA_LINE_LEN) == ' ') {
				return false;
			}
			if (td.isData) {
				return false;
			}
			if ((td.itm.spf1 & M_ERRCOR) > 0) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Overwrites the selected indexes lines with new data values in the
	 *
	 * @param indexes
	 *            : The indexes array of selected items from UI
	 * @param type
	 *            : error correct
	 * @param allItems
	 *            : allitems
	 */
	public void overWrite(int[] indexes, int type, boolean allItems) {
		char voidFlag = GdPos.panel.mnemo.getText(60).charAt(6);

		if (indexes.length > 0 && ctl.view > 0) { // mark all
			markFlag(indexes[0] - 1, voidFlag);
		}
		if (ctl.view == 0) { // error correct from JRN View
			int l = tdList.size();

			while (l-- > 0) {
				TransData td = (TransData) tdList.elementAt(l);

				if (td.isData || td.text.charAt(DATA_LINE_LEN) == '0') {
					continue;
				}
				if (td.itm != null) {
					markFlag(l, voidFlag);
				}
				break;
			}
		} // ctl.view ==0
	}

	/**
	 * Mark the Void / Error correct flag at position on the indexed line
	 *
	 * @param ind
	 *            : Index for marking
	 * @param flag
	 *            Marking flag
	 */
	public void markFlag(int ind, char flag) {
		StringBuffer str = new StringBuffer(((TransData) tdList.elementAt(ind)).text);

		str.setCharAt(52, flag);
		((TransData) tdList.elementAt(ind)).text = str.toString();
	}

	/**
	 * Retruns the Itemdata object at the last selected position in the list
	 *
	 * @return Itemdata object
	 */
	public Itemdata getLastSelectedItem() {
		TransData td = null;

		td = (TransData) tdList.elementAt(tdList.size() - 1);
		Itemdata id = td.getItemDataObject();

		return id;
	}

	/**
	 * Error correct the last tender
	 */
	public void ecLastTender() {
		int sz = tdList.size();

		for (int i = sz - 1; i >= 0; i--) {
			String val = readData(i); // UtilLog4j.logInformation(this.getClass(), val);

			if (val.charAt(DATA_LINE_LEN) != '0') {
				markFlag(i, GdPos.panel.mnemo.getText(60).charAt(6));
				break;
			}
		}
	}

	/**
	 * debug method
	 */
	public void printObj() {
		if (debug) {
			for (int i = 0; i < tdList.size(); i++) {
				UtilLog4j.logInformation(this.getClass(), tdList.elementAt(i).toString());
			}
		}
	}

} // End Of Main-class
