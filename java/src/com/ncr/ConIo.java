package com.ncr;

import com.ncr.gui.DynakeyGroup;
import jpos.ScannerConst;
import org.apache.log4j.Logger;

import java.awt.event.*;

public class ConIo extends LinIo {
	private static final Logger logger = Logger.getLogger(ConIo.class);

	static DynakeyGroup dyna = null; // EMEA-00046-DSA
	public static int dky = -1, sel = 0, lck = 8;
	public int key, num, dec, tic;
	public int flg, max, msk, pnt;

	public String alpha, label, prompt;
	public String qrcode = "";  //QRCODE-SELL-CGA#A
	public String aux1 = ""; // TSC-ENH2014-1-AMZ#ADD -- extended EAN 128 info
	static final char QR_CODE_SEPARATOR = ';';
	// MMS-SCANNER#A BEGIN
	public static boolean uppercase = false;
	int scanType = 0;
	String scanLabel = "";
	public int error;

	public final static int DECODED_LABEL_TYPE_UNKNOWN = 0; // Non gestito
	public final static int DECODED_LABEL_TYPE_UPCA = 101;
	public final static int DECODED_LABEL_TYPE_UPCE = 102;
	public final static int DECODED_LABEL_TYPE_EAN8 = 103;
	public final static int DECODED_LABEL_TYPE_EAN13 = 104;
	public final static int DECODED_LABEL_TYPE_INTERLEAVED = 106;
	public final static int DECODED_LABEL_TYPE_CODABAR = 107;
	public final static int DECODED_LABEL_TYPE_CODE39 = 108;
	public final static int DECODED_LABEL_TYPE_CODE128 = 110;
	public final static int DECODED_LABEL_TYPE_EAN128 = 120;
	public final static int DECODED_LABEL_TYPE_GS1 = 131;
	public final static int DECODED_LABEL_TYPE_QRCODE = 204; // AMZ-QRCODE#ADD


	// MMS-GS1#A END
	public static final int KEY_00 = 0x3a;
	public static final int LEFT = 0x8800; // MMS-LOTTERY#A
	public static final int RIGHT = 0x8801; // MMS-LOTTERY#A

	/**
	 * Ripulisce le informazioni riguardanti la tipologia di barcode.
	 */
	public void resetScanData() {
		scanType = ScannerConst.SCAN_SDT_UNKNOWN;
		scanLabel = "";
	}

	public boolean isSpecialKey() {
		switch (key) {
			case ConIo.JRNAL:
			case ConIo.NORTH:
			case ConIo.SOUTH:
			case ConIo.NOTES:
			case ConIo.AUTHO:
				return true;
			default:
				return false;
		}
	}

