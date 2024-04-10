package com.ncr;

import java.util.Vector;

/*******************************************************************
 * vector of sales and discount items
 *******************************************************************/
public class ItemVector extends Vector {
    /***************************************************************************
     * add to vector of items
     *
     * @param id
     *            S=sales P=salesperson M=money, C=manual discount
     ***************************************************************************/
    public void addElement(char id, Itemdata ptr) {
        ptr.id = id;
        ptr.index = size();
        addElement(ptr);
    }

    /***************************************************************************
     * get reference to item
     *
     * @param ind
     *            index of item in vector
     * @return reference to com.ncr.Itemdata object
     ***************************************************************************/
    public Itemdata getElement(int ind) {
        return (Itemdata) elementAt(ind);
    }

    /***************************************************************************
     * get element with matching id from vector of items
     *
     * @param id
     *            S=sales L=link M=money C=manual discount *=any
     * @param ind
     *            start of search (0, 1, ..., n)
     * @param step
     *            direction (-1=previous, 0=this, +1=next)
     * @return reference to com.ncr.Itemdata object (null=not in vector)
     ***************************************************************************/
    public Itemdata getElement(char id, int ind, int step) {
        for (ind += step; ind >= 0 && ind < elementCount; ind += step) {
            Itemdata ptr = getElement(ind);
            if (id == '*' || ptr.id == id)
                return ptr;
            if (step == 0)
                break;
        }
        return null;
    }
}
