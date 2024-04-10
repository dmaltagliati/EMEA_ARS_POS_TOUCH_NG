package com.ncr.tablet;

import com.ncr.BmpIo;
import com.ncr.Config;
import com.ncr.FmtIo;
import com.ncr.Struc;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class ReceiptManager extends Config {
    private static final Logger logger = Logger.getLogger(ReceiptManager.class);
    private static ReceiptManager instance;

    public static final String RECEIPT_HEADER_NAME = "header.bmp";
    public static final String RECEIPT_BODY_NAME = "body.bmp";
    public static final String RECEIPT_FOOTER_NAME = "footer.bmp";
    public static final String RECEIPT_COMPLETE_NAME = "receipt.bmp";

    public static ReceiptManager getInstance() {
        if (instance == null) {
            instance = new ReceiptManager();
        }

        return instance;
    }

    public void bitmap(String name) {
        String path = AsrTabletConfig.getInstance().getPath();
        BmpIo bmp = new BmpIo(name);
        int wide = bmp.width, high = 24, clip = wide * (high >> 3);

        for (int line = 0; line < bmp.height; line += high) {
            byte[][] dots = new byte[wide][high >> 3];
            bmp.getColumns(dots, line, high, false);
            write("\u001ba1\u001b*!");
            try {
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path +"out.dat"), 2 + clip);
                out.write(wide & 255);
                out.write(wide >> 8);
                for (int ind = 0; ind < wide; out.write(dots[ind++])) ;
                out.close();
            } catch (IOException e) {
                logger.error("exception, e: " + e.getMessage());
            }
            write("\u001bJ\u0000\u001ba0");
        }
        bmp.close();
    }

    public void send(String data) throws IOException {
        OutputStream out = new FileOutputStream("c:\\gd90\\bmp\\dani.txt");

        out.write(data.getBytes(oem));
    }

    public void write(String data) {
        while (true) try {
            send(data);
            break;
        } catch (IOException e) {
            logger.error("exception, e: " + e.getMessage());
        }
    }

    public void generateReceipt(String receiptName) {
        String baseName = "PrtLine";
        String fileName =  "";
        String path = AsrTabletConfig.getInstance().getPath();
        int widht = 546, height = 24;

        if (AsrTabletConfig.getInstance().isEnable()) {
            try {
                BufferedImage scontrino = new BufferedImage(widht, height * Struc.tra.bmpSequence, BufferedImage.TYPE_BYTE_BINARY);
                Graphics g;
                for (int i = 1; i <= Struc.tra.bmpSequence; i++) {
                    fileName = path + baseName + "." + FmtIo.editNum(i, 4) + ".BMP";
                    g = scontrino.getGraphics();
                    BufferedImage src1 = ImageIO.read(new File(fileName));
                    g.drawImage(src1, 0, height * (i-1), null);
                    g.dispose();
                }
                ImageIO.write(scontrino, "bmp", new File(path + receiptName));
                Struc.tra.bmpSequence = 0;
            } catch (IOException e) {
                logger.error("error generateReceipt.");
            }
        }
    }

    private void buildReceipt(){
        String path = AsrTabletConfig.getInstance().getPath();
        Graphics g;
        BufferedImage header = null;
        BufferedImage body = null;
        BufferedImage footer = null;
        int heightHeader = 0, heightBody = 0, heightFooter = 0, heightTotal = 0;
        int widht = 546;

        try {
            header = ImageIO.read(new File(path + RECEIPT_HEADER_NAME));
            heightHeader += header.getHeight();
            heightTotal += header.getHeight();
        }catch (Exception e){
            logger.error("header's receipt not found");
        }

        try {
            body = ImageIO.read(new File(path + RECEIPT_BODY_NAME));
            heightBody += body.getHeight();
            heightTotal += body.getHeight();
        }catch (Exception e){
            logger.error("body's receipt not found");
        }

        try {
            footer = ImageIO.read(new File(path + RECEIPT_FOOTER_NAME));
            heightFooter += footer.getHeight();
            heightTotal += footer.getHeight();
        }catch (Exception e){
            logger.error("footer's receipt not found");
        }
        try {
            BufferedImage scontrinoFinale = new BufferedImage(widht, heightTotal, BufferedImage.TYPE_BYTE_BINARY);
            g = scontrinoFinale.getGraphics();

            if (header != null) {
                g.drawImage(header, 0, 0, null);

            }

            if (body != null) {
                g.drawImage(body, 0, heightHeader, null);

            }

            if (footer != null) {
                g.drawImage(footer, 0, heightHeader + heightBody , null);

            }

            g.dispose();
            try {
                ImageIO.write(scontrinoFinale, "bmp", new File(path + RECEIPT_COMPLETE_NAME));
            } catch (Exception e) {
                logger.error("error buildReceipt.");
            }
        }catch (Exception e){
            logger.error("error buildReceipt 2.");
        }
    }

    public void printReceipt(){
        String path = AsrTabletConfig.getInstance().getPath();

        if (AsrTabletConfig.getInstance().isEnable()) {
            buildReceipt();
            try {
                BufferedImage img = ImageIO.read(new File(path + ReceiptManager.RECEIPT_COMPLETE_NAME));
                PrinterJob printJob = PrinterJob.getPrinterJob();
                printJob.setPrintable(new PrintReceipt(printJob, img));

                if (AsrTabletConfig.getInstance().isDebug()) {
                    printJob.printDialog();
                }

                printJob.print();
            } catch (Exception e) {
                logger.error("error PrintReceiptOld.");
            }

        }
    }



}