	public void dumpScanData() {
		String s = "";

		switch (scanType) {
			case ScannerConst.SCAN_SDT_UPCA:
				s = "SCAN_SDT_UPCA";
				break;

			case ScannerConst.SCAN_SDT_UPCE:
				s = "SCAN_SDT_UPCE";
				break;

			// case ScannerConst.SCAN_SDT_JAN8:
			case ScannerConst.SCAN_SDT_EAN8:
				s = "SCAN_SDT_EAN8";
				break;

			// case ScannerConst.SCAN_SDT_JAN13:
			case ScannerConst.SCAN_SDT_EAN13:
				s = "SCAN_SDT_EAN13";
				break;

			case ScannerConst.SCAN_SDT_TF:
				s = "SCAN_SDT_TF";
				break;

			case ScannerConst.SCAN_SDT_ITF:
				s = "SCAN_SDT_ITF";
				break;

			case ScannerConst.SCAN_SDT_Codabar:
				s = "SCAN_SDT_Codabar";
				break;

			case ScannerConst.SCAN_SDT_Code39:
				s = "SCAN_SDT_Code39";
				break;

			case ScannerConst.SCAN_SDT_Code93:
				s = "SCAN_SDT_Code93";
				break;

			case ScannerConst.SCAN_SDT_Code128:
				s = "SCAN_SDT_Code128";
				break;

			case ScannerConst.SCAN_SDT_UPCA_S:
				s = "SCAN_SDT_UPCA_S";
				break;

			case ScannerConst.SCAN_SDT_UPCE_S:
				s = "SCAN_SDT_UPCE_S";
				break;

			case ScannerConst.SCAN_SDT_UPCD1:
				s = "SCAN_SDT_UPCD1";
				break;

			case ScannerConst.SCAN_SDT_UPCD2:
				s = "SCAN_SDT_UPCD2";
				break;

			case ScannerConst.SCAN_SDT_UPCD3:
				s = "SCAN_SDT_UPCD3";
				break;

			case ScannerConst.SCAN_SDT_UPCD4:
				s = "SCAN_SDT_UPCD4";
				break;

			case ScannerConst.SCAN_SDT_UPCD5:
				s = "SCAN_SDT_UPCD5";
				break;

			case ScannerConst.SCAN_SDT_EAN8_S:
				s = "SCAN_SDT_EAN8_S";
				break;

			case ScannerConst.SCAN_SDT_EAN13_S:
				s = "SCAN_SDT_EAN13_S";
				break;

			case ScannerConst.SCAN_SDT_EAN128:
				s = "SCAN_SDT_EAN128";
				break;

			case ScannerConst.SCAN_SDT_OCRA:
				s = "SCAN_SDT_OCRA";
				break;

			case ScannerConst.SCAN_SDT_OCRB:
				s = "SCAN_SDT_OCRB";
				break;

			case ScannerConst.SCAN_SDT_RSS14:
				s = "SCAN_SDT_RSS14";
				break;

			case ScannerConst.SCAN_SDT_RSS_EXPANDED:
				s = "SCAN_SDT_RSS_EXPANDED";
				break;

			case ScannerConst.SCAN_SDT_CCA:
				s = "SCAN_SDT_CCA";
				break;

			case ScannerConst.SCAN_SDT_CCB:
				s = "SCAN_SDT_CCB";
				break;

			case ScannerConst.SCAN_SDT_CCC:
				s = "SCAN_SDT_CCC";
				break;

			case ScannerConst.SCAN_SDT_PDF417:
				s = "SCAN_SDT_PDF417";
				break;

			case ScannerConst.SCAN_SDT_MAXICODE:
				s = "SCAN_SDT_MAXICODE";
				break;

			case ScannerConst.SCAN_SDT_OTHER:
				s = "SCAN_SDT_OTHER";
				break;

			case ScannerConst.SCAN_SDT_UNKNOWN:
				s = "SCAN_SDT_UNKNOWN";
				break;
			case ScannerConst.SCAN_SDT_QRCODE:
				s = "SCAN_SDT_QRCODE";
				break;

			default:
				s = "UNHANDLED";
				break;
		}

		UtilLog4j.logInformation(this.getClass(), s + ": " + scanLabel);
	}

	/**
	 * @return the scanLabel
	 */
	public String getScanLabel() {
		return scanLabel;
	}

	/**
	 * @param scanLabel
	 *            the scanLabel to set
	 */
	public void setScanLabel(String scanLabel) {
		this.scanLabel = scanLabel;
	}

	/**
	 * @return the scanType
	 */
	public int getScanType() {
		return scanType;
	}

	/**
	 * @param scanType
	 *            the scanType to set
	 */
	public void setScanType(int scanType) {
		this.scanType = scanType;
	}

	// MMS-SCANNER#A END

	public static final int ABORT = 0x1B;
	public static final int AUTHO = 0x0A;
	public static final int BACK1 = 0x08;
	public static final int CLEAR = 0x0B;
	public static final int ENTER = 0x0D;
	public static final int JRNAL = 0x0C;
	public static final int NORTH = 0x1E;
	public static final int NOTES = 0xF0;
	public static final int PAUSE = 0xA8;
	public static final int POINT = 0x2E;
	public static final int SOUTH = 0x1F;
	public static final int TOTAL = 0x19;
	public static final int SCANNER = 0x4f4f;
	public static final int MSR = 0x4d4d;

