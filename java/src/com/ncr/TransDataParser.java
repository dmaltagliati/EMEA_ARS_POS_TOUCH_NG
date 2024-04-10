package com.ncr;

// STD-ENH-ASR31STC-MMS#A
import java.util.Vector;

/**
 * This class traps the creation of Idc record and parse the item
 *
 * @author bs230003
 */
public class TransDataParser extends Action {
	// ask the view for the correct data provider
	static TransDataProvider dataProvider = GdPos.panel.transViewPanel.getDataProvider();
	static Vector idcLines = new Vector(); // Contiene le entryID corrispondenti
											// alle righe del DC
											// //STD-ENH-ASR31STC-MMS#A

	static String sRate = "";
	static String discText = null;
	static boolean hRecord = false;
	static boolean tRecord = false; // flag used in case of Cancelled /
									// Suspended transaction
	static boolean isHeader = false; // Header is even prepared before H record
	static boolean isCashCount = false; // When cash T records are processed,
										// means IDC transaction is not created
	static boolean trainingMode = false;
	static boolean netTotal = false;
	static String tag = "    ";

	// static char WIDE_CHAR = ' ';
	static int LINE_SIZE = TransDataProvider.LINE_SIZE;

	/**
	 * Prints the header of transaction in case regiter functions
	 */
	public static void setTransHeader() {
		netTotal = false; // UtilLog4j.logInformation(this.getClass(), "tra.code=" + tra.code );
		if (tra.code == 16 || tra.code == 17) {
			trainingMode = true;
			return;
		} else {
			trainingMode = false;
		}
		String text = GdPos.panel.mnemo.getMenu(tra.head); // UtilLog4j.logInformation(this.getClass(), "tra.head"
		// + tra.head + " " + text);

		if (tra.res != 0) {
			text = GdPos.panel.mnemo.getMenu(47);
		}
		if (tra.head != 0 || tra.res != 0) {
			addViewLine(getDoubleSpaced(text));
			if (tra.code == 7) { // extra till entry
				addViewLine(GdPos.panel.mnemo.getText(42) + " " + tra.number);
			}
			if (tra.head == 22 || tra.head == 23) {
				addViewLine(GdPos.panel.mnemo.getMenu(tra.head) + tra.number);
				// addViewLine (GdPos.panel.mnemo.getText (54) + " "+ editNum
				// (cus.spec, 2)
				// + '/' + editNum (cus.branch, 2));
				// addViewLine (cus.name);
				// addViewLine (cus.nam2);
				// addViewLine (cus.adrs);
				// addViewLine (cus.city);
			}
			isHeader = true;
		}
		update();
	}

	/**
	 * This method is implementation of loan / float / media declare
	 */
	public static void add() {
		// MMS-R10#A BEGIN
		if (dataProvider == null) {
			return;
		}
		// MMS-R10#A END

		Itemdata item = itm; // TransDebug.printItem(itm);

		if (item == null) {
			return;
		}
		String text = tnd[item.tnd].text + "        ";

		tag = vat[item.vat].text.substring(4, 6) + GdPos.panel.mnemo.getText(58).substring(0, 1) + item.sit;
		if (!isHeader) {
			setTransHeader();
		}
		tag = " " + tnd[item.tnd].symbol;
		isCashCount = true;
		text = getDescription(text);
		addViewLine(text, itm.cnt, itm.pos / itm.cnt, itm.pos, '1', 'T');
		update();
	}

	// STD-ENH-ASR31STC-MMS#A BEGIN
	// In base al numero di riga del DC, ritorno il corrispettivo EntryID
	public static int getIDCLineItem(int idcLine) {
		return ((Integer) idcLines.elementAt(idcLine)).intValue();
	}

	// STD-ENH-ASR31STC-MMS#A END

