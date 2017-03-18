package com.falstad.dfilter.client;

public class ButterBandStop extends ButterBandPass {
    void getPole(int i, Complex c1) { getBandStopPole(i, c1); }
    void getZero(int i, Complex c1) { getBandStopZero(i, c1); }

}
