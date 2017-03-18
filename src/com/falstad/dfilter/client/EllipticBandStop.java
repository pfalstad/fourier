package com.falstad.dfilter.client;

public class EllipticBandStop extends EllipticBandPass {
    void getPole(int i, Complex c1) { getBandStopPole(i, c1); }
    void getZero(int i, Complex c1) {
        getEllipticZero(i/2, c1, pi*.5);
        bandStopXform(i, c1);
    }
}
