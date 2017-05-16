package com.falstad.fourier.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;

// this used to be a thread in the java version, but now it's not
public class PlayThread {
    boolean shutdownRequested;
    boolean stereo;
    double fbufLi[];
    double fbufRi[];
    double fbufLo[];
    double fbufRo[];
    double stateL[], stateR[];
    double outputGain;
    int fbufmask, fbufsize;
    int spectrumOffset, spectrumLen;
    FourierSim sim;
    static final double pi = Math.PI; 
    boolean maxGain = true;
    boolean useConvolve = false;
    int gainCounter = 0;

    PlayThread() {
    	shutdownRequested = false;
    	sim = FourierSim.theSim;
    }
    void requestShutdown() {
//    	sim.console("requestshutdown");
    	shutdownRequested = true;
    	sim.playThread = null;
    	FourierSim.stopSound();
    }

    void soundChanged() {
    	changed = true;
    }
    
    void openLine() {
        try {
            stereo = true;
            int bufsz = FourierSim.getPower2(sim.sampleRate/4);
            // open audio
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int inbp, outbp;
    int spectCt;
    int saveBufPtr, saveBufCt;
    boolean changed;
    FFT playFFT;

    public void start() {
    	initLoop();
    	FourierSim.startSound();
    }
    
    public void run() {
        try {
//            doRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        sim.playThread = null;
    }

    void process(JsArrayNumber leftIn, JsArrayNumber rightIn, JsArrayNumber leftOut, JsArrayNumber rightOut) {
    	int i = inbp;
		int outi = 0;
    	loop();
    		for (i = inbp; i != playfunc.length && outi != leftIn.length(); i++, outi++) {
    			leftOut.set (outi, playfunc[i]);
    			rightOut.set(outi, playfunc[i]);
    		}
    		inbp = i;
    		if (inbp >= playfunc.length)
    			inbp = 0;
    }
    
    void initLoop() {
//        sim.rateChooser.setEnabled(true);
        sim.unstable = false;
        playFFT = new FFT(sim.playSampleCount);

        fbufsize = 16384; // 32768;
        fbufmask = fbufsize-1;
        fbufLi = new double[fbufsize];
        fbufRi = new double[fbufsize];
        fbufLo = new double[fbufsize];
        fbufRo = new double[fbufsize];
        openLine();
        inbp = outbp = spectCt = 0;
        outputGain = 1;
        spectrumLen = FourierSim.getPower2(sim.sampleRate/12);
    }
    
    double playfunc[] = null;

    void loop() {
        int ss = (stereo) ? 2 : 1;
        int shiftCtr = 0;
        
        if (!sim.soundCheck.getState())
        	requestShutdown();
        
        int playSampleCount = sim.playSampleCount;
        boolean solos[] = sim.solos;
        boolean mutes[] = sim.mutes;
        double magcoef[] = sim.magcoef;
        double phasecoef[] = sim.phasecoef;
        boolean hasSolo = sim.hasSolo;
        int dfreq0 = sim.dfreq0;
        if (playfunc == null || changed) {
            playfunc = new double[playSampleCount*2];
            int i;
            int terms = sim.termBar.getValue();
            double bstep = 2*pi*sim.getFreq()/sim.sampleRate;
            double mx = .2;
            changed = false;
            for (i = 1; i != terms; i++) {
                if (hasSolo && !solos[i])
                    continue;
                if (mutes[i])
                    continue;
                int dfreq = dfreq0*i;
                if (dfreq >= playSampleCount)
                    break;
                int sgn = (i & 1) == 1 ? -1 : 1;
                playfunc[dfreq]   =  sgn*magcoef[i]*Math.cos(phasecoef[i]);
                playfunc[dfreq+1] = -sgn*magcoef[i]*Math.sin(phasecoef[i]);
            }
            playFFT.transform(playfunc, true);
            for (i = 0; i != playSampleCount; i++) {
                double dy = playfunc[i*2];
                if (dy > mx)  mx = dy;
                if (dy < -mx) mx = -dy;
            }
            
            double func[] = new double[playSampleCount];
            double mult = 1./mx;
            for (i = 0; i != playSampleCount; i++)
            	func[i] = playfunc[i*2]*mult;
            playfunc = func;
        }
    }

    double impulseBuf[], convolveBuf[];
    int convBufPtr;
    FFT convFFT;
    
    double saveBufL[];
    double saveBufR[];
    
    void doOutput(int outlen) {
        if (saveBufL == null || saveBufL.length < outlen) {
            saveBufL = new double[outlen];
            saveBufR = new double[outlen];
            saveBufPtr = saveBufCt = 0;
        }
        int i, i2;
        while (true) {
            double max = 0;
            i = outbp;
            for (i2 = 0; i2 < outlen; i2++) {
                double qi = (playfunc[i]*outputGain);
                if (qi > max)  max = qi;
                if (qi < -max) max = -qi;
                saveBufL[i2+saveBufCt] = qi;
                i = (i+1) & fbufmask;
            }
            i = outbp;
            for (i2 = 0; i2 < outlen; i2++) {
                double qi = (playfunc[i]*outputGain);
                if (qi > max)  max = qi;
                if (qi < -max) max = -qi;
                saveBufR[i2+saveBufCt] = qi;
                i = (i+1) & fbufmask;
            }
            // if we're getting overflow, adjust the gain
            if (max > 1) {
                //System.out.println("max = " + max);
                outputGain *= 1./max;
                if (outputGain < 1e-10 || Double.isInfinite(outputGain) ||
                		Double.isInfinite(max)) {
                    sim.unstable = true;
                    break;
                }
                continue;
            } else if (maxGain && max < .7) {
            	// avoid insanely high gain that might cause overflow
                if (max < 1e-20) {
                    if (outputGain == 1)
                        break;
                    outputGain = 1;
                } else
                    outputGain *= 1./max;
                continue;
            }
            break;
        }
        if (sim.unstable)
            return;
        outbp = i;
        spectCt += outlen;
        saveBufCt += outlen;
    }
}
