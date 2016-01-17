package AI;

import java.util.Random;

public class Gene implements Cloneable {

	private int from;
	private int to;
	private double weight;
	private int histID;
	private boolean disabled;
	
	public Gene(int from_, int to_, int histID_, boolean disabled_) {
		from = from_;
		to = to_;
		
		newWeight();
		
		histID = histID_;
		
		disabled = disabled_;
	}
	
	public Gene(int from_, int to_, double weight_, int histID_, boolean disabled_) {
		from = from_;
		to = to_;
		
		weight = weight_;
		
		histID = histID_;
		
		disabled = disabled_;
	}
	
	public void setWeight(double weight_) {
		weight = weight_;
	}
	
	public void setDisabled(boolean bool) {
		disabled = bool;
	}
	
	public void perturbe(double magnitude) {
		Random rand = new Random();
		double change = rand.nextDouble()*magnitude;
		change = 2*change - magnitude;

		weight += change;
		if (weight > 1) {
			weight = 1;
		} else if (weight < -1) {
			weight = -1;
		}
	}
	
	public void newWeight() {
		Random rand = new Random();
		weight = rand.nextDouble()*2 - 1;
	}
	
	public void swapFromTo() {
		int temp = from;
		from = to;
		to = temp;
	}
	
	public int getFrom() { return from; }
	public int getTo() { return to; }
	public double getWeight() { return weight; }
	public int getHistID() { return histID; }
	public int getHistoricalID() { return histID; }
	public boolean isDisabled() { return disabled; }
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass().equals(Gene.class)) {
			Gene temp = (Gene)obj;
			if (temp.getFrom() == getFrom() && temp.getTo() == getTo()) {
				return true;
			}
		}
		return false;
	}
	
	public Gene clone() {
		return new Gene(from, to, weight, histID, disabled);
	}
}
