package com.falstad.dfilter.client;

public class EllipticLowPass extends EllipticFilterType {
    int select() {
        int s = selectLowPass();
        selectElliptic(s);
        return s+2;
    }
    void setup() {
        setupLowPass();
        setupElliptic(2);
    }
    void getInfo(String x[]) {
        x[0] = "Elliptic (IIR), " + getPoleCount() + "-pole";
        getInfoLowPass(x);
        getInfoElliptic(x);
    }
    void getZero(int i, Complex c1) { getEllipticZero(i, c1, wc); }
}
