package com.falstad.dfilter.client;

public class InverseCombFilter extends FIRFilterType {
    int n;
    double mult, peak;
    int select() {
        auxLabels[0].setText("2nd Zero");
        auxBars[0].setValue(60);
        auxLabels[1].setText("Sharpness");
        auxBars[1].setValue(1000);
        return 2;
    }
    void setup() {
        n = 1990/auxBars[0].getValue();
        mult = auxBars[1].getValue()/1000.;
        peak = 1+mult;
    }
    void getZero(int i, Complex c1) {
        c1.setMagPhase(Math.pow(mult, 1./n), pi*2*i/n);
    }
    int getZeroCount() { return n; }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[] { 1/peak, -mult/peak };
        f.nList = new int[] { 0, n };
        setResponse(f);
        return f;
    }
    void getInfo(String x[]) {
        x[0] = "Inverse Comb (FIR)";
        x[1] = "Zeros every " + getOmegaText(2*pi/n);
    }
}
