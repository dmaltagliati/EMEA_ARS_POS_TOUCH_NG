package com.ncr;

import javax.comm.SerialPort;
import java.io.*;

public class PrnIo extends FmtIo {
    SerialPort port = null;
    Device device;
    int recColumns = 44, jrnColumns = 40;
    int slpColumns = 66, slpTopzone = 60;
    int recCompressed = 0, inProgress = 0;
    File logo = localFile("bmp", "P_REGELO.BMP");

    public PrnIo(Device dev) {
        if (dev.version < 1) return;
        device = dev;
        if (dev.version == 7166 || dev.version == 7196 || dev.version == 6000) {
            recColumns = 42;
            slpTopzone = 72;
        }
        if (dev.version == 7167 || dev.version == 6000) {
            slpColumns = 45;
        }
        if (dev.version == 7162) {
            recColumns = jrnColumns;
            recCompressed = 40;
        }
        for (connect(); port == null; connect()) {
            if (gui.clearLink(Mnemo.getInfo(17), 5) > 1)
                gui.eventStop(255);
        }
        paperState();
        if (logo.exists()) downLoad(logo.getAbsolutePath());
    }

    public void connect() {
        if (port != null) port.close();
        try {
            port = device.open(device.baud, port.DATABITS_8, port.STOPBITS_1, port.PARITY_NONE);
            send("\u001bc1\u0007\u001b2"); /* 6 lines per inch */
        } catch (Exception e) {
            device.error(e);
        }
    }

    public void stop() {
        if (port != null) {
            port.close();
            port = null;
        }
    }

    public void error(Exception e) {
        logConsole(0, "MFPTR:" + e.toString(), null);
        gui.clearLink(Mnemo.getInfo(17), 1);
        if (!port.isCTS()) connect();
    }

    public void send(String data) throws IOException {
        OutputStream out = port.getOutputStream();

        if (!port.isDSR()) {//if (recColumns > 40)
            // out.write (new byte [] { 0x10, 0x05, 0x01 });
            throw new IOException("no DSR");
        }
        out.write(data.getBytes(oem));
    }

    public void write(String data) {
        while (true) try {
            send(data);
            break;
        } catch (IOException e) {
            error(e);
        }
    }

