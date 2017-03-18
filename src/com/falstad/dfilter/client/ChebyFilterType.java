package com.falstad.dfilter.client;

public class ChebyFilterType extends PoleFilterType {
    double epsilon;
    int sign;
    void selectCheby(int s) {
        auxLabels[s].setText("Passband Ripple");
        auxBars[s].setValue(60);
    }
    void setupCheby(int a) {
        int val = auxBars[a].getValue();
        double ripdb = 0;
        if (val < 300)
            ripdb = 5*val/300.;
        else
            ripdb = 5+45*(val-300)/700.;
        double ripval = Math.exp(-ripdb*.1*DFilterSim.log10);
        epsilon = Math.sqrt(1/ripval-1);
    }
    void getSPole(int i, Complex c1, double wc) {
        double alpha = 1/epsilon + Math.sqrt(1+1/(epsilon*epsilon));
        double a = .5*(Math.pow(alpha, 1./n) - Math.pow(alpha, -1./n));
        double b = .5*(Math.pow(alpha, 1./n) + Math.pow(alpha, -1./n));
        double theta = pi/2 + (2*i+1)*pi/(2*n);
        if (sign == -1)
            wc = pi-wc;
        c1.setMagPhase(Math.tan(wc*.5), theta);
        c1.re *= a;
        c1.im *= b;
        c1.setMagPhase();
    }
    void getInfoCheby(String x[]) {
        x[2] = "Ripple: " +
            DFilterSim.theSim.showFormat.format(-10*Math.log(1/(1+epsilon*epsilon))/
                              DFilterSim.log10) + " dB";
    }           
}
