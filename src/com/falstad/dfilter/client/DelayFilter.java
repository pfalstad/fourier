package com.falstad.dfilter.client;

public class DelayFilter extends CombFilter {
    DelayFilter() {
        super(1);
    }
    void getResponse(double w, Complex c) {
        if (n > 500) // 212)
            c.set(1);
        else
            super.getResponse(w, c);
    }
    void setCutoff(double f) {}
    int select() {
        auxLabels[0].setText("Delay");
        auxBars[0].setValue(300);
        auxLabels[1].setText("Strength");
        auxBars[1].setValue(700);
        return 2;
    }
    void setup() {
        n = auxBars[0].getValue()*16384/1000;
        mult = auxBars[1].getValue()/1250.;
        peak = 1/(1-mult);
    }
    void getInfo(String x[]) {
    	DFilterSim sim = DFilterSim.theSim;
    	double sampleRate = sim.sampleRate;
        x[0] = "Delay (IIR)";
        x[1] = "Delay: " + n + " samples, " +
            sim.getUnitText(n/(double) sampleRate, "s");
        double tl = 340.*n/sampleRate / 2;
        x[2] = "Echo Distance: " + sim.getUnitText(tl, "m");
        if (tl > 1)
            x[2] += " (" + sim.showFormat.format(tl*3.28084) + " ft)";
    }
}
