package com.falstad.dfilter.client;

public class CombFilter extends IIRFilterType {
    int n, sign;
    double mult, peak;
    CombFilter(int s) { sign = s; }
    int select() {
        auxLabels[0].setText("1st Pole");
        auxBars[0].setValue(60);
        auxLabels[1].setText("Sharpness");
        auxBars[1].setValue(700);
        return 2;
    }
    void setup() {
        n = 2000/auxBars[0].getValue();
        mult = auxBars[1].getValue()/1000.;
        peak = 1/(1-mult);
    }
    void getPole(int i, Complex c1) {
        int odd = (sign == 1) ? 0 : 1;
        c1.setMagPhase(Math.pow(mult, 1./n), pi*(odd+2*i)/n);
    }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[] { 1/peak, 0 };
        f.bList = new double[] { 0, -sign*mult };
        f.nList = new int[] { 0, n };
        setResponse(f);
        return f;
    }
    void getInfo(String x[]) {
        x[0] = "Comb (IIR); Resonance every " + getOmegaText(2*pi/n);
        x[1] = "Delay: " + n + " samples, " +
            DFilterSim.theSim.getUnitText(n/(double) DFilterSim.theSim.sampleRate, "s");
        double tl = 340.*n/(DFilterSim.theSim.sampleRate*2);
        x[2] = "Tube length: " + DFilterSim.theSim.getUnitText(tl, "m");
        if (sign == -1)
            x[2] += " (closed)";
        else
            x[2] += " (open)";
    }
    int getPoleCount() { return n; }
    int getZeroCount() { return n; }
    void getZero(int i, Complex c1) { c1.set(0); }
}
