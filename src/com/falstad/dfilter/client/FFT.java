package com.falstad.dfilter.client;

public class FFT {
    double wtabf[];
    double wtabi[];
    int size;
    FFT(int sz) {
        size = sz;
        if ((size & (size-1)) != 0)
            System.out.println("size must be power of two!");
        calcWTable();
    }
    
    void calcWTable() {
        // calculate table of powers of w
        wtabf = new double[size];
        wtabi = new double[size];
        int i;
        for (i = 0; i != size; i += 2) {
            double pi = 3.1415926535;
            double th = pi*i/size;
            wtabf[i  ] = Math.cos(th);
            wtabf[i+1] = Math.sin(th);
            wtabi[i  ] = wtabf[i];
            wtabi[i+1] = -wtabf[i+1];
        }
    }

    void transform(double data[], boolean inv) {
        int i;
        int j = 0;
        int size2 = size*2;

        if ((size & (size-1)) != 0)
            System.out.println("size must be power of two!");
    
        // bit-reversal
        double q;
        int bit;
        for (i = 0; i != size2; i += 2) {
            if (i > j) {
                q = data[i]; data[i] = data[j]; data[j] = q;
                q = data[i+1]; data[i+1] = data[j+1]; data[j+1] = q;
            }
            // increment j by one, from the left side (bit-reversed)
            bit = size;
            while ((bit & j) != 0) {
                j &= ~bit;
                bit >>= 1;
            }
            j |= bit;
        }

        // amount to skip through w table
        int tabskip = size << 1;
        double wtab[] = (inv) ? wtabi : wtabf;
    
        int skip1, skip2, ix, j2;
        double wr, wi, d1r, d1i, d2r, d2i, d2wr, d2wi;
    
        // unroll the first iteration of the main loop
        for (i = 0; i != size2; i += 4) {
            d1r = data[i];
            d1i = data[i+1];
            d2r = data[i+2];
            d2i = data[i+3];
            data[i  ] = d1r+d2r;
            data[i+1] = d1i+d2i;
            data[i+2] = d1r-d2r;
            data[i+3] = d1i-d2i;
        }
        tabskip >>= 1;
    
        // unroll the second iteration of the main loop
        int imult = (inv) ? -1 : 1;
        for (i = 0; i != size2; i += 8) {
            d1r = data[i];
            d1i = data[i+1];
            d2r = data[i+4];
            d2i = data[i+5];
            data[i  ] = d1r+d2r;
            data[i+1] = d1i+d2i;
            data[i+4] = d1r-d2r;
            data[i+5] = d1i-d2i;
            d1r = data[i+2];
            d1i = data[i+3];
            d2r = data[i+6]*imult;
            d2i = data[i+7]*imult;
            data[i+2] = d1r-d2i;
            data[i+3] = d1i+d2r;
            data[i+6] = d1r+d2i;
            data[i+7] = d1i-d2r;
        }
        tabskip >>= 1;
    
        for (skip1 = 16; skip1 <= size2; skip1 <<= 1) {
            // skip2 = length of subarrays we are combining
            // skip1 = length of subarray after combination
            skip2 = skip1 >> 1;
            tabskip >>= 1;
            for (i = 0; i != 1000; i++);
            // for each subarray
            for (i = 0; i < size2; i += skip1) {
                ix = 0;
                // for each pair of complex numbers (one in each subarray)
                for (j = i; j != i+skip2; j += 2, ix += tabskip) {
                    wr = wtab[ix];
                    wi = wtab[ix+1];
                    d1r = data[j];
                    d1i = data[j+1];
                    j2 = j+skip2;
                    d2r = data[j2];
                    d2i = data[j2+1];
                    d2wr = d2r*wr - d2i*wi;
                    d2wi = d2r*wi + d2i*wr;
                    data[j]    = d1r+d2wr;
                    data[j+1]  = d1i+d2wi;
                    data[j2  ] = d1r-d2wr;
                    data[j2+1] = d1i-d2wi;
                }
            }
        }
    }
}
