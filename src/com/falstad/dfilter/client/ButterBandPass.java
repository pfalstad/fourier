package com.falstad.dfilter.client;

public class ButterBandPass extends ButterFilterType {
    int select() { return selectBandPass(); }
    void setup() { setupBandPass(); }
    void getPole(int i, Complex c1) { getBandPassPole(i, c1); }
    void getZero(int i, Complex c1) { getBandPassZero(i, c1); }
    int getPoleCount() { return n*2; }
    int getZeroCount() { return n*2; }
    void getInfo(String x[]) {
        x[0] = "Butterworth (IIR), " + getPoleCount() + "-pole";
        getInfoBandPass(x, this instanceof ButterBandStop);
    }

}
