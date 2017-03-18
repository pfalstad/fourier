package com.falstad.dfilter.client;

public class MovingAverageFilter extends FIRFilterType {
    double n;
    int ni;
    int select() {
        auxLabels[0].setText("Cutoff Frequency");
        auxBars[0].setValue(500);
        return 1;
    }
    void setup() {
        n = 2000./auxBars[0].getValue();
        if (n > 1000)
            n = 1000;
        ni = (int) n;
    }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[ni+1];
        int i;
        for (i = 0; i != ni; i++)
            f.aList[i] = 1./n;
        f.aList[i] = (n-ni) / n;
        setResponse(f);
        return f;
    }
    void getInfo(String x[]) {
        x[0] = "Moving Average (FIR)";
        x[1] = "Cutoff: " + getOmegaText(2*pi/n);
        x[2] = "Length: " + DFilterSim.theSim.showFormat.format(n);
    }
}
