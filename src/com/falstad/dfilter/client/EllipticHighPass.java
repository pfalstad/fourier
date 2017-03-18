package com.falstad.dfilter.client;

public class EllipticHighPass extends EllipticLowPass {
    void getPole(int i, Complex c1) {
        getSPole(i, c1, pi-wc);
        bilinearXform(c1);
        c1.mult(-1);
    }
    void getZero(int i, Complex c1) {
        getEllipticZero(i, c1, pi-wc);
        c1.mult(-1);
    }
}
