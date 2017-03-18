package com.falstad.dfilter.client;

public class EllipticBandPass extends EllipticFilterType {
    int select() {
        int s = selectBandPass();
        auxBars[2].setValue(5);
        selectElliptic(s);
        return s + 2;
    }
    void setup() {
        setupBandPass();
        setupElliptic(3);
    }
    void getPole(int i, Complex c1) { getBandPassPole(i, c1); }
    void getZero(int i, Complex c1) {
        getEllipticZero(i/2, c1, pi*.5);
        bandPassXform(i, c1);
    }
    int getPoleCount() { return n*2; }
    int getZeroCount() { return n*2; }
    void getInfo(String x[]) {
        x[0] = "Elliptic (IIR), " + getPoleCount() + "-pole";
        getInfoBandPass(x, this instanceof EllipticBandStop);
        getInfoElliptic(x);
    }
}
