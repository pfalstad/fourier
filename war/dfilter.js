var sim;

var sample;
var context;
var NOISE_FACTOR = .5;

function ScriptSample() {
	this.BUFFER_SIZE = 2048;

	this.isPlaying = false;
	this.isNoise = true;
	this.isChannelFlip = false;
	
	// Load a sound.
	//loadSounds(this, { buffer: 'chrono.mp3' });
	context = new (window.AudioContext || window.webkitAudioContext)();

	this.play = function() {
		console.log("this = " +this);
		var source = context.createBufferSource();
		this.source = source;
//		source.buffer = this.buffer;
//		console.log("got buffer " + source.buffer + " " + source.buffer.length);

		// Hook it up to a ScriptProcessorNode.
		console.log("bufsize = " + this.BUFFER_SIZE);
		console.log("samplgrate = " + context.sampleRate);
		var processor = context.createScriptProcessor(this.BUFFER_SIZE);
		processor.onaudioprocess = this.onProcess;

		source.connect(processor);
		source.loop = true;
		processor.connect(context.destination);

		console.log('start');
		source[source.start ? 'start': 'noteOn'](0);
		this.source = source;
		this.processor = processor;
	};

	this.stop = function() {
		try { this.source.stop(0); } catch (err) { }
		try { this.processor.disconnect(context.destination); } catch (err) { }
	};

	this.onProcess = function(e) {
		var leftIn = e.inputBuffer.getChannelData(0);
		var rightIn = e.inputBuffer.getChannelData(1);
		var leftOut = e.outputBuffer.getChannelData(0);
		var rightOut = e.outputBuffer.getChannelData(1);
		sim.process(leftIn, rightIn, leftOut, rightOut);
	};
	
	this.loadFile = function(f) {
		var request = new XMLHttpRequest();
		request.open("GET", f, true);
		request.responseType = "arraybuffer";
		var loader = this;
		request.onload = function() {
			// Asynchronously decode the audio file data in request.response
			var audioData = request.response;

			context.decodeAudioData(audioData, function(buffer) {
				console.log("decode audio data " + buffer.length + " "+ loader.source);
				loader.source.buffer = buffer;
//				loader.buffer = buffer;
				loader.source.start(0);
			},

			function(e){ console.log("Error with decoding audio data" + e.err); });
		}
		request.send();
	}
};

document.passSimulator = function passSimulator (sim_) {
	sim = sim_;
	sample = new ScriptSample();
	console.log("pass simulator " + sim);
	sim.startSound = function() { sample.play(); }
	sim.stopSound = function() { sample.stop(); }
	sim.loadMp3 = function(f) { sample.loadFile(f); }
};