	/**
	 * This is main method that is responsible for the sending data to SVJXXX.DAT file and
	 *
	 * @param type
	 *            : Record type
	 * @param code
	 *            : record code (this is subcode2 in idc)
	 * @param subcode
	 *            : Tenders Sub-code (this is subcode3 in idc record)
	 * @param nbr
	 *            : Additional info
	 */
	public static void add(char type, int code, int subcode, String nbr) {

		// STD-ENH-ASR31STC-MMS#A BEGIN
		// Inserisco l'entryId dell'articolo che ha generato la riga del DC (0
		// se null) nel vettore
		// per un futuro uso
		if (itm != null) {
			idcLines.add(new Integer((int) itm.entryId));
		} else {
			idcLines.add(new Integer(0));
		}
		// STD-ENH-ASR31STC-MMS#A END

		if (type == 'S' || type == 'I' || type == 'J' || type == 'L' || type == 'R' || type == 'X' || type == 'P'
				|| type == 'T' || type == 'B' || type == 'C' || type == 'D' || type == 'F' || type == 'H' || type == 'M'
				|| type == 'V') {
			// UtilLog4j.logInformation(this.getClass(), "\nType=" + type + ",code=" + code +
			// ",subcode="+subcode ); TransDebug.printItem (itm);
			if (tra.code == 1) {
				addViewLine(getDoubleSpaced(GdPos.panel.mnemo.getMenu(20)));
				LinIo line = null;

				for (int ind = 0; ind < 2; ind++) {
					line = getLine();
					line.push(vrs_tbl[ind]).upto(30, editDec(version[ind], 1));
					addViewLine(line);
				}
				line.init(' ').push(GdPos.panel.mnemo.getText(1)).upto(30, editNum(ctl.ckr_nbr, 3)).push(" ")
						.push(lCTL.text);
				addViewLine(line);
				addViewLine("" + getTrailerLine());
				return;
			}
			if (itm == null || trainingMode) {
				return;
			}

			sRate = discText = "";

			if (type == 'H') {
				addIdcH();
			} else if (type == 'F') {
				addIdcF();
			} else if (type == 'T' && !isCashCount) {
				addIdcT(subcode);
			} else if (type == 'P') {
				addIdcP(subcode);
			} else if (type == 'B') {
				addIdcB(subcode, nbr);
			} else if (type == 'V') {
				addIdcV(code, subcode, nbr, options[O_CandC] > 0);
			} else if (type == 'M') {
				addIdcM(code);
			} // ROA
			else if (type == 'J') {
				addIdcJ(code);
			} else if (type == 'D') {
				addIdcD(code);
			} else if (type == 'C') {
				addIdcC(code);
			} else if (type == 'S' || type == 'I' || type == 'L' || type == 'R' || type == 'X') {
				addIdcS(code);
			}
		}
	}

	/**
	 * TransView is updated from S_SVJXXX.DAT file
	 */
	public static void update() {
		GdPos.panel.transViewPanel.view(true); // refresh the screen to reset
												// its data area
	}

	private static void addViewLine(String str) {
		LinIo line = getLine();

		line.push(str);
		addViewLine(line);
	}

	private static void addViewLine(LinIo line) {
		if (dataProvider == null) {
			return;
		}
		line.poke(55, '0');
		dataProvider.append(" " + line.toString(0, 58));
	}

	/**
	 * Creates the sales / discount item line and writes to the file SVJXXX.
	 *
	 * @param desc
	 *            : description of item / discount
	 * @param qty
	 *            : quantity sold
	 * @param prc
	 *            : unit price
	 * @param amt
	 *            : amount
	 * @param flag
	 *            : ' '=link item, '0'=data, 1=sales 2=voided, 4=err correct.
	 * @param type
	 *            : idc type; used in case of finding the correct S record in case of discounts.
	 */
	private static void addViewLine(String desc, int qty, long prc, long amt, char flag, char type) {
		String sqty = "", sprc = "", samt = "";

		if (qty != 0) {
			sqty = String.valueOf(qty);
		}
		if (prc != 0) {
			sprc = editMoney(0, prc < 0 ? prc * -1 : prc).trim();
		}
		if (amt != 0) {
			samt = editMoney(0, amt).trim();
		}
		addViewLine(desc, sqty, sprc, samt, flag, type);
	}

	/**
	 * Creates the sales / discount item line and writes to the file SVJXXX.
	 *
	 * @param desc
	 *            : description of item / discount
	 * @param sqty
	 *            : discount % or numeric value
	 * @param prc
	 *            : unit price
	 * @param amt
	 *            : amount
	 * @param flag
	 *            : ' '=link item, '0'=data, 1=sales 2=voided, 4=err correct.
	 * @param type
	 *            : idc type; used in case of finding the correct S record in case of discounts.
	 */

	private static void addViewLine(String desc, String sqty, long prc, long amt, char flag, char type) {
		String sprc = "", samt = "";

		if (prc != 0) {
			sprc = editMoney(0, prc < 0 ? prc * -1 : prc).trim();
		}
		if (amt != 0) {
			samt = editMoney(0, amt).trim();
		}
		addViewLine(desc, sqty, sprc, samt, flag, type);
	}

