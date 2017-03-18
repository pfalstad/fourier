package com.falstad.dfilter.client;

public class SquareWaveform extends Waveform {
    int ix;
    double omega;
    short smbuf[];
    int getChannels() { return 1; }
    boolean start() {
        getBuffer();
        ix = 0;
        smbuf = new short[1];
        return true;
    }
    int getData() {
        int i;
        int period = (int) (2*Math.PI/sim.inputW);
        if (period != smbuf.length) {
            smbuf = new short[period];
            for (i = 0; i != period/2; i++)
                smbuf[i] = 32000;
            if ((period & 1) > 0)
                smbuf[i++] = 0;
            for (; i != period; i++)
                smbuf[i] = -32000;
        }
        for (i = 0; i != buffer.length; i++, ix++) {
            if (ix >= period)
                ix = 0;
            buffer[i] = smbuf[ix];
        }
        return buffer.length;
    }
}
