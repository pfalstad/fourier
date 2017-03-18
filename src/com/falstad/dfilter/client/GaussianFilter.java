package com.falstad.dfilter.client;

public     class GaussianFilter extends FIRFilterType {
    int n;
    double cw;
    int select() {
        auxLabels[0].setText("Offset");
        auxBars[0].setMaximum(1000);
        auxBars[0].setValue(100);
        auxLabels[1].setText("Width");
        auxBars[1].setMaximum(1000);
        auxBars[1].setValue(100);
        auxLabels[2].setText("Order");
        auxBars[2].setMaximum(1600);
        auxBars[2].setValue(160);
        return 3;
    }
    void setup() {
        n = auxBars[2].getValue();
        cw = auxBars[0].getValue()*pi/1000.;
    }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[n];
        int i;
        double w = auxBars[1].getValue()/100000.;
        int n2 = n/2;
        for (i = 0; i != n; i++) {
            int ii = i-n2;
            f.aList[i] = Math.exp(-w*ii*ii)*Math.cos(ii*cw)*getWindow(i, n);
        }
        setResponse(f);
        return f;
    }
    boolean needsWindow() { return true; }
    void getInfo(String x[]) {
        x[0] = "Gaussian (FIR)";
        x[1] = "Order: " + n;
    }
}
