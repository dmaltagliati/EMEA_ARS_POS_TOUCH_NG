package com.ncr.common.utilities;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ncr.DevIo;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import static com.ncr.Struc.*;

public class QrCodeGenerator {
    private static final Logger logger = Logger.getLogger(QrCodeGenerator.class);
    public final static String PRINT_AS_QR = "qr";
    public final static String PRINT_AS_BMP = "bitmap";

    public static void printQR(String format, String qrCode, String type, int width, int height, String path, String imageType) {
        logger.debug("Format: " + format + " QR code: " + qrCode + " type: " + type);;
        if (PRINT_AS_QR.equals(type)) {
            logger.debug("QR to be printed" + qrCode + " width:" + width + " height:" + height);
            DevIo.tpmQrLabel(2, qrCode, width, height);
            logger.debug("QR successfully printed as QR");
        } else if (PRINT_AS_BMP.equals(type)) {
            try {
                logger.debug("QR to be printed as BMP" + qrCode + " width:" + width + " height:" + height + " ImagePath= " + path + " ImageType = " + imageType);
                generateQRCodeBitMap(qrCode, width, height, path, imageType);
                logger.debug("format: " + format);
                prtLine.init(format).type(3);
            } catch (Exception e) {
                logger.error("Error Printing QR code: ", e);
            }
        }
    }

    private static BufferedImage generateQRCodeBitMap(String text, int width, int height, String path, String imageType)
            throws WriterException, IOException {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(new String(text.getBytes("UTF-8"), "UTF-8"), BarcodeFormat.QR_CODE, width, height, hintMap);

        int bitMatrixWidth = bitMatrix.getWidth();

        BufferedImage qrImage = new BufferedImage(bitMatrixWidth, bitMatrixWidth, BufferedImage.TYPE_INT_RGB);

        qrImage.createGraphics();
        Graphics2D qrGraphics = (Graphics2D) qrImage.getGraphics();
        qrGraphics.setColor(Color.white);
        qrGraphics.fillRect(0, 0, bitMatrixWidth, bitMatrixWidth);
        qrGraphics.setColor(Color.BLACK);

        for (int i = 0; i < bitMatrixWidth; i++) {
            for (int j = 0; j < bitMatrixWidth; j++) {
                if (bitMatrix.get(i, j)) {
                    qrGraphics.fillRect(i, j, 1, 1);
                }
            }
        }
        logger.debug("Saving file: " + path);
        ImageIO.write(qrImage, imageType, new File(path));
        return qrImage;
    }

    public static boolean deleteQRImageGenerated(String path) {
        File file = new File(path);
        if (file.exists()) {
            logger.debug(file.getAbsoluteFile());
            try {
                file.delete();
                return true;
            } catch (Exception e) {
                logger.error("Failed to delete the file." + e.getMessage());
                return false;
            }
        }
        return true;
    }
}
