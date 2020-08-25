/*
    Copyright (C) 2017 by Paul Falstad

    This file is part of Fourier.

    Fourier is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    Fourier is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Fourier.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.falstad.fourier.client;

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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

import static com.google.gwt.event.dom.client.KeyCodes.*; 

import java.util.HashMap;
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

public class FourierSim implements MouseDownHandler, MouseMoveHandler,
		MouseUpHandler, ClickHandler, DoubleClickHandler,
		NativePreviewHandler, MouseOutHandler, MouseWheelHandler, ChangeHandler,
		ValueChangeHandler<Boolean> {

	Logger logger = Logger.getLogger(FourierSim.class.getName());
	
	Dimension winSize;
	Random random;
    int maxSampleCount = 70; // was 50
    int sampleCountR, sampleCountTh;
    int modeCountR, modeCountTh;
    int maxDispRModes = 5, maxDispThModes = 5;
    public static final double epsilon = .00001;
    public static final double epsilon2 = .003;
    public static final double log10 = 2.30258509299404568401;
    public static int WINDOW_KAISER = 4;

    Button sineButton;
    Button cosineButton;
    Button rectButton;
    Button fullRectButton;
    Button triangleButton;
    Button sawtoothButton;
    Button squareButton;
    Button noiseButton;
    Button blankButton;
    Button phaseButton;
    Button clipButton;
    Button resampleButton;
    Button quantizeButton;
    Button highPassButton;
    Checkbox magPhaseCheck;
    Checkbox soundCheck;
    Checkbox logCheck;
    Scrollbar termBar;
    Scrollbar freqBar;
    CheckboxMenuItem expansionCheckItem;
    
    MenuItem exitItem;
    
    double magcoef[];
    double phasecoef[];
    boolean mutes[], solos[], hasSolo;
    static final double pi = 3.14159265358979323846;
    double func[];
    int maxTerms = 250; // 160;
    int selectedCoef;
    static final int SEL_NONE = 0;
    static final int SEL_FUNC = 1;
    static final int SEL_MAG = 2;
    static final int SEL_PHASE = 3;
    static final int SEL_MUTES = 4;
    static final int SEL_SOLOS = 5;
    int quantizeCount, resampleCount;
    boolean dragging, freqAdjusted;
    View viewFunc, viewMag, viewPhase, viewMutes, viewSolos;
    FFT fft;

    int selection;
    boolean editingFunc;
    boolean dragStop;
    double inputW;
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
    boolean unstable;
    Image memimage;
    double t;
    int pause;
    PlayThread playThread;
    double spectrumBuf[];
    double rms;
    boolean filterChanged;
    HashMap<String,String> showTable;

    class View extends Rectangle {
        View(int x, int y, int w, int h) {
            super(x, y, w, h);
            midy = y+h/2;
            ymult = .6 * h/2;
            periodWidth = w/3;
            labely = midy - 5 - h*3/8;
        }
        int midy, labely;
        double ymult;
        int periodWidth;
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
	String versionString = "1.1";
	static FourierSim theSim;
    NumberFormat showFormat, showFormat2, rmsFormat;

    public static final int sampleCount = 1024;
    static final double step = 2 * pi / sampleCount;
    public static final int halfSampleCount = sampleCount/2;
    public static final double halfSampleCountFloat = sampleCount/2;
    final int rate = 44100;
    final int playSampleCount = 131072; // 16384;

	static final int MENUBARHEIGHT = 30;
	static final int MAXVERTICALPANELWIDTH = 166;
	static final int POSTGRABSQ = 16;

	int getrand(int x) {
		return random.nextInt(x);
	}

	public void setCanvasSize() {
		int width, height;
		int fullwidth = width = (int) RootLayoutPanel.get().getOffsetWidth();
		height = (int) RootLayoutPanel.get().getOffsetHeight();
		height = height - MENUBARHEIGHT;   // put this back in if we add a menu bar
		width = width - MAXVERTICALPANELWIDTH;
//		width = height = (width < height) ? width : height;
		winSize = new Dimension(width, height);
		verticalPanelWidth = fullwidth-width;
		if (layoutPanel != null)
			layoutPanel.setWidgetSize(verticalPanel, verticalPanelWidth);
		if (termBar != null) {
			termBar.setWidth(verticalPanelWidth);
			freqBar.setWidth(verticalPanelWidth);
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
			@com.falstad.fourier.client.FourierSim::process(*)(li, ri, lo, ro);
		}
	}-*/;

    static void process(JsArrayNumber leftIn, JsArrayNumber rightIn, JsArrayNumber leftOut, JsArrayNumber rightOut) {
		PlayThread playThread = FourierSim.theSim.playThread;
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
	
    boolean mustShow(String s) {
        return showTable == null || showTable.containsKey(s);
    }
    
    Button doButton(String s) {
        final Button b = new Button(s);
        if (mustShow(s))
            verticalPanel.add(b);
        b.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              pressButton(b);
            }
          });
        return b;
    }
    
    QueryParameters qp;
    
    Checkbox doCheckbox(String s) {
        Checkbox b = new Checkbox(s);
        if (mustShow(s))
        	verticalPanel.add(b);
        try {
            String param = qp.getValue(s);
            if (param != null && param.equalsIgnoreCase("true"))
                b.setState(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        b.addValueChangeHandler(this);
        return b;
    }

	public void init() {
		theSim = this;
		

        qp = new QueryParameters();
        String state = "";
      
        try {
            String show = qp.getValue("show");
            if (show != null) {
                showTable = new HashMap<String,String>(10);
                StringTokenizer st =
                    new StringTokenizer(show, ",");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    showTable.put(s, "");
                }
                showTable.put("Sound", "");
            }
            state = qp.getValue("state");
            if (state == null)
            	state = "";
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
		
        selectedCoef = -1;
        magcoef = new double[maxTerms];
        phasecoef = new double[maxTerms];
        mutes = new boolean[maxTerms];
        solos = new boolean[maxTerms];
        
        // we have func[0] == func[sampleCount] to make drawing function slightly easier
        func = new double[sampleCount+1];
        
        random = new Random();
        fft = new FFT(sampleCount);

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
        
        MenuBar am = new MenuBar(true);
        am.addItem(getMenuItem("Reverse Polarity", new Command() {
        	public void execute() { reversePolarity(); }
        }));
        am.addItem(getMenuItem("Reverse Time", new Command() {
        	public void execute() { reverseTime(); }
        }));
        am.addItem(getMenuItem("Only Even", new Command() {
        	public void execute() { onlyEven(1); }
        }));
        am.addItem(getMenuItem("Only Odd", new Command() {
        	public void execute() { onlyEven(0); }
        }));
        mb.addItem("Actions", am);
        
        MenuBar om = new MenuBar(true);
        om.addItem(expansionCheckItem = new CheckboxMenuItem("Show Full Expansion", new Command() {
        	public void execute() { repaint(); }
        }));
        mb.addItem("Options", om);
        
        layoutPanel.addNorth(mb, MENUBARHEIGHT);
        layoutPanel.addEast(verticalPanel, verticalPanelWidth);
        RootLayoutPanel.get().add(layoutPanel);

        
        
        sineButton = doButton("Sine");
        cosineButton = doButton("Cosine");
        triangleButton = doButton("Triangle");
        sawtoothButton = doButton("Sawtooth");
        squareButton = doButton("Square");
        noiseButton = doButton("Noise");
        phaseButton = doButton("Phase Shift");
        clipButton = doButton("Clip");
        resampleButton = doButton("Resample");
        quantizeButton = doButton("Quantize");
        rectButton = doButton("Rectify");
        fullRectButton = doButton("Full Rectify");
        highPassButton = doButton("High-Pass Filter");
        blankButton = doButton("Clear");
    
        soundCheck = doCheckbox("Sound");
        magPhaseCheck = doCheckbox("Mag/Phase View");
        logCheck = doCheckbox("Log View");
        logCheck.setEnabled(false);
        if (mustShow("Terms"))
            verticalPanel.add(new Label("Number of Terms"));
        termBar = new Scrollbar(Scrollbar.HORIZONTAL, 50,
                                1, 1, maxTerms,
                    			new Command() {
                    		public void execute() { scrollbarMoved(); } });
        if (mustShow("Terms"))
        	verticalPanel.add(termBar);
        verticalPanel.add(new Label("Playing Frequency"));
        freqBar = new Scrollbar(Scrollbar.HORIZONTAL, 251*2, 1, -123*2, 500*2, // was -100
    			new Command() {
    		public void execute() { scrollbarMoved(); freqAdjusted = true; } });
        	verticalPanel.add(freqBar);
        	verticalPanel.add(new Label("http://www.falstad.com"));

        
    	verticalPanel.add(iFrame = new Frame("iframe.html"));
    	iFrame.setWidth(verticalPanelWidth+"px");
    	iFrame.setHeight("100 px");
    	iFrame.getElement().setAttribute("scrolling", "no");

        random = new Random();

        if (state.equalsIgnoreCase("square"))
            doSquare();
        else if (state.equalsIgnoreCase("sine"))
            doSine();
        else if (state.equalsIgnoreCase("triangle"))
            doTriangle();
        else if (state.equalsIgnoreCase("noise"))
            doNoise();
        else if (state.equalsIgnoreCase("quant")) {
            doSine();
            doQuantize();
        } else if (state.equalsIgnoreCase("resample")) {
            doSine();
            doResample();
        } else if (state.equalsIgnoreCase("clip")) {
            doSine();
            doClip();
        } else if (state.equalsIgnoreCase("rect")) {
            doSine();
            doRect();
        } else if (state.equalsIgnoreCase("fullrect")) {
            doSine();
            doFullRect();
        } else if (state.equalsIgnoreCase("fullsaw")) {
            doSawtooth();
            doFullRect();
        } else if (state.equalsIgnoreCase("beats"))
            doBeats();
        else if (state.equalsIgnoreCase("loudsoft"))
            doLoudSoft();
        else
            doSawtooth();

//        l.addStyleName("topSpace");
//		resBar.setWidth(verticalPanelWidth);
//		dampingBar.setWidth(verticalPanelWidth);

        showFormat=NumberFormat.getFormat("####.#####");
        showFormat2=NumberFormat.getFormat("####.##");
		rmsFormat=NumberFormat.getFormat("####.###");
		
		cv.addMouseMoveHandler(this);
		cv.addMouseDownHandler(this);
		cv.addMouseOutHandler(this);
		cv.addMouseUpHandler(this);
        cv.addMouseWheelHandler(this);
        cv.addClickHandler(this);
		Event.addNativePreviewHandler(this);        
        doTouchHandlers(cv.getCanvasElement());
//		cv.addDomHandler(this,  ContextMenuEvent.getType());
		
		setCanvasSize();
		layoutPanel.add(cv);
//		timer.scheduleRepeating(FASTTIMER);
		repaint();
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
        
    void handleResize() {
        if (winSize.width == 0)
            return;
        if (magPhaseCheck == null)
        	return;
        int margin = 20;
        Dimension d = winSize;
        int pheight = (d.height-margin*2)/3;
        viewFunc = new View(0, 0, d.width, pheight);
        int y = pheight + margin*2;
        viewMag = new View(0, y, d.width, pheight);
        if (magPhaseCheck.getState()) {
            viewMag.ymult *= 1.6;
            viewMag.midy += (int) viewMag.ymult/2;
            logCheck.setEnabled(true);
        } else {
            logCheck.setEnabled(false);
            logCheck.setState(false);
        }
        y += pheight;
        viewPhase = new View(0, y, d.width, pheight);
        int pmy = viewPhase.midy + (int) viewPhase.ymult + 10;
        int h = (d.height-pmy)/2;
        //System.out.println("height " + h);
        viewMutes = new View(0, pmy, d.width, h);
        viewSolos = new View(0, pmy+h, d.width, h);
        //System.out.println(viewMutes + " " + viewSolos + " " +d.height);
        repaint();
    }

    void doBeats() {
        int x;
        for (x = 0; x != sampleCount; x++) {
            double q = (x-halfSampleCount)*step;
            func[x] = .5*(Math.cos(q*20) + Math.cos(q*21));
        }
        func[sampleCount] = func[0];
        transform();
        freqBar.setValue(-100*2/3-7);
    }

    void doLoudSoft() {
        int x;
        for (x = 0; x != sampleCount; x++) {
            double q = (x-halfSampleCount)*step;
            func[x] = Math.cos(q) + .05 * Math.cos(q*10);
        }
        func[sampleCount] = func[0];
        transform();
    }

    void doSawtooth() {
        int x;
        for (x = 0; x != sampleCount; x++)
            func[x] = (x-sampleCount/2) / halfSampleCountFloat;
        func[sampleCount] = func[0];
        transform();
    }

    void doTriangle() {
        int x;
        for (x = 0; x != halfSampleCount; x++) {
            func[x] = (x*2-halfSampleCount) / halfSampleCountFloat;
            func[x+halfSampleCount] =
                ((halfSampleCount-x)*2-halfSampleCount) / halfSampleCountFloat;
        }
        func[sampleCount] = func[0];
        transform();
    }

    void doSine() {
        int x;
        for (x = 0; x != sampleCount; x++) {
            func[x] = Math.sin((x-halfSampleCount)*step);
        }
        func[sampleCount] = func[0];
        transform();
    }

    void doCosine() {
        int x;
        for (x = 0; x != sampleCount; x++) {
            func[x] = Math.cos((x-halfSampleCount)*step);
        }
        func[sampleCount] = func[0];
        transform();
    }

    void doRect() {
        int x;
        for (x = 0; x != sampleCount; x++)
            if (func[x] < 0)
                func[x] = 0;
        func[sampleCount] = func[0];
        transform();
    }

    void doFullRect() {
        int x;
        for (x = 0; x != sampleCount; x++)
            if (func[x] < 0)
                func[x] = -func[x];
        func[sampleCount] = func[0];
        transform();
    }

    void doHighPass() {
        int i;
        int terms = termBar.getValue();
        for (i = 0; i != terms; i++)
            if (magcoef[i] != 0) {
                magcoef[i] = 0;
                break;
            }
        doSetFunc();
    }
    
    void doSquare() {
        int x;
        for (x = 0; x != halfSampleCount; x++) {
            func[x] = -1;
            func[x+halfSampleCount] = 1;
        }
        func[sampleCount] = func[0];
        transform();
    }

    void doNoise() {
        int x;
        int blockSize = 3;
        for (x = 0; x != sampleCount/blockSize; x++) {
            double q = Math.random() *2 - 1;
            int i;
            for (i = 0; i != blockSize; i++)
                func[x*blockSize+i] = q;
        }
        func[sampleCount] = func[0];
        transform();
    }

    void doPhaseShift() {
        int i;
        int sh = sampleCount/20;
        double copyf[] = new double[sh];
        for (i = 0; i != sh; i++)
            copyf[i] = func[i];
        for (i = 0; i != sampleCount-sh; i++)
            func[i] = func[i+sh];
        for (i = 0; i != sh; i++)
            func[sampleCount-sh+i] = copyf[i];
        func[sampleCount] = func[0];
        transform();
    }

    void doBlank() {
        int x;
        for (x = 0; x <= sampleCount; x++)
            func[x] = 0;
        for (x = 0; x != termBar.getValue(); x++)
            mutes[x] = solos[x] = false;
        transform();
    }

    void doSetFunc() {
        int i;
        double data[] = new double[sampleCount*2];
        int terms = termBar.getValue();
        for (i = 0; i != terms; i++) {
            int sgn = (i & 1) == 1 ? -1 : 1;
            data[i*2]   =  sgn*magcoef[i]*Math.cos(phasecoef[i]);
            data[i*2+1] = -sgn*magcoef[i]*Math.sin(phasecoef[i]);
        }
        fft.transform(data, true);
        for (i = 0; i != sampleCount; i++)
            func[i] = data[i*2];
        func[sampleCount] = func[0];
        calcRMS();
        updateSound();
    }

    void updateSound() {
        if (playThread != null)
            playThread.soundChanged();
    }
    
    void calcRMS() {
    	rms = 0;
    	int i;
    	for (i = 0; i != sampleCount; i++)
    		rms += func[i]*func[i];
    	rms = Math.sqrt(rms/sampleCount);
    }

    void doClip() {
        int x;
        double mult = 1.2;
        for (x = 0; x != sampleCount; x++) {
            func[x] *= mult;
            if (func[x] > 1)
                func[x] = 1;
            if (func[x] < -1)
                func[x] = -1;
        }
        func[sampleCount] = func[0];
        transform();
    }

    void reversePolarity() {
    	int i;
    	for (i = 0; i != sampleCount; i++)
    		func[i] *= -1;
    	transform();
    	repaint();
    }
    
    void reverseTime() {
    	int i;
    	for (i = 0; i != sampleCount/2; i++) {
    		double q = func[i];
    		func[i] = func[sampleCount-1-i];
    		func[sampleCount-1-i] = q;
    	}
    	transform();
    	repaint();
    }
    
    void onlyEven(int x) {
        int i;
        int terms = termBar.getValue();
        for (i = x; i < terms; i += 2)
        	magcoef[i] = 0;
        doSetFunc();
    	repaint();
    }

    void doResample() {
        int x, i;
        if (resampleCount == 0)
            resampleCount = 32;
        if (resampleCount == sampleCount)
            return;
        for (x = 0; x != sampleCount; x += resampleCount) {
            for (i = 1; i != resampleCount; i++)
                func[x+i] = func[x];
        }
        func[sampleCount] = func[0];
        transform();
        resampleCount *= 2;
    }

    double origFunc[];
    void doQuantize() {
        int x;
        if (quantizeCount == 0) {
            quantizeCount = 8;
            origFunc = new double[sampleCount];
            System.arraycopy(func, 0, origFunc, 0, sampleCount);
        }
        for (x = 0; x != sampleCount; x++) {
            func[x] = Math.round(origFunc[x]*quantizeCount)/
                (double) quantizeCount;
        }
        func[sampleCount] = func[0];
        transform();
        quantizeCount /= 2;
    }

    int dfreq0;
    double getFreq() {
        // get approximate freq from slider (log scale)
    	int v = freqBar.getValue();
    	if (v < 0)
    		v *= 3;
        double freq = 27.5*Math.exp(v*.004158883084);
        // get offset into FFT array for frequency selected (as close as possible;
        // it can't be exact because we use an FFT to generate the wave, and so the
        // frequency choices must be integer multiples of a base frequency)
        dfreq0 = ((int)(freq*(double) playSampleCount/rate))*2;
        // get exact frequency being played
        return rate*dfreq0/(playSampleCount*2.);
    }

    void transform() {
        int y;
        double data[] = new double[sampleCount*2];
        int i;
        for (i = 0; i != sampleCount; i++)
            data[i*2] = func[i];
        fft.transform(data, false);
        double epsilon = .00001;
        double mult = 2./sampleCount;
        for (y = 0; y != maxTerms; y++) {
            double acoef =  data[y*2  ]*mult;
            double bcoef = -data[y*2+1]*mult;
            if ((y & 1) == 1)
                acoef = -acoef;
            else
                bcoef = -bcoef;
            //System.out.println(y + " " + acoef + " " + bcoef);
            if (acoef < epsilon && acoef > -epsilon) acoef = 0;
            if (bcoef < epsilon && bcoef > -epsilon) bcoef = 0;
            if (y == 0) {
                magcoef[0] = acoef / 2;
                phasecoef[0] = 0;
            } else {
                magcoef[y] = Math.sqrt(acoef*acoef+bcoef*bcoef);
                phasecoef[y] = Math.atan2(-bcoef, acoef);
            }
            // System.out.print("phasecoef " + phasecoef[y] + "\n");
        }
        calcRMS();
        updateSound();
    }

    View getView(int i, int ct) {
        int dh3 = winSize.height/ct;
        int bd = 5;
        int tpad = 15;
        return new View(bd, bd+i*dh3+tpad, winSize.width-bd*2, dh3-bd*2-tpad);
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
    
    public void updateFourier() {
		Graphics g=new Graphics(backcontext);
	
		if (font == null)
			font = new Font("SansSerif", 0, 15);
		g.setFont(font);

	    g.setColor(Color.black);
		g.fillRect(0, 0, g.context.getCanvas().getWidth(), g.context.getCanvas().getHeight());
		g.setColor(Color.white);

        int i;
        int ox = -1, oy = -1;
        int midy = viewFunc.midy;
        int periodWidth = viewFunc.periodWidth;
        double ymult = viewFunc.ymult;
        Color gray1 = new Color(76,  76,  76);
        Color gray2 = new Color(127, 127, 127);

        for (i = -1; i <= 1; i++) {
            g.setColor((i == 0) ? gray2 : gray1);
            g.drawLine(0,             midy+(i*(int) ymult),
                       winSize.width, midy+(i*(int) ymult));
        }
        for (i = 2; i <= 4; i++) {
            g.setColor((i == 3) ? gray2 : gray1);
            g.drawLine(periodWidth*i/2, midy-(int) ymult,
                       periodWidth*i/2, midy+(int) ymult);
        }
        g.setColor(Color.white);
        if (!(dragging && selection != SEL_FUNC)) {
            for (i = 0; i != sampleCount+1; i++) {
                int x = periodWidth * i / sampleCount;
                int y = midy - (int) (ymult * func[i]);
                if (ox != -1) {
                    g.drawLine(ox, oy, x, y);
                    g.drawLine(ox+periodWidth, oy,   x+periodWidth,   y);
                    g.drawLine(ox+periodWidth*2, oy, x+periodWidth*2, y);
                }
                ox = x;
                oy = y;
            }
        }
        int terms = termBar.getValue();
        if (!(dragging && selection == SEL_FUNC)) {
            g.setColor(Color.red);
            ox = -1;
            for (i = 0; i != sampleCount+1; i++) {
                int x = periodWidth * i / sampleCount;
                int j;
                double dy = 0;
                for (j = 0; j != terms; j++) {
                    dy += magcoef[j] * Math.cos(
                        step*(i-halfSampleCount)*j+phasecoef[j]);
                }
                int y = midy - (int) (ymult * dy);
                if (ox != -1) {
                    g.drawLine(ox, oy, x, y);
                    g.drawLine(ox+periodWidth, oy,   x+periodWidth,   y);
                    g.drawLine(ox+periodWidth*2, oy, x+periodWidth*2, y);
                }
                ox = x;
                oy = y;
            }
        }
        int texty = viewFunc.height+15;
        g.setColor(Color.white);
        centerString(g, "RMS " + rmsFormat.format(rms), viewFunc.height);
        g.setColor(Color.yellow);
        if (selectedCoef != -1)
        	showHarmonic(g, texty);
        else if (freqAdjusted) {
        	// adjusting frequency bar, show new fundamental frequency
            centerString(g, formatFreq(getFreq()) + " Hz", texty);
        } else if (expansionCheckItem.getState())
        	showExpansion(g, texty);
        freqAdjusted = false;
        int termWidth = getTermWidth();
        
        ymult = viewMag.ymult;
        midy = viewMag.midy;
        g.setColor(Color.white);
        if (magPhaseCheck.getState()) {
            centerString(g, "Magnitudes", viewMag.labely);
            centerString(g, "Phases", viewPhase.labely);
            g.setColor(gray2);
            g.drawLine(0, midy, winSize.width, midy);
            g.setColor(gray1);
            g.drawLine(0, midy-(int)ymult, winSize.width, midy-(int) ymult);
            int dotSize = termWidth-3;
            for (i = 0; i != terms; i++) {
                int t = termWidth * i + termWidth/2;
                int y = midy - (int) (showMag(i)*ymult);
                g.setColor(i == selectedCoef ? Color.yellow : Color.white);
                g.drawLine(t, midy, t, y);
                g.fillOval(t-dotSize/2, y-dotSize/2, dotSize, dotSize);
            }
            
            ymult = viewPhase.ymult;
            midy = viewPhase.midy;
            for (i = -2; i <= 2; i++) {
                g.setColor((i == 0) ? gray2 : gray1);
                g.drawLine(0,             midy+(i*(int) ymult)/2,
                           winSize.width, midy+(i*(int) ymult)/2);
            }
            ymult /= pi;
            for (i = 0; i != terms; i++) {
                int t = termWidth * i + termWidth/2;
                int y = midy - (int) (phasecoef[i]*ymult);
                g.setColor(i == selectedCoef ? Color.yellow : Color.white);
                g.drawLine(t, midy, t, y);
                g.fillOval(t-dotSize/2, y-dotSize/2, dotSize, dotSize);
            }
        } else {
            centerString(g, "Sines", viewMag.labely);
            centerString(g, "Cosines", viewPhase.labely);
            g.setColor(gray2);
            g.drawLine(0, midy, winSize.width, midy);
            g.setColor(gray1);
            g.drawLine(0, midy-(int)ymult, winSize.width, midy-(int) ymult);
            g.drawLine(0, midy+(int)ymult, winSize.width, midy+(int) ymult);
            int dotSize = termWidth-3;
            for (i = 1; i != terms; i++) {
                int t = termWidth * i + termWidth/2;
                int y = midy + (int) (magcoef[i]*Math.sin(phasecoef[i])*ymult);
                g.setColor(i == selectedCoef ? Color.yellow : Color.white);
                g.drawLine(t, midy, t, y);
                g.fillOval(t-dotSize/2, y-dotSize/2, dotSize, dotSize);
            }
            
            ymult = viewPhase.ymult;
            midy = viewPhase.midy;
            for (i = -2; i <= 2; i += 2) {
                g.setColor((i == 0) ? gray2 : gray1);
                g.drawLine(0,             midy+(i*(int) ymult)/2,
                           winSize.width, midy+(i*(int) ymult)/2);
            }
            for (i = 0; i != terms; i++) {
                int t = termWidth * i + termWidth/2;
                int y = midy - (int) (magcoef[i]*Math.cos(phasecoef[i])*ymult);
                g.setColor(i == selectedCoef ? Color.yellow : Color.white);
                g.drawLine(t, midy, t, y);
                g.fillOval(t-dotSize/2, y-dotSize/2, dotSize, dotSize);
            }
        }
        double basef = getFreq();
        if (viewMutes.height > 8) {
            Font f = new Font("SansSerif", 0, viewMutes.height*3/5);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            for (i = 1; i != terms; i++) {
                if (basef*i > rate/2)
                    break;
                int t = termWidth * i + termWidth/2;
                int y = viewMutes.y + fm.getAscent();
                g.setColor(i == selectedCoef ? Color.yellow : Color.white);
                if (hasSolo && !solos[i])
                    g.setColor(Color.gray);
                String pm = "m";
                if (mutes[i])
                    pm = "M";
                int w = fm.stringWidth(pm);
                g.drawString(pm, t-w/2, y);
                y = viewSolos.y + fm.getAscent();
                pm = "s";
                if (solos[i])
                    pm = "S";
                w = fm.stringWidth(pm);
                g.drawString(pm, t-w/2, y);
            }
        }

		cvcontext.drawImage(backcontext.getCanvas(), 0.0, 0.0);
    }

    String formatFreq(double f) {
    	if (f >= 1000)
    		return "" + (int)f;
    	return showFormat2.format(f);
    }
    
    double showMag(int n) {
        double m = magcoef[n];
        if (!logCheck.getState() || n == 0)
            return m;
        m = Math.log(m)/6.+1;
        //System.out.println(magcoef[i] + " " + m);
        return (m < 0) ? 0 : m;
    }

    double getMagValue(double m) {
        if (!logCheck.getState())
            return m;
        if (m == 0)
            return 0;
        return Math.exp(6*(m-1));
    }
    
    void showHarmonic(Graphics g, int texty) {
        g.setColor(Color.yellow);
        int periodWidth = viewFunc.periodWidth;
        int ox = -1;
        double phase = phasecoef[selectedCoef];
        int x;
        double mag = magcoef[selectedCoef];
        if (!magPhaseCheck.getState()) {
            if (selection == SEL_MAG) {
                mag *= -Math.sin(phase);
                phase = -pi/2;
            } else {
                mag *= Math.cos(phase);
                phase = 0;
            }
        }
        double ymult = viewFunc.ymult;
        ymult *= mag;
        int i, oy = 0;
        int midy = viewFunc.midy;
        if (!dragging) {
            for (i = 0; i != sampleCount+1; i++) {
                x = periodWidth * i / sampleCount;
                double dy = Math.cos(
                        step*(i-halfSampleCount)*selectedCoef+phase);
                int y = midy - (int) (ymult * dy);
                if (ox != -1) {
                    g.drawLine(ox, oy, x, y);
                    g.drawLine(ox+periodWidth, oy,   x+periodWidth,   y);
                    g.drawLine(ox+periodWidth*2, oy, x+periodWidth*2, y);
                }
                ox = x;
                oy = y;
            }
        }
        if (selectedCoef > 0) {
        	double f = (getFreq() * selectedCoef);
        	String s = formatFreq(f) + " Hz";
        	if (f > rate/2)
        		s += " (filtered)";
        	else if (mutes[selectedCoef])
        		s += " (muted)";
        	else if (hasSolo)
        		s += (solos[selectedCoef]) ? " (solo)" : " (muted)";
            centerString(g, s, texty);
        }
        if (selectedCoef != -1) {
            String harm;
            if (selectedCoef == 0)
                harm = showFormat.format(mag) + "";
            else {
                String func = "cos";
                if (!magPhaseCheck.getState() && selection == SEL_MAG)
                    func = "sin";
                if (selectedCoef == 1)
                    harm = showFormat.format(mag) + " " + func + "(x";
                else
                    harm = showFormat.format(mag) +
                        " " + func + "(" + selectedCoef + "x";
                if (!magPhaseCheck.getState() || phase == 0)
                    harm += ")";
                else {
                    harm += (phase < 0) ? " - " : " + ";
                    harm += showFormat.format(Math.abs(phase)) + ")";
                }
                if (logCheck.getState()) {
                    harm += "   (" +
                        showFormat2.format(20*Math.log(mag)/Math.log(10)) +
                        " dB)";
                }
            }
            centerString(g, harm, texty+15);
        }
    }
        
    void showExpansion(Graphics g, int texty) {
        String str = "";
        if (Math.abs(magcoef[0]) >= .00001)
        	str = showFormat.format(magcoef[0]);
        String lastStr = str;
        int i;
        FontMetrics fm = g.getFontMetrics();
        int terms = termBar.getValue();
        for (i = 1; i != terms; i++) {
        	int j;
        	int jmax = (magPhaseCheck.getState() ? 1 : 2);
        	for (j = 0; j != jmax; j++) {
        		double mag = magcoef[i];
        		double phase = phasecoef[i];
                if (!magPhaseCheck.getState()) {
                	// convert mag/phase to sin/cos
                    if (j == 1) {
                        mag *= -Math.sin(phase);
                        phase = -pi/2;
                    } else {
                        mag *= Math.cos(phase);
                        phase = 0;
                    }
                }
                if (Math.abs(mag) < .00001)
                	continue;
                if (mag >= 0 && str.length() > 0)
                	str += "+";
        		String func = (j == 0) ? "cos" : "sin";
        		if (mag != 1)
        			str += showFormat.format(mag);
        		str += func;
        		if (i == 1)
        			str += "(x";
        		else
        			str += "(" + i + "x";
        		if (!magPhaseCheck.getState() || phase == 0)
        			str += ")";
        		else {
        			str += (phase < 0) ? "-" : "+";
        			str += showFormat.format(Math.abs(phase)) + ")";
        		}
        	}
        	// is string too long for window?  if so, remove last harmonic and quit
        	if (fm.stringWidth(str) > winSize.width) {
        		str = lastStr;
        		break;
        	}
        	lastStr = str + "+...";
        }
        g.setColor(Color.yellow);
        centerString(g, str, texty+15);
    }
    
    int getTermWidth() {
        int terms = termBar.getValue();
        int termWidth = winSize.width / terms;
        int maxTermWidth = winSize.width/30;
        if (termWidth > maxTermWidth)
            termWidth = maxTermWidth;
        if (termWidth > 12)
            termWidth = 12;
        termWidth &= ~1;
        return termWidth;
    }

    void edit(MouseEvent e) {
        if (selection == SEL_NONE)
            return;
        int x = e.getX();
        int y = e.getY();
        switch (selection) {
        case SEL_MAG:       editMag(x, y); break;
        case SEL_FUNC:      editFunc(x, y); break;
        case SEL_PHASE:     editPhase(x, y); break;
        case SEL_MUTES:     editMutes(e, x, y); break;
        case SEL_SOLOS:     editSolos(e, x, y); break;
        }
        quantizeCount = resampleCount = 0;
    }

    void editMag(int x, int y) {
        if (selectedCoef == -1)
            return;
        double ymult = viewMag.ymult;
        double midy = viewMag.midy;
        double coef = -(y-midy) / ymult;
        if (magPhaseCheck.getState()) {
            if (selectedCoef > 0) {
                if (coef < 0)
                    coef = 0;
                coef = getMagValue(coef);
            } else if (coef < -1)
                coef = -1;
            if (coef > 1)
                coef = 1;
            if (magcoef[selectedCoef] == coef)
                return;
            magcoef[selectedCoef] = coef;
        } else {
            int c = selectedCoef;
            if (c == 0)
                return;
            double m2 =  magcoef[c]*Math.cos(phasecoef[c]);
            if (coef > 1)  coef = 1;
            if (coef < -1) coef = -1;
            double m1 = coef;
            magcoef[c] = Math.sqrt(m1*m1+m2*m2);
            phasecoef[c] = Math.atan2(-m1, m2);
        }
        calcRMS();
        updateSound();
        repaint();
    }

    void editFunc(int x, int y) {
        if (dragX == x) {
            editFuncPoint(x, y);
            dragY = y;
        } else {
            // need to draw a line from old x,y to new x,y and
            // call editFuncPoint for each point on that line.  yuck.
            int x1 = (x < dragX) ? x : dragX;
            int y1 = (x < dragX) ? y : dragY;
            int x2 = (x > dragX) ? x : dragX;
            int y2 = (x > dragX) ? y : dragY;
            dragX = x;
            dragY = y;
            for (x = x1; x <= x2; x++) {
                y = y1+(y2-y1)*(x-x1)/(x2-x1);
                editFuncPoint(x, y);
            }
        }
    }
    
    void editFuncPoint(int x, int y) {
        int midy = viewFunc.midy;
        int periodWidth = viewFunc.periodWidth;
        double ymult = viewFunc.ymult;
        int lox = (x % periodWidth) * sampleCount / periodWidth;
        int hix = (((x % periodWidth)+1) * sampleCount / periodWidth)-1;
        double val = (midy - y) / ymult;
        if (val > 1)
            val = 1;
        if (val < -1)
            val = -1;
        for (; lox <= hix; lox++)
            func[lox] = val;
        func[sampleCount] = func[0];
        repaint();
    }

    Timer paintTimer;
    
    void repaint() {
    	if (paintTimer == null) {
    		paintTimer = new Timer() {
    			public void run() {
    				updateFourier();
    				paintTimer = null;
    			}
    		};
    		paintTimer.schedule(30);
    	}
    }
    
    void editPhase(int x, int y) {
        if (selectedCoef == -1)
            return;
        double ymult = viewPhase.ymult;
        double midy = viewPhase.midy;
        double coef = -(y-midy) / ymult;
        if (magPhaseCheck.getState()) {
            coef *= pi;
            if (coef < -pi)
                coef = -pi;
            if (coef > pi)
                coef = pi;
            if (phasecoef[selectedCoef] == coef)
                return;
            phasecoef[selectedCoef] = coef;
        } else {
            int c = selectedCoef;
            double m1 = -magcoef[c]*Math.sin(phasecoef[c]);
            if (coef > 1)  coef = 1;
            if (coef < -1) coef = -1;
            double m2 = coef;
            magcoef[c] = Math.sqrt(m1*m1+m2*m2);
            phasecoef[c] = Math.atan2(-m1, m2);
            updateSound();
        }
        calcRMS();
        repaint();
    }

    void editMutes(MouseEvent e, int x, int y) {
//        if (e.getID() != MouseEvent.MOUSE_PRESSED)
//            return;
        if (selectedCoef == -1)
            return;
        mutes[selectedCoef] = !mutes[selectedCoef];
        repaint();
    }
    
    void editSolos(MouseEvent e, int x, int y) {
//        if (e.getID() != MouseEvent.MOUSE_PRESSED)
//            return;
    	toggleSolo(selectedCoef);
    }
    
    void toggleSolo(int coef) {
        if (coef == -1)
            return;
        solos[coef] = !solos[coef];
        int terms = termBar.getValue();
        hasSolo = false;
        int i;
        for (i = 0; i != terms; i++)
            if (solos[i]) {
                hasSolo = true;
                break;
            }
        repaint();
    }

    void pressButton(Object b) {
        if (b == triangleButton) {
            doTriangle();
            repaint();
        }
        if (b == sineButton) {
            doSine();
            repaint();
        }
        if (b == cosineButton) {
            doCosine();
            repaint();
        }
        if (b == rectButton) {
            doRect();
            repaint();
        }
        if (b == fullRectButton) {
            doFullRect();
            repaint();
        }
        if (b == squareButton) {
            doSquare();
            repaint();
        }
        if (b == highPassButton) {
            doHighPass();
            repaint();
        }
        if (b == noiseButton) {
            doNoise();
            repaint();
        }
        if (b == phaseButton) {
            doPhaseShift();
            repaint();
        }
        if (b == blankButton) {
            doBlank();
            repaint();
        }
        if (b == sawtoothButton) {
            doSawtooth();
            repaint();
        }
        if (b == clipButton) {
            doClip();
            repaint();
        }
        if (b == quantizeButton) {
            doQuantize();
            repaint();
        } else
            quantizeCount = 0;
        if (b == resampleButton) {
            doResample();
            repaint();
        } else
            resampleCount = 0;
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

    void scrollbarMoved() {
    	updateSound();
    	calcRMS();
    	repaint();
    }
    
    
    @Override
    public void onValueChange(ValueChangeEvent<Boolean> e) {
        if (e.getSource() == soundCheck && soundCheck.getState() &&
                playThread == null) {
                playThread = new PlayThread();
                playThread.start();
        }
        if (e.getSource() == magPhaseCheck)
        	handleResize();
        repaint();
    }

    void restartPlayThread() {
        if (playThread != null) {
            playThread.requestShutdown();
            startPlayThread();
        }
    }
    
	@Override
	public void onChange(ChangeEvent e) {
		/*
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
        */
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
        if (selection == SEL_FUNC)
            transform();
        else if (selection != SEL_NONE)
            doSetFunc();
        repaint();
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		event.preventDefault();
		if (dragging)
			edit(event);
		else
			doMouseMove(event);
	}
	
	void doMouseMove(MouseEvent<?> e) {
        int x = e.getX();
        int y = e.getY();
        dragX = x; dragY = y;
        int oldCoef = selectedCoef;
        selectedCoef = -1;
        selection = 0;
        int oldsel = selection;
        if (viewFunc.contains(x, y))
            selection = SEL_FUNC;
        else {
            int termWidth = getTermWidth();
            selectedCoef = x/termWidth;
            if (selectedCoef > termBar.getValue())
                selectedCoef = -1;
            if (selectedCoef != -1) {
                if (viewMag.contains(x, y))
                    selection = SEL_MAG;
                else if (viewMutes.contains(x, y))
                    selection = SEL_MUTES;
                else if (viewSolos.contains(x, y))
                    selection = SEL_SOLOS;
                else if (viewPhase.contains(x, y))
                    selection = SEL_PHASE;
            }
        }
        if (selectedCoef != oldCoef || oldsel != selection)
            repaint();
	}

    
	void dragMouse(MouseEvent<?> event) {
			}

	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		event.preventDefault();
        doMouseMove(event);
        /*
        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0 &&
                selectedCoef != -1) {
                termBar.setValue(selectedCoef+1);
                cv.repaint();
            }*/
        dragging = true;
        edit(event);
	}
	
	@Override
	public void onMouseWheel(MouseWheelEvent event) {
        event.preventDefault();
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		dragging = false;
		selection = SEL_NONE;
		selectedCoef = -1;
		repaint();
	}

	@Override
	public void onPreviewNativeEvent(NativePreviewEvent e) {
		int t=e.getTypeInt();
		int code=e.getNativeEvent().getKeyCode();
		if ((t & Event.ONKEYDOWN)!=0) {
			if (code >= KEY_ZERO && code <= KEY_NINE) {
				int coef = code-KEY_ZERO;
				if (coef == 0)
					coef = 10;
		        if (e.getNativeEvent().getShiftKey())
		        	toggleSolo(coef);
		        else
		        	mutes[coef] = !mutes[coef];
		        updateSound();
		        repaint();
			}
		}
	}

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		event.preventDefault();
	}

	@Override
	public void onClick(ClickEvent event) {
		event.preventDefault();
	}

}
