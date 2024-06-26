package com.ncr;

import com.ncr.gui.GdLabel;
import org.apache.log4j.Logger;

import java.io.*;

abstract class BirIo extends Basis {
	private static final Logger logger = Logger.getLogger(BirIo.class);
	static int enroll(String name) {
		File f = localFile("bir", name + ".BIR");

		if (DevIo.biom.sensor() == 0) {
			logger.debug("Sensor capability problem");
			return ERROR;
		}
		if (f.exists()) {
			logger.debug("File " + f.getName() + " exists");
			return ERROR;
		}
		if (!DevIo.biom.capture(1)) {
			logger.debug("Problems capturing fingerprint");
			return ERROR;
		}
		int sts = GdPos.panel.clearLink(Mnemo.getInfo(63), 0x21);
		if (sts >= 0) {
			DevIo.biom.cancel();
			return ERROR;
		}
		if (!DevIo.biom.enroll())
			return 9;
		if (DevIo.biom.dataRAW != null) {
			GdLabel lbl = GdPos.panel.picture;
			lbl.setText(null);
			lbl.setImage(null);
			//lbl.prepareImage(lbl.image = DevIo.biom.dataRAW, lbl);
			if (GdPos.panel.clearLink(Mnemo.getInfo(40), 0x03) < 2)
				return ERROR;
		}
		if (DevIo.biom.dataBIR != null) {
			try {
				FileOutputStream out = new FileOutputStream(f);
				out.write(DevIo.biom.dataBIR);
				out.close();
			} catch (Exception e) {
				logConsole(0, f.getName(), e.toString());
				return 15;
			}
		} else return ERROR;
		return 0;
	}

	static int verify(String name) {
		File f = localFile("bir", name + ".BIR");

		if (DevIo.biom.sensor() == 0)
			return ERROR;
		if (!f.exists()) {
			String tmp = "LAST_BIR.TMP";
			if (netio.copyF2f("bir\\" + name + ".BIR", tmp, false) != 0)
				return ERROR;
			logConsole(2, "add " + f.getPath(), null);
			if (!localMove(new File(tmp), f))
				return ERROR;
		}
		int len = (int) f.length();
		byte bir[] = new byte[len];
		try {
			FileInputStream in = new FileInputStream(f);
			in.read(bir);
			in.close();
		} catch (IOException e) {
			logConsole(0, f.getName(), e.toString());
			return ERROR;
		}
		if (!DevIo.biom.capture(2))
			return ERROR;
		int sts = GdPos.panel.clearLink(Mnemo.getInfo(63), 0x21);
		if (sts != -2) {
			DevIo.biom.cancel();
			return 7;
		}
		return DevIo.biom.verify(bir) ? 0 : 8;
	}

	static void status(int code) {
		if (code == 0) /* data events */
			GdPos.panel.modal.modalMain(-2);
		else /* status update events */
			GdPos.panel.display(2, Mnemo.getHint(code));
	}

	static void collect(String name) {
		Itmdc.IDC_write('Y', 0, 0, "bir\\" + name + ".BIR", 0, 0l);
		prtLine.init(Mnemo.getInfo(63)).book(3);
	}
}
