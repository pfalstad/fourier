package com.falstad.dfilter.client;

public class NoFilter extends FilterType {
    void getResponse(double w, Complex c) {
        c.set(1);
    }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[1];
        f.aList[0] = 1;
        return f;
    }
}
