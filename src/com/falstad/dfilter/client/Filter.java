package com.falstad.dfilter.client;

// Filter class, created by a subclass of FilterType.
// A Filter object doesn't change once created.  It is concerned with the details of executing a filter,
// not filter design.
public abstract class Filter {
    abstract void run(double inBuf[], double outBuf[], int bp, int mask, int count, double x[]);
    abstract void evalTransfer(Complex c);
    abstract int getImpulseOffset();
    abstract int getStepOffset();
    abstract int getLength();
    boolean useConvolve() { return false; }
    
    double [] getImpulseResponse(int offset) {
        int pts = 1000;
        double inbuf[] = new double[offset+pts];
        double outbuf[] = new double[offset+pts];
        inbuf[offset] = 1;
        double state[] = createState();
        run(inbuf, outbuf, offset, ~0, pts, state);
        return outbuf;
    }
    
    double [] getStepResponse(int offset) {
        int pts = 1000;
        double inbuf[] = new double[offset+pts];
        double outbuf[] = new double[offset+pts];
        int i;
        for (i = offset; i != inbuf.length; i++)
            inbuf[i] = 1;
        double state[] = createState();
        run(inbuf, outbuf, offset, ~0, pts, state);
        return outbuf;
    }
    
    int countPoints(double buf[], int offset) {
        int len = buf.length;
        double max = 0;
        int i;
        int result = 0;
        double last = 123;
        for (i = offset; i < len; i++) {
            double qa = Math.abs(buf[i]);
            if (qa > max)     max = qa;
            if (Math.abs(qa-last) > max*.003) {
                result = i-offset+1;
                //System.out.println(qa + " " + last + " " + i + " " + max);
            }
            last = qa;
        }
        return result;
    }

    int getImpulseLen(int offset, double buf[]) {
        return countPoints(buf, offset);
    }
    
    int getStepLen(int offset, double buf[]) {
        return countPoints(buf, offset);
    }
    
    double [] createState() { return null; }

}
