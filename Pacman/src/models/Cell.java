package models;

import jade.util.leap.ArrayList;

public class Cell {
	private int value = 0;
	public int nligne;
	public int ncolonne;
	
	public Cell(int val, int nl, int nc) {
		if (val != 0) {
			this.value = val;
		} 
		this.nligne = nl;
		this.ncolonne = nc;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public void setValue(int val) {
		this.value = val;
	}
	
	public boolean isNotEmpty() {
		return (this.value != 0);
	}
	
}