	private static void addViewLine(String desc, String sqty, String sprc, String samt, char flag, char type) {
		LinIo line = getLine().push(' ');

		if (pit != null && pit.link > 0 && ref != null && ref.number.equals(itm.number)) {
			flag = '0';
		}
		line.push(desc).upto(32, sqty).upto(41, sprc).upto(52, samt).push(tag).push(flag);
		if (itm.mark == TransDataProvider.ITEM_QTY_MARK) {
			line.poke(52, TransDataProvider.ITEM_QTY_MARK);
		}
		TransData td = new TransData(type == 'S' ? itm : null, line.toString(), type);

		if (type == 'T' && flag == '4') {
			dataProvider.ecLastTender();
		}
		dataProvider.append(td);
		update();
	}

	/**
	 * Add sales item to view
	 */
	private static void addIdcS(int code) {
		if (tra.code > 6 && itm.number.trim().length() > 0) { // stock count
																// with PLU
																// number
			addViewLine(editIdent(itm.number, (itm.flag & F_SPCSLS) > 0));
		} else if ((options[O_ItmPr] & 0x10) == 0) { //
			addViewLine(editIdent(itm.number, (itm.flag & F_SPCSLS) > 0));
		}
		char flg = '1';

		if (itm.amt < 0 && code == 7) {
			flg = '2';
		}
		if (itm.amt < 0 && code == 8) {
			flg = '4';
		}
		if (itm.prm > 0) {
			getWeightItemLine(itm.text, tnd[0].symbol, itm.ptyp);
			addViewLine(itm.text, GdScale.editWeight(itm.dec), editMoney(0, itm.price), editMoney(0, itm.amt), flg,
					'S');
		} else if (itm.unit / 10 != 1) {
			addViewLine(itm.text, (itm.cnt + " x " + itm.unit / 10), editMoney(0, itm.price), editMoney(0, itm.amt),
					flg, 'S');
		} else {
			addViewLine(itm.text, itm.cnt, itm.price, itm.amt, flg, 'S');
		}
	}

	private static void addIdcC(int code) {
		Itemdata tmp = dataProvider.getLastestSItemObject();

		if (itm.crd != 0) { // modify the S record that was before any of C
							// records
			tmp.crd = itm.crd;
			tmp.rate = itm.rate;
			tmp.spf2 |= itm.spf2;
		}
		if (code > 6 || code <= 1) {
			return;
		} // empl. / cust discounts || 0 = mark-down , 1= Price override
		getDiscountText(3, code, itm.rate, 'C');
		// 2- $discount ; 3- %discount; 4- AutoDiscount; 5- mixmatch; 6-
		// SetDiscount
		if (code == 3 || code == 2) {
			addViewLine(" " + discText, (itm.rate > 0) ? editRate(itm.rate) : "", "", editMoney(0, itm.crd), ' ', 'C');
		}
		if (code == 4) {
			addViewLine(" " + discText, 0, 0, itm.crd != 0 ? itm.crd : itm.dsc, ' ', 'C');
		}
		if (code == 5) {
			addViewLine(" " + discText, itm.cnt, itm.amt / (itm.cnt == 0 ? 1 : itm.cnt), itm.amt, ' ', 'C');
		}
		// if (code == 6) ; // not used
	}

	private static void addIdcD(int code) {
		if (tra.mode == 0 && itm.spf3 > 0) { // Discount with ROA / PDO / Money
			int rec = reg.find(tra.code, itm.dpt + 10);

			if (rec > 0) {
				lREG.read(rec, FmtIo.LOCAL);
				discText = lREG.text;
			}
			sRate = (itm.rate > 0) ? editRate(itm.rate) : "";
		} else { // sales trans, at the end discount
			getDiscountText(4, code, tra.rate, 'D');
			sRate = itm.number; // assign surcharge to tax rate description
		}
		addViewLine(discText, sRate, "", editMoney(0, itm.dsc), '0', 'D');
	}

	/**
	 * ROA and POA (M records with..); code=8; error correct
	 *
	 * @param code
	 *            : code2 value
	 */
	private static void addIdcM(int code) {
		addViewLine(GdPos.panel.mnemo.getText(30) + itm.number); // ROA
		if (code == 8) {
			addViewLine(itm.text, editKey(itm.dpt_nbr, 4), "", editMoney(0, itm.amt), '4', 'M');
		} else {
			addViewLine(itm.text, editKey(itm.dpt_nbr, 4), "", editMoney(0, itm.amt), '1', 'M');
		}
	}

