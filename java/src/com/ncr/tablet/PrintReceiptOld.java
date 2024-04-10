package com.ncr.tablet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;

public class PrintReceiptOld implements Printable {

    int widht, height;
    PrinterJob pj;
    public PrintReceiptOld(int widht, int height) throws PrinterException {

        this.widht = widht;
        this.height = height;

        pj = PrinterJob.getPrinterJob();

        // apre la finestra di dialogo della stampante
        //pj.printDialog();

       // pj.setPrintable(this);
        pj.print();
    }

    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        String path = AsrTabletConfig.getInstance().getPath();
        try {
            // abbiamo solo una pagina
            // (l'indicizzate da 0)
            if (page > 0) {
                return NO_SUCH_PAGE;
            }

            Paper paper = new Paper();
            paper.setImageableArea(0,0, widht, height);
            paper.setSize(widht, height);
            pf.setPaper(paper);

            Book book = new Book();
            book.append(this, pf);
            pj.setPageable(book);


            // Posizioniamo correttamente le coordinate
            // da dove cominciare a disegnare
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            BufferedImage img = ImageIO.read(new File(path + ReceiptManager.RECEIPT_COMPLETE_NAME));


            // Rendering
            g2d.drawImage(img, 0, 0, widht, height, null);

        }catch (Exception e){

        }
        return PAGE_EXISTS;
    }
}
