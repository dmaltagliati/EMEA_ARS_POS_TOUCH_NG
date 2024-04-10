package com.ncr.tablet;

import com.ncr.LineMap;

import javax.sound.sampled.Line;

public  class LineMapFactory {

     String name;
    public LineMapFactory(){

    }

    public static LineMapIf getLineMap(boolean tabletEnable, String name){
        LineMapIf lMap;

        if (tabletEnable){
            lMap = new TabletLineMap(name);
        }else{
            lMap = new LineMap(name);
        }

        return lMap;
    }
}
