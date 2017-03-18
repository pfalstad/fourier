package com.falstad.dfilter.client;

import java.util.Random;

public class PeriodicNoiseWaveform extends Waveform {
    short smbuf[];
    int ix;
    int getChannels() { return 1; }
    boolean start() {
        getBuffer();
        smbuf = new short[1];
        ix = 0;
        return true;
    }
    int getData() {
        int period = (int) (2*pi/sim.inputW);
        if (period != smbuf.length) {
            smbuf = new short[period];
            int i;
            Random random = DFilterSim.theSim.random;
            for (i = 0; i != period; i++)
                smbuf[i] = (short) random.nextInt();
        }
        int i;
        for (i = 0; i != buffer.length; i++, ix++) {
            if (ix >= period)
                ix = 0;
            buffer[i] = smbuf[ix];
        }
        return buffer.length;
    }
}
