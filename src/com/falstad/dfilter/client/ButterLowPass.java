package com.falstad.dfilter.client;

public class ButterLowPass extends ButterFilterType {
    int sign;
    ButterLowPass() { sign = 1; }
    int select() { return selectLowPass(); }
    void setup() { setupLowPass(); }
    void getZero(int i, Complex c1) { c1.set(-sign); }
    int getPoleCount() { return n; }
    int getZeroCount() { return n; }
    void getInfo(String x[]) {
        x[0] = "Butterworth (IIR), " + getPoleCount() + "-pole";
        getInfoLowPass(x);
    }
}
