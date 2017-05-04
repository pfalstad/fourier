package com.falstad.fourier.client;

public abstract class Waveform {
    short buffer[];
    FourierSim sim;
    static final double pi = Math.PI;

    Waveform() {
    	sim = FourierSim.theSim;
    }
    boolean start() { return true; }
    abstract int getData();
    int getChannels() { return 2; }
    void getBuffer() {
//        buffer = new short[FourierSim.getPower2(sim.sampleRate/12)*getChannels()];
    	buffer = new short[2048*getChannels()];
    }
    String getInputText() { return "Input Frequency"; }
    boolean needsFrequency() { return true; }
}

