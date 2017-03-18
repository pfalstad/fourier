package com.falstad.dfilter.client;

public     class PluckedStringFilter extends IIRFilterType {
    int n;
    double mult;
    int select() {
        auxLabels[0].setText("Fundamental");
        auxBars[0].setValue(20);
        auxLabels[1].setText("Sharpness");
        auxBars[1].setValue(970);
        return 2;
    }
    void setup() {
        n = 2000/auxBars[0].getValue();
        mult = .5*Math.exp(-.5+auxBars[1].getValue()/2000.);
    }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[] { 1, 1, 0, 0 };
        f.bList = new double[] { 1, 0, -mult, -mult };
        f.nList = new int[] { 0, 1, n, n+1 };
        setResponse(f);
        return f;
    }
    void getInfo(String x[]) {
    	DFilterSim sim = DFilterSim.theSim;
        x[0] = "Plucked String (IIR); Resonance every " + getOmegaText(2*pi/n);
        x[1] = "Delay: " + n + " samples, " +
            sim.getUnitText(n/(double) sim.sampleRate, "s");
    }
}
