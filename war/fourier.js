var sim;

var player;
var context;
var NOISE_FACTOR = .5;

function Player() {
	this.BUFFER_SIZE = 2048; // 16384;

	this.isPlaying = false;
	this.isNoise = true;
	this.isChannelFlip = false;
	
	context = new (window.AudioContext || window.webkitAudioContext)();

	this.play = function() {
		if (this.source != null) debugger;
		
		// create source
		var source = context.createBufferSource();
		this.source = source;

		// Hook it up to a ScriptProcessorNode.
		var processor = context.createScriptProcessor(this.BUFFER_SIZE);
		processor.onaudioprocess = this.onProcess;

		source.connect(processor);
		source.loop = true;
		processor.connect(context.destination);

		source[source.start ? 'start': 'noteOn'](0);
		this.source = source;
		this.processor = processor;
	};

	this.stop = function() {
		try { this.source.stop(0); } catch (err) { }
		try { this.processor.disconnect(context.destination); } catch (err) { }
		this.source = null;
	};

	this.onProcess = function(e) {
		var leftIn = e.inputBuffer.getChannelData(0);
		var rightIn = e.inputBuffer.getChannelData(1);
		var leftOut = e.outputBuffer.getChannelData(0);
		var rightOut = e.outputBuffer.getChannelData(1);
		
		// pass buffer to GWT code for processing
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
				loader.source.buffer = buffer;
				// start player
				loader.source.start(0);
			},

			function(e){ console.log("Error with decoding audio data" + e.err); });
		}
		request.send();
	}
};

// install callbacks for GWT
document.passSimulator = function passSimulator (sim_) {
	sim = sim_;
	player = new Player();
	sim.startSound = function() { player.play(); }
	sim.stopSound = function() { player.stop(); }
	sim.loadAudioFile = function(f) { player.stop(); player.loadFile(f); }
	sim.getSampleRate = function() { return context ? context.sampleRate : 0; }
};
