package com.ncr.tablet;

import com.ncr.BmpIo;
import com.ncr.LineMap;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

public class TabletLineMap extends com.ncr.LineMap implements LineMapIf{
    private static final Logger logger = Logger.getLogger(TabletLineMap.class);
    BmpIo bmp;
    Point base;
    Font sgle, dble;
    BufferedImage bim;
    /* CourierNew char width B10,B11,B12,B13 */
    static int fontsize[] = { 17, 18, 20, 22 };

    public TabletLineMap(String filename) {
        File file = localFile(AsrTabletConfig.getInstance().getPath(), filename + ".BMP");
        if (!file.exists())
            return;
        bmp = new BmpIo(file.getPath());
        base = new Point(bmp.width / 42, bmp.height * 3 >> 2);
        if (base.x < 10 || base.x > 13)
            return;
        double factor = bmp.height / 24.0;
        sgle = getFont(Font.BOLD, fontsize[base.x - 10]);
        dble = sgle.deriveFont(AffineTransform.getScaleInstance(2, factor));
        sgle = sgle.deriveFont(AffineTransform.getScaleInstance(1, factor));
        bim = new BufferedImage(bmp.width, bmp.height, BufferedImage.TYPE_BYTE_BINARY);
        bmp.close();
    }

    public String update(String text) {
        return update(text, null);
    }

    public String update(String text, String appendix) {
        if (bim == null)
            return null;
        Graphics g = bim.getGraphics();
        g.setFont(sgle);
        g.setColor(Color.white);
        g.fillRect(0, 0, bmp.width, bmp.height);
        g.setColor(Color.black);
        if (text.charAt(1) == '>') {
            g.setFont(dble);
            g.drawString(text.substring(2, 22), base.x, base.y);
        } else
            g.drawString(text, 0, base.y);
        try {
            String pathname = bmp.getPathname();

            bmp.setFile(new RandomAccessFile(bmp.getPathname(), "rw"));
            bmp.setData(((DataBufferByte) bim.getData().getDataBuffer()).getData());

            bmp.rewrite();
            bmp.getFile().close();
            if (appendix != null) {
                pathname = pathname.substring(0, pathname.indexOf(".BMP")) + appendix + ".BMP";
                fileCopy(bmp.getPathname(), pathname);
            }
            return pathname;
        } catch (IOException e) {
            logConsole(0, bmp.getPathname(), e.toString());
            return null;
        }
    }

    public void fileCopy(String source, String dest) {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {
            inStream = new FileInputStream(new File(source));
            outStream = new FileOutputStream(new File(dest));

            byte[] buffer = new byte[1024];

            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            System.out.println("File is copied successful!");
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            try {
                inStream.close();
                outStream.close();
            } catch (Exception e) {
                logger.error("Error closing files: ", e);
            }
        }
    }

    public static void main(String[] args) {
        TabletLineMap lm = new TabletLineMap("PrtLine");

        if (args.length > 0)
            lm.update(" >20 chars double-wide                    ");
        else
            lm.update("The line of 42 characters ends right here.");
    }
}
