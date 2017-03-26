package com.falstad.dfilter.client;

// infinite impulse response filter (using feedback)
public abstract class IIRFilterType extends FilterType {
    double response[];
    void getResponse(double w, Complex c) {
        if (response == null) {
            c.set(0);
            return;
        }
        int off = (int) (response.length*w/Math.PI);
        off &= ~1;
        if (off < 0)
            off = 0;
        if (off >= response.length)
            off = response.length-1;
        c.set(response[off], response[off+1]);
    }
    void setResponse(DirectFilter f) {
        response = new double[8192];
        Complex czn = new Complex();
        Complex top = new Complex();
        Complex bottom = new Complex();
        int i, j;
        double maxresp = 0;
        f.bList[0] = 1;

        if (f.aList.length != f.bList.length)
            System.out.println("length mismatch " + f.aList.length +
                               " " + f.bList.length);
        // use the coefficients to multiply out the transfer function for
        // various values of z
        for (j = 0; j != response.length; j += 2) {
            top.set(0);
            bottom.set(0);
            int czni = 0;
            for (i = 0; i != f.aList.length; i++) {
                czn.setMagPhase(1, -Math.PI*j*f.nList[i]/response.length);
                top   .addMult(f.aList[i], czn);
                bottom.addMult(f.bList[i], czn);
            }
            top.div(bottom);
            if (top.mag > maxresp)
                maxresp = top.mag;
            response[j] = top.re;
            response[j+1] = top.im;
        }
        // normalize response
        for (j = 0; j != response.length; j++)
            response[j] /= maxresp;
        for (j = 0; j != f.aList.length; j++)
            f.aList[j] /= maxresp;
        //System.out.println(f.aList.length + " " + f.bList.length + " XX");
    }
    
    void setResponse(CascadeFilter f) {
        // it's good to have this bigger for normalization
        response = new double[4096];
        Complex czn1 = new Complex();
        Complex czn2 = new Complex();
        Complex ch = new Complex();
        Complex ct = new Complex();
        Complex cb = new Complex();
        Complex cbot = new Complex();
        int i, j;
        double maxresp = 0;

        // use the coefficients to multiply out the transfer function for
        // various values of z
        //System.out.println("sr1");
        for (j = 0; j != response.length; j += 2) {
            ch.set(1);
            cbot.set(1);
            int czni = 0;
            czn1.setMagPhase(1, -Math.PI*j/response.length);
            czn2.setMagPhase(1, -Math.PI*j*2/response.length);
            for (i = 0; i != f.size; i++) {
                ct.set(f.b0[i]);
                cb.set(1);
                ct.addMult(f.b1[i] , czn1);
                cb.addMult(-f.a1[i], czn1);
                ct.addMult(f.b2[i] , czn2);
                cb.addMult(-f.a2[i], czn2);
                ch.mult(ct);
                cbot.mult(cb);
            }
            ch.div(cbot);
            if (ch.mag > maxresp)
                maxresp = ch.mag;
            response[j] = ch.re;
            response[j+1] = ch.im;
        }
        //System.out.println("sr2");
        // normalize response
        for (j = 0; j != response.length; j++)
            response[j] /= maxresp;
        f.b0[0] /= maxresp;
        f.b1[0] /= maxresp;
        f.b2[0] /= maxresp;
        
        //System.out.println(f.aList.length + " " + f.bList.length + " XX");
    }
    
    // create cascade filter using poles/zeroes
    Filter genFilter() {
        int n = getPoleCount();
        CascadeFilter f = new CascadeFilter((n+1)/2);
        int i;
        Complex c1 = new Complex();
        int s;
        for (i = s = 0; i != n; i++) {
            getPole(i, c1);
            //System.out.println("pole " + i + " " + c1.re + " " + c1.im);
            if (Math.abs(c1.im) < 1e-6)
                c1.im = 0;
            if (c1.im < 0)
                continue;
            if (c1.im == 0) {
                double cc0 = -c1.re;
                f.setAStage(-cc0, 0);
                //System.out.println("real pole " + i + " " + c1.re + " " + c1.im);
            } else {
                double cc0 = -2*c1.re;
                double cd0 = c1.magSquared();
                f.setAStage(-cc0, -cd0);
            }
        }
        n = getZeroCount();
        for (i = s = 0; i != n; i++) {
            getZero(i, c1);
            //System.out.println("zero " + i + " " + c1.re + " " + c1.im);
            if (Math.abs(c1.im) < 1e-6)
                c1.im = 0;
            if (c1.im < 0)
                continue;
            if (c1.im == 0)
                f.setBStage(1, -c1.re, 0);
            else {
                double cc0 = -2*c1.re;
                double cd0 = c1.magSquared();
                f.setBStage(1, cc0, cd0);
            }
        }
        setResponse(f);
        return f;
    }
}