	/**
	 * Add the check / credit card details
	 */
	static void addCheckDetails() {
		if ((itm.flag & T_BNKREF) > 0 && ecn.credit.length() > 0) { // creadit
																	// card
			int opt = options[O_CardX] >> 4;

			opt = options[O_CardX] & 15;
			String nbr = opt > 0 ? leftMask(ecn.credit, opt, '*') : ecn.credit;

			addViewLine("  " + GdPos.panel.mnemo.getText(31) + " " + nbr);
			update();
		} else if ((itm.flag & T_BNKREF) > 0 && ecn.acct.length() > 0) {
			addViewLine("  " + GdPos.panel.mnemo.getText(33) + " " + editTxt(ecn.acct, 16));
			addViewLine("  " + GdPos.panel.mnemo.getText(34) + " " + editTxt(ecn.bank, 16));
			addViewLine("  " + GdPos.panel.mnemo.getText(32) + " " + editTxt(ecn.cheque, 16));
			update();
		}
	}

	private static void addIdcT(int subcode) {
		if (!tRecord) {
			printTotal();
		}
		tRecord = true;
		tag = " " + tnd[itm.tnd].symbol;
		String text = tnd[itm.tnd].text, padd = "        ";
		long price = 0, amt = itm.amt;
		char flag = '1';

		switch (subcode) {
		case 1:
		case 6:
			return; // home currency tender record when FC is selected

		case 0: // cash tender
			if (amt == 0 && itm.pos < 0) {
				amt = itm.pos;
				text = GdPos.panel.mnemo.getText(27); // change in case of
														// FC over
														// tender
			}
			break;

		case 2: // FC Tender tender record
			price = itm.pos;
			padd = tnd[itm.tnd].editXrate(true);
			tag = " " + tnd[0].symbol;
			break;

		case 4: // Change back / HomeCurrecncy Tender/ Error Correct is
				// done
			if (amt < 0) {
				flag = '4';
			}
			amt = itm.pos;
			if (amt < 0 && itm.amt == 0) {
				text = GdPos.panel.mnemo.getText(27); // CHANGE
			}
			break;

		case 5: // FC tender / error corrected tender
			if (amt < 0) {
				flag = '4';
			}
			price = itm.pos;
			padd = tnd[itm.tnd].editXrate(true);
			tag = " " + tnd[0].symbol;
			break;
		}
		if (itm.number.length() > 0) {
			padd = itm.number.trim();
		}
		addViewLine(text, padd, price, amt, flag, 'T');
		// addCheckDetails ();
	}

	private static void addIdcJ(int code) {
		getDiscountText(3, code, tra.rate, 'J');
		addViewLine(discText, sRate = tra.rate > 0 ? editRate(tra.rate) : "", "", editMoney(0, tra.dsc_amt), '0', 'J');
	}

	/**
	 * Returns the Discount rate / Tax Rate (used for the editing the discount texts based..)
	 *
	 * @param code
	 *            : base code for searching S_REGXXX
	 * @param subcode
	 *            : subcode code for searching S_REGXXX
	 * @param rate
	 */
	private static void getDiscountText(int bc, int code, int rate, char type) {
		// STD-ENH-ASR31TRV-SBE#A BEG
		if ((code == 4 && type == 'C') || (code == 9 && type == 'D')) {
			discText = itm.getDscDescription();
		} else if (itm.crd == 0) {
			int rec = reg.find(bc, code);

			if (rec > 0) {
				lREG.read(rec, FmtIo.LOCAL);
				discText = lREG.text;
			}
		} else {
			if (itm.qual.startsWith("MS")) {
				discText = itm.text;
			}
		}
		if (discText == null || discText.equals("")) {
			int rec = reg.find(bc, code);

			if (rec > 0) {
				lREG.read(rec, FmtIo.LOCAL);
				discText = lREG.text;
			}
		}
	}

