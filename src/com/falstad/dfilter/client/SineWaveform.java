package com.falstad.dfilter.client;

public class SineWaveform extends Waveform {
    int ix;
    int getChannels() { return 1; }
    boolean start() {
        getBuffer();
        ix = 0;
        return true;
    }
    int getData() {
        int i;
        for (i = 0; i != buffer.length; i++) {
            ix++;
            buffer[i] = (short) (Math.sin(ix*sim.inputW)*32000);
        }
        return buffer.length;
    }
}