	public static final int SELLCURRENT = 0x9998;
	public static final int VOIDCURRENT = 0x9999;

	public static int optAuth = 0, posLock = 0;
	// MMS-SUPERVISOR-SELLING#A BEGIN
	// Controlla se la chiave fisica o virtuale � in supervisore
	public boolean isSupervisor() {
		if (posLock > 0) {
			return posLock == 3;
		} else {
			return ((lck & 0x10) == 0x10);
		}
	}
	// MMS-SUPERVISOR-SELLING#A END
	public ConIo(int size) {
		super("CON", 0, size);
	}

	public void init(int flg, int max, int msk, int pnt) {
		this.flg = flg;
		this.max = max;
		this.msk = msk;
		this.pnt = pnt;
		reset("");
	}

	public void reset(String s) {
		key = dec = 0;
		num = (pb = s).length();
		if ((flg & 0x80) == 0 || num > 0)
			echo();
		else
			tic = index = 0;
	}

	public void echo() {
		char chr;
		int ind, pos = dec, pre = msk - num;

		super.init(prompt);
		tic = index;
		if (pnt > 0 && pos == 0)
			pos = pnt + 1;
		for (ind = dataLen(); ind > 0; poke(--ind, chr)) {
			if (pos > 0)
				if (--pos == 0) {
					chr = POINT;
					continue;
				}
			if (num > 0) {
				chr = pb.charAt(--num);
				if ((flg & (pos > 0 ? 0x20 : 0x40)) > 0)
					chr = 'X';
				continue;
			}
			chr = '*';
			if (--pre < 0) {
				if (ind == dataLen())
					break;
				if ((chr = ' ') == peek(ind))
					break;
			}
		}
		num = pb.length();
		show(2);
	}

	public int numeric(int code) {
		if (num >= max)
			return 2;
		pb += (char) code;
		if (dec > 0)
			dec++;
		num++;
		echo();
		return -1;
	}

	public int accept(int code) {
		int sts = -1;
		label = null;
		if (code >= '0' && code <= '9') {
			return numeric(code);
		}
		if (code >= 0x3a && code <= 0x3b) {
			while (code-- >= '9')
				sts = numeric('0');
			return sts;
		}
		if (code == POINT) {
			if (max == 0)
				return 2;
			if (dec++ > 0)
				return 5;
			echo();
			return sts;
		}
		if (code == BACK1) {
			if (max == 0)
				return sts;
			if (num > 0 && dec != 1)
				pb = pb.substring(0, --num);
			if (dec > 0)
				dec--;
			echo();
			return sts;
		}
		key = code;
		logConsole(16, "input.flg=0x" + editHex(flg, 2) + " key=0x" + editHex(key, 2) + " lck=0x" + editHex(lck, 2)
				+ " dec=" + dec + " pb=" + pb, null);
		if (code == CLEAR)
			if (num > 0 || dec > 0)
				return 5;
		return 0;
	}

	public int adjust(int len) {
		if (dec == 0)
			return 0;
		if (len < 1 || dec > ++len)
			return 4;
		while (dec < len)
			if (numeric('0') > 0)
				return 2;
		dec--;
		return 0;
	}

	public boolean isEmpty() {
		return num + dec == 0;
	}

	// MMS-GS1#A BEGIN
	public int labelFromScanner(String cmd) {
		pb = cmd;
		num = pb.length();
		echo();
		key = 0x4f4f;
		return 0;
	}