	/**
	 * Process the Transaction header addtional information (eg: Writing self number / Transfer In / Out and Approval Take / Return Register functions..
	 */
	private static void addIdcH() {
		String text = ""; // UtilLog4j.logInformation(this.getClass(), printItem(itm));UtilLog4j.logInformation(this.getClass(), "ctl.mode="
							// + ctl.mode);

		// pre-select finctions
		if ((tra.spf1 & M_TRRTRN) > 0 && tra.code != 56 && tra.code != 57) {
			text = GdPos.panel.mnemo.getMenu(45);
		} // trans return
		if ((tra.spf1 & M_TRVOID) > 0) {
			text = GdPos.panel.mnemo.getMenu(44);
		} // trans void
		if ((tra.spf3 & 4) > 0) {
			text = GdPos.panel.mnemo.getMenu(46);
		} // delivery surcharge
		if (ctl.mode == 3) {
			text = GdPos.panel.mnemo.getInfo(20);
		} // training mode
		if (ctl.mode == 4) {
			text = GdPos.panel.mnemo.getInfo(21);
		} // re-entry mode
		if (text.length() > 0) {
			addViewLine(getDoubleSpaced(text));
		}
		// Action codes
		boolean hit = false;

		if (tra.code == 45) { // self number
			stsLine.init(GdPos.panel.mnemo.getText(40)).onto(12, tra.number);
			hit = true;
		} else if (tra.code == 46 || tra.code == 56) { // Transfer OUT / IN
			stsLine.init(GdPos.panel.mnemo.getText(11)).onto(13, tra.number);
			hit = true;
		} else if (tra.code == 47 || tra.code == 57) { // Approval Take / Return
			stsLine.init(GdPos.panel.mnemo.getText(30)).onto(13, tra.number);
			hit = true;
		}
		if (hit) {
			tag = "    ";
			addViewLine(stsLine.toString(), "", "", "", '0', 'H');
		}
		hRecord = true;
	}

	private static void addIdcF() {
		// delete this transaction when two F records are adjacent
		dataProvider.setTender(false);
		if (isCashCount) { // when Media Declare function was preformed and now
							// transaction total is performed
			printTotal();
			addViewLine(TransDataProvider.DATA_LINE_SEPARATOR);
			dataProvider.setTransFinished(true);
			update();
			isHeader = hRecord = tRecord = isCashCount = false;
			return;
		} // Normal transaction ends in this block
		dataProvider.setTransFinished(true);
		if (hRecord) {
			if (!tRecord) {
				printTotal();
			}
			// normal transaction ending
			addViewLine(TransDataProvider.DATA_LINE_SEPARATOR);
			if (tra.mode == M_SUSPND) {
				addViewLine(getDoubleSpaced(GdPos.panel.mnemo.getMenu(77)));
			} else if (tra.mode == M_CANCEL) {
				addViewLine(getDoubleSpaced(GdPos.panel.mnemo.getInfo(23)));
			}
			if (ctl.mode == 3) {
				addViewLine(getDoubleSpaced(GdPos.panel.mnemo.getInfo(20)));
			} // training mode
			if (ctl.mode == 4) {
				addViewLine(getDoubleSpaced(GdPos.panel.mnemo.getInfo(21)));
			} // re-entry mode
		}
		addViewLine("" + getTrailerLine());
		update();
		isHeader = hRecord = tRecord = false;
		dataProvider.printObj();
	}

	private static void printTotal() {
		if (!tRecord) {
			// if (tra.pnt != 0) getValue (GdPos.panel.mnemo.getText
			// (39),tra.pnt, true);
			if (tra.cnt != 0) {
				getValue(GdPos.panel.mnemo.getText(21), tra.cnt, false);
			}
		}
		addViewLine(TransDataProvider.DATA_LINE_SEPARATOR);
		if (tra.code == 30) {
			if (tra.amt != 0) {
				totalLine(GdPos.panel.mnemo.getText(24), tra.amt);
			} // show total in Media Ex.
		} else {
			totalLine(GdPos.panel.mnemo.getText(24), tra.amt);
		}
		dataProvider.setTender(true);
	}

	/**
	 * Prints the information about P record on subcode values subcode 0 = Commision code + User entered number 1 = Sales Person Info 2 = Free arbitrary number entry
	 *
	 * @param subcode
	 */
	private static void addIdcP(int subcode) {
		String str = GdPos.panel.mnemo.getText(15) + itm.number;

		if (subcode == 2) {
			addViewLine(str);
			update();
		} else if (subcode == 0 && itm.prcom != 0) {
			str = GdPos.panel.mnemo.getText(60).charAt(5) + editNum(itm.prcom, 8);
			addViewLine(getDescription(str));
			update();
		} else if (subcode == 1) {
			str = GdPos.panel.mnemo.getText(5) + editKey(tra.slm_nbr, 4) + "  " + lSLM.text;
			addViewLine(getDescription(str));
			update();
		}
	}

