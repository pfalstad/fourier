package com.falstad.dfilter.client;

public class InvChebyLowPass extends InvChebyFilterType {
    int select() {
        int s = selectLowPass();
        selectCheby(s++);
        return s;
    }
    void setup() {
        setupLowPass();
        setupCheby(2);
    }
    void getInfo(String x[]) {
        x[0] = "Inverse Chebyshev (IIR), " + getPoleCount() + "-pole";
        getInfoLowPass(x);
        getInfoCheby(x);
    }
    void getZero(int i, Complex c1) { getChebyZero(i, c1, wc); }
}
