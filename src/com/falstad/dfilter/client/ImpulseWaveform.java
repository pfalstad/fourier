package com.falstad.dfilter.client;

public     class ImpulseWaveform extends Waveform {
    int ix;
    int getChannels() { return 1; }
    boolean start() {
        getBuffer();
        ix = 0;
        return true;
    }
    int getData() {
        int i;
        int ww = DFilterSim.theSim.inputBar.getValue()/510+1;
        int period = 10000/ww;
        for (i = 0; i != buffer.length; i++) {
            short q = 0;
            if (ix % period == 0)
                q = 32767;
            ix++;
            buffer[i] = q;
        }
        return buffer.length;
    }
    String getInputText() { return "Impulse Frequency"; }
    boolean needsFrequency() { return false; }
}
