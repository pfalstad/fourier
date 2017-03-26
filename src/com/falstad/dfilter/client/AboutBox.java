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

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Button;

public class AboutBox extends PopupPanel {
	
	VerticalPanel vp;
	Button okButton;
	
	AboutBox(String version) {
		super();
		vp = new VerticalPanel();
		setWidget(vp);
		vp.setWidth("400px");
		vp.add(new HTML("<p>Digital Filters version "+version+".</p>"+
		"<p>Originally written in Java by Paul Falstad.<br><a href=\"http://www.falstad.com/\" target=\"_blank\">http://www.falstad.com/</a></p>"+
		"<p>Javascript conversion by Paul Falstad, based on work by Iain Sharp and Erick Maldonado.</p>"+
"Thanks to <a href=\"http://www.digitaldroo.com\">Digital Droo</a> for "+
"the Monkey Developers sample.<br>"+
"Thanks to S&oslash;ren \"Jeff\" Lund for the Arabian Bias sample "+
"(and also to the <a href=\"http://www.hvsc.c64.org\">HVSC</a> team and "+
"<a href=\"http://www.sidmusic.org/sidplay/mac\">Andreas Varga</a>). "+
"Thanks to Tory Esbensen for the speech sample.<br>"+
"Thanks to the <a href=\"http://www.mame.net\">MAME</a> team (and Eugene Jarvis and Larry Demar) for the "+
"Robotron sample.<br>"+

		"<p style=\"font-size:9px\">This program is free software: you can redistribute it and/or modify it "+
		"under the terms of the GNU General Public License as published by "+
		"the Free Software Foundation, either version 2 of the License, or "+
		"(at your option) any later version.</p>"+
		"<p style=\"font-size:9px\">This program is distributed in the hope that it will be useful,"+
		"but WITHOUT ANY WARRANTY; without even the implied warranty of "+
		"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "+
		"GNU General Public License for more details.</p>"+
		"<p style=\"font-size:9px\">For details of licensing see <A href=\"http://www.gnu.org/licenses/\" target=\"_blank\">http://www.gnu.org/licenses/</A>.</p>"+
		"<p style=\"font-size:9px\">Source code:<A href=\"https://github.com/pfalstad/dfilter\" target=\"_blank\">https://github.com/pfalstad/dfilter</A></p>"));
		
		
		vp.add(okButton = new Button("OK"));
		okButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				close();
			}
		});
		center();
		show();
	}

	public void close() {
		hide();
	}
}
