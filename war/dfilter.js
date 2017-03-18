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
  var source = context.createBufferSource();
  //source.buffer = this.buffer;

  // Hook it up to a ScriptProcessorNode.
console.log("bufsize = " + this.BUFFER_SIZE);
  var processor = context.createScriptProcessor(this.BUFFER_SIZE);
  processor.onaudioprocess = this.onProcess;

  source.connect(processor);
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
  //var leftIn = e.inputBuffer.getChannelData(0);
  //var rightIn = e.inputBuffer.getChannelData(1);
  var leftOut = e.outputBuffer.getChannelData(0);
  var rightOut = e.outputBuffer.getChannelData(1);

  for (var i = 0; i < leftOut.length; i++) {
    // Flip left and right channels.
   /* if (this.isChannelFlip) {
      leftOut[i] = rightIn[i];
      rightOut[i] = leftIn[i];
    } else {
      leftOut[i] = leftIn[i];
      rightOut[i] = rightIn[i];
    }*/
    leftOut[i] = rightOut[i] = 0;

    // Add some noise
//    if (sample.isPlaying) {
      leftOut[i] += (Math.random() - 0.5) * NOISE_FACTOR;
      rightOut[i] += (Math.random() - 0.5) * NOISE_FACTOR;
  }
  sim.process(leftOut, rightOut);
};
};

    document.passSimulator = function passSimulator (sim_) {
    	sim = sim_;
	sample = new ScriptSample();
        console.log("pass simulator " + sim);
	sim.startSound = function() { sample.play(); }
	sim.stopSound = function() { sample.stop(); }
}

