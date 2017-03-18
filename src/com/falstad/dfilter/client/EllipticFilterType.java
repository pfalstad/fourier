package com.falstad.dfilter.client;

public class EllipticFilterType extends PoleFilterType {
    void selectElliptic(int s) {
        auxLabels[s].setText("Passband Ripple");
        auxBars[s].setValue(60);
        auxLabels[s+1].setText("Transition Band Width");
        auxBars[s+1].setValue(100);
    }
    double p0, q;
    double zeros[];
    double K, Kprime;

    double c1[] = new double[100];
    double b1[] = new double[100];
    double a1[] = new double[100];
    double d1[] = new double[100];
    double q1[] = new double[100];
    double z1[] = new double[100];
    double f1[] = new double[100];
    double s1[] = new double[100];
    double p [] = new double[100];
    double zw1[] = new double[100];
    double zf1[] = new double[100];
    double zq1[] = new double[100];
    double rootR[] = new double[100];
    double rootI[] = new double[100];
    int nin;
    int m, n2, em;
    double e;
    
    void setupElliptic(int a) {
        double rp = auxBars[a].getValue()/25.;
        //System.out.println("rp = " + rp);
        double e2 = Math.pow(10, rp*.1)-1;
        //System.out.println("e2 = " + e2 + " e = " + Math.sqrt(e2));
        // xi = 1/k
        double xi = (Math.exp(auxBars[a+1].getValue()/1000.)-1)*5+1;
        // System.out.println("xi " + xi);
        Kprime = ellipticK(Math.sqrt(1-1/(xi*xi)));
        K = ellipticK(1/xi);
        int ni = ((n & 1) == 1) ? 0 : 1;
        int i;
        double f[] = new double[n/2+1];
        zeros = new double[n+1];
        for (i = 1; i <= n/2; i++) {
            double u = (2*i-ni)*K/n;
            double sn = calcSn(u);
            sn *= 2*pi/K;
            f[i] = zeros[i-1] = 1/sn;
            //System.out.println("zero " + i + " " + zeros[i-1]);
        }
        zeros[n/2] = 1e30;
        double fb = 1/(2*pi);
        nin = n % 2;
        n2 = n/2;
        double f1[] = new double[n2+1];
        for (i = 1; i <= n2; i++) {
            double x = f[n2+1-i];
            z1[i] = Math.sqrt(1-1/(x*x));
        }
        double ee = Math.pow(10, .1*rp)-1;
        //System.out.println("ee " + ee);
        e = Math.sqrt(ee);
        double fbb = fb*fb;
        m = nin+2*n2;
        em = 2*(m/2);
        double tp = 2*pi;
        calcfz();
        calcqz();
        if (m > em)
            c1[2*m] = 0;
        for (i = 0; i <= 2*m; i += 2)
            a1[m-i/2] = c1[i] + d1[i];
        double a0 = factorFinder(m);
        int r = 0;
        while (r < em/2) {
            r++;
            p[r] /= 10;
            q1[r] /= 100;
            double d = 1+p[r]+q1[r];
            b1[r] = (1+p[r]/2)*fbb/d;
            zf1[r] = fb/Math.pow(d, .25);
            zq1[r] = 1/Math.sqrt(Math.abs(2*(1-b1[r]/(zf1[r]*zf1[r]))));
            zw1[r] = tp*zf1[r];
            rootR[r] = -.5*zw1[r]/zq1[r];
            rootR[r+em/2] = rootR[r];
            rootI[r] = .5*Math.sqrt(Math.abs(zw1[r]*zw1[r]/(zq1[r]*zq1[r]) - 4*zw1[r]*zw1[r]));
            rootI[r+em/2] = -rootI[r];
            //System.out.println(r + " " + rootR[r] + " " + rootI[r]);
        }
        if (a0 != 0) {
            rootR[r+1+em/2] = -Math.sqrt(fbb/(.1*a0-1))*tp;
            rootI[r+1+em/2] = 0;
        }
    }

    void calcfz() {
        // calculate f(z)
        int i = 1;
        if (nin == 1)
            s1[i++] = 1;
        for (; i <= nin+n2; i++)
            s1[i] = s1[i+n2] = z1[i-nin];
        genProductPoly(nin+2*n2);
        for (i = 0; i <= em; i += 2)
            a1[i] = e*b1[i];
        for (i = 0; i <= 2*em; i += 2)
            calcfz2(i);
    }
    
    // generate the product of (z+s1[i]) for i = 1 .. sn and store it in b1[]
    // (i.e. f[z] = b1[0] + b1[1] z + b1[2] z^2 + ... b1[sn] z^sn)
    void genProductPoly(int sn) {
        b1[0] = s1[1];
        b1[1] = 1;
        int i, j;
        for (j = 2; j <= sn; j++) {
            a1[0] = s1[j]*b1[0];
            for (i = 1; i <= j-1; i++)
                a1[i] = b1[i-1]+s1[j]*b1[i];
            for (i = 0; i != j; i++)
                b1[i] = a1[i];
            b1[j] = 1;
        }
    }

