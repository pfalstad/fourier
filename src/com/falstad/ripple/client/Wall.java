package com.falstad.ripple.client;

public class Wall extends DragObject {

	Wall() {
		handles.add(new DragHandle(this));
		handles.add(new DragHandle(this));
	}
	
	Wall(int x1, int y1, int x2, int y2) {
		handles.add(new DragHandle(this, x1, y1));
		handles.add(new DragHandle(this, x2, y2));
	}

	void prepare() {
		RippleSim.console("prepare " + handles.get(0) + " " + handles.get(1));
		RippleSim.drawWall(handles.get(0).x, handles.get(0).y, handles.get(1).x, handles.get(1).y);
	}
	
	@Override void drawSelection() {
		prepare();
	}
}