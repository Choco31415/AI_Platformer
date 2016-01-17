package AI;

import java.util.ArrayList;
import java.util.Random;

/*
 * A roulette wheel is a circular object.
 * This circular object is cut into several radial slices.
 * Each radial slice varies in size, and each slice has an object associated with it.
 * Picking an object on the roulette wheel is done by spinning the wheel and seeing what object corresponds to the slice that lands pointing upwards.
 * 
 * This class tries to mimic a real roulette wheel. It can take any class as an input type.
 */

public class RouletteWheel {
	final int size; //How many items are in the wheel?
	Object[] items; //What items are in the wheel?
	Double[] weights; //What are the weights of items in the wheel?
	int[] pickCounts; //How many times have items in the wheel been picked?
	Double weightSize = null; //Combined total of weights.
	
	private Random rnd = new Random();
	
	public RouletteWheel(int size_) {
		size = size_;
		items = new Object[size];
		weights = new Double[size];
		initPickCounts();
	}
	
	public RouletteWheel(Object[] items_) {
		size = items_.length;
		items = items_;
		weights = new Double[size];
		initPickCounts();
	}
	
	public RouletteWheel(Object[] items_, Double[] weights_) {
		size = items_.length;
		items = items_;
		weights = weights_;
		initPickCounts();
		try {
			sumWeights();
		} catch (InvalidWeights e) {
			System.out.println("You messed up.");
			e.printStackTrace();
		}
	}
	
	public void initPickCounts() {
		//Initializes pickCounts. See above for what pickCounts is.
		pickCounts = new int[size];
		for (int i = 0; i < size; i++) {
			pickCounts[i] = 0;
		}
	}
	
	public void setWeight(Double weight, int i) {
		//This function sets the weight at i to weight.
		weights[i] = weight;
	}
	
	public void setItems(Object[] items_) {
		items = items_;
	}
	
	public void sumWeights() throws InvalidWeights {
		//This function finds the sum of wheel weights.
		Double sum = 0.0;
		for (int i = 0; i < size; i++) {
			if (weights[i].equals(null)) {
				throw new InvalidWeights();
			} else {
				sum += weights[i];
			}
		}
		weightSize = sum;
	}
	
	public void normalizeWeights(Double target) throws InvalidWeights {
		//This function normalizes weights so that their total is the same as specified in the function.
		sumWeights();
		Double factor = target/weightSize;
		for (int i = 0; i < size; i++) {
			weights[i] *= factor;
		}
		weightSize = target;
	}
	
	public void forcePositiveWeights() throws InvalidWeights {
		//This method is good for making all weights positive, if not done already.
		for (int i = 0; i < size; i++) {
			if (weights[i].equals(null)) {
				throw new InvalidWeights();
			} else {
				weights[i] = Math.abs(weights[i]);
			}
		}
	}
	
	public Object pickRandomItem() {
		//This function picks a random item, based randomly on an item's weight in relation to other items' weight.
		try {
			sumWeights();
		} catch (InvalidWeights e) {
			e.printStackTrace();
		}
		Double i = rnd.nextDouble()*weightSize;
		Double j = 0.0;
		int k;
		for (k = 0; j < i; k++) {
			try {
				j += weights[k];
			} catch (ArrayIndexOutOfBoundsException e) {
				//Normally an error shouldn't be thrown, but occassionally it does happen...
				return items[items.length-1];
			}
		}
		try {
			pickCounts[k-1]++;
			return items[k-1];
		} catch (ArrayIndexOutOfBoundsException e) {
			//Extremely rare error. Only occurs once every 400,000 search depths with N being 20.
			pickCounts[0]++;
			return items[0];
		}
	}
	
	public void subtractLowest(int n) {
		if (n > size-1 || n == 0) {
			return;
		}
		
		//We hunt for the lowest values.
		ArrayList<Double> sorted = new ArrayList<Double>();
		sorted.add(weights[0]);
		
		for (int weight = 1; weight < size; weight++) {
			for (int i = 0; i < sorted.size(); i++) {
				if (weights[weight] < sorted.get(i)) {
					sorted.add(i, weights[weight]);
					break;
				}
			}
			if (sorted.size()-1 != weight) {
				sorted.add(weights[weight]);
			}
		}
		
		//One the lowest values are calculated, we act!
		Double newWeight;
		for (int i = 0; i < size; i++) {
			newWeight = weights[i] - sorted.get(n-1);
			if (newWeight>0) {
				weights[i] = newWeight;
			} else {
				weights[i] = 0.0;
			}
		}
	}
	
	public void removeLowest(int n) {
		if (n > size-1 || n == 0) {
			return;
		}
		
		//We hunt for the lowest values.
		ArrayList<Integer> sorted = new ArrayList<Integer>();
		sorted.add(0);
		
		for (int weight = 1; weight < size; weight++) {
			for (int i = 0; i < sorted.size(); i++) {
				if (weights[weight] < weights[sorted.get(i)]) {
					sorted.add(i, weight);
					break;
				}
			}
			if (sorted.size()-1 != weight) {
				sorted.add(weight);
			}
		}
		
		//One the lowest values are calculated, we act!
		for (int i = 0; i < n; i++) {
			weights[sorted.get(i)] = 0.0;
		}
	}
	
	public void removeBelow(double threshold) {
		for (int i = 0; i < size; i++) {
			if (weights[i] < threshold) {
				weights[i] = 0.0;
			}
		}
	}
	
	public void removeBelowAndAt(double threshold) {
		for (int i = 0; i < size; i++) {
			if (weights[i] <= threshold) {
				weights[i] = 0.0;
			}
		}
	}
	
	public Object getHighestWeightedObject() {
		int highest = 0;
		for (int i = 1; i < size; i++) {
			if (weights[i] > weights[highest]) {
				highest = i;
			}
		}
		
		return items[highest];
	}
	
	public double getWeightTotal() {
		try {
			sumWeights();
		} catch (InvalidWeights e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return weightSize;
	}
	
	@Override
	public String toString() {
		if (weightSize == null) {
			return "No weightsize detected.";
		}
		String foo = "Rhoulette Wheel of size " + size + ":\n";
		for (int i = 0; i < size; i++) {
			foo += "Item: " + items[i] + " (Weight " + weights[i] + " // Picked " + pickCounts[i] + " time(s))\n";
		}
		return foo;
	}
}