	// MMS-GS1-FILTER-LABEL#A BEGIN
	/**
	 * Workaround per eliminare dai barcode letti dalle pistole i caratteri non stampabili
	 *
	 * @param dirtyCmd
	 * @return a cleaned cmd
	 */
	String filter(String dirtyCmd) {
		final char GS1_SEPARATOR = 0x1d;
		String cmd = "";

		for (int i = 0; i < dirtyCmd.length(); i++) {
			char c = dirtyCmd.charAt(i);

			if (c >= 0x20 || c == GS1_SEPARATOR) {
				cmd += c;
			} else {
				if (c != 0x0d && c != 0x0a) {
					// Appena trovo un carattere non valido diverso da CR e LF
					// esco dalla routine buttando tutto il resto
					return cmd;
				}
			}
		}
		return cmd;
	}

	// MMS-GS1-FILTER-LABEL#A END

	public int label(String cmd) {
		if (max < 1 || !isEmpty())
			return 5;
		int ind = 4;
		char c = cmd.charAt(ind);
		if (cmd.length() <= ind)
			return 9;
		label = cmd;
		if (c == ']') /* RSS14 */
		{
			if (cmd.indexOf("01") != 7)
				return 9;
			cmd = cmd.substring(0, 5) + cmd.substring(9, 23);
		}
		if (cmd.length() == 12 && cmd.indexOf("B1") == ind) {
			cmd = cmd.substring(0, 6) + ipcDecode(cmd, 6);
		}
		if (c == 'B' || cmd.indexOf("FF") == ind)
			ind++;
		if (!Character.isDigit(c))
			ind++;
		for (; ind < cmd.length(); num++) {
			// TSC-ENH2014-1-AMZ#BEG
			if (cmd.length() == 30 && num == 13) {
				aux1 = cmd.substring(18, 30);
				break;
			}
			// TSC-ENH2014-1-AMZ#END
			if (num >= dataLen())
				return 2;
			pb += cmd.charAt(ind++);
		}
		key = 0x4f4f;
		return 0;
	}

