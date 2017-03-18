package com.falstad.dfilter.client;

import java.util.Random;

public     class RandomFilter extends FIRFilterType {
    int n;
    int select() {
        auxLabels[0].setText("Order");
        auxBars[0].setMaximum(1600);
        auxBars[0].setValue(100);
        return 1;
    }
    void setCutoff(double f) {}
    void setup() {
        n = auxBars[0].getValue();;
    }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[n];
        int i;
        Random random = DFilterSim.theSim.random;
        for (i = 0; i != n; i++)
            f.aList[i] = random.nextInt()*getWindow(i, n);
        setResponse(f);
        return f;
    }
    boolean needsWindow() { return true; }
    void getInfo(String x[]) {
        x[0] = "Random (FIR)";
        x[1] = "Order: " + n;
    }
}