    public int status(String data) throws IOException {
        InputStream in = port.getInputStream();

        in.skip(in.available());
        send(data);
        while (in.available() < 1) {
            if (!port.isDSR()) throw new IOException("no DSR");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        int sts = in.read();
        if (sts < 0) throw new IOException("timeout");
        inProgress = 0;
        return sts;
    }

    public int readMICR(LinIo io) {
        int chr = '?', ind = 0, sts;
        byte record[] = new byte[io.dataLen()];

        while (true) try {
            sts = status("\u001bw\u0001"); /* read MICR data and transmit */
            break;
        } catch (IOException e) {
            error(e);
        }
        if (sts < 2) try {
            InputStream in = port.getInputStream();
            for (; chr >= ' '; record[ind++] = (byte) chr) {
                if ((chr = in.read()) < 0) throw new InterruptedIOException();
                if (ind == record.length) throw new IOException("data overrun");
            }
            if (ind < 1) throw new IOException("noise");
            if (sts < 1) io.pb = new String(record, io.index = 0, ind - 1, oem);
        } catch (IOException e) {
            error(e);
            return ERROR;
        }
        return sts;
    }

    public int paperState() {
        while (true) try {
            return status(recColumns > 40 ? "\u001dr1" : "\u001bv");
        } catch (IOException e) {
            error(e);
        }
    }

    public int slipState() {
        return ~paperState() >> 5 & 3;
    }

    public int tillState() {
        try {
            return status(recColumns > 40 ? "\u001dr2" : "\u001bu\u0000") & 1;
        } catch (IOException e) {
            logConsole(0, "Drawer:" + e.toString(), null);
        }
        gui.display(1, Mnemo.getInfo(17));
        if (!port.isCTS()) connect();
        return 0;
    }

    public void select(int dev) {
        if (dev == 4)
            write("\u001bf\u0000\u0001"); /* slip waiting time */
        write("\u001bc0" + (char) dev + pitch(dev, false));
    }

    public void bold(int mode) {
        write("\u001bE" + (char) mode);
    }

    public void center(String data) {
        write("\u001ba1" + data + "\u001ba0");
    }

    public void label(int dev, char type, String nbr) {
        if (recColumns == 40) return;
        select(dev);
        if (type == 'E') {
            write("\n\u001dhM\u001dH0");
            center("\u001dk\u0004" + ipcBase32(nbr) + "\u0000");
            center(dwide(dev, "A" + nbr + '\n'));
            return;
        }
        write("\n\u001dhM\u001dH2");
        center("\u001dk" + (char) (type - 'A') + nbr + "\u0000");
    }

    public void bitmap(String name) {
        BmpIo bmp = new BmpIo(name);
        int wide = bmp.width, high = 24, clip = wide * (high >> 3);

        for (int line = 0; line < bmp.height; line += high) {
            byte[][] dots = new byte[wide][high >> 3];
            bmp.getColumns(dots, line, high, false);
            write("\u001ba1\u001b*!");
            try {
                BufferedOutputStream out = new BufferedOutputStream(port.getOutputStream(), 2 + clip);
                out.write(wide & 255);
                out.write(wide >> 8);
                for (int ind = 0; ind < wide; out.write(dots[ind++])) ;
                out.close();
            } catch (IOException e) {
                error(e);
            }
            write("\u001bJ\u0000\u001ba0");
        }
        bmp.close();
    }

    public void ldata(int dev, String data, StringBuffer sb) {
        int cols = dev > 1 ? recColumns : jrnColumns;

        if (inProgress > 15) paperState(); /* avoid buffer full */
        if (dev == 4) /* right aligned on slip */ {
            for (cols = slpColumns - data.length(); cols-- > 0; sb.append(' ')) ;
            if (data.charAt(1) != '>') sb.append(data);
            else sb.append(' ' + dwide(dev, data.substring(2, 22)));
            inProgress += 2;
            return;
        }
        if (recCompressed > 0) cols = recCompressed;
        if (cols == 40) {
            if (data.charAt(1) != '>') sb.append(data.substring(1, 41));
            else sb.append(dwide(dev, data.substring(2, 22)));
            inProgress++;
            return;
        }
        for (cols = cols - 42 >> 1; cols-- > 0; sb.append(' ')) ;
        if (data.charAt(1) != '>') sb.append(data);
        else sb.append(' ' + dwide(dev, data.substring(2, 22)));
    }

    public void lfeed(int dev, int lfs) {
        if (dev < 4) select(dev);
        if (lfs > 0) write("\u001bd" + (char) lfs);
    }

    public void pulse(int nbr) {
        write("\u001bp" + (char) nbr + "\u0008\u0008");
    }

    public void knife(int msk) {
        int lfs = (msk & 0x80) > 0 ? 2 : 0;

        if ((msk & 0x40) != 0) select(2);
        else lfeed(2, lfs + (recColumns > 40 ? 3 : 7));
        if ((msk & 0x80) != 0)
            write(recColumns > 40 ? "\u001dV1" : "\u001bm");
    }

    public String pitch(int dev, boolean dwide) {
        int size = dwide ? 32 : 0;
        if (dev < 4) if (recCompressed > 0) size++;
        return "\u001b!" + (char) size;
    }

    public String dwide(int dev, String data) {
        return pitch(dev, true) + data + pitch(dev, false);
    }

    public void downLoad(String name) {
        BmpIo bmp = new BmpIo(name);
        int wide = bmp.width, high = bmp.height;

        if (wide < 1) return;
        if (wide > 72 * 8 || high > 64 * 8) /* ensure GS*XY 7-bit */ {
            logConsole(0, name + " too big", null);
            /* 80mm in 7158/7194,7167/7197 576x512 */
            /* 80mm in 7156/7193 emulation 432x512 */
            /* 58mm in 7158/7194,7167/7197 424x512 */
            /* 58mm in 7156/7193 emulation 312x512 */
            /* 80mm in 7166/7196 TM-T88    512x384 */
            /* 58mm in 7166/7196 TM-T88    360x384 */
            /* 80mm in 7156/7193           448x384 */
            return;
        }
        byte[][] dots = new byte[wide + 7 & ~7][high + 7 >> 3];
        bmp.getColumns(dots, 0, high, false);
        bmp.close();
        wide = dots.length;
        high = dots[0].length << 3;
        logConsole(1, null, name = "loading logo " + wide + "x" + high);
        gui.display(2, name);
        write("\u001d*" + (char) (wide >> 3) + (char) (high >> 3));
        try {
            for (int ind = 0; ind < wide; ind++) {
                port.getOutputStream().write(dots[ind]);
                if ((ind & 7) == 7) port.getOutputStream().flush();
            }
        } catch (IOException e) {
            error(e);
        }
    }
}
