package com.falstad.dfilter.client;

public class CustomIIRFilter extends IIRFilterType {
    int npoles, nzeros;
    Complex customPoles[], customZeros[];
    
    CustomIIRFilter() {
    	customPoles = DFilterSim.theSim.customPoles;
    	customZeros = DFilterSim.theSim.customZeros;
    }
    int select() {
        auxLabels[0].setText("# of Pole Pairs");
        auxBars[0].setMaximum(10);
        auxBars[0].setValue(DFilterSim.theSim.lastPoleCount/2);
        return 1;
    }
    void setup() {
        npoles = nzeros = auxBars[0].getValue()*2;
    }
    void getPole(int i, Complex c1) {
        c1.set(customPoles[i]);
    }
    int getPoleCount() { return npoles; }
    void getZero(int i, Complex c1) {
        c1.set(customZeros[i]);
    }
    int getZeroCount() { return nzeros; }
    void getInfo(String x[]) {
        x[0] = "Custom IIR";
        x[1] = npoles + " poles and zeros";
    }
    void editPoleZero(Complex c) {
        if (c.mag > 1.1)
            return;
        int selectedPole = DFilterSim.theSim.selectedPole;
        int selectedZero = DFilterSim.theSim.selectedZero;
        if (selectedPole != -1) {
            customPoles[selectedPole].set(c);
            customPoles[selectedPole ^ 1].set(c.re, -c.im);
        }
        if (selectedZero != -1) {
            customZeros[selectedZero].set(c);
            customZeros[selectedZero ^ 1].set(c.re, -c.im);
        }
    }
}

