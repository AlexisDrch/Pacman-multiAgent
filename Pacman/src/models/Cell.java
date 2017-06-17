package models;

import com.sun.corba.se.impl.orbutil.closure.Constant;

import jade.util.leap.ArrayList;

public class Cell {


	private int value = 0;
	public int nligne;
	public int ncolonne;
	private int oldValue = 0;
	private boolean notEmpty = false;
	private boolean monster = false;
	private boolean traveler = false;
	
	public Cell() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Cell(int val, int nl, int nc) {
		notEmpty = true;
		if (val != 0) {
			this.value = val;
		} 
		this.nligne = nl;
		this.ncolonne = nc;
		this.monster = this.value != 0 && this.value <= Constants.MONSTER_NUMBER;
		this.traveler =this.value == Constants.TRAVELER_VALUE;
	}
	

	public int getValue() {
		return this.value;
	}
	
	public int getOldValue() {
		return this.oldValue;
	}
	
	public void setValue(int val) {
		this.value = val;
	}
	
	public void setOldValue(int val) {
		this.oldValue = val;
	}
	
	public boolean isNotEmpty() {
		return (this.value != 0);
	}
	
	public boolean isMonster() {
		return (this.value != 0 && this.value <= Constants.MONSTER_NUMBER);
	}
	
	public boolean isTraveler() {
		return (this.value == Constants.TRAVELER_VALUE);
	}
	
	public boolean wasMonster() {
		return (this.value != 0 && this.value <= Constants.MONSTER_NUMBER);
	}
	
	public boolean wasTraveler() {
		return (this.oldValue == Constants.TRAVELER_VALUE);
	}
	
}