	/**
	 * Prints the Box Sales data / Transaction Resume
	 *
	 * @param subcode
	 * @param nbr
	 */
	private static void addIdcB(int subcode, String nbr) {
		LinIo line = getLine();

		line.push(GdPos.panel.mnemo.getText(21)).onto(20, editTxt("" + itm.cnt, 11));
		line.upto(40, (subcode == 1 ? GdPos.panel.mnemo.getText(30).trim() : GdPos.panel.mnemo.getText(15).trim()));
		line.upto(51, nbr.trim());
		addViewLine(line);
		update();
	}

	/**
	 * Prints with or without VAT included in TOTAL
	 *
	 * @param code
	 * @param subcode
	 * @param nbr
	 * @param includedVat
	 */
	private static void addIdcV(int code, int subcode, String nbr, boolean includedVat) {
		LinIo line = getLine();

		if (!netTotal && includedVat) {
			line.push(GdPos.panel.mnemo.getText(55)).upto(51, editMoney(0, tra.amt)).push(" " + tnd[0].symbol);
			addViewLine(line);
			netTotal = true;
		} // Price has VAT included, prints individual Net Totals & VAT values
		if (subcode == 0 && includedVat && itm.amt != 0) {
			line.push(editMoney(0, itm.amt).trim()).upto(31, vat[code].text.trim());
			line.upto(41, nbr.trim()).upto(51, editMoney(0, itm.dsc)).push(" " + tnd[0].symbol);
			addViewLine(line);
		} // UtilLog4j.logInformation(this.getClass(), "V" + line.toString());
		update();
	}

	/**
	 * Info about the trailer line
	 *
	 * @return a trailing line as shown in the journal receipt
	 */
	public static String getTrailerLine() {
		LinIo line = getLine();

		line.init(trl_line).poke(1, (tra.slip & 0x21) > 0 ? '.' : '*');
		line.push(editNum(ctl.tran, 4)).skip().push(editNum(ctl.sto_nbr, 4)).skip().push(editKey(ctl.reg_nbr, 3)).skip()
				.push(editNum(ctl.ckr_nbr, 3));
		line.onto(20, editDate(ctl.date)).onto(29, editTime(ctl.time / 100)).onto(38, editNum(tra.code, 2));
		return line.toString();
	}

	/**
	 * Returns the points string that fits Quantity column ' *41*' format
	 *
	 * @param i
	 * @return formatted string that shows Points
	 */
	public static void getValue(String txt, int i, boolean pts) {
		String val = String.valueOf(i);

		if (pts) {
			val = editPoints(i, true);
		}
		LinIo line = getLine();

		line.push(txt).upto(31, val);
		addViewLine(line);
	}

	/**
	 * Returns the String for weight items with Eur/Kg text (Used for the product certification rqrmts. )
	 */
	private static void getWeightItemLine(String text, String curr, String unit) {
		LinIo line = getLine();

		line.upto(20, "--------------------"); // 20 spaces
		line.upto(31, DevIo.scale.version > 0 ? "*/*" : "").upto(40, curr + "/" + unit);
		addViewLine(line);
	}

	/**
	 * Only data part is append (eg. employee / sales / cashier functions)
	 *
	 * @param data
	 * @param total
	 * @return 0
	 */
	public static void totalLine(String data, long total) {
		LinIo line = getLine();

		line.push(data);
		// 7 spaces after T O T A L EUR
		line.onto(21, tnd[0].symbol).upto(40, editMoney(0, total));
		addViewLine(line);
	}

	public static LinIo getLine() {
		LinIo line = new LinIo("LIN", 0, LINE_SIZE);

		line.init(' ');
		return line;
	}

	/**
	 * Returns the String in 20 char formats, adding trailing spaces if text < 20
	 *
	 * @param text
	 * @return Item Description in 20 chars
	 */
	public static String getDescription(String text) {
		StringBuffer sb = new StringBuffer(text);

		if (text.length() < 21) {
			for (int i = 20 - text.length(); i >= 0; i--) {
				sb.append(" ");
			}
		}
		text = sb.toString();
		return text;
	}

	/**
	 * Return the string with > char at begining and one space each in text
	 *
	 * @param text
	 *            : input string (TOTAL)
	 * @return : Double spaced string > T O T A L
	 */
	public static String getDoubleSpaced(String text) {
		StringBuffer lt = new StringBuffer("");

		for (int i = 0; i < text.length(); i++) {
			lt.append(text.charAt(i)).append(" ");
		}
		return lt.toString();
	}
} // end of class