    // determine f(z)^2
    void calcfz2(int i) {
        int ji = 0;
        int jf = 0;
        if (i < em+2) {
            ji = 0;
            jf = i;
        }
        if (i > em) {
            ji = i-em;
            jf = em;
        }
        c1[i] = 0;
        int j;
        for (j = ji; j <= jf; j += 2)
            c1[i] += a1[j]*(a1[i-j]*Math.pow(10, m-i/2));
    }

    // determine q(z)
    void calcqz() {
        int i;
        for (i = 1; i <= nin; i++)
            s1[i] = -10;
        for (; i <= nin+n2; i++)
            s1[i] = -10*z1[i-nin]*z1[i-nin];
        for (; i <= nin+2*n2; i++)
            s1[i] = s1[i-n2];
        genProductPoly(m);
        int dd = ((nin & 1) == 1) ? -1 : 1;
        for (i = 0; i <= 2*m; i += 2)
            d1[i] = dd*b1[i/2];
    }

    double factorFinder(int t) {
        int i;
        double a = 0;
        for (i = 1; i <= t; i++)
            a1[i] /= a1[0];
        a1[0] = b1[0] = c1[0] = 1;
        int i1 = 0;
        while (true) {
            if (t <= 2)
                break;
            double p0 = 0, q0 = 0;
            i1++;
            while (true) {
                b1[1] = a1[1] - p0;
                c1[1] = b1[1] - p0;
                for (i = 2; i <= t; i++)
                    b1[i] = a1[i]-p0*b1[i-1]-q0*b1[i-2];
                for (i = 2; i < t; i++)
                    c1[i] = b1[i]-p0*c1[i-1]-q0*c1[i-2];
                int x1 = t-1;
                int x2 = t-2;
                int x3 = t-3;
                double x4 = c1[x2]*c1[x2]+c1[x3]*(b1[x1]-c1[x1]);
                if (x4 == 0)
                    x4 = 1e-3;
                double ddp = (b1[x1]*c1[x2]-b1[t]*c1[x3])/x4;
                p0 += ddp;
                double dq = (b1[t]*c1[x2]-b1[x1]*(c1[x1]-b1[x1]))/x4;
                q0 += dq;
                if (Math.abs(ddp+dq) < 1e-6)
                    break;
            }
            p[i1] = p0;
            q1[i1] = q0;
            a1[1] = a1[1]-p0;
            t -= 2;
            for (i = 2; i <= t; i++)
                a1[i] -= p0*a1[i-1]+q0*a1[i-2];
            if (t <= 2)
                break;
        }
        if (t == 2) {
            i1++;
            p[i1] = a1[1];
            q1[i1] = a1[2];
        }
        if (t == 1)
            a = -a1[1];
        return a;
    }
    
    double calcSn(double u) {
        double sn = 0;
        int j;
        // q = modular constant
        double q = Math.exp(-pi*Kprime/K);
        double v = pi*.5*u/K;
        for (j = 0; ; j++) {
            double w = Math.pow(q, j+.5);
            sn += w*Math.sin((2*j+1)*v)/(1-w*w);
            if (w < 1e-7)
                break;
        }
        return sn;
    }

    double ellipticK(double k) {
        double a[] = new double[50];
        double theta[] = new double[50];
        a[0] = Math.atan(k/Math.sqrt(1-k*k));
        theta[0] = pi*.5;
        int i = 0;
        while (true) {
            double x = 2/(1+Math.sin(a[i]))-1;
            double y = Math.sin(a[i])*Math.sin(theta[i]);
            a[i+1] = Math.atan(Math.sqrt(1-x*x)/x);
            theta[i+1] = .5*(theta[i]+Math.atan(y/Math.sqrt(1-y*y)));
            double e = 1-a[i+1]*2/pi;
            i++;
            if (e < 1e-7)
                break;
            if (i == 49)
                break;
        }
        int j;
        double p = 1;
        for (j = 1; j <= i; j++)
            p *= 1+Math.cos(a[j]);
        double x = pi*.25 + theta[i]/2;
        return Math.log(Math.tan(x))*p;
    }
    
    void getSPole(int i, Complex c1, double wc) {
        double tanwc = Math.tan(wc*.5);
        c1.set(rootR[i+1]*tanwc, rootI[i+1]*tanwc);
    }
    void getEllipticZero(int i, Complex c1, double wc) {
        double tanwc = Math.tan(wc*.5);
        c1.set(0, zeros[i/2]*tanwc);
        if ((i & 1) == 1)
            c1.im = -c1.im;
        bilinearXform(c1);
    }
    void getInfoElliptic(String x[]) {
    }           
    int getPoleCount() { return n; }
    int getZeroCount() { return n; }

}
