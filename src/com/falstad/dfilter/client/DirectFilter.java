package com.falstad.dfilter.client;

// a filter that's directly evaluated at each point, not using state.  This could be FIR or IIR
 public class DirectFilter extends Filter {
        double aList[]; // feedforward coefficients
        double bList[]; // feedback coefficients
        int nList[];    // offsets (typically 0, 1, 2, ..., n-1)
        DirectFilter() {
            aList = new double[] { 1 };
            bList = null;
            nList = new int[] { 0 };
        }
        int getLength() { return aList.length; }
        
        // use FFT convolution?
        boolean useConvolve() { return bList == null && aList.length > 25; }
        
        void dump() {
            System.out.print("a "); dump(aList);
            if (bList != null) {
                System.out.print("b "); dump(bList);
            }
        }
        void dump(double x[]) {
            int i;
            for (i = 0; i != x.length; i++) System.out.print(x[i] + " ");
            System.out.println("");
        }
        Complex czn, top, bottom;
        void evalTransfer(Complex c) {
            if (czn == null) {
                czn = new Complex();
                top = new Complex();
                bottom = new Complex();
            }
            int i, j;
            czn.set(1);
            top.set(0);
            bottom.set(0);
            int n = 0;
            for (i = 0; i != aList.length; i++) {
                int n1 = nList[i];
                while (n < n1) {
                    if (n+3 < n1) {
                        czn.set(c);
                        czn.pow(-n1);
                        n = n1;
                        break;
                    }
                    czn.div(c);
                    n++;
                }
                top   .addMult(aList[i], czn);
                if (bList != null)
                    bottom.addMult(bList[i], czn);
            }
            if (bList != null)
                top.div(bottom);
            c.set(top);
        }
        
        void run(double inBuf[], double outBuf[], int bp, int mask, int count, double state[]) {
            int j;
            int fi2 = bp, i20;
            double q = 0;
            
            int i2;
            for (i2 = 0; i2 != count; i2++) {
                fi2 = bp+i2;
                i20 = fi2 & mask;
                
                q = inBuf[i20]*aList[0];
                if (bList == null) {
                    for (j = 1; j < aList.length; j++) {
                        int ji = (fi2-nList[j]) & mask;
                        q += inBuf[ji]*aList[j];
                    }
                } else {
                    for (j = 1; j < aList.length; j++) {
                        int ji = (fi2-nList[j]) & mask;
                        q += inBuf[ji]*aList[j] -
                            outBuf[ji]*bList[j];
                    }
                }
                outBuf[i20] = q;
            }
        }
        
        boolean isSimpleAList() {
            if (bList != null)
                return false;
            return nList[nList.length-1] == nList.length-1;
        }
        
        int getImpulseOffset() {
            if (isSimpleAList())
                return 0;
            return getStepOffset();
        }
        
        int getStepOffset() {
            int i;
            int offset = 0;
            for (i = 0; i != aList.length; i++)
                if (nList[i] > offset)
                    offset = nList[i];
            return offset;
        }
        
        double [] getImpulseResponse(int offset) {
            if (isSimpleAList())
                return aList;
            return super.getImpulseResponse(offset);
        }
        
        int getImpulseLen(int offset, double buf[]) {
            if (isSimpleAList())
                return aList.length;
            return countPoints(buf, offset);
        }
 }
 
