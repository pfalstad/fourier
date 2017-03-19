package com.falstad.dfilter.client;

public abstract class Waveform {
    short buffer[];
    DFilterSim sim;
    static final double pi = Math.PI;

    Waveform() {
    	sim = DFilterSim.theSim;
    }
    boolean start() { return true; }
    abstract int getData();
    int getChannels() { return 2; }
    void getBuffer() {
//        buffer = new short[DFilterSim.getPower2(sim.sampleRate/12)*getChannels()];
    	buffer = new short[2048*getChannels()];
    }
    String getInputText() { return "Input Frequency"; }
    boolean needsFrequency() { return true; }
}

