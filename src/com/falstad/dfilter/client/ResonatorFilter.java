package com.falstad.dfilter.client;

public class ResonatorFilter extends IIRFilterType {
    double r, wc;
    int select() {
        auxLabels[0].setText("Resonant Frequency");
        auxBars[0].setValue(500);
        auxLabels[1].setText("Sharpness");
        auxBars[1].setValue(900);
        return 2;
    }
    void setup() {
        wc = auxBars[0].getValue()*pi/1000.;
        double rolldb = -auxBars[1].getValue()*3/1000.;
        r = 1-Math.pow(10, rolldb);
    }
    void getPole(int i, Complex c1) {
        c1.setMagPhase(r, (i == 1) ? -wc : wc);
    }
    int getPoleCount() { return 2; }
    void getInfo(String x[]) {
        x[0] = "Reson (IIR)";
        x[1] = "Res. Frequency: " + getOmegaText(wc);
    }
}