	public int track(String cmd) {
		logger.debug("Cmd: " + cmd);
		int len, track = cmd.charAt(3) & 15, yymm = 0;
		PayCards ecn = Struc.ecn;

		if (max < 1 || !isEmpty())
			return 5;
		label = cmd;
		if (track == 1) {
			logger.debug("MSR Card , Track1 = [" + cmd + "]");

			if (GdSaf.isEnabled()) {
				logger.debug("Track1 [" + cmd + "] Len [" + cmd.length() + "] ");
				if (cmd.length() >= 34) {
					ecn.custom = cmd.substring(22, 34);
				}
			}

			return -1;
		}
		if (track == 2) {
			logger.debug("Param.getNewMSR_Track(): " + Param.getNewMSR_Track());
			logger.debug("MSR Card , Track2 = [" + cmd + "]");
			if (Param.getNewMSR_Track() > 0) {

				if (Param.getNewMSR_Track() == 3) {
					logger.debug("MSR Card , using new specifications for customer mapping");
					String costumerCode = "";

					costumerCode = cmd.substring(4, 6) + cmd.substring(13, 22);

					if ((len = cmd.indexOf('=')) < 0)
						len = cmd.length();
					else
						yymm = Integer.parseInt(cmd.substring(len + 1, len + 5));

					logger.debug(" len: " + len);
					logger.debug(" date read: " + yymm);
					pb = costumerCode;
					logger.debug("customer code - pb: " + pb);
					key = 0x4d4d;
				} else {
					if (cmd.substring(4, 7).equals("672")) {
						logger.debug("EXIT return -1  - because substr(4,7) == 672 ");
						return -1;
					}

					if ((len = cmd.indexOf('=')) < 0)
						len = cmd.length();
					else
						yymm = Integer.parseInt(cmd.substring(len + 1, len + 5));

					logger.debug(" len: " + len);
					logger.debug(" date read: " + yymm);

					if (Param.getNewMSR_Track() == 2) {
						yymm += 4900;
						logger.debug(" new date: " + yymm);
					}
					if (GdPsh.getInstance().readingGCSerial) {
						if (len > 32 + 4) {
							logger.debug("EXIT - len > 32 + 4 - return 31");
							return 31;
						}
					} else {
						if (len > 24) {
							logger.debug("EXIT - len > 24 - return 31");
							return 31;
						}
					}
					pb = cmd.substring(13, len - 3);
					logger.debug("customer code - pb: " + pb);
					key = 0x4d4d;
				}
			} else {
				if (cmd.substring(4, 7).equals("672"))
					return -1;
				if ((len = cmd.indexOf('=')) < 0)
					len = cmd.length();
				else
					yymm = Integer.parseInt(cmd.substring(len + 1, len + 5));

				pb = cmd.substring(4, len);
				key = 0x4d4d;
			}
		}
		if (track == 3) {
			logger.debug("MSR Card , Track3 = [" + cmd + "]");
			try {
				if (Integer.parseInt(cmd.substring(4, 6)) > 1)
					return -1;
				if (Integer.parseInt(cmd.substring(6, 8)) != 59)
					return -1;
			} catch (NumberFormatException e) {
				return -1;
			} catch (StringIndexOutOfBoundsException e) {
				return -1;
			}
			ecn.bank = cmd.substring(8, 16);
			ecn.nation = Integer.parseInt(cmd.substring(29, 32));
			ecn.currency = Integer.parseInt(cmd.substring(32, 35));
			ecn.seqno = Integer.parseInt(cmd.substring(68, 69));
			yymm = Integer.parseInt(cmd.substring(64, 68));
			pb = cmd.substring(17, 27);
			key = 0x4dec;
		}
		num = pb.length();
		if ((ecn.yymm = yymm) > 0) {
			if (cmpDates(yymm * 100 + 31, Struc.ctl.date) < 0) {
				logger.debug("Dates: [" + ecn.yymm + "] [" + Struc.ctl.date + "]");
				if (!GdTsc.customerCardDateChk(pb)) {
					return 31;
				}
			}
		}
		return 0;
	}

	public void keyLock(int pos) {
		int lck_bit[] = { 8, 1, 4, 2 };
		String lck_txt[] = { "[ L ]", "[N/R]", "[ S ]", "[ X ]" };

		if ((optAuth & 2) > 0)
			return;
		if (pos < 1 || pos > lck_txt.length)
			return;
		posLock = pos--;
		tic = 0;
		gui.dspStatus(2, lck_txt[pos], true, pos > 1);
		lck &= 0xF0;
		lck |= lck_bit[pos];
		if (pos == 2) {
			if (optAuth < 2)
				lck |= 0x10;
		} else if (optAuth != 1 || pos != 1)
			lck &= ~0x10;
	}

	public boolean isLockPos(int pos) {
		return (optAuth & 2) > 0 || (lck & pos) > 0;
	}

	public int keyTrans(int vkey) {
		for (int ind = vkeys.length; ind-- > 0;)
			if (vkeys[ind][0] == vkey)
				return vkeys[ind][1];
		return 0;
	}

	public int keyBoard(KeyEvent e) {
		logger.debug("KeyChar: " + e.getKeyChar() + " KeyCode: " + String.format("0x%04x", e.getKeyCode()));
		if (DevIo.wdge.filter(e)) {
			e.consume();
			return ERROR;
		}
		if (e.isAltDown())
			return ERROR;
		int code = e.getKeyChar();
		int vkey = e.getKeyCode();
		gui.feedBack(e);
		if (vkey == e.VK_PAUSE)
			return accept(PAUSE);
		if (vkey >= e.VK_F1 && vkey <= e.VK_F10) {
			code = vkey - e.VK_F1 + 0xbb;
			if (e.isControlDown())
				code += 0x23;
			else if (e.isShiftDown())
				code += 0x19;
		} else if ((vkey = keyTrans(vkey)) > 0) {
			code = vkey;
		}
		else if (code < 0x20 || code > 0xff)
			return ERROR;
		else if ((flg & 0x10) > 0)
			return numeric(code);
		else if (code > 0x9f)
			return ERROR;
		code = table[code];
		if (code < 1 || code >= 255)
			return ERROR;
		e.consume();
		if (code >= 0x80 && code < 0x88) {
			if (dky < 0)
				return ERROR;
			code -= 0x80;
			if (sel > 0) {
				gui.select(code);
				return ERROR;
			}
			if ((code = dynas[dky][code]) == 0xFF)
				return ERROR;
		}
		logger.debug("Code: " + String.format("0x%04x", code));
		return accept(code);
	}

