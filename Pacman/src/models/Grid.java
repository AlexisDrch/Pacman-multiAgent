package models;
import org.json.JSONArray;

import models.*;


public class Grid {
	public Cell[][] grid = new Cell[Constants.DIM_GRID_X][Constants.DIM_GRID_Y];
	
	public Grid() {
		int i,j;
		
		for(i = 0; i < Constants.DIM_GRID_X ; i ++) {
			for(j = 0; j < Constants.DIM_GRID_Y ; j ++) {
				Cell cell = new Cell(0, i, j);
				grid[i][j] = cell;
			}
		}
	}

	public boolean isOver() {
		return false;
	}
	
	public void printDashRow() {
		System.out.print("\n");
		for (int i=0; i< Constants.DIM_GRID_Y; i++) {
		    System.out.print("--- ");
		}
		System.out.print("\n");
	}

	public void display() {
		int i;
		int j;
		for(i = 0; i < Constants.DIM_GRID_X ; i ++) {
			this.printDashRow();
			for(j = 0; j < Constants.DIM_GRID_Y ; j ++) {
				Cell cell = grid[i][j];
				int value = cell.getValue();
				System.out.print("|" + value + "| ");
			}
			System.out.print("\n");
		}
	}
	
	public void updateCell(Cell newCell) {
		if (newCell != null) {
			int nl = newCell.nligne;
			int nc = newCell.ncolonne;
			grid[nl][nc] = newCell;
		}
	}
	
	// @todo
	public void updateListOfCells(Cell[] cells) {
		int j;
		for(j = 0; j < cells.length; j++) {
			System.out.println(" Old : " + this.grid[cells[j].nligne][cells[j].ncolonne].getValue() + " / new " + cells[j].getValue());
		}
	}
}

