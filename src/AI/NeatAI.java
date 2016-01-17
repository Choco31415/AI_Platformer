package AI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class NeatAI implements Cloneable {
	
	int numInputs;
	int numOutputs;
	
	private SimpleMap sm;
	private ArrayList<Neuron> neurons = new ArrayList<Neuron>();
	private ArrayList<Integer> neuronOrder = new ArrayList<Integer>();
	private ArrayList<Gene> links = new ArrayList<Gene>();
	
	private int neuronSize = 4;
	
	private Random rand;
	
	private int species;
	
	public NeatAI(SimpleMap sm_, int numInputs_, int numOutputs_) {
		sm = sm_;
		numInputs = numInputs_;
		numOutputs = numOutputs_;
		
		
		rand = new Random();
		
		double[] pos = new double[2];
		for (int i = 0; i < 4; i++) {
			pos = sm.getOutputTilePos(i);
			neurons.add(new Neuron(pos[0], (int)pos[1]));
		}
		
		bubbleSortNeurons();
	}
	
	public NeatAI(SimpleMap sm_, ArrayList<Neuron> n, ArrayList<Gene> l, int numInputs_, int numOutputs_) {
		sm = sm_;
		neurons = n;
		links = l;
		
		numInputs = numInputs_;
		numOutputs = numOutputs_;
		
		//System.out.println("hard reset");
		hardReset();
		
		rand = new Random();
		//System.out.println("new ai");
	}
	
	public double[] get(Integer[] input) {
		Double activation;
		Double temp;
		
		for (Integer i : neuronOrder) {
			Neuron n = neurons.get(i);
			activation = null;
			for (Gene l : n.getLinks()) {
				temp = getNeuronActivation(l.getFrom());
				if ( temp != null ) {
					if (activation == null) {
						activation = 0.0;
					}
					activation += temp*l.getWeight();
				}
			}
			n.setActivation(sigmoidFunction(activation));
		}
		Double temp1 = neurons.get(0).getActivation();
		if (temp1 == null) { temp1 = 0.0; }
		Double temp2 = neurons.get(1).getActivation();
		if (temp2 == null) { temp2 = 0.0; }
		Double temp3 = neurons.get(2).getActivation();
		if (temp3 == null) { temp3 = 0.0; }
		Double temp4 = neurons.get(3).getActivation();
		if (temp4 == null) { temp4 = 0.0; }
		return new double[]{temp1, temp2, temp3, temp4};
		//return null;
	}
	
	private Double sigmoidFunction(Double activation) {
		if (activation == null) {
			return null;
		} else {
			return 2.0/(1.0 + Math.exp(-4.9*activation)) - 1.0;
		}
	}
	
	public void draw(Graphics2D g) {
		Rectangle rect;
		double colorLevel;
		
		double[] from;
		double[] to;
		int red;
		int green;
		int blue;
		int alpha;
		for (Gene l: links) {
			if (l.isDisabled()) {
				continue;
			}
			
			if (l.getWeight() < 0) {
				red = 255;
				blue = 0;
				green= 0;
				g.setColor(Color.RED);
					
			} else {
				red = 0;
				blue = 0;
				green= 255;
				g.setColor(Color.GREEN);
			}
			if (getNeuronActivation(l.getFrom()) == null) {
				alpha = 100;
				red *= 0.4;
				blue *= 0.5;
				green *= 0.5;
			} else {
				alpha = 255;
			}
			g.setColor(new Color(red, green, blue, alpha));
			
			from = getNeuronPos(l.getFrom());
			
			to = getNeuronPos(l.getTo());

			
			g.drawLine((int)from[0], (int)from[1], (int)to[0], (int)to[1]);
		}
		
		for (Neuron n: neurons) {
			rect = new Rectangle((int) (n.getx() - neuronSize/2), n.gety() - neuronSize/2, neuronSize, neuronSize);
			g.setColor(new Color(0, 0, 0));
			g.draw(rect);
			
			if (n.getActivation() == null) {
				continue;
			}
			rect.setSize((int)rect.getWidth()-1, (int)rect.getHeight()-1);
			rect.setLocation((int)rect.getX()+1, (int)rect.getY()+1);
			colorLevel = ((n.getActivation()+1.0)/2.0)*(255.0);
			g.setColor(new Color((int)colorLevel, (int)colorLevel, (int)colorLevel));
			g.fill(rect);
		}
	}
	
	public NeatAI crossOver(NeatAI second, double fitness1, double fitness2, double disableRate) {
		NeatAI first;
		
		//Line up links/genes
		ArrayList<Integer> ai1linkListPos;
		ArrayList<Integer> ai2linkListPos;
		ArrayList<ArrayList<Integer>> temp = lineUpAILinks(second);
		ArrayList<Integer> linkIDs = temp.get(0);
		
		//Make it so first is the fitter individual.
		if (fitness2 > fitness1) {
			first = second;
			second = this;
			
			ai1linkListPos = temp.get(2);
			ai2linkListPos = temp.get(1);
		} else {
			first = this;
			
			ai1linkListPos = temp.get(1);
			ai2linkListPos = temp.get(2);
		}
		
		//Clone data.
		//System.out.println("p2");
		ArrayList<Gene> firstL = first.getCloneLinks();
		ArrayList<Gene> secondL = second.getCloneLinks();
		
		ArrayList<Neuron> newN = new ArrayList<Neuron>();
		ArrayList<Gene> newL = new ArrayList<Gene>();
		
		newN = first.getCloneNeurons();
		
		//System.out.println("p3");
		for (int i = 0; i < firstL.size(); i++) {
			Gene gene1 = firstL.get(i);
			Gene gene2;

			int gene2pos = ai2linkListPos.get(linkIDs.indexOf(gene1.getHistID()));
			if (gene2pos == -1) {
				gene2 = null;
			} else {
				gene2 = secondL.get(gene2pos);
			}
			
			if (gene2 != null && rand.nextBoolean() && !gene2.isDisabled()) {
				newL.add(gene2);
			} else {
				newL.add(gene1);				
			}
		}
		
		//System.out.println("New AI");
		//(SimpleMap sm_, ArrayList<Neuron> n, ArrayList<Link> l, int numInputs_, int numOutputs_) {
		//System.out.println("p4");
		NeatAI newai = new NeatAI(sm, newN, newL, numInputs, numOutputs);
		
		return newai;
	}
	
	/*public NeatAI crossOver(NeatAI second, double fitness1, double fitness2, double disableRate) {
		ArrayList<Gene> secondL = second.getCloneLinks();
		
		ArrayList<Neuron> newN = new ArrayList<Neuron>();
		ArrayList<Gene> newL = new ArrayList<Gene>();
		
		//Line up links/genes
		ArrayList<ArrayList<Integer>> temp;
		temp = lineUpAILinks(second);
		
		ArrayList<Integer> linkIDs = temp.get(0);
		ArrayList<Integer> ai1linkListPos = temp.get(1);
		ArrayList<Integer> ai2linkListPos = temp.get(2);
		
		//Reconstruct output neurons
		for (int i = 0; i < 4; i++) {
			newN.add(neurons.get(i).clone());
		}
		
		//Crossover links, and reconstruct hidden neurons
		Integer pos1;
		Integer pos2;
		Gene link1;
		Gene link2;
		for (int i = 0; i < linkIDs.size(); i++) {
			pos1 = ai1linkListPos.get(i);
			pos2 = ai2linkListPos.get(i);
			if (pos1 != -1 && pos2 != -1) {
				link1 = links.get(pos1);
				link2 = secondL.get(pos2);
				if (fitness1 > fitness2) {
					newL.add(link1.clone());
					
					if (link1.getTo() >= numInputs + newN.size()) {
						newN.add(getNeuron(link1.getTo()).clone());
					}
				} else {
					newL.add(link2.clone());
					
					if (link2.getTo() >= numInputs + newN.size()) {
						newN.add(second.getNeuron(link2.getTo()).clone());
					}
				}
				if (link1.isDisabled() || link2.isDisabled()) {
					if (rand.nextDouble() < disableRate) {
						newL.get(newL.size()-1).setDisabled(true);
					} else {
						newL.get(newL.size()-1).setDisabled(false);
					}
				}
			} else if (pos1 != -1) {
				link1 = links.get(pos1);
				newL.add(link1.clone());
				
				if (link1.getTo() >= numInputs + newN.size()) {
					newN.add(getNeuron(link1.getTo()).clone());
				}
			} else {
				link2 = secondL.get(pos2);
				newL.add(link2.clone());
				
				if (link2.getTo() >= numInputs + newN.size()) {
					newN.add(second.getNeuron(link2.getTo()).clone());
				}
			}
		}
		
		//System.out.println("New AI");
		//(SimpleMap sm_, ArrayList<Neuron> n, ArrayList<Link> l, int numInputs_, int numOutputs_) {
		NeatAI newai = new NeatAI(sm, newN, newL, numInputs, numOutputs);
		
		return newai;
	}*/
	
	public double compatibilityDistance(NeatAI second, double c1, double c2, double c3, int minSize) {
		double compatibilityDistance = 0.0;
		
		ArrayList<Gene> secondL = second.getCloneLinks();
		
		//Line up links/genes
		ArrayList<ArrayList<Integer>> temp;
		temp = lineUpAILinks(second);
		
		ArrayList<Integer> ai1linkListPos = temp.get(1);
		ArrayList<Integer> ai2linkListPos = temp.get(2);
		
		ArrayList<Double> nodeWeightDifferences = new ArrayList<Double>();
		
		int disjointGeneCount = 0;
		int excessGeneCount = 0;
		
		int pos1;
		int pos2;
		for (int i = 0; i < ai1linkListPos.size(); i++) {
			pos1 = ai1linkListPos.get(i);
			pos2 = ai2linkListPos.get(i);
			if (pos1 == -1 || pos2 == -1) {
				excessGeneCount++;
			} else {
				disjointGeneCount += excessGeneCount;
				excessGeneCount = 0;
				nodeWeightDifferences.add(Math.abs(links.get(pos1).getWeight() - secondL.get(pos2).getWeight()));
			}
		}
		
		double sum = 0;
		double averageWeightDifference;
		for (double weightDifference : nodeWeightDifferences) {
			sum += weightDifference;
		}
		averageWeightDifference = sum/nodeWeightDifferences.size();
		
		int largerGenomeSize = Math.max(links.size(), secondL.size());
		if (largerGenomeSize < minSize) {
			largerGenomeSize = 1;
		}
		
		compatibilityDistance = c1*excessGeneCount/largerGenomeSize + c2*disjointGeneCount/largerGenomeSize + c3*averageWeightDifference;
		
		return compatibilityDistance;
	}
	
	public int addLink(int historicalID) {
		int attempts = 0;
		int maxAttempts = 5;
		boolean success = false;
		
		int input;
		int output;
		Gene link;
		do {
			input = rand.nextInt(numInputs + neurons.size() - numOutputs);
			if (rand.nextDouble() < GeneticAlgorithm.biasRate) {
				input = numInputs-1;
			} else if (input >= numInputs) {
				input += numOutputs;
			}
			do {
				output = rand.nextInt(neurons.size()) + numInputs;
			} while (output == input);
			
			//make sure link is not backwards
			if (neuronType(input) == 1 && neuronType(output) == 1) {
				if (getNeuron(input).getx() > getNeuron(output).getx()) {
					//if input is ahead of output, swap
					int temp = input;
					input = output;
					output = temp;
				} else if (getNeuron(input).getx() == getNeuron(output).getx()) {
					//if input is same as output, move one
					getNeuron(output).setx(getNeuron(output).getx()+0.1);
				}
			}
			
			link = new Gene(input, output, historicalID, false);
			
			success = true;
			for (Gene l: links) {
				if (l.equals(link)) {
					success = false;
				}
			}
			attempts++;
		} while (attempts < maxAttempts && !success);
		
		if (success) {
			addLink(link);
			
			return historicalID+1;
		} else {
			return historicalID;
		}
	}
	
	/*
	 * aka. addNeuron ()
	 */
	public int addNode(int historicalID) {
		Gene randLink;

		do {
			randLink = links.get(rand.nextInt(links.size()));
		} while (randLink.isDisabled());
		double[] from = getNeuronPos(randLink.getFrom());
		double[] to = getNeuronPos(randLink.getTo());
		
		int minx = sm.getCornerOffset() + sm.getWidth()*(sm.getSmallTileSize()+2) + 2;
		if (from[0] < minx) {
			from[0] = minx;
		}
		
		randLink.setDisabled(true);
		
		int neuralID = neurons.size() + numInputs;
		neurons.add(new Neuron((from[0]+to[0])/2, (int)(from[1] + to[1])/2) );
		
		//(int from_, int to_, double weight_, int histID_, boolean negative_, boolean disabled_
		addLink(randLink.getFrom(), neuralID, 1.0, historicalID, false);
		addLink(neuralID, randLink.getTo(), randLink.getWeight(), historicalID+1, false);
		
		bubbleSortNeurons();
		
		return historicalID+2;
	}
	
	/*
	 * Only for neurons that are hidden or output.
	 */
	public Neuron getNeuron(int neuronID) {
		return neurons.get(neuronID-numInputs);
	}
	
	public double[] getNeuronPos(int neuronID) {
		double[] pos;
		int fromType = neuronType(neuronID);
		
		if (fromType == 0) {
			int row = (int)neuronID/(int)sm.getWidth();
			int col = neuronID%sm.getWidth();
			pos = sm.getTilePos(row, col);
		} else if (fromType == 2) {
			pos = sm.getOutputTilePos(neuronID-numInputs);
		} else {
			pos = getNeuron(neuronID).getPos();
		}
		
		return pos;
	}
	
	public Double getNeuronActivation(int neuronID) {
		int fromType = neuronType(neuronID);
		
		if (fromType == 0) {
			int row = (int)neuronID/(int)sm.getWidth();
			int col = neuronID%sm.getWidth();
			if (row >= sm.getHeight()) {
				return 1.0;
			} else {
				Integer result = sm.getType(row, col);
				if (result == null) {
					return null;
				} else {
					return (double)result;
				}
			}
		} else if (fromType == 2) {
			throw new Error();
		} else {
			return getNeuron(neuronID).getActivation();
		}
	}
	
	public int neuronType(int ID) {
		if (ID < numInputs) {
			return 0;//Input
		} else if (ID < numInputs + numOutputs) {
			return 2;//Output
		} else {
			return 1;//Hidden
		}
	}
	
	public void addLink(int from, int to, int id, boolean disabled) {
		Gene l = new Gene(from, to, id, disabled);
		links.add(l);
		getNeuron(to).addLink(l);
	}
	
	public void addLink(int from, int to, double weight, int id, boolean disabled) {
		Gene l = new Gene(from, to, weight, id, disabled);
		links.add(l);
		getNeuron(to).addLink(l);
	}
	
	public void addLink(Gene l) {
		links.add(l);
		getNeuron(l.getTo()).addLink(l);
	}
	
	/*
	 * Used for testing purposes.
	 */
	public void addNeuron(int x_, int y_) {
		neurons.add(new Neuron(x_, y_));
		
		hardReset();
	}
	
	/*
	 * Used by the program to figure out which neurons should be computed first.
	 * Bubble sort, meh
	 */
    public void bubbleSortNeurons() {
    	int size = neurons.size();
        double[] xneuron = new double[size];
        neuronOrder = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
        	xneuron[i] = neurons.get(i).getx();
        	neuronOrder.add(i);
        }
        
        int lowest;
        double temp;
        double temp2;
        for (int i = 0; i < size; i++) {
        	lowest = i;
        	for (int j = i+1; j < size; j++) {
        		if (xneuron[j] < xneuron[lowest]) {
        			lowest = j;
        		}
        	}
        	temp = xneuron[i];
        	xneuron[i] = xneuron[lowest];
        	xneuron[lowest] = temp;
        	
        	temp = neuronOrder.get(i);
        	temp2 = neuronOrder.get(lowest);
        	neuronOrder.set(i, (int)temp2);
        	neuronOrder.set(lowest, (int)temp);
        }
    }
    
    /*
     * This method ensures that no links are pointing backwards.
     */
    public void correctLinks() {
    	double pos1;
    	double pos2;
    	for (Gene l : links) {

    		pos1 = getNeuronPos(l.getFrom())[0];
        	//System.out.println("link");
        	//System.out.println(l.getTo());
        	//System.out.println(neurons.size());
    		pos2 = getNeuronPos(l.getTo())[0];
        	//System.out.println("link2");
    		if (pos1 > pos2) {
    	    	//System.out.println("swap");
    			l.swapFromTo();
    		}
    	}
    	//System.out.println("fully done");
    }
    
    /*
     * This method wipes all neuron data on links and reconstructs it.
     */
    public void hardCorrectNeuron() {
    	for (Neuron n: neurons) {
    		n.clearLink();
    	}
    	
    	for (Gene l: links) {
    		getNeuron(l.getTo()).addLink(l);
    	}
    }
    
    /*
     * This method pulls out the whole shebang.
     */
    public void hardReset() {
    	//System.out.println("bubble");
    	bubbleSortNeurons();
    	//System.out.println("correct");
		correctLinks();
    	//System.out.println("hard correct");
		hardCorrectNeuron();
    }
    
    public void setSpecies(int num) {
    	species = num;
    }
    
    public int getSpecies() { return species; }
    public ArrayList<Neuron> getCloneNeurons() {
    	ArrayList<Neuron> temp = new ArrayList<Neuron>();
    	for (Neuron n: neurons) {
    		temp.add(n.clone());
    	}
    	return temp;
    }
    public ArrayList<Gene> getCloneLinks() {
    	ArrayList<Gene> temp = new ArrayList<Gene>();
    	for (Gene l: links) {
    		temp.add(l.clone());
    	}
    	return temp;
    }
    public int getNumInputs() { return numInputs; }
    public int getNumOutputs() { return numOutputs; }
    
    public void setNeurons(ArrayList<Neuron> n) {
    	neurons = n;
    	
    	bubbleSortNeurons();
    	correctLinks();
    }
    
    public void setLinks(ArrayList<Gene> l) {
    	links = l;
    	
    	hardCorrectNeuron();
    	correctLinks();
    }
    
    public void perturbe(double uniformPerturbeRate, double perturbeMagnitude) {
    	Random rand = new Random();
    	
    	for (Gene l : links) {
    		if (rand.nextDouble() < uniformPerturbeRate) {
    			l.perturbe(perturbeMagnitude);
    		} else {
    			l.newWeight();
    		}
    	}
    }
    
    /*
     * This method lines up two AI's links, aka genes, and returns the order of the lined up genes, and where they occur.
     * This method returns an arraylist of arraylist of Integer.
     * The outer arraylist contains arraylists on:
     * 0: neuron historical ID of links present in the two AI
     * 1: where in the first ai's link list the link occurs
     * 2: where in the second ai's link list the link occurs
     */
    public ArrayList<ArrayList<Integer>> lineUpAILinks(NeatAI second) {
		
		ArrayList<Gene> secondL = second.getCloneLinks();
		
		ArrayList<Integer> ai1histIDlist = new ArrayList<Integer>();
		ArrayList<Integer> ai2histIDlist = new ArrayList<Integer>();
		
		ArrayList<Integer> linkID = new ArrayList<Integer>();
		ArrayList<Integer> linkList1Pos = new ArrayList<Integer>();
		ArrayList<Integer> linkList2Pos = new ArrayList<Integer>();
		
		//Get genes.
		int histID;
		for (Gene l : links) {
			histID = l.getHistID();
			linkID.add(histID);
			ai1histIDlist.add(histID);
		}

		int pos;
		for (Gene l : secondL) {
			histID = l.getHistID();
			pos = linkID.indexOf(histID);
			if (pos == -1) {
				//New histID found
				linkID.add(histID);
			}
			ai2histIDlist.add(histID);
		}
		
		//Sort genes
		//Bubble sort, meh
		int lowest;
		for (int i = 0; i < linkID.size(); i++) {
			lowest = i;
			for (int j = i+1; j < linkID.size(); j++) {
				if (linkID.get(j) < linkID.get(lowest)) {
					lowest = j;
				}
			}
			
			int temp = linkID.get(i);
			linkID.set(i, linkID.get(lowest));
			linkID.set(lowest, temp);
		}
		
		//Add gene positions
		for (Integer tempID : linkID) {
			linkList1Pos.add(ai1histIDlist.indexOf(tempID));
			linkList2Pos.add(ai2histIDlist.indexOf(tempID));
		}
		
		ArrayList<ArrayList<Integer>> temp = new ArrayList<ArrayList<Integer>>();
		
		temp.add(linkID);
		temp.add(linkList1Pos);
		temp.add(linkList2Pos);
		
		return temp;
    }
    
	@Override
	public String toString() {
		String temp = "Num inputs: " + numInputs + " : Num neurons: " + neurons.size() + "\n Links \n From : To : HistID";
		
		for (Gene l: links) {
			temp += "\n" + l.getFrom() + ":" + l.getTo() + ":" + l.getHistID();
		}
		
		return temp;
	}
	
	public NeatAI clone() {
		return new NeatAI(sm, getCloneNeurons(), getCloneLinks(), numInputs, numOutputs);
	}
}
