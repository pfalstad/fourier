package com.falstad.dfilter.client;

public class InvChebyHighPass extends InvChebyLowPass {
    void getPole(int i, Complex c1) {
        getSPole(i, c1, pi-wc);
        bilinearXform(c1);
        c1.mult(-1);
    }
    void getZero(int i, Complex c1) {
        getChebyZero(i, c1, pi-wc);
        c1.mult(-1);
    }
}