	String dkyRule(int ind) {
		return rules[dky][ind];
	}

	static boolean hasDyna() {
		return table[0xBB] == 0x80;
	}

	static int vkeys[][] = { /* virtual key code, MS DOS code */
			{ KeyEvent.VK_ENTER, 0x0d }, { KeyEvent.VK_BACK_SPACE, 0x08 }, { KeyEvent.VK_TAB, 0x09 },
			{ KeyEvent.VK_ESCAPE, 0x1b }, { KeyEvent.VK_HOME, 0x47 + 0x80 }, { KeyEvent.VK_UP, 0x48 + 0x80 },
			{ KeyEvent.VK_PAGE_UP, 0x49 + 0x80 }, { KeyEvent.VK_LEFT, 0x4b + 0x80 }, { KeyEvent.VK_RIGHT, 0x4d + 0x80 },
			{ KeyEvent.VK_END, 0x4f + 0x80 }, { KeyEvent.VK_DOWN, 0x50 + 0x80 }, { KeyEvent.VK_PAGE_DOWN, 0x51 + 0x80 },
			{ KeyEvent.VK_INSERT, 0x52 + 0x80 }, { KeyEvent.VK_DELETE, 0x53 + 0x80 }, { KeyEvent.VK_F11, 0x54 + 0x80 },
			{ KeyEvent.VK_F12, 0x55 + 0x80 }, };

	static int table[] = { // x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 xA xB xC xD xE xF
			/* 00 - 0F */ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC2, 0x00, 0x0D, 0x00, 0x00, 0x0D, 0x00,
			0x00, /* 10 - 1F */ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0b, 0x00, 0x00,
			0x00, 0x00, /* 20 - 2F */ 0xC1, 0x00, 0xC4, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x06, 0xDD,
			0x11, 0x2E, 0xDB, /* 30 - 3F */ 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0xC3, 0x00,
			0x00, 0x00, 0x00, 0x00, /* 40 - 4F */ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, /* 50 - 5F */ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0xDC, 0x00, 0x00, 0xDE, /* 60 - 6F */ 0x00, 0xA5, 0xA6, 0xB9, 0xBA, 0xD1, 0xD2, 0xD3, 0xB1,
			0xB2, 0xBB, 0xBC, 0xD4, 0xD5, 0xD6, 0xA3, /* 70 - 7F */ 0xA4, 0xBD, 0xBE, 0xD7, 0xD8, 0xD9, 0xF9, 0xFA,
			0xFB, 0xE1, 0xE2, 0x00, 0x00, 0x00, 0x00, 0x00, /* 80 - 8F */ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 90 - 9F */ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x0a, 0x00, 0x00, 0x00, 0x00, /* A0 - AF */ 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* B0 - BF */ 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF0, 0xF1, 0x1E, 0xF2, 0x1F, /* C0 - CF */ 0xF3, 0x0F, 0xF4,
			0xF8, 0x04, 0x00, 0x00, 0x00, 0x1E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* D0 - DF */ 0x1F, 0x00,
			0xB6, 0x08, 0x0B, 0x05, 0xFF, 0x1B, 0x01, 0x02, 0x18, 0x19, 0x3A, 0x00, 0x00, 0x00, /* E0 - EF */ 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			/* F0 - FF */ 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, };

