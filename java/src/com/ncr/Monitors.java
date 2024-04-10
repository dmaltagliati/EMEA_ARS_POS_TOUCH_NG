package com.ncr;

/*******************************************************************
 * control data for various monitors
 *******************************************************************/
public class Monitors {
    /**
     * minute (00-59) of latest time display
     **/
    public int clock = -1;
    /**
     * operator display toggle (bit0=line0/1, bit1=normal/alternative text)
     **/
    public int odisp = -1;
    /**
     * total display in home/alternative currency
     **/
    public int total = -1;
    /**
     * deposit value in cash-recycler
     **/
    public int money = -1;
    /**
     * h/o customer account inquiry (-1=inactive, 0=started, 1=monitoring
     **/
    public int hocus = -1;
    /**
     * watch for keylock change in authorization dialogue
     **/
    public int autho;
    /**
     * ring bell for important news 0=no, 1=yes, -1=end
     **/
    public int alert;
    /**
     * watch for completion of image data transfer to server
     **/
    public int image;
    /**
     * watch for terminal status change in cluster EoD
     **/
    public int lan99;
    /**
     * record number of remote journal watch
     **/
    public int watch;
    /**
     * used to trigger automatic procedures (four ticks per second)
     **/
    public int tick;

    /**
     * advertizing on customer display (character position)
     **/
    public int adv_dsp;
    /**
     * advertizing on customer display (line of text)
     **/
    public int adv_rec;
    /**
     * status information message by message on operator display
     **/
    public int opd_sts;
    /**
     * notes/messages (originator of message received and displayed)
     **/
    public int rcv_ckr;
    /**
     * notes/messages (record number of message received and displayed)
     **/
    public int rcv_dsp;
    /**
     * notes/messages (record number of message sent and displayed)
     **/
    public int snd_dsp;
    /**
     * notes/messages (text received and displayed)
     **/
    public String rcv_msg;
    /**
     * notes/messages (header info of message received)
     **/
    public String rcv_mon;
    /**
     * notes/messages (header info of message sent)
     **/
    public String snd_mon;
    /**
     * advertizing on customer display (text)
     **/
    public String adv_txt;
    /**
     * total display in home/alternative currency (text)
     **/
    public String tot_txt;
    /**
     * operator display toggle (alternative text)
     **/
    public String opd_alt;
}
