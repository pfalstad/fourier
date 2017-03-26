package com.falstad.dfilter.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;

// this used to be a thread in the java version, but now it's not
public class PlayThread {
    Waveform wform;
    boolean shutdownRequested;
    boolean stereo;
    Filter filt, newFilter;
    double fbufLi[];
    double fbufRi[];
    double fbufLo[];
    double fbufRo[];
    double stateL[], stateR[];
    double outputGain;
    int fbufmask, fbufsize;
    int spectrumOffset, spectrumLen;
    DFilterSim sim;
    static final double pi = Math.PI; 
    boolean maxGain = true;
    boolean useConvolve = false;
    int gainCounter = 0;

    PlayThread() {
    	shutdownRequested = false;
    	sim = DFilterSim.theSim;
    }
    void requestShutdown() {
//    	sim.console("requestshutdown");
    	shutdownRequested = true;
    	sim.playThread = null;
    	DFilterSim.stopSound();
    }
    void setFilter(Filter f) { newFilter = f; }

    void openLine() {
        try {
            stereo = (wform.getChannels() == 2);
            int bufsz = DFilterSim.getPower2(sim.sampleRate/4);
            // open audio
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int inbp, outbp;
    int spectCt;
    int saveBufPtr, saveBufCt;

    public void start() {
    	initLoop();
    	DFilterSim.startSound();
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
    	int i2;
		for (i2 = 0; i2 != leftIn.length(); i2++) {
			fbufLi[i] = leftIn.get(i2);
			fbufRi[i] = rightIn.get(i2);
            i = (i+1) & fbufmask;
		}
		int outi = 0;
    	if (saveBufCt > 0) {
    		if (saveBufCt-saveBufPtr > leftIn.length())
    			sim.console("fail1");
    		for (i = saveBufPtr; i != saveBufCt; i++, outi++) {
    			leftOut.set (outi, saveBufL[i]);
    			rightOut.set(outi, saveBufR[i]);
    		}
    		saveBufCt = saveBufPtr = 0;
    	}
    	loop();
    	if (saveBufCt > 0) {
    		if (saveBufPtr > 0)
    			sim.console("fail2");
    		for (i = saveBufPtr; i != saveBufCt && outi != leftIn.length(); i++, outi++) {
    			leftOut.set (outi, saveBufL[i]);
    			rightOut.set(outi, saveBufR[i]);
    		}
    		saveBufPtr = i;
    		if (saveBufPtr == saveBufCt)
    			saveBufCt = saveBufPtr = 0;
    	}
    }
    
    void initLoop() {
//        sim.rateChooser.setEnabled(true);
        wform = sim.getWaveformObject();
        sim.mp3Error = null;
        sim.unstable = false;
        if (!wform.start()) {
            return;
        }

        fbufsize = 32768;
        fbufmask = fbufsize-1;
        fbufLi = new double[fbufsize];
        fbufRi = new double[fbufsize];
        fbufLo = new double[fbufsize];
        fbufRo = new double[fbufsize];
        openLine();
        inbp = outbp = spectCt = 0;
        outputGain = 1;
        newFilter = filt = sim.curFilter;
        spectrumLen = DFilterSim.getPower2(sim.sampleRate/12);
    }
    
    void loop() {
        int ss = (stereo) ? 2 : 1;
        int shiftCtr = 0;
        
        if (!sim.soundCheck.getState())
        	requestShutdown();
//        while (!shutdownRequested && sim.soundCheck.getState()) {
        	
            //System.out.println("nf " + newFilter + " " +(inbp-outbp));
            if (newFilter != null) {
//            	sim.console("newfilter");
                gainCounter = 0;
                maxGain = true;
                if (wform instanceof SweepWaveform ||
                    wform instanceof SineWaveform)
                    maxGain = false;
                outputGain = 1;
                // we avoid doing this unless necessary because it sounds bad
                if (filt == null || filt.getLength() != newFilter.getLength())
                    convBufPtr = inbp = outbp = spectCt = 0;
                filt = newFilter;
                newFilter = null;
                impulseBuf = null;
                useConvolve = filt.useConvolve();
                stateL = filt.createState();
                stateR = filt.createState();
            }
            int length = wform.getData();
            if (length == 0)
                return;
            short ib[] = wform.buffer;
            
//            useConvolve = false; // TODO make this true?

            int i = inbp;
            int i2;
            int sampleCount = 2048;
            if (!(wform instanceof Mp3Waveform)) {
            	double mult = 1/32767.;
            	for (i2 = 0; i2 < length; i2 += ss) {
            		fbufLi[i] = ib[i2]*mult;
            		i = (i+1) & fbufmask;
            	}
            	i = inbp;
            	if (stereo) {
            		for (i2 = 0; i2 < length; i2 += 2) {
            			fbufRi[i] = ib[i2+1]*mult;
            			i = (i+1) & fbufmask;
            		}
            	} else {
            		for (i2 = 0; i2 < length; i2++) {
            			fbufRi[i] = fbufLi[i];
            			i = (i+1) & fbufmask;
            		}
            	}
//            	sampleCount = length/ss;
            }
            
            if (sim.shiftSpectrumCheck.getState()) {
                double shiftFreq = sim.shiftFreqBar.getValue()*pi/1000.;
                if (shiftFreq > pi)
                    shiftFreq = pi;
                i = inbp;
                for (i2 = 0; i2 < length; i2 += ss) {
                    double q = Math.cos(shiftFreq*shiftCtr++);
                    fbufLi[i] *= q;
                    fbufRi[i] *= q;
                    i = (i+1) & fbufmask;
                }
            }

            if (useConvolve)
                doConvolveFilter(sampleCount, maxGain);
            else {
                doFilter(sampleCount);
                if (sim.unstable)
                    return;
                int outlen = sampleCount;
                doOutput(outlen, maxGain);
            }
            
            if (sim.unstable)
                return;
            
            if (spectCt >= spectrumLen) {
                spectrumOffset = (outbp-spectrumLen) & fbufmask;
                spectCt -= spectrumLen;
            }
            gainCounter += sampleCount;
            if (maxGain && gainCounter >= sim.sampleRate/10) {
                gainCounter = 0;
                maxGain = false;
                //System.out.println("gain ctr up " + outputGain);
            }
            
            sim.outputGain = outputGain;

//        }
//        if (shutdownRequested || sim.unstable || !sim.soundCheck.getState())
//            line.flush();
//        else
//            line.drain();
//        cv.repaint();
    }

    void doFilter(int sampleCount) {
        filt.run(fbufLi, fbufLo, inbp, fbufmask, sampleCount, stateL);
        filt.run(fbufRi, fbufRo, inbp, fbufmask, sampleCount, stateR);
        inbp = (inbp+sampleCount) & fbufmask;
        double q = fbufLo[(inbp-1) & fbufmask];
        if (Double.isNaN(q) || Double.isInfinite(q))
            sim.unstable = true;
    }
    double impulseBuf[], convolveBuf[];
    int convBufPtr;
    FFT convFFT;
    
    void doConvolveFilter(int sampleCount, boolean maxGain) {
        int i;
        int fi2 = inbp, i20;
        double filtA[] = ((DirectFilter) filt).aList;
        int cblen = DFilterSim.getPower2(512+filtA.length*2);
        if (convolveBuf == null || convolveBuf.length != cblen)
            convolveBuf = new double[cblen];
        if (impulseBuf == null) {
            // take FFT of the impulse response
            impulseBuf = new double[cblen];
            for (i = 0; i != filtA.length; i++)
                impulseBuf[i*2] = filtA[i];
            convFFT = new FFT(convolveBuf.length/2);
            convFFT.transform(impulseBuf, false);
        }
        int cbptr = convBufPtr;
        // result = impulseLen+inputLen-1 samples long; result length
        // is fixed, so use it to get inputLen
        int cbptrmax = convolveBuf.length+2-2*filtA.length;
        //System.out.println("reading " + sampleCount);
        for (i = 0; i != sampleCount; i++, fi2++) {
            i20 = fi2 & fbufmask;
            convolveBuf[cbptr  ] = fbufLi[i20];
            convolveBuf[cbptr+1] = fbufRi[i20];
            cbptr += 2;
            if (cbptr == cbptrmax) {
                // buffer is full, do the transform
                convFFT.transform(convolveBuf, false);
                double mult = 2./cblen;
                int j;
                // multiply transforms to get convolution
                for (j = 0; j != cblen; j += 2) {
                    double a = convolveBuf[j]*impulseBuf[j] -
                        convolveBuf[j+1]*impulseBuf[j+1];
                    double b = convolveBuf[j]*impulseBuf[j+1] +
                        convolveBuf[j+1]*impulseBuf[j];
                    convolveBuf[j]   = a*mult;
                    convolveBuf[j+1] = b*mult;
                }
                // inverse transform to get signal
                convFFT.transform(convolveBuf, true);
                int fj2 = outbp, j20;
                int overlap = cblen-cbptrmax;
                // generate output that overlaps with old data
                for (j = 0; j != overlap; j += 2, fj2++) {
                    j20 = fj2 & fbufmask;
                    fbufLo[j20] += convolveBuf[j];
                    fbufRo[j20] += convolveBuf[j+1];
                }
                // generate new output
                for (; j != cblen; j += 2, fj2++) {
                    j20 = fj2 & fbufmask;
                    fbufLo[j20] = convolveBuf[j];
                    fbufRo[j20] = convolveBuf[j+1];
                }
                cbptr = 0;
                // output the sound
                doOutput(cbptrmax/2, maxGain);
                //System.out.println("outputting " + cbptrmax);
                // clear transform buffer
                for (j = 0; j != cblen; j++)
                    convolveBuf[j] = 0;
            }
        }
        inbp = fi2 & fbufmask;
        convBufPtr = cbptr;
    }
    
//    byte ob[];
    double saveBufL[];
    double saveBufR[];
    
    void doOutput(int outlen, boolean maxGain) {
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
                double qi = (fbufLo[i]*outputGain);
                if (qi > max)  max = qi;
                if (qi < -max) max = -qi;
                saveBufL[i2+saveBufCt] = qi;
                i = (i+1) & fbufmask;
            }
            i = outbp;
            for (i2 = 0; i2 < outlen; i2++) {
                double qi = (fbufRo[i]*outputGain);
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
