package com.falstad.dfilter.client;

public class ResonatorZeroFilter extends ResonatorFilter {
    int getZeroCount() { return 2; }
    void getZero(int i, Complex c1) {
        c1.set(i == 0 ? 1 : -1);
    }
}
