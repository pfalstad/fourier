package com.falstad.dfilter.client;

public class Complex {
    public double re, im, mag, phase;
    Complex() { re = im = mag = phase = 0; }
    Complex(double r, double i) { set(r, i); }
    Complex(Complex c) { set(c.re, c.im); }
    double magSquared() { return mag*mag; }
    void set(double aa, double bb) {
        re = aa; im = bb;
        setMagPhase();
    }
    void set(double aa) {
        re = aa; im = 0;
        setMagPhase();
    }
    void set(Complex c) {
        re = c.re;
        im = c.im;
        mag = c.mag;
        phase = c.phase;
    }
    void add(double r) {
        re += r;
        setMagPhase();
    }
    void add(double r, double i) {
        re += r; im += i;
        setMagPhase();
    }
    void add(Complex c) {
        re += c.re;
        im += c.im;
        setMagPhase();
    }
    void addMult(double x, Complex z) {
        re += z.re*x;
        im += z.im*x;
        setMagPhase();
    }
    void square() {
        set(re*re-im*im, 2*re*im);
    }
    void sqrt() {
        setMagPhase(Math.sqrt(mag), phase*.5);
    }
    void mult(double c, double d) {
        set(re*c-im*d, re*d+im*c);
    }
    void mult(double c) {
        re *= c; im *= c;
        mag *= c;
    }
    void mult(Complex c) {
        mult(c.re, c.im);
    }
    void setMagPhase() {
        mag = Math.sqrt(re*re+im*im);
        phase = Math.atan2(im, re);
    }
    void setMagPhase(double m, double ph) {
        mag = m;
        phase = ph;
        re = m*Math.cos(ph);
        im = m*Math.sin(ph);
    }
    void recip() {
        double n = re*re+im*im;
        set(re/n, -im/n);
    }
    void div(Complex c) {
        double n = c.re*c.re+c.im*c.im;
        mult(c.re/n, -c.im/n);
    }
    void rotate(double a) {
        setMagPhase(mag, (phase+a) % (2*Math.PI));
    }
    void conjugate() {
        im = -im;
        phase = -phase;
    }
    void pow(double p) {
        double arg = java.lang.Math.atan2(im, re);
        phase *= p;
        double abs = java.lang.Math.pow(re*re+im*im, p*.5);
        setMagPhase(abs, phase);
    }

}
