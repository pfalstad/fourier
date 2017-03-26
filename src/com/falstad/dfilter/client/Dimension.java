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

public class Dimension {
	
	int width;
	int height;
	
	public Dimension(int width, int height){
		this.width = width;
		this.height = height;
		
	}
	
	
	public int getWidth() {	
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	
	
}
