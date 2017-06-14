package models;

public class MovePrediction {
	private Cell[] cells_predicted;
	private Integer analyzer_number;
	
	public Cell[] getCells_predicted() {
		return cells_predicted;
	}
	public void setCells_predicted(Cell[] cells_predicted) {
		this.cells_predicted = cells_predicted;
	}
	public Integer getAnalyzer_number() {
		return analyzer_number;
	}
	public void setAnalyzer_number(Integer analyzer_number) {
		this.analyzer_number = analyzer_number;
	}
	public MovePrediction(Cell[] cells_predicted, Integer analyzer_number) {
		super();
		this.cells_predicted = cells_predicted;
		this.analyzer_number = analyzer_number;
	}
	
}
