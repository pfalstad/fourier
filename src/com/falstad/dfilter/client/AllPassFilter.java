package com.falstad.dfilter.client;

public     class AllPassFilter extends IIRFilterType {
    double a;
    int select() {
        auxLabels[0].setText("Phase Delay");
        auxBars[0].setValue(500);
        return 1;
    }
    void setup() {
        double delta = auxBars[0].getValue()/1000.;
        a = (1-delta)/(1+delta);
    }
    void getPole(int i, Complex c1) {
        c1.set(-a);
    }
    int getPoleCount() { return 1; }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[2];
        f.bList = new double[2];
        f.nList = new int[] { 0, 1 };
        f.aList[0] = a;
        f.aList[1] = 1;
        f.bList[0] = 1;
        f.bList[1] = a;
        setResponse(f);
        return f;
    }
    void getInfo(String x[]) {
        x[0] = "Allpass Fractional Delay (IIR)";
    }
}
