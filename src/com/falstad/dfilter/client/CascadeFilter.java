package com.falstad.dfilter.client;

// IIR filter that uses state for efficiency in processing
public class CascadeFilter extends Filter {
    CascadeFilter(int s) {
        size = s;
        a1  = new double[s];
        a2  = new double[s];
        b0  = new double[s];
        b1  = new double[s];
        b2  = new double[s];
        int i;
        for (i = 0; i != s; i++)
            b0[i] = 1;
    }
    
    double a1[], a2[], b0[], b1[], b2[];
    int size;

    double [] createState() {
        return new double[size*3];
    }
    
    void setAStage(double x1, double x2) {
        int i;
        for (i = 0; i != size; i++) {
            if (a1[i] == 0 && a2[i] == 0) {
                a1[i] = x1;
                a2[i] = x2;
                return;
            }
            if (a2[i] == 0 && x2 == 0) {
                a2[i] = -a1[i] * x1;
                a1[i] += x1;
                //System.out.println("setastate " + i + " " + a1[i] + " " + a2[i]);
                return;
            }
        }
        System.out.println("setAStage failed");
    }
    
    void setBStage(double x0, double x1, double x2) {
        //System.out.println("setting b " + i + " "+ x0 + " "+ x1 + " "+ x2 + " " + size);
        int i;
        for (i = 0; i != size; i++) {
            if (b1[i] == 0 && b2[i] == 0) {
                b0[i] = x0;
                b1[i] = x1;
                b2[i] = x2;
                //System.out.println("setbstage " + i + " " + x0 + " " + x1 + " " + x2);
                return;
            }
            if (b2[i] == 0 && x2 == 0) {
                // (b0 + z b1)(x0 + z x1) = (b0 x0 + (b1 x0+b0 x1) z + b1 x1 z^2)
                b2[i] = b1[i]*x1;
                b1[i] = b1[i]*x0 + b0[i]*x1;
                b0[i] *= x0;
                //System.out.println("setbstage " + i + " " + b0[i]+" "+b1[i] + " " + b2[i]);
                return;
            }
        }
        System.out.println("setBStage failed");
    }
    
    void run(double inBuf[], double outBuf[], int bp, int mask, int count, double state[]) {
        int fi2, i20;
        int i2, j;
        double in = 0, d2, d1, d0;
        for (i2 = 0; i2 != count; i2++) {
            fi2 = bp+i2;
            i20 = fi2 & mask;
            in = inBuf[i20];
            for (j = 0; j != size; j++) {
                int j3 = j*3;
                d2 = state[j3+2] = state[j3+1];
                d1 = state[j3+1] = state[j3];
                d0 = state[j3] = in + a1[j]*d1 + a2[j]*d2;
                in = b0[j]*d0 + b1[j]*d1 + b2[j]*d2;
            }
            outBuf[i20] = in;
        }
    }
    
    Complex cm2, cm1, top, bottom;
    void evalTransfer(Complex c) {
        if (cm1 == null) {
            cm1 = new Complex();
            cm2 = new Complex();
            top = new Complex();
            bottom = new Complex();
        }
        int i;
        cm1.set(c);
        cm1.recip();
        cm2.set(cm1);
        cm2.square();
        c.set(1);
        for (i = 0; i != size; i++) {
            top.set    (b0[i]);
            top.addMult(b1[i], cm1);
            top.addMult(b2[i], cm2);
            bottom.set    (1);
            bottom.addMult(-a1[i], cm1);
            bottom.addMult(-a2[i], cm2);
            c.mult(top);
            c.div(bottom);
        }
    }
    
    int getImpulseOffset() { return 0; }
    int getStepOffset() { return 0; }
    int getLength() { return 1; }

}
