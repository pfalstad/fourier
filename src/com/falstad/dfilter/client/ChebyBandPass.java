package com.falstad.dfilter.client;

public class ChebyBandPass extends ChebyFilterType {
    int select() {
        int s = selectBandPass();
        selectCheby(s++);
        return s;
    }
    void setup() {
        setupBandPass();
        setupCheby(3);
    }
    void getPole(int i, Complex c1) { getBandPassPole(i, c1); }
    void getZero(int i, Complex c1) { getBandPassZero(i, c1); }
    int getPoleCount() { return n*2; }
    int getZeroCount() { return n*2; }
    void getInfo(String x[]) {
        x[0] = "Chebyshev (IIR), " + getPoleCount() + "-pole";
        getInfoBandPass(x, this instanceof ChebyBandStop);
        getInfoCheby(x);
    }

}
