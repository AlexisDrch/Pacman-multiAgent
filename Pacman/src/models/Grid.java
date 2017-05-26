package models;
import models.*;


public class Grid {
	public Cell[][] grid = new Cell[9][9];
	
	public Grid(int [][] gridContent) {
		int i,j;
		
		for(i = 0; i < 9 ; i ++) {
			for(j = 0; j < 9 ; j ++) {
				Cell cell = new Cell(gridContent[i][j], i, j);
				grid[i][j] = cell;
			}
		}
	}

	public boolean isOver() {
		
		return false;
	}

	public void display() {
		int i;
		int j;
		for(i = 0; i < 9 ; i ++) {
			System.out.println("--- --- --- --- --- --- --- --- ---");
			for(j = 0; j < 9 ; j ++) {
				Cell cell = grid[i][j];
				int value = cell.getValue();
				System.out.print("|" + value + "| ");
			}
			System.out.println();
		}
	}

	// @todo
	public void update(Cell[] cells) {
		int j;
		for(j = 0; j < cells.length; j++) {
			System.out.println(" Old : " + this.grid[cells[j].nligne][cells[j].ncolonne].getValue() + " / new " + cells[j].getValue());
		}
	}
}

