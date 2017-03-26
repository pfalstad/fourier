/*
    Copyright (C) 2017 by Paul Falstad

    This file is part of DFilter.

    DFilter is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    DFilter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DFilter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.falstad.dfilter.client;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.javascript.host.Console;
import com.gargoylesoftware.htmlunit.javascript.host.Navigator;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import java.util.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DFilterSim implements MouseDownHandler, MouseMoveHandler,
		MouseUpHandler, ClickHandler, DoubleClickHandler,
		NativePreviewHandler, MouseOutHandler, MouseWheelHandler, ChangeHandler,
		ValueChangeHandler<Boolean> {

	Logger logger = Logger.getLogger(DFilterSim.class.getName());
	
	Dimension winSize;
	Random random;
    View respView, impulseView, phaseView, stepView, spectrumView, waveformView, poleInfoView,
    polesView;
    int maxSampleCount = 70; // was 50
    int sampleCountR, sampleCountTh;
    int modeCountR, modeCountTh;
    int maxDispRModes = 5, maxDispThModes = 5;
    public static final double epsilon = .00001;
    public static final double epsilon2 = .003;
    public static final double log10 = 2.30258509299404568401;
    public static int WINDOW_KAISER = 4;

    Checkbox soundCheck;
    Checkbox displayCheck;
    Checkbox shiftSpectrumCheck;
    //Checkbox woofCheck;
    CheckboxMenuItem freqCheckItem;
    CheckboxMenuItem phaseCheckItem;
    CheckboxMenuItem spectrumCheckItem;
    CheckboxMenuItem impulseCheckItem;
    CheckboxMenuItem stepCheckItem;
    CheckboxMenuItem waveformCheckItem;
    CheckboxMenuItem logFreqCheckItem;
    CheckboxMenuItem logAmpCheckItem;
    CheckboxMenuItem allWaveformCheckItem;
    CheckboxMenuItem ferrisCheckItem;
    MenuItem exitItem;
    Choice filterChooser;
    int selection;
    final int SELECT_RESPONSE = 1;
    final int SELECT_SPECTRUM = 2;
    final int SELECT_POLES    = 3;
    int filterSelection;
    Choice inputChooser;
    Choice windowChooser;
    Choice rateChooser;
    Scrollbar auxBars[];
    Label auxLabels[];
    Label inputLabel;
    Scrollbar inputBar;
    Label shiftFreqLabel;
    Scrollbar shiftFreqBar;
    Label kaiserLabel;
    Scrollbar kaiserBar;
    boolean editingFunc;
    boolean dragStop;
    double inputW;
    static final double pi = 3.14159265358979323846;
    double step;
    double waveGain = 1./65536;
    double outputGain = 1;
    int sampleRate;
    int xpoints[] = new int[4];
    int ypoints[] = new int[4];
    int dragX, dragY;
    int dragStartX, dragStartY;
    int mouseX, mouseY;
    int selectedPole, selectedZero;
    int lastPoleCount = 2, lastZeroCount = 2;
    boolean dragSet, dragClear;
    boolean dragging;
    boolean unstable;
    Image memimage;
    double t;
    int pause;
    PlayThread playThread;
    Filter curFilter;
    FilterType filterType;
    double spectrumBuf[];
    FFT spectrumFFT;
    Waveform wformInfo;
    PhaseColor phaseColors[];
    static final int phaseColorCount = 50*8;
    boolean filterChanged;

    class View extends Rectangle {
//        View(Dimension r) { super(r); }
        View(int a, int b, int c, int d) {
            super(a, b, c, d);
            right = a+c-1;
            bottom = b+d-1;
        }
        int right, bottom;
        void drawLabel(Graphics g, String str) {
            g.setColor(Color.white);
            centerString(g, str, y-5);
        }
    }

	public boolean useFrame;
	CanvasPixelArray pixels;
	ImageData imageData;
	
	DockLayoutPanel layoutPanel;
	VerticalPanel verticalPanel;
	AbsolutePanel absolutePanel;
	AboutBox aboutBox;
	Canvas cv;
	Context2d cvcontext;
	Canvas backcv;
	Context2d backcontext;
	HandlerRegistration handler;
	DialogBox dialogBox;
	int verticalPanelWidth;
	String startLayoutText = null;
	String versionString = "1.0";
	static DFilterSim theSim;
    NumberFormat showFormat;
    int customMp3Index;
    String mp3List[];
    String mp3Error;
    Complex customPoles[], customZeros[];

	static final int MENUBARHEIGHT = 30;
	static final int MAXVERTICALPANELWIDTH = 166;
	static final int POSTGRABSQ = 16;

	final Timer timer = new Timer() {
		public void run() {
			updateRipple();
		}
	};
	final int FASTTIMER = 33; // 16;

	int getrand(int x) {
		return random.nextInt(x);
	}

	public void setCanvasSize() {
		int width, height;
		int fullwidth = width = (int) RootLayoutPanel.get().getOffsetWidth();
		height = (int) RootLayoutPanel.get().getOffsetHeight();
		height = height - MENUBARHEIGHT;   // put this back in if we add a menu bar
		width = width - MAXVERTICALPANELWIDTH;
		width = height = (width < height) ? width : height;
		winSize = new Dimension(width, height);
		verticalPanelWidth = fullwidth-width;
		if (layoutPanel != null)
			layoutPanel.setWidgetSize(verticalPanel, verticalPanelWidth);
		if (inputBar != null) {
			inputBar.setWidth(verticalPanelWidth);
			shiftFreqBar.setWidth(verticalPanelWidth);
			kaiserBar.setWidth(verticalPanelWidth);
		}
		if (cv != null) {
			cv.setWidth(width + "PX");
			cv.setHeight(height + "PX");
			cv.setCoordinateSpaceWidth(width);
			cv.setCoordinateSpaceHeight(height);
		}
		if (backcv != null) {
			backcv.setWidth(width + "PX");
			backcv.setHeight(height + "PX");
			backcv.setCoordinateSpaceWidth(width);
			backcv.setCoordinateSpaceHeight(height);
		}
		handleResize();
	}

    public static native void console(String text)
    /*-{
	    console.log(text);
	}-*/;

    // install all the callbacks we need into "this" 
	static native void passSimulator() /*-{
		$doc.passSimulator(this);
		
		this.process = function(li, ri, lo, ro) {
			@com.falstad.dfilter.client.DFilterSim::process(*)(li, ri, lo, ro);
		}
	}-*/;

    static void process(JsArrayNumber leftIn, JsArrayNumber rightIn, JsArrayNumber leftOut, JsArrayNumber rightOut) {
		PlayThread playThread = DFilterSim.theSim.playThread;
		if (playThread != null)
			playThread.process(leftIn, rightIn, leftOut, rightOut);
	}
	
	static native void startSound() /*-{
		startSound();
	}-*/;
	
	static native void stopSound() /*-{
		stopSound();
	}-*/;

	static native void debugger() /*-{
		debugger;
	}-*/;

	static native void loadAudioFile(String f) /*-{
		loadAudioFile(f);
	}-*/;

	static native void loadAudioData(String d) /*-{
		loadAudioData(d);
	}-*/;

	static native int getSampleRate() /*-{
		return getSampleRate();
	}-*/;
	
	Frame iFrame;
    LoadFile loadFileInput;
	
	public void init() {
		theSim = this;
        mp3List = new String[6];
        mp3List[0] = "speech.mp3";
        mp3List[1] = "piano1.mp3";
        mp3List[2] = "piano2.mp3";
        mp3List[3] = "developers.mp3";
        mp3List[4] = "robotron.mp3";
        mp3List[5] = "arabian.mp3";
        mp3List[6] = null;
        customMp3Index = 6;
		
        QueryParameters qp = new QueryParameters();
        
        try {
                // look for layout embedded in URL
                String cct=qp.getValue("rol");
                if (cct!=null)
                	startLayoutText = cct.replace("%24", "$");
        } catch (Exception e) { }

		cv = Canvas.createIfSupported();
		if (cv == null) {
			RootPanel
					.get()
					.add(new Label(
							"Not working. You need a browser that supports the CANVAS element."));
			return;
		}

		passSimulator();
		
        int j;
        int pc8 = phaseColorCount/8;
        phaseColors = new PhaseColor[phaseColorCount];
        int i;
        for (i = 0; i != 8; i++)
            for (j = 0; j != pc8; j++) {
                double ang = Math.atan(j/(double) pc8);
                phaseColors[i*pc8+j] = genPhaseColor(i, ang);
            }

        customPoles = new Complex[20];
        customZeros = new Complex[20];
        for (i = 0; i != customPoles.length; i++)
            customPoles[i] = new Complex();
        for (i = 0; i != customZeros.length; i++)
            customZeros[i] = new Complex();

		cvcontext = cv.getContext2d();
		backcv = Canvas.createIfSupported();
		backcontext = backcv.getContext2d();
		setCanvasSize();
		layoutPanel = new DockLayoutPanel(Unit.PX);
		verticalPanel = new VerticalPanel();
		
        MenuBar mb = new MenuBar();
        MenuBar m = new MenuBar(true);
//        m.addItem(exitItem = getMenuItem("Exit", new MyCommand("file", "exit")));
        m.addItem(getMenuItem("About", new Command() {
        	public void execute() {
                aboutBox = new AboutBox(versionString);
        	}
        }));
        mb.addItem("File", m);
        m = new MenuBar(true);
        m.addItem(freqCheckItem = getCheckItem("Frequency Response", true));
        m.addItem(phaseCheckItem = getCheckItem("Phase Response", false));
        m.addItem(spectrumCheckItem = getCheckItem("Spectrum", true));
        m.addItem(waveformCheckItem = getCheckItem("Waveform", true));
        m.addItem(impulseCheckItem  = getCheckItem("Impulse Response", true));
        m.addItem(stepCheckItem     = getCheckItem("Step Response", false));
        m.addSeparator();
        m.addItem(logFreqCheckItem = getCheckItem("Log Frequency Scale", false));
        m.addItem(allWaveformCheckItem = getCheckItem("Show Entire Waveform", false));
        m.addItem(ferrisCheckItem = getCheckItem("Ferris Plot", false));
        // this doesn't fully work when turned off
        logAmpCheckItem = getCheckItem("Log Amplitude Scale", true);
        mb.addItem("View", m);
        
        layoutPanel.addNorth(mb, MENUBARHEIGHT);
        layoutPanel.addEast(verticalPanel, verticalPanelWidth);
        RootLayoutPanel.get().add(layoutPanel);

        soundCheck = new Checkbox("Sound On");
        soundCheck.setState(false);
        soundCheck.addValueChangeHandler(this);
        verticalPanel.add(soundCheck);

        displayCheck = new Checkbox("Stop Display");
        displayCheck.addValueChangeHandler(this);
        verticalPanel.add(displayCheck);
        displayCheck.addStyleName("topSpace");

        shiftSpectrumCheck = new Checkbox("Shift Spectrum");
        shiftSpectrumCheck.addValueChangeHandler(this);
        verticalPanel.add(shiftSpectrumCheck);
        shiftSpectrumCheck.addStyleName("topSpace");

        /*woofCheck = new Checkbox("Woof");
        woofCheck.addItemListener(this);
        add(woofCheck);*/

        verticalPanel.add(inputChooser = new Choice());
        inputChooser.addStyleName("topSpace");
        inputChooser.add("Input = Noise");
        inputChooser.add("Input = Sine Wave");
        inputChooser.add("Input = Sawtooth");
        inputChooser.add("Input = Triangle Wave");
        inputChooser.add("Input = Square Wave");
        inputChooser.add("Input = Periodic Noise");
        inputChooser.add("Input = Sweep");
        inputChooser.add("Input = Impulses");
        for (i = 0; mp3List[i] != null; i++)
            inputChooser.add("Input = " + mp3List[i]);
        inputChooser.add("Input = Custom File");
        inputChooser.addChangeHandler(this);
        
        if (LoadFile.isSupported())
            verticalPanel.add(loadFileInput = new LoadFile(this));

        verticalPanel.add(filterChooser = new Choice());
        filterChooser.add("Filter = FIR Low-pass");
        filterChooser.add("Filter = FIR High-pass");
        filterChooser.add("Filter = FIR Band-pass");
        filterChooser.add("Filter = FIR Band-stop");
        filterChooser.add("Filter = Custom FIR");
        filterChooser.add("Filter = None");
        filterChooser.add("Filter = Butterworth Low-pass");
        filterChooser.add("Filter = Butterworth High-pass");
        filterChooser.add("Filter = Butterworth Band-pass");
        filterChooser.add("Filter = Butterworth Band-stop");
        filterChooser.add("Filter = Chebyshev Low-pass");
        filterChooser.add("Filter = Chebyshev High-pass");
        filterChooser.add("Filter = Chebyshev Band-pass");
        filterChooser.add("Filter = Chebyshev Band-stop");
        filterChooser.add("Filter = Inv Cheby Low-pass");
        filterChooser.add("Filter = Inv Cheby High-pass");
        filterChooser.add("Filter = Inv Cheby Band-pass");
        filterChooser.add("Filter = Inv Cheby Band-stop");
        filterChooser.add("Filter = Elliptic Low-pass");
        filterChooser.add("Filter = Elliptic High-pass");
        filterChooser.add("Filter = Elliptic Band-pass");
        filterChooser.add("Filter = Elliptic Band-stop");
        filterChooser.add("Filter = Comb (+)");
        filterChooser.add("Filter = Comb (-)");
        filterChooser.add("Filter = Delay");
        filterChooser.add("Filter = Plucked String");
        filterChooser.add("Filter = Inverse Comb");
        filterChooser.add("Filter = Reson");
        filterChooser.add("Filter = Reson w/ Zeros");
        filterChooser.add("Filter = Notch");
        filterChooser.add("Filter = Moving Average");
        filterChooser.add("Filter = Triangle");
        filterChooser.add("Filter = Allpass");
        filterChooser.add("Filter = Gaussian");
        filterChooser.add("Filter = Random");
        filterChooser.add("Filter = Custom IIR");
        filterChooser.addChangeHandler(this);
        filterSelection = -1;
        
        verticalPanel.add(windowChooser = new Choice());
        windowChooser.add("Window = Rectangular");
        windowChooser.add("Window = Hamming");
        windowChooser.add("Window = Hann");
        windowChooser.add("Window = Blackman");
        windowChooser.add("Window = Kaiser");
        windowChooser.add("Window = Bartlett");
        windowChooser.add("Window = Welch");
        windowChooser.addChangeHandler(this);
        windowChooser.select(1);
        
        /*
        verticalPanel.add(rateChooser = new Choice());
        rateChooser.add("Sampling Rate = 8000");
        rateChooser.add("Sampling Rate = 11025");
        rateChooser.add("Sampling Rate = 16000");
        rateChooser.add("Sampling Rate = 22050");
        rateChooser.add("Sampling Rate = 32000");
        rateChooser.add("Sampling Rate = 44100");
        rateChooser.select(5);
        rateChooser.addChangeHandler(this);
*/
        sampleRate = 44100;
        
        auxLabels = new Label[5];
        auxBars = new Scrollbar[5];
        for (i = 0; i != 5; i++) {
        	verticalPanel.add(auxLabels[i] = new Label(""));
        	verticalPanel.add(auxBars[i] = new Scrollbar(Scrollbar.HORIZONTAL, 25, 1, 1, 999,
        			new Command() {
        		public void execute() { scrollbarMoved(); } }));
        }

        verticalPanel.add(inputLabel = new Label("Input Frequency"));
        verticalPanel.add(inputBar = new Scrollbar(Scrollbar.HORIZONTAL, 200, 1, 1, 9999,
    			new Command() {
    		public void execute() { scrollbarMoved(); } }));

        verticalPanel.add(shiftFreqLabel = new Label("Shift Frequency"));
        verticalPanel.add(shiftFreqBar = new Scrollbar(Scrollbar.HORIZONTAL, 10, 1, 0, 1001,
    			new Command() {
    		public void execute() { scrollbarMoved(); } }));
        shiftFreqLabel.setVisible(false);
        shiftFreqBar.setVisible(false);

        verticalPanel.add(kaiserLabel = new Label("Kaiser Parameter"));
        verticalPanel.add(kaiserBar = new Scrollbar(Scrollbar.HORIZONTAL, 500, 1, 1, 999,
    			new Command() {
    		public void execute() { scrollbarMoved(); } }));

    	verticalPanel.add(iFrame = new Frame("iframe.html"));
    	iFrame.setWidth(verticalPanelWidth+"px");
    	iFrame.setHeight("100 px");
    	iFrame.getElement().setAttribute("scrolling", "no");

        random = new Random();
        setInputLabel();
        reinit();

//        l.addStyleName("topSpace");
//		resBar.setWidth(verticalPanelWidth);
//		dampingBar.setWidth(verticalPanelWidth);

        showFormat=NumberFormat.getFormat("####.##");
		
		cv.addMouseMoveHandler(this);
		cv.addMouseDownHandler(this);
		cv.addMouseOutHandler(this);
		cv.addMouseUpHandler(this);
        cv.addMouseWheelHandler(this);
        cv.addClickHandler(this);
        doTouchHandlers(cv.getCanvasElement());
//		cv.addDomHandler(this,  ContextMenuEvent.getType());
		
		setCanvasSize();
		layoutPanel.add(cv);
		timer.scheduleRepeating(FASTTIMER);
	}

    void reinit() {
        setupFilter();
        setInputW();
    }
    
    // install touch handlers to handle touch events properly on mobile devices.
    // don't feel like rewriting this in java.  Anyway, java doesn't let us create mouse
    // events and dispatch them.
    native void doTouchHandlers(CanvasElement cv) /*-{
	// Set up touch events for mobile, etc
	var lastTap;
	var tmout;
	var sim = this;
	cv.addEventListener("touchstart", function (e) {
        	mousePos = getTouchPos(cv, e);
  		var touch = e.touches[0];
  		var etype = "mousedown";
  		clearTimeout(tmout);
  		if (e.timeStamp-lastTap < 300) {
     		    etype = "dblclick";
  		} else {
//  		    tmout = setTimeout(function() {
//  		    }, 1000);
  		}
  		lastTap = e.timeStamp;
  		
  		var mouseEvent = new MouseEvent(etype, {
    			clientX: touch.clientX,
    			clientY: touch.clientY
  		});
  		e.preventDefault();
  		cv.dispatchEvent(mouseEvent);
	}, false);
	cv.addEventListener("touchend", function (e) {
  		var mouseEvent = new MouseEvent("mouseup", {});
  		e.preventDefault();
  		clearTimeout(tmout);
  		cv.dispatchEvent(mouseEvent);
	}, false);
	cv.addEventListener("touchmove", function (e) {
  		var touch = e.touches[0];
  		var mouseEvent = new MouseEvent("mousemove", {
    			clientX: touch.clientX,
    			clientY: touch.clientY
  		});
  		e.preventDefault();
  		clearTimeout(tmout);
  		cv.dispatchEvent(mouseEvent);
	}, false);

	// Get the position of a touch relative to the canvas
	function getTouchPos(canvasDom, touchEvent) {
  		var rect = canvasDom.getBoundingClientRect();
  		return {
    			x: touchEvent.touches[0].clientX - rect.left,
    			y: touchEvent.touches[0].clientY - rect.top
  		};
	}
	
    }-*/;
    
    MenuItem getMenuItem(String s, Command cmd) {
        MenuItem mi = new MenuItem(s, cmd);
        return mi;
    }

    CheckboxMenuItem getCheckItem(String s, boolean b) {
    	Command cmd = new Command() {
    		public void execute() { DFilterSim.theSim.checkMenuItemClicked(); }
    	};
        CheckboxMenuItem mi = new CheckboxMenuItem(s, cmd);
        mi.setState(b);
//        mi.addItemListener(this)
        return mi;
    }

    void checkMenuItemClicked() {
        filterChanged = true;
        handleResize();
    }
    
    static int getPower2(int n) {
        int o = 2;
        while (o < n)
            o *= 2;
        return o;
    }
        
    PhaseColor genPhaseColor(int sec, double ang) {
        // convert to 0 .. 2*pi angle
        ang += sec*pi/4;
        // convert to 0 .. 6
        ang *= 3/pi;
        int hsec = (int) ang;
        double a2 = ang % 1;
        double a3 = 1.-a2;
        PhaseColor c = null;
        switch (hsec) {
        case 6:
        case 0: c = new PhaseColor(1, a2, 0); break;
        case 1: c = new PhaseColor(a3, 1, 0); break;
        case 2: c = new PhaseColor(0, 1, a2); break;
        case 3: c = new PhaseColor(0, a3, 1); break;
        case 4: c = new PhaseColor(a2, 0, 1); break;
        case 5: c = new PhaseColor(1, 0, a3); break;
        }
        return c;
    }
    
    class PhaseColor {
        public double r, g, b;
        PhaseColor(double rr, double gg, double bb) {
            r = rr; g = gg; b = bb;
        }
    }

    void handleResize() {
        if (winSize.width == 0)
            return;
        int ct = 1;
        respView = spectrumView = impulseView = phaseView =
            stepView = waveformView = null;
        if (freqCheckItem == null)
        	return;
        if (freqCheckItem.getState())     ct++;
        if (phaseCheckItem   .getState())  ct++;
        if (spectrumCheckItem.getState()) ct++;
        if (waveformCheckItem.getState()) ct++;
        if (impulseCheckItem .getState())  ct++;
        if (stepCheckItem    .getState())  ct++;
        
        int bd = 15;
        
        int i = 0;
        if (freqCheckItem.getState())     respView     = getView(i++, ct);
        if (phaseCheckItem.getState())    phaseView    = getView(i++, ct);
        if (spectrumCheckItem.getState()) spectrumView = getView(i++, ct);
        if (waveformCheckItem.getState()) waveformView = getView(i++, ct);
        if (impulseCheckItem.getState())  impulseView  = getView(i++, ct);
        if (stepCheckItem.getState())     stepView     = getView(i++, ct);
        poleInfoView = getView(i++, ct);
        if (poleInfoView.height > 200)
            poleInfoView.height = 200;
        polesView = new View(poleInfoView.x, poleInfoView.y,
                             poleInfoView.height, poleInfoView.height);
        getPoleBuffer();
    }

    View getView(int i, int ct) {
        int dh3 = winSize.height/ct;
        int bd = 5;
        int tpad = 15;
        return new View(bd, bd+i*dh3+tpad, winSize.width-bd*2, dh3-bd*2-tpad);
    }

    void getPoleBuffer() {
        pixels = null;
        if (pixels == null) {
            imageData = backcontext.createImageData(polesView.width, polesView.height);
            pixels = imageData.getData();
        }
    }
    
    void centerString(Graphics g, String s, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (winSize.width-fm.stringWidth(s))/2, y);
    }

    long lastTime;
    double minlog, logrange;

    void startPlayThread() {
        if (playThread == null && !unstable && soundCheck.getState()) {
            playThread = new PlayThread();
            playThread.start();
            sampleRate = getSampleRate();
        }
    }

    Font font;
    
    public void updateRipple() {
		Graphics g=new Graphics(backcontext);
	
		if (font == null)
			font = new Font("SansSerif", 0, 15);
		g.setFont(font);

	    g.setColor(Color.black);
		g.fillRect(0, 0, g.context.getCanvas().getWidth(), g.context.getCanvas().getHeight());
		g.setColor(Color.white);

        if (curFilter == null) {
            Filter f = filterType.genFilter();
            curFilter = f;
            if (playThread != null)
                playThread.setFilter(f);
            filterChanged = true;
            unstable = false;
        }
        
        if (displayCheck.getState())
            return;
        
        double minf = 40./sampleRate;
        minlog = Math.log(minf);
        logrange = Math.log(.5)-minlog;
        Complex cc = new Complex();
        
        int i;
        if (respView != null) {
            respView.drawLabel(g, "Frequency Response");
            g.setColor(Color.darkGray);
            g.fillRect(respView.x, respView.y, respView.width, respView.height);
            g.setColor(Color.black);
            /*i = respView.x + respView.width/2;
              g.drawLine(i, respView.y, i, respView.y+respView.height);*/
            double ym = .069;
            for (i = 0; ; i += 2) {
                double q = ym*i;
                if (q > 1)
                    break;
                int y = respView.y + (int) (q*respView.height);
                g.drawLine(respView.x, y, respView.right, y);
            }
            for (i = 1; ; i++) {
                double ll = logrange-i*Math.log(2);
                int x = 0;
                if (logFreqCheckItem.getState())
                    x = (int) (ll*respView.width/logrange);
                else
                    x = respView.width/(1<<i);
                if (x <= 0)
                    break;
                x += respView.x;
                g.drawLine(x, respView.y, x, respView.bottom);
            }
            g.setColor(Color.white);
            int ox = -1, oy = -1, ox2 = -1, oy2 = -1;
            for (i = 0; i != respView.width; i++) {
                double w = 0;
                if (!logFreqCheckItem.getState())
                    w = pi*i/(respView.width);
                else {
                    double f = Math.exp(minlog+i*logrange/respView.width);
                    w = 2*pi*f;
                }
                filterType.getResponse(w, cc);
                double bw = cc.magSquared();
                double val = -ym*Math.log(bw*bw)/log10;
                int x = i+respView.x;
                if (val > 1) {
                    if (ox != -1)
                        g.drawLine(ox, oy, ox, respView.bottom);
                    ox = -1;
                } else {
                    int y = respView.y + (int) (respView.height*val);
                    if (ox != -1)
                        g.drawLine(ox, oy, x, y);
                    else if (x > respView.x)
                        g.drawLine(x, respView.bottom, x, y);
                    ox = x;
                    oy = y;
                }
                if (filterType instanceof CustomFIRFilter) {
                    g.setColor(Color.white);
                    CustomFIRFilter cf = (CustomFIRFilter) filterType;
                    bw = cf.getUserResponse(w);
                    val = -ym*Math.log(bw*bw)/log10;
                    if (val > 1) {
                        if (ox2 != -1)
                            g.drawLine(ox2, oy2, ox2, respView.bottom);
                        ox2 = -1;
                    } else {
                        int y = respView.y + (int) (respView.height*val);
                        if (ox2 != -1)
                            g.drawLine(ox2, oy2, x, y);
                        else if (x > respView.x)
                            g.drawLine(x, respView.bottom, x, y);
                        ox2 = x;
                        oy2 = y;
                    }
                    g.setColor(Color.red);
                }
            }
        }
        g.setColor(Color.white);

        if (phaseView != null) {
            phaseView.drawLabel(g, "Phase Response");
            g.setColor(Color.darkGray);
            g.fillRect(phaseView.x, phaseView.y, phaseView.width, phaseView.height);
            g.setColor(Color.black);
            for (i = 0; i < 5; i++) {
                double q = i*.25;
                int y = phaseView.y + (int) (q*phaseView.height);
                g.drawLine(phaseView.x, y, phaseView.right, y);
            }
            for (i = 1; ; i++) {
                double ll = logrange-i*Math.log(2);
                int x = 0;
                if (logFreqCheckItem.getState())
                    x = (int) (ll*phaseView.width/logrange);
                else
                    x = phaseView.width/(1<<i);
                if (x <= 0)
                    break;
                x += phaseView.x;
                g.drawLine(x, phaseView.y, x, phaseView.bottom);
            }
            g.setColor(Color.white);
            int ox = -1, oy = -1;
            for (i = 0; i != phaseView.width; i++) {
                double w = 0;
                if (!logFreqCheckItem.getState())
                    w = pi*i/(phaseView.width);
                else {
                    double f = Math.exp(minlog+i*logrange/phaseView.width);
                    w = 2*pi*f;
                }
                filterType.getResponse(w, cc);
                double val = .5+cc.phase/(2*pi);
                int y = phaseView.y + (int) (phaseView.height*val);
                int x = i+phaseView.x;
                if (ox != -1)
                    g.drawLine(ox, oy, x, y);
                else if (x > phaseView.x)
                    g.drawLine(x, phaseView.bottom, x, y);
                ox = x;
                oy = y;
            }
        }

        int polect = filterType.getPoleCount();
        int zeroct = filterType.getZeroCount();
        int infoX = 10;
        int ph = 0, pw = 0, cx = 0, cy = 0;
        if (poleInfoView != null && (polect > 0 || zeroct > 0 || ferrisCheckItem.getState())) {
            ph = polesView.height/2;
            pw = ph;
            cx = polesView.x + pw;
            cy = polesView.y + ph;
            infoX = cx + pw + 10;

            if (!ferrisCheckItem.getState()) {
                g.setColor(Color.white);
                FontMetrics fm = g.getFontMetrics();
                String s = "Poles/Zeros";
                g.drawString(s, cx-fm.stringWidth(s)/2, polesView.y-5);
                g.drawOval(cx-pw, cy-ph, pw*2, ph*2);
                g.drawLine(cx, cy-ph, cx, cy+ph);
                g.drawLine(cx-ph, cy, cx+ph, cy);
                Complex c1 = new Complex();
                
                // if there are large numbers of poles/zeroes, it can take too long to draw them all, so
                // skip some
                int pstep = 1;
                if (polect > 400)
                	pstep = polect/200;
                if (zeroct > 400)
                	pstep = zeroct/200;
                
                for (i = 0; i < polect; i += pstep) {
                    filterType.getPole(i, c1);
                    g.setColor(i == selectedPole ? Color.yellow : Color.white);
                    int c1x = cx+(int) (pw*c1.re);
                    int c1y = cy-(int) (ph*c1.im);
                    g.drawLine(c1x-3, c1y-3, c1x+3, c1y+3);
                    g.drawLine(c1x-3, c1y+3, c1x+3, c1y-3);
                }
                for (i = 0; i < zeroct; i += pstep) {
                    filterType.getZero(i, c1);
                    g.setColor(i == selectedZero ? Color.yellow : Color.white);
                    int c1x = cx+(int) (pw*c1.re);
                    int c1y = cy-(int) (ph*c1.im);
                    g.drawOval(c1x-3, c1y-3, 6, 6);
                }
                if (filterChanged)
                    setCustomPolesZeros();
            } else {
                if (filterChanged) {
                    int ri, ii;
                    Complex c1 = new Complex();
                    for (ri = 0; ri != polesView.width; ri++)
                        for (ii = 0; ii != polesView.height; ii++) {
                            c1.set((ri-pw)/(double) pw,
                                   (ii-pw)/(double) pw);
                            if (c1.re == 0 && c1.im == 0)
                                c1.set(1e-30);
                            curFilter.evalTransfer(c1);
                            double cv = 0, wv = 0;
                            double m = Math.sqrt(c1.mag);
                            if (m < 1) {
                                cv = m;
                                wv = 1-cv;
                            } else if (m < 2)
                                cv = 2-m;
                            cv *= 255;
                            wv *= 255;
                            double p = c1.phase;
                            if (p < 0)
                                p += 2*pi;
                            if (p >= 2*pi)
                                p -= 2*pi;
                            PhaseColor pc = phaseColors[(int) (p*phaseColorCount/(2*pi))];
                            int i4 = (ri+ii*polesView.width)*4;
                            pixels.set(i4+0, (int)(pc.r*cv+wv));
                            pixels.set(i4+1, (int)(pc.g*cv+wv));
                            pixels.set(i4+2, (int)(pc.b*cv+wv));
                            pixels.set(i4+3, 255);
                        }
                }
                backcontext.putImageData(imageData, polesView.x, polesView.y);
            }
        }
        if (poleInfoView != null) {
            g.setColor(Color.white);
            String info[] = new String[10];
            filterType.getInfo(info);
            for (i = 0; i != 10; i++)
                if (info[i] == null)
                    break;
            if (wformInfo.needsFrequency())
                info[i++] = "Input Freq = " + (int)(inputW*sampleRate/(2*pi));
            info[i++] = "Output adjust = " +
                showFormat.format(-10*Math.log(outputGain)/Math.log(.1)) + " dB";
            for (i = 0; i != 10; i++) {
                if (info[i] == null)
                    break;
                g.drawString(info[i], infoX, poleInfoView.y+5+20*i);
            }
            if ((respView != null && respView.contains(mouseX, mouseY)) ||
                (spectrumView != null && spectrumView.contains(mouseX, mouseY))) {
                double f = getFreqFromX(mouseX, respView);
                if (f >= 0) {
                    double fw = 2*pi*f;
                    f *= sampleRate;
                    g.setColor(Color.yellow);
                    String s = "Selected Freq = " + (int) f;
                    if (respView.contains(mouseX, mouseY)) {
                        filterType.getResponse(fw, cc);
                        double bw = cc.magSquared();
                        bw = Math.log(bw*bw)/(2*log10);
                        s += ", Response = " + showFormat.format(10*bw) + " dB";
                    }
                    g.drawString(s, infoX, poleInfoView.y+5+20*i);
                    if (ph > 0) {
                        int x = cx+(int) (pw*Math.cos(fw));
                        int y = cy-(int) (pw*Math.sin(fw));
                        if (ferrisCheckItem.getState()) {
                            g.setColor(Color.black);
                            g.fillOval(x-3, y-3, 7, 7);
                        }
                        g.setColor(Color.yellow);
                        g.fillOval(x-2, y-2, 5, 5);
                    }
                }
            }
        }

        if (impulseView != null) {
            impulseView.drawLabel(g, "Impulse Response");
            g.setColor(Color.darkGray);
            g.fillRect(impulseView.x, impulseView.y, impulseView.width, impulseView.height);
            g.setColor(Color.black);
            g.drawLine(impulseView.x, impulseView.y+impulseView.height/2,
                       impulseView.x+impulseView.width-1,
                       impulseView.y+impulseView.height/2);
            g.setColor(Color.white);
            int offset = curFilter.getImpulseOffset();
            double impBuf[] = curFilter.getImpulseResponse(offset);
            int len = curFilter.getImpulseLen(offset, impBuf);
            int ox = -1, oy = -1;
            double mult = .5/max(impBuf);
            int flen = (len < 50) ? 50 : len;
            if (len < flen && flen < impBuf.length-offset)
                len = flen;
            //System.out.println("cf " + offset + " " + len + " " + impBuf.length);
            for (i = 0; i != len; i++) {
                int k = offset+i;
                double q = impBuf[k]*mult;
                int y = impulseView.y + (int) (impulseView.height*(.5-q));
                int x = impulseView.x + impulseView.width*i/flen;
                if (len < 100) {
                    g.drawLine(x, impulseView.y + impulseView.height/2, x, y);
                    g.fillOval(x-2, y-2, 5, 5);
                } else {
                    if (ox != -1)
                        g.drawLine(ox, oy, x, y);
                    ox = x;
                    oy = y;
                }
            }
        }

        if (stepView != null) {
            stepView.drawLabel(g, "Step Response");
            g.setColor(Color.darkGray);
            g.fillRect(stepView.x, stepView.y, stepView.width, stepView.height);
            g.setColor(Color.black);
            g.drawLine(stepView.x, stepView.y+stepView.height/2,
                       stepView.x+stepView.width-1,
                       stepView.y+stepView.height/2);
            g.setColor(Color.white);
            int offset = curFilter.getStepOffset();
            double impBuf[] = curFilter.getStepResponse(offset);
            int len = curFilter.getStepLen(offset, impBuf);
            int ox = -1, oy = -1;
            double mult = .5/max(impBuf);
            int flen = (len < 50) ? 50 : len;
            if (len < flen && flen < impBuf.length-offset)
                len = flen;
            //System.out.println("cf " + offset + " " + len + " " + impBuf.length);
            for (i = 0; i != len; i++) {
                int k = offset+i;
                double q = impBuf[k]*mult;
                int y = stepView.y + (int) (stepView.height*(.5-q));
                int x = stepView.x + stepView.width*i/flen;
                if (len < 100) {
                    g.drawLine(x, stepView.y + stepView.height/2, x, y);
                    g.fillOval(x-2, y-2, 5, 5);
                } else {
                    if (ox != -1)
                        g.drawLine(ox, oy, x, y);
                    ox = x;
                    oy = y;
                }
            }
        }

        if (playThread != null) {
            int splen = playThread.spectrumLen;
            if (spectrumBuf == null || spectrumBuf.length != splen*2)
                spectrumBuf = new double[splen*2];
            int off = playThread.spectrumOffset;
            int i2;
            int mask = playThread.fbufmask;
            for (i = i2 = 0; i != splen; i++, i2 += 2) {
                int o = mask&(off+i);
                spectrumBuf[i2] = playThread.fbufLo[o]+playThread.fbufRo[o];
                spectrumBuf[i2+1] = 0;
            }
        } else
            spectrumBuf = null;

        if (waveformView != null && spectrumBuf != null) {
            waveformView.drawLabel(g, "Waveform");
            g.setColor(Color.darkGray);
            g.fillRect(waveformView.x, waveformView.y,
                       waveformView.width, waveformView.height);
            g.setColor(Color.black);
            g.drawLine(waveformView.x, waveformView.y+waveformView.height/2,
                       waveformView.x+waveformView.width-1,
                       waveformView.y+waveformView.height/2);
            g.setColor(Color.white);
            int ox = -1, oy = -1;

            if (waveGain < .1)
                waveGain = .1;
            double max = 0;
            for (i = 0; i != spectrumBuf.length; i += 2) {
                if (spectrumBuf[i] >  max) max = spectrumBuf[i];
                if (spectrumBuf[i] < -max) max = -spectrumBuf[i];
            }
            if (waveGain > 1/max)
                waveGain = 1/max;
            else if (waveGain*1.05 < 1/max)
                waveGain *= 1.05;
            double mult = .5*waveGain;
            int nb = waveformView.width;
            if (nb > spectrumBuf.length || allWaveformCheckItem.getState())
                nb = spectrumBuf.length;
            for (i = 0; i < nb; i += 2) {
                double bf = .5-spectrumBuf[i]*mult;
                int ya = (int) (waveformView.height*bf);
                if (ya > waveformView.height) {
                    ox = -1;
                    continue;
                }
                int y = waveformView.y + ya;
                int x = waveformView.x+i*waveformView.width/nb;
                if (ox != -1)
                    g.drawLine(ox, oy, x, y);
                ox = x;
                oy = y;
            }
        }

        if (spectrumView != null && spectrumBuf != null) {
            spectrumView.drawLabel(g, "Spectrum");
            g.setColor(Color.darkGray);
            g.fillRect(spectrumView.x, spectrumView.y,
                       spectrumView.width, spectrumView.height);
            g.setColor(Color.black);
            double ym = .138;
            for (i = 0; ; i++) {
                double q = ym*i;
                if (q > 1)
                    break;
                int y = spectrumView.y + (int) (q*spectrumView.height);
                g.drawLine(spectrumView.x, y, spectrumView.x+spectrumView.width, y);
            }
            for (i = 1; ; i++) {
                double ll = logrange-i*Math.log(2);
                int x = 0;
                if (logFreqCheckItem.getState())
                    x = (int) (ll*spectrumView.width/logrange);
                else
                    x = spectrumView.width/(1<<i);
                if (x <= 0)
                    break;
                x += spectrumView.x;
                g.drawLine(x, spectrumView.y, x, spectrumView.bottom);
            }
            
            g.setColor(Color.white);
            int isub = spectrumBuf.length/2;
            double cosmult = 2*pi/(spectrumBuf.length-2);
            for (i = 0; i != spectrumBuf.length; i += 2) {
                double ht = .54 - .46*Math.cos(i*cosmult);
                spectrumBuf[i] *= ht;
            }
            if (spectrumFFT == null || spectrumFFT.size != spectrumBuf.length/2)
                spectrumFFT = new FFT(spectrumBuf.length/2);
            spectrumFFT.transform(spectrumBuf, false);
            double logmult = spectrumView.width / Math.log(spectrumBuf.length/2+1);
            
            int ox = -1, oy = -1;
            double bufmult = 1./(spectrumBuf.length/2);
/*            if (logAmpCheckItem.getState())
                bufmult /= 65536;
            else
                bufmult /= 768;*/
            bufmult *= bufmult;

            double specArray[] = new double[spectrumView.width];
            if (logFreqCheckItem.getState()) {
                // freq = i*rate/(spectrumBuf.length)
                // min frequency = 40 Hz
                for (i = 0; i != spectrumBuf.length/2; i += 2) {
                    double f = i/(double) spectrumBuf.length;
                    int ix = (int)
                        (specArray.length*(Math.log(f)-minlog)/logrange);
                    if (ix < 0)
                        continue;
                    specArray[ix] += spectrumBuf[i]*spectrumBuf[i] +
                        spectrumBuf[i+1]*spectrumBuf[i+1];
                }
            } else {
                for (i = 0; i != spectrumBuf.length/2; i += 2) {
                    int ix = specArray.length*i*2/spectrumBuf.length;
                    specArray[ix] += spectrumBuf[i]*spectrumBuf[i] +
                        spectrumBuf[i+1]*spectrumBuf[i+1];
                }
            }

            int maxi = specArray.length;
            for (i = 0; i != spectrumView.width; i++) {
                double bf = specArray[i] * bufmult;
                if (logAmpCheckItem.getState())
                    bf = -ym*Math.log(bf)/log10;
                else
                    bf = 1-bf;

                int ya = (int) (spectrumView.height*bf);
                if (ya > spectrumView.height)
                    continue;
                int y = spectrumView.y + ya;
                int x = spectrumView.x + i*spectrumView.width/maxi;
                g.drawLine(x, y, x, spectrumView.y+spectrumView.height-1);
            }
        }
        
        if (unstable) {
            g.setColor(Color.red);
            centerString(g, "Filter is unstable", winSize.height/2);
        }
        if (mp3Error != null) {
            g.setColor(Color.red);
            centerString(g, mp3Error, winSize.height/2+20);
        }
            
        if (respView != null && respView.contains(mouseX, mouseY)) {
            g.setColor(Color.yellow);
            g.drawLine(mouseX, respView.y,
                       mouseX, respView.y+respView.height-1);
        }
        if (spectrumView != null && spectrumView.contains(mouseX, mouseY)) {
            g.setColor(Color.yellow);
            g.drawLine(mouseX, spectrumView.y,
                       mouseX, spectrumView.y+spectrumView.height-1);
        }
        filterChanged = false;
        
		cvcontext.drawImage(backcontext.getCanvas(), 0.0, 0.0);
    }

    void setCutoff(double f) { }

    void setCustomPolesZeros() {
        if (filterType instanceof CustomIIRFilter)
            return;
        int polect = filterType.getPoleCount();
        int zeroct = filterType.getZeroCount();
        int i, n;
        Complex c1 = new Complex();
        for (i = n = 0; i != polect; i++) {
            filterType.getPole(i, c1);
            if (c1.im >= 0) {
                customPoles[n++].set(c1);
                customPoles[n++].set(c1.re, -c1.im);
                if (n == customPoles.length)
                    break;
            }
        }
        lastPoleCount = n;
        for (i = n = 0; i != zeroct; i++) {
            filterType.getZero(i, c1);
            if (c1.im >= 0) {
                customZeros[n++].set(c1);
                customZeros[n++].set(c1.re, -c1.im);
                if (n == customZeros.length)
                    break;
            }
        }
        lastZeroCount = n;
    }
    
    int countPoints(double buf[], int offset) {
        int len = buf.length;
        double max = 0;
        int i;
        int result = 0;
        double last = 123;
        for (i = offset; i < len; i++) {
            double qa = Math.abs(buf[i]);
            if (qa > max)     max = qa;
            if (Math.abs(qa-last) > max*.003) {
                result = i-offset+1;
                //System.out.println(qa + " " + last + " " + i + " " + max);
            }
            last = qa;
        }
        return result;
    }
    
    double max(double buf[]) {
        int i;
        double max = 0;
        for (i = 0; i != buf.length; i++) {
            double qa = Math.abs(buf[i]);
            if (qa > max)     max = qa;
        }
        return max;
    }

    // get freq (from 0 to .5) given an x coordinate
    double getFreqFromX(int x, View v) {
        double f = .5*(x-v.x)/(double) v.width;
        if (f <= 0 || f >= .5)
            return -1;
        if (logFreqCheckItem.getState())
            return Math.exp(minlog+2*f*logrange);
        return f;
    }

    void setupFilter() {
        int filt = filterChooser.getSelectedIndex();
        switch (filt) {
        case 0: filterType = new SincLowPassFilter();  break;
        case 1: filterType = new SincHighPassFilter();  break;
        case 2: filterType = new SincBandPassFilter();  break;
        case 3: filterType = new SincBandStopFilter();  break;
        case 4: filterType = new CustomFIRFilter(); break;
        case 5: filterType = new NoFilter(); break;
        case 6: filterType = new ButterLowPass(); break;
        case 7: filterType = new ButterHighPass();  break;
        case 8: filterType = new ButterBandPass();  break;
        case 9: filterType = new ButterBandStop();  break;
        case 10: filterType = new ChebyLowPass(); break;
        case 11: filterType = new ChebyHighPass();  break;
        case 12: filterType = new ChebyBandPass();  break;
        case 13: filterType = new ChebyBandStop();  break;
        case 14: filterType = new InvChebyLowPass(); break;
        case 15: filterType = new InvChebyHighPass();  break;
        case 16: filterType = new InvChebyBandPass();  break;
        case 17: filterType = new InvChebyBandStop();  break;
        case 18: filterType = new EllipticLowPass(); break;
        case 19: filterType = new EllipticHighPass();  break;
        case 20: filterType = new EllipticBandPass();  break;
        case 21: filterType = new EllipticBandStop();  break;
        case 22: filterType = new CombFilter(1); break;
        case 23: filterType = new CombFilter(-1); break;
        case 24: filterType = new DelayFilter(); break;
        case 25: filterType = new PluckedStringFilter(); break;
        case 26: filterType = new InverseCombFilter(); break;
        case 27: filterType = new ResonatorFilter(); break;
        case 28: filterType = new ResonatorZeroFilter(); break;
        case 29: filterType = new NotchFilter(); break;
        case 30: filterType = new MovingAverageFilter(); break;
        case 31: filterType = new TriangleFilter(); break;
        case 32: filterType = new AllPassFilter(); break;
        case 33: filterType = new GaussianFilter(); break;
        case 34: filterType = new RandomFilter(); break;
        case 35: filterType = new CustomIIRFilter(); break;
        }
        if (filterSelection != filt) {
            filterSelection = filt;
            int i;
            for (i = 0; i != auxBars.length; i++)
                auxBars[i].setMaximum(999);
            int ax = filterType.select();
            for (i = 0; i != ax; i++) {
                auxLabels[i].setVisible(true);
                auxBars[i].setVisible(true);
            }
            for (i = ax; i != auxBars.length; i++) {
                auxLabels[i].setVisible(false);
                auxBars[i].setVisible(false);
            }
            if (filterType.needsWindow()) {
                windowChooser.setVisible(true);
                setWindow();
            } else {
                windowChooser.setVisible(false);
                setWindow();
            }
//            validate();
        }
        filterType.setup();
        curFilter = null;
    }

    void setInputLabel() {
        wformInfo = getWaveformObject();
        String inText = wformInfo.getInputText();
        if (inText == null) {
            inputLabel.setVisible(false);
            inputBar.  setVisible(false);
        } else {
            inputLabel.setText(inText);
            inputLabel.setVisible(true);
            inputBar.  setVisible(true);
        }
//        validate();
    }

    Waveform getWaveformObject() {
        Waveform wform = null;
        int ic = inputChooser.getSelectedIndex();
        switch (ic) {
        case 0:  wform = new NoiseWaveform(); break;
        case 1:  wform = new SineWaveform(); break;
        case 2:  wform = new SawtoothWaveform(); break;
        case 3:  wform = new TriangleWaveform(); break;
        case 4:  wform = new SquareWaveform(); break;
        case 5:  wform = new PeriodicNoiseWaveform(); break;
        case 6:  wform = new SweepWaveform(); break;
        case 7:  wform = new ImpulseWaveform(); break;
        default: wform = new Mp3Waveform(ic-8); break;
        }
        return wform;
    }

    void loadedFileData(String s) {
//    	console("loadedfiledata " + mp3List.length);
    	inputChooser.select(8+customMp3Index);
    	mp3List[customMp3Index] = s;
    	if (playThread != null)
    		restartPlayThread();
//    	loadAudioFile(s);
    }
    
    void scrollbarMoved() {
    	setupFilter();
    	setInputW();
    }
    
    void setInputW() {
        inputW = pi*inputBar.getValue()/10000.;
    }
    
    public void menuPerformed(String menu, String item) {
    }
    
    void selectPoleZero(int x, int y) {
        selectedPole = selectedZero = -1;
        int i;
        int ph = polesView.height/2;
        int pw = ph;
        int cx = polesView.x + pw;
        int cy = polesView.y + ph;
        Complex c1 = new Complex();
        int polect = filterType.getPoleCount();
        int zeroct = filterType.getZeroCount();
        int bestdist = 10000;
        for (i = 0; i != polect; i++) {
            filterType.getPole(i, c1);
            int c1x = cx+(int) (pw*c1.re);
            int c1y = cy-(int) (ph*c1.im);
            int dist = distanceSq(c1x, c1y, x, y);
            if (dist <= bestdist) {
                bestdist = dist;
                selectedPole = i;
                selectedZero = -1;
            }
        }
        for (i = 0; i != zeroct; i++) {
            filterType.getZero(i, c1);
            int c1x = cx+(int) (pw*c1.re);
            int c1y = cy-(int) (ph*c1.im);
            int dist = distanceSq(c1x, c1y, x, y);
            if (dist < bestdist) {
                bestdist = dist;
                selectedPole = -1;
                selectedZero = i;
            }
        }
    }

    int distanceSq(int x1, int y1, int x2, int y2) {
        return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2);
    }

    void edit(MouseEvent e) {
        if (selection == SELECT_RESPONSE) {
            if (filterType instanceof CustomFIRFilter) {
                editCustomFIRFilter(e);
                return;
            }
            double f = getFreqFromX(e.getX(), respView);
            if (f < 0)
                return;
            filterType.setCutoff(f);
            setupFilter();
        }
        if (selection == SELECT_SPECTRUM) {
            if (!wformInfo.needsFrequency())
                return;
            double f = getFreqFromX(e.getX(), spectrumView);
            if (f < 0)
                return;
            inputW = 2*pi*f;
            inputBar.setValue((int) (20000*f));
        }
        if (selection == SELECT_POLES && filterType instanceof CustomIIRFilter) {
            editCustomIIRFilter(e);
            return;
        }
    }

    void editCustomFIRFilter(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (dragX == x) {
            editCustomFIRFilterPoint(x, y);
            dragY = y;
        } else {
            // need to draw a line from old x,y to new x,y and
            // call editFuncPoint for each point on that line.  yuck.
        	console("edit custom " + x + " " +y+" " + dragX + " " + dragY);
            int x1 = (x < dragX) ? x : dragX;
            int y1 = (x < dragX) ? y : dragY;
            int x2 = (x > dragX) ? x : dragX;
            int y2 = (x > dragX) ? y : dragY;
            dragX = x;
            dragY = y;
            for (x = x1; x <= x2; x++) {
                y = y1+(y2-y1)*(x-x1)/(x2-x1);
                editCustomFIRFilterPoint(x, y);
            }
        }
        setupFilter();
    }

    void editCustomFIRFilterPoint(int x, int y) {
        double xx1 = getFreqFromX(x  , respView)*2;
        double xx2 = getFreqFromX(x+1, respView)*2;
        y -= respView.y;
        double ym = .069;
        double yy = Math.exp(-y*Math.log(10)/(ym*4*respView.height));
        if (yy >= 1)
            yy = 1;
        ((CustomFIRFilter) filterType).edit(xx1, xx2, yy);
    }

    void editCustomIIRFilter(MouseEvent e) {
        if (ferrisCheckItem.getState())
            return;
        int x = e.getX();
        int y = e.getY();
        int ph = polesView.height/2;
        int pw = ph;
        int cx = polesView.x + pw;
        int cy = polesView.y + ph;
        Complex c1 = new Complex();
        c1.set((x-cx)/(double) pw, (y-cy)/(double) ph);
        ((CustomIIRFilter) filterType).editPoleZero(c1);
        setupFilter();
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<Boolean> e) {
        filterChanged = true;
        if (e.getSource() == displayCheck) {
            return;
        }
        if ((e.getSource()) == shiftSpectrumCheck) {
            if (shiftSpectrumCheck.getState()) {
                shiftFreqLabel.setVisible(true);
                shiftFreqBar  .setVisible(true);
            } else {
                shiftFreqLabel.setVisible(false);
                shiftFreqBar  .setVisible(false);
            }
//            validate();
        }
        if (e.getSource() instanceof CheckboxMenuItem)
            handleResize();
        else
            setupFilter();
        startPlayThread();
    }

    void restartPlayThread() {
        if (playThread != null) {
            playThread.requestShutdown();
            startPlayThread();
        }
    }
    
	@Override
	public void onChange(ChangeEvent e) {
        filterChanged = true;
        if (e.getSource() == inputChooser) {
        	restartPlayThread();
          setInputLabel();
      }
      if ((e.getSource()) == rateChooser) {
      		restartPlayThread();
             inputW *= sampleRate;
          switch (rateChooser.getSelectedIndex()) {
          case 0: sampleRate = 8000; break;
          case 1: sampleRate = 11025; break;
          case 2: sampleRate = 16000; break;
          case 3: sampleRate = 22050; break;
          case 4: sampleRate = 32000; break;
          case 5: sampleRate = 44100; break;
          }
          inputW /= sampleRate;
      }
        if ((e.getSource()) == windowChooser)
            setWindow();
        setupFilter();
	}


    void setWindow() {
        if (windowChooser.getSelectedIndex() == WINDOW_KAISER &&
            filterType.needsWindow()) {
            kaiserLabel.setVisible(true);
            kaiserBar  .setVisible(true);
        } else {
            kaiserLabel.setVisible(false);
            kaiserBar  .setVisible(false);
        }
//        validate();
    }
    
    void setSampleRate(int r) {
    	/*
        int x = 0;
        switch (r) {
        case 8000:  x = 0; break;
        case 11025: x = 1; break;
        case 16000: x = 2; break;
        case 22050: x = 3; break;
        case 32000: x = 4; break;
        case 44100: x = 5; break;
        }
        rateChooser.select(x);
        */
        sampleRate = r;
    }
    
	@Override
	public void onMouseUp(MouseUpEvent event) {
		event.preventDefault();
		dragging = false;
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		event.preventDefault();
        doMouseMove(event);
	}
	
	void doMouseMove(MouseEvent<?> e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (!dragging) {
        	dragX = mouseX;
        	dragY = mouseY;
        }
        if (respView != null && respView.contains(e.getX(), e.getY()))
            selection = SELECT_RESPONSE;
        if (spectrumView != null && spectrumView.contains(e.getX(), e.getY()))
            selection = SELECT_SPECTRUM;
        if (polesView != null && polesView.contains(e.getX(), e.getY()) &&
              !ferrisCheckItem.getState()) {
            selection = SELECT_POLES;
            selectPoleZero(e.getX(), e.getY());
        }
        if (dragging)
        	edit(e);
	}

    
	void dragMouse(MouseEvent<?> event) {
			}

	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		event.preventDefault();
        dragging = true;
        doMouseMove(event);
	}
	
	@Override
	public void onMouseWheel(MouseWheelEvent event) {
        event.preventDefault();
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		dragging = false;
	}

	@Override
	public void onPreviewNativeEvent(NativePreviewEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		event.preventDefault();
	}

	@Override
	public void onClick(ClickEvent event) {
		event.preventDefault();
	}
	
    String getUnitText(double v, String u) {
        double va = Math.abs(v);
        if (va < 1e-17)
            return "0 " + u;
        if (va < 1e-12)
            return showFormat.format(v*1e15) + " f" + u;
        if (va < 1e-9)
            return showFormat.format(v*1e12) + " p" + u;
        if (va < 1e-6)
            return showFormat.format(v*1e9) + " n" + u;
        if (va < 1e-3)
            return showFormat.format(v*1e6) + " \u03bc" + u;
        if (va < 1e-2 || (u.compareTo("m") != 0 && va < 1))
            return showFormat.format(v*1e3) + " m" + u;
        if (va < 1)
            return showFormat.format(v*1e2) + " c" + u;
        if (va < 1e3)
            return showFormat.format(v) + " " + u;
        if (va < 1e6)
            return showFormat.format(v*1e-3) + " k" + u;
        if (va < 1e9)
            return showFormat.format(v*1e-6) + " M" + u;
        if (va < 1e12)
            return showFormat.format(v*1e-9) + " G" + u;
        if (va < 1e15)
            return showFormat.format(v*1e-12) + " T" + u;
        return v + " " + u;
    }

}
