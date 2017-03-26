package com.falstad.dfilter.client;

// finite impulse response filter creator
abstract public class FIRFilterType extends FilterType {
    double response[];
    void getResponse(double w, Complex c) {
        if (response == null) {
            c.set(0);
            return;
        }
        int off = (int) (response.length*w/(2*pi));
        off &= ~1;
        if (off < 0)
            off = 0;
        if (off >= response.length)
            off = response.length-2;
        c.set(response[off], response[off+1]);
    }
    
    double getWindow(int i, int n) {
        if (n == 1)
            return 1;
        double x = 2*pi*i/(n-1);
        double n2 = n/2; // int
        switch (DFilterSim.theSim.windowChooser.getSelectedIndex()) {
        case 0: return 1; // rect
        case 1: return .54 - .46*Math.cos(x); // hamming
        case 2: return .5  -  .5*Math.cos(x); // hann
        case 3: return .42 -  .5*Math.cos(x) + .08*Math.cos(2*x); // blackman
        case 4: {
            double kaiserAlphaPi = DFilterSim.theSim.kaiserBar.getValue()*pi/120.;
            double q = (2*i/(double) n)-1;
            return bessi0(kaiserAlphaPi*Math.sqrt(1-q*q));
        }
        case 5: return (i < n2) ? i/n2 : 2-i/n2; // bartlett
        case 6: { double xt = (i-n2)/n2; return 1-xt*xt; } // welch
        }
        return 0;
    }
    void setResponse(DirectFilter f) {
        response = new double[8192];
        int i;
        if (f.nList.length != f.aList.length) {
            f.nList = new int[f.aList.length];
            for (i = 0; i != f.aList.length; i++)
                f.nList[i] = i;
        }
        for (i = 0; i != f.aList.length; i++)
            response[f.nList[i]*2] = f.aList[i];
        new FFT(response.length/2).transform(response, false);
        double maxresp = 0;
        int j;
        for (j = 0; j != response.length; j += 2) {
            double r2 = response[j]*response[j] + response[j+1]*response[j+1];
            if (maxresp < r2)
                maxresp = r2;
        }
        // normalize response
        maxresp = Math.sqrt(maxresp);
        for (j = 0; j != response.length; j++)
            response[j] /= maxresp;
        for (j = 0; j != f.aList.length; j++)
            f.aList[j] /= maxresp;
    }
    
    double bessi0(double x) {
        double ax,ans;
        double y;
        
        if ((ax=Math.abs(x)) < 3.75) {
            y=x/3.75;
            y*=y;
            ans=1.0+y*(3.5156229+y*(3.0899424+y*(1.2067492
                +y*(0.2659732+y*(0.360768e-1+y*0.45813e-2)))));
        } else {
            y=3.75/ax;
            ans=(Math.exp(ax)/Math.sqrt(ax))*(0.39894228+y*(0.1328592e-1
              +y*(0.225319e-2+y*(-0.157565e-2+y*(0.916281e-2
                                                 +y*(-0.2057706e-1+y*(0.2635537e-1+y*(-0.1647633e-1
                                                                                      +y*0.392377e-2))))))));
        }
        return ans;
    }


}
