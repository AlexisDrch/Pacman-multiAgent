package models;

import jade.util.leap.ArrayList;

public class Cell {
	private int value = 0;
	public ArrayList possibles = new ArrayList();
	public int nligne;
	public int ncolonne;
	
	public Cell(int val, int nl, int nc) {
		if (val != 0) {
			this.value = val;
		} else {
			int i;
			for (i = 1; i < 10; i ++) {
				possibles.add(i);
			}
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
	
	public void setPossibles(ArrayList possibles) {
		this.possibles = possibles;
	}
	
	public void updatePossible() {
		// now that we have removed a possible Value
		// we have to verify whether the possibles value are now only 1
		if (this.possibles.size() == 1) {
			this.value = (int) this.possibles.get(1);
		}
	}
	
	public void removePossibleValue(int valueToRemove) {
		int i;
		for (i = 0; i < this.possibles.size(); i ++) {
			int possibleValue = (int)this.possibles.get(i);
			if (possibleValue == valueToRemove) {
				this.possibles.remove(i);
				this.updatePossible();
			}
		}
	}
}
