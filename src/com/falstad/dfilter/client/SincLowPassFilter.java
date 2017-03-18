package com.falstad.dfilter.client;

public class SincLowPassFilter extends FIRFilterType {
    int n;
    double wc, mult, peak;
    double resp[];
    boolean invert;
    int select() {
        auxLabels[0].setText("Cutoff Frequency");
        auxLabels[1].setText("Order");
        auxBars[0].setValue(invert ? 500 : 100);
        auxBars[1].setValue(120);
        auxBars[1].setMaximum(1600);
        return 2;
    }
    void setup() {
        wc = auxBars[0].getValue() * pi/1000.;
        n = auxBars[1].getValue();
    }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[n];
        int n2 = n/2;
        int i;
        double sum = 0;
        for (i = 0; i != n; i++) {
            int ii = i-n2;
            f.aList[i] = ((ii == 0) ? wc : Math.sin(wc*ii)/ii) * getWindow(i, n);
            sum += f.aList[i];
        }
        // normalize
        for (i = 0; i != n; i++)
            f.aList[i] /= sum;
        if (invert) {
            for (i = 0; i != n; i++)
                f.aList[i] = -f.aList[i];
            f.aList[n2] += 1;
        }
        if (n == 1)
            f.aList[0] = 1;
        setResponse(f);
        return f;
    }
    void getInfo(String x[]) {
        x[0] = "Cutoff freq: " + getOmegaText(wc);
        x[1] = "Order: " + n;
    }
    boolean needsWindow() { return true; }
}
