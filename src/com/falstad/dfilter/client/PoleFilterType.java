package com.falstad.dfilter.client;

public abstract class PoleFilterType extends IIRFilterType {
    int n;
    double wc, wc2;
    abstract void getSPole(int i, Complex c1, double wc);
    void getPole(int i, Complex c1) {
        getSPole(i, c1, wc);
        bilinearXform(c1);
    }
    void bilinearXform(Complex c1) {
        Complex c2 = new Complex(c1);
        c1.add(1);
        c2.mult(-1);
        c2.add(1);
        c1.div(c2);
    }
    int selectLowPass() {
        auxLabels[0].setText("Cutoff Frequency");
        auxLabels[1].setText("Number of Poles");
        auxBars[1].setMaximum(40);
        auxBars[0].setValue(100);
        auxBars[1].setValue(4);
        return 2;
    }
    int selectBandPass() {
    	auxLabels[0].setText("Center Frequency");
    	auxLabels[1].setText("Passband Width");
    	auxLabels[2].setText("Number of Poles");
    	auxBars[2].setMaximum(20);
    	auxBars[0].setValue(500);
    	auxBars[1].setValue(200);
    	auxBars[2].setValue(6);
        return 3;
    }
    void getBandPassPole(int i, Complex z) {
        getSPole(i/2, z, Math.PI*.5);
        bilinearXform(z);
        bandPassXform(i, z);
    }
    void bandPassXform(int i, Complex z) {
        double a = Math.cos((wc+wc2)*.5) /
                   Math.cos((wc-wc2)*.5);
        double b = 1/Math.tan(.5*(wc-wc2));
        Complex c2 = new Complex();
        c2.addMult(4*(b*b*(a*a-1)+1), z);
        c2.add(8*(b*b*(a*a-1)-1));
        c2.mult(z);
        c2.add(4*(b*b*(a*a-1)+1));
        c2.sqrt();
        if ((i & 1) == 0)
            c2.mult(-1);
        c2.addMult(2*a*b, z);
        c2.add(2*a*b);
        Complex c3 = new Complex();
        c3.addMult(2*(b-1), z);
        c3.add(2*(1+b));
        c2.div(c3);
        z.set(c2);
    }
    void getBandStopPole(int i, Complex z) {
        getSPole(i/2, z, Math.PI*.5);
        bilinearXform(z);
        bandStopXform(i, z);
    }
    void getBandStopZero(int i, Complex z) {
        z.set(-1, 0);
        bandStopXform(i, z);
    }
    void bandStopXform(int i, Complex z) {
        double a = Math.cos((wc+wc2)*.5) /
                   Math.cos((wc-wc2)*.5);
        double b = Math.tan(.5*(wc-wc2));
        Complex c2 = new Complex();
        c2.addMult(4*(b*b+a*a-1), z); // z^2 terms
        c2.add(8*(b*b-a*a+1)); // z terms
        c2.mult(z);
        c2.add(4*(a*a+b*b-1));
        c2.sqrt(); // c2 = discrim.
        c2.mult(((i & 1) == 0) ? .5 : -.5);
        c2.add(a);
        c2.addMult(-a, z);
        Complex c3 = new Complex(b+1, 0);
        c3.addMult(b-1, z);
        c2.div(c3);
        z.set(c2);
    }
    void getBandPassZero(int i, Complex c1) {
        if (i >= n)
            c1.set(1);
        else
            c1.set(-1);
    }

    void setupLowPass() {
        wc = auxBars[0].getValue() * pi/1000.;
        n = auxBars[1].getValue();  
    }
    void setupBandPass() {
        double wcmid = auxBars[0].getValue() * pi/1000.;
        double width = auxBars[1].getValue() * pi/1000.;
        wc  = wcmid+width/2;
        wc2 = wcmid-width/2;
        if (wc2 < 0)
            wc2 = 1e-8;
        if (wc >= pi)
            wc = pi-1e-8;
        n = auxBars[2].getValue();
    }
    void getInfoLowPass(String x[]) {
        x[1] = "Cutoff freq: " + getOmegaText(wc);
    }
    void getInfoBandPass(String x[], boolean stop) {
        x[1] = (stop ? "Stopband: " : "Passband: ") +
            getOmegaText(wc2) + " - " + getOmegaText(wc);
    }
}
    
