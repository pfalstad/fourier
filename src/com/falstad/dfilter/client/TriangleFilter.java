package com.falstad.dfilter.client;

public class TriangleFilter extends FIRFilterType {

    int ni;
    double n;
    int select() {
        auxLabels[0].setText("Cutoff Frequency");
        auxBars[0].setValue(500);
        return 1;
    }
    void setup() {
        n = 4000./auxBars[0].getValue();
        if (n > 1000)
            n = 1000;
        ni = (int) n;
    }
    Filter genFilter() {
        DirectFilter f = new DirectFilter();
        f.aList = new double[ni+1];
        int i;
        double sum = 0;
        double n2 = n/2;
        for (i = 0; i < n; i++) {
            double q = 0;
            if (i < n2)
                q = i/n2;
            else
                q = 2-(i/n2);
            sum += q;
            f.aList[i] = q;
        }
        for (i = 0; i != f.aList.length; i++)
            f.aList[i] /= sum;
        setResponse(f);
        return f;
    }
    void getInfo(String x[]) {
        x[0] = "Triangle (FIR)";
        x[1] = "Cutoff: " + getOmegaText(4*pi/n);
        x[2] = "Length: " + DFilterSim.theSim.showFormat.format(n);
    }

}
