package com.falstad.fourier.client;

public class NoiseWaveform extends Waveform {
    boolean start() {
        getBuffer();
        return true;
    }
    int getData() {
        int i;
        for (i = 0; i != buffer.length; i++)
            buffer[i] = (short) FourierSim.theSim.random.nextInt();
        return buffer.length;
    }
    String getInputText() { return null; }
    boolean needsFrequency() { return false; }
}
