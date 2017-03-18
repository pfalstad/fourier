package com.falstad.dfilter.client;

public class FontMetrics {
	Graphics g;
	FontMetrics(Graphics g_) { g = g_; }
	int stringWidth(String s) {
        return (int) g.context.measureText(s).getWidth();
	}
}
