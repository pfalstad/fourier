package com.falstad.dfilter.client;

import com.google.gwt.user.client.ui.Label;

// filter type; designs and creates Filter objects for a particular type of filter.
public abstract class FilterType {
	Scrollbar auxBars[];
	Label auxLabels[];
	
	static final double pi = Math.PI;
	static final double log10 = DFilterSim.log10;
	
	FilterType() {
		auxBars = DFilterSim.theSim.auxBars;
		auxLabels = DFilterSim.theSim.auxLabels;
	}
    int select() { return 0; }
    void setup() {}
    abstract void getResponse(double w, Complex c);
    int getPoleCount() { return 0; }
    int getZeroCount() { return 0; }
    void getPole(int i, Complex c) { c.set(0); }
    void getZero(int i, Complex c) { c.set(0); }
    abstract Filter genFilter();
    void getInfo(String x[]) { }
    boolean needsWindow() { return false; }
    void setCutoff(double f) { DFilterSim.theSim.auxBars[0].setValue((int) (2000*f)); }
    
    String getOmegaText(double wc) {
        return ((int) (wc*DFilterSim.theSim.sampleRate/(2*pi))) + " Hz";
    }

    double cosh(double x) {
        return .5*(Math.exp(x)+Math.exp(-x));
    }
    double sinh(double x) {
        return .5*(Math.exp(x)-Math.exp(-x));
    }
    double acosh(double x) {
        return Math.log(x+Math.sqrt(x*x-1));
    }

}

