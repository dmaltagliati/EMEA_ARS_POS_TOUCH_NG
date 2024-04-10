package com.ncr.tablet;

public interface LineMapIf {
    String update(String text);
    String update(String text, String appendix);
     void fileCopy(String source, String dest);
}
