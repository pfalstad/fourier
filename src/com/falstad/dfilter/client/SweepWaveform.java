package com.falstad.dfilter.client;

public class SweepWaveform extends Waveform {
    int ix;
    double omega, nextOmega, t, startOmega;
    int getChannels() { return 1; }
    boolean start() {
        getBuffer();
        ix = 0;
        startOmega = nextOmega = omega = 2*Math.PI*40/sim.sampleRate;
        t = 0;
        return true;
    }
    int getData() {
        int i;
        double nmul = 1;
        double nadd = 0;
        double maxspeed = 1/(.66*sim.sampleRate);
        double minspeed = 1/(sim.sampleRate*16);
        if (sim.logFreqCheckItem.getState())
            nmul = Math.pow(2*pi/startOmega,
                2*(minspeed+(maxspeed-minspeed)*sim.inputBar.getValue()/10000.));
        else
            nadd = (2*pi-startOmega)*
                (minspeed+(maxspeed-minspeed)*sim.inputBar.getValue()/10000.);
        for (i = 0; i != buffer.length; i++) {
            ix++;
            t += omega;
            if (t > 2*pi) {
                t -= 2*pi;
                omega = nextOmega;
                if (nextOmega > pi)
                    omega = nextOmega = startOmega;
            }
            buffer[i] = (short) (Math.sin(t)*32000);
            nextOmega = nextOmega*nmul+nadd;
        }
        return buffer.length;
    }
    String getInputText() { return "Sweep Speed"; }
    boolean needsFrequency() { return false; }
}
