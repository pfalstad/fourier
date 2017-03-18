package com.falstad.dfilter.client;

abstract class ButterFilterType extends PoleFilterType {
    void getSPole(int i, Complex c1, double wc) {
        double theta = pi/2 + (2*i+1)*pi/(2*n);
        c1.setMagPhase(Math.tan(wc*.5), theta);
    }
}

