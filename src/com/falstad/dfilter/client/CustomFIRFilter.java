package com.falstad.dfilter.client;

public class CustomFIRFilter extends FIRFilterType {
	static double uresp[];

    CustomFIRFilter() { if (uresp == null) uresp = new double[1024]; }
    int select() {
        auxLabels[0].setText("Order");
        auxBars[0].setValue(120);
        auxBars[0].setMaximum(1600);
        int i;
        for (i = 0; i != 512; i++)
            uresp[i] = 1.;
        return 1;
    }
    void setup() { }
    double getUserResponse(double w) {
        double q = uresp[(int) (w*uresp.length/pi)];
        return q*q;
    }
    void edit(double x, double x2, double y) {
        int xi1 = (int) (x *uresp.length);
        int xi2 = (int) (x2*uresp.length);
        for (; xi1 < xi2; xi1++)
            if (xi1 >= 0 && xi1 < uresp.length)
                uresp[xi1] = y;
    }
    Filter genFilter() {
        int n = auxBars[0].getValue();
        int nsz = uresp.length*4;
        double fbuf[] = new double[nsz];
        int i;
        int nsz2 = nsz/2;
        int nsz4 = nsz2/2;
        for (i = 0; i != nsz4; i++) {
            double ur = uresp[i]/nsz2;
            fbuf[i*2] = ur;
            if (i > 0)
                fbuf[nsz-i*2] = ur;
        }
        new FFT(nsz2).transform(fbuf, true);
        
        DirectFilter f = new DirectFilter();
        f.aList = new double[n];
        f.nList = new int[n];
        for (i = 0; i != n; i++) {
            int i2 = (i-n/2)*2;
            f.aList[i] = fbuf[i2 & (nsz-1)]*getWindow(i, n);
            f.nList[i] = i;
        }
        setResponse(f);
        return f;
    }
    void getInfo(String x[]) {
        int n = auxBars[0].getValue();
        x[0] = "Order: " + n;
    }
    boolean needsWindow() { return true; }
}