	static int dynas[][] = { // F1 F2 F3 F4 F5 F6 F7 F8
			/* state 0 */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xBE },
			/* state 1 */ { 0xA2, 0xA8, 0x04, 0xF1, 0xF2, 0xF3, 0xF4, 0xF8 },
			/* state 2 */ { 0x01, 0x02, 0xB9, 0x11, 0x18, 0x1D, 0xB6, 0x07 },
			/* state 3 */ { 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8 },
			/* state 4 */ { 0xC1, 0xC2, 0xC3, 0xC4, 0xF8, 0xF9, 0xFA, 0xFB },
			/* state 5 */ { 0xFF, 0xFF, 0xB9, 0xFF, 0x06, 0xFF, 0x11, 0x12 },
			/* state 6 */ { 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88 },
			/* state 7 */ { 0xB0, 0xB4, 0xB5, 0xFF, 0xA3, 0xFF, 0xA5, 0xA6 },
			/* state 8 */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
			/* state 9 */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
			/* state A */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
			/* state B */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
			/* state C */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
			/* state D */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
			/* state E */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 },
			/* state F */ { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }, };

	static String rules[][] = { { " 000", " 000", " 000", " 000", " 000", " 000", " 000", "M057" },
			{ "M021", "M027", "K005", "M003", "M019", "M043", "M051", "M050" },
			{ "M073", "M074", "M052", "M075", "K023", "M077", "M049", "M048" },
			{ "D001", "D002", "D003", "D004", "D005", "D006", "D007", "D008" },
			{ "c001", "c002", "c003", "c004", "M059", "M061", "M060", "M062" },
			{ " 000", " 000", "M052", " 000", "K035", " 000", "M078", "M079" },
			{ "M000", "M000", "M000", "M000", "M000", "M000", "M000", "M000" },
			{ "M037", "M022", "M023", " 000", "M045", " 000", "M024", "M025" }, };

	static String GS1AIdent[] =
	// * AINx=Application Identifier (x=length of identifier)
	// ....Nxx = field 1 (N=numeric/X=alpha, xx = fixed length)
	// .......Xxx = field 2 (N=numeric/X=alpha, xx = max length)
	{ "01N2N14 00", /* Global Trade Item Number */
			"15N2N06 00", /* best before date */
			"17N2N06 00", /* expiration date */
			"21N2 00 20", /* serial number */
			"31N4N06 00", /* trade measures */
	};

	int parseGS1(String[] result, int ind) {
		int size = -1, hdr = 4 + 3; /* RDRn]e0 */
		String rule = "??N2 00 99"; /* default = not in table */

		if (label.charAt(4) != ']')
			return 0;
		if ((ind += hdr) + 2 >= label.length())
			return 0;
		String id = label.substring(ind, ind + 2);
		while (++size < GS1AIdent.length) {
			if (GS1AIdent[size].startsWith(id)) {
				rule = GS1AIdent[size];
			}
		}
		ind += size = rule.charAt(3) & 15;
		result[0] = label.substring(ind - size, ind); /* application identifier */
		ind += size = Integer.parseInt(rule.substring(5, 7));
		result[1] = label.substring(ind - size, ind); /* data part 1 (fixed size) */
		size = Integer.parseInt(rule.substring(8, 10));
		if (size > 0) /* data part 2 (terminated by GS) */
		{
			int last = label.indexOf(0x1d, ind);
			if (last < 0)
				last = label.length();
			result[2] = label.substring(ind, last);
			if (rule.charAt(7) == 'N')
				result[2] = leftFill(result[2], size, '0');
			if (rule.charAt(7) == 'X')
				result[2] = rightFill(result[2], size, ' ');
			if (last < label.length())
				last++;
			ind = last;
		} else
			result[2] = "";
		return ind - hdr;
	}
}
