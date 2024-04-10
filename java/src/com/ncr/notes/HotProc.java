package com.ncr.notes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

class HotProc {
    static class Redirect extends Thread {
        PrintStream out; /* Java console */
        BufferedInputStream bin;

        Redirect(PrintStream out, InputStream in) {
            this.out = out;
            bin = new BufferedInputStream(in);
            start();
        }

        public void run() {
            try {
                for (int b; (b = bin.read()) > 0; out.write(b))
                    ;
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    static int exec(String cmd) {
        int sts = -1;
        try {
            Process pro = Runtime.getRuntime().exec(cmd);
            new HotProc.Redirect(System.out, pro.getInputStream());
            new HotProc.Redirect(System.err, pro.getErrorStream());
            sts = pro.waitFor();
        } catch (Exception e) {
        }
        return sts;
    }
}
