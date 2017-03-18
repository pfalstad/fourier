package com.falstad.dfilter.client;

public class InvChebyFilterType extends ChebyFilterType {
    double scale;
    void selectCheby(int s) {
        auxLabels[s].setText("Stopband Attenuation");
        auxBars[s].setValue(600);
    }
    void setupCheby(int a) {
        epsilon = Math.exp(-auxBars[a].getValue()/120.);
        scale = cosh(acosh(1/epsilon)/n);
    }
    void getSPole(int i, Complex c1, double wc) {
        wc = pi-wc;
        super.getSPole(i, c1, wc);
        c1.recip();
        c1.mult(scale);
    }
    void getChebyZero(int i, Complex c1, double wc) {
        double bk = 1/Math.cos((2*i+1)*pi/(2*n))*scale;
        double a = Math.sin(pi/4-wc/2)/Math.sin(pi/4+wc/2);
        c1.set(1+a, bk*(1-a));
        Complex c2 = new Complex(1+a, bk*(a-1));
        c1.div(c2);
    }
    void getInfoCheby(String x[]) {
        x[2] = "Stopband attenuation: " +
            DFilterSim.theSim.showFormat.format(-10*Math.log(1+1/(epsilon*epsilon))/
                              DFilterSim.log10) + " dB";
    }           
    int getPoleCount() { return n; }
    int getZeroCount() { return n; }

}
