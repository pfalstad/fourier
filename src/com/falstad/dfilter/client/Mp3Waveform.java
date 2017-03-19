package com.falstad.dfilter.client;

public class Mp3Waveform extends Waveform {
    String fileName;

    Mp3Waveform(int f) { fileName = DFilterSim.theSim.mp3List[f]; }

    boolean start() {
        getBuffer();
        DFilterSim.loadMp3(fileName);
        return true;
    }

	@Override
	int getData() {
		return buffer.length;
	}

	int getChannels() { return 2; }
    String getInputText() { return null; }
    boolean needsFrequency() { return false; }
}
