package models;

public class Cell {
	public int line;
	public int column;
	
	public Cell (int nl, int nc) {
		this.line = nl;
		this.column = nc;
	}

	public int getLine() {
		return this.line;
	}
	
	public int getColonne() {
		return this.column;
	}
}
