package com.falstad.dfilter.client;

public     class NotchFilter extends IIRFilterType {
    double wc, a, b, bw;
    int select() {
        auxLabels[0].setText("Notch Frequency");
        auxBars[0].setValue(500);
        auxLabels[1].setText("Bandwidth");
        auxBars[1].setValue(900);
        return 2;
    }
    void setup() {
        wc = auxBars[0].getValue()*pi/1000.;
        bw = auxBars[1].getValue()*pi/2000.;
        a = (1-Math.tan(bw/2))/(1+Math.tan(bw/2));
        b = Math.cos(wc);
    }
    void getPole(int i, Complex c1) {
        c1.set(-4*a+(b+a*b)*(b+a*b));
        c1.sqrt();
        if (i == 1)
            c1.mult(-1);
        c1.add(b+a*b);
        c1.mult(.5);
    }
    int getPoleCount() { return 2; }
    void getInfo(String x[]) {
        x[0] = "Notch (IIR)";
        x[1] = "Notch Frequency: " + getOmegaText(wc);
        x[2] = "Bandwidth: " + getOmegaText(bw);
    }
    int getZeroCount() { return 2; }
    void getZero(int i, Complex c1) {
        c1.set(b*b-1);
        c1.sqrt();
        if (i == 1)
            c1.mult(-1);
        c1.add(b);
    }
}
