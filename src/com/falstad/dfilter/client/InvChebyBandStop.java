package com.falstad.dfilter.client;

public class InvChebyBandStop extends InvChebyBandPass {
    void getPole(int i, Complex c1) { getBandStopPole(i, c1); }
    void getZero(int i, Complex c1) {
        getChebyZero(i/2, c1, pi*.5);
        bandStopXform(i, c1);
    }

}
