package AI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import Entity.Player;
import GameState.GameState;
import GameState.PlatformerLevelState;
import Main.GamePanel;

public class GeneticAlgorithm {

	private int population;
	
	private double c1;
	private double c2;
	private double c3;
	private int cMinNetworkSize;
	private double compatibilityThreshold;
	
	private int minNetworkSizeForChamp;

	private double mutateRate;
	private double uniformPerturbeRate;//Each weight: uniform perturbed or new value?
	private double perturbeMagnitude;
	
	private double disableRate;//How often should a link stay disabled if when during crossover, only one of hte parents has the link disabled?
	
	private double crossOverRate;
	
	private double interSpeciesRate;
	
	private double newNodeRate;
	private double newLinkRate;
	
	public static double biasRate;//Used by NeatAI class. How often should the input of a new link be the bias?
	
	private double removeWeakRate;//Remove the weakest % from each species
	private int minSizeForRemove;
	
	private int staleLimit;//Minimum amount of staleness to re-populate species. 0 or below to not use.
	private int absoluteStaleLimit;//If staleness reaches this level, start from scratch. 0 or below to not use.
	private int numSpeciesToProtect;//If the population is stale, how many species do we protect?
	
	private NeatAI[] ais;
	private int currentAInum;
	private int[] distances;
	private double[] fitnesses;
	
	public static double deathSceneLength;

	private double firstStillTimeLimit;
	private double stillTimeLimit;
	private int stillCounter;
	private boolean moved;
	
	private SimpleMap sm;
	private PlatformerLevelState level;
	
	private Player player;
	
	private int historicalCounter;
	private int speciesCounter;
	private int generation;
	
	private int xFarthest;
	private int xbeginning;
	
	private int xFarthestAverage;
	private int numSpecies;
	
	private ArrayList<ArrayList<Integer>> historicalSpeciesNum;//What species existed in each generation?
	private ArrayList<ArrayList<Integer>> historicalSpeciesNumCount;//How many of each species existed in each generation?
	private ArrayList<ArrayList<Integer>> historicalSpeciesFarthestX;//What was the farthest each species went in each generation?
	private ArrayList<Integer> historicalAverageDistance;//Average distance per generation
	private ArrayList<NeatAI> historicalBestAI;// TODO: The best AI per generation.
	private NeatAI globalHistoricalBestAI = null;//Best AI developed.
	private int globalHistoricalFarthestX;//Best distance.
	private int stalenessCounter;
	private int rollingHistoricalFarthestX;
	
	private boolean writeGraphs;
	private boolean enableColor;
	private boolean drawYellow;
	
	private boolean displayingBestAI = false;
	private NeatAI AIdisplayed = null;
	
	public GeneticAlgorithm(SimpleMap sm_, GameState level_) {
		
		sm = sm_;
		level = (PlatformerLevelState) level_;
		
		//set ga information
		population = 300;//100;
		
		mutateRate = 0.25;//0.8;
		uniformPerturbeRate = 0.9;
		perturbeMagnitude = 0.2;
		
		newNodeRate = 0.5;//0.03;
		newLinkRate = 2;//0.2;
		
		disableRate = 0.4;//0.75;
		
		interSpeciesRate = 0.001;
		
		biasRate = 0.4;//0.1;
		
		c1 = 2.0;//1.0;
		c2 = 2.0;//1.0;
		c3 = 0.4;//1.0;
		cMinNetworkSize = 0;
		compatibilityThreshold = 2.5;//4.0;
		
		crossOverRate = 0.75;//0.75;
		
		removeWeakRate = 0.5;
		minSizeForRemove = 0;
		
		minNetworkSizeForChamp = 1;
		
		staleLimit = 20;
		absoluteStaleLimit = 70;
		numSpeciesToProtect = 2;
		
		//other minor things
		currentAInum = -1;
		
		historicalCounter = 0;
		speciesCounter = 1;
		generation = 1;
		
		deathSceneLength = 0.1;
		firstStillTimeLimit = 0.5;
		stillTimeLimit = 2.5;
		stillCounter = 0;
		
		player = null;
		
		distances = new int[population];
		fitnesses = new double[population];
		ais = new NeatAI[population];
		
		xFarthest = 0;
		xFarthestAverage = 0;
		numSpecies = 1;
		
		historicalSpeciesNum = new ArrayList<ArrayList<Integer>>();
		historicalSpeciesNumCount = new ArrayList<ArrayList<Integer>>();
		historicalSpeciesFarthestX = new ArrayList<ArrayList<Integer>>();
		historicalAverageDistance = new ArrayList<Integer>();
		stalenessCounter = 0;
		rollingHistoricalFarthestX = 0;
		globalHistoricalFarthestX = 0;
		
		writeGraphs = false;
		drawYellow = false;
		enableColor = true;
		
		repopulate();
		
		resetAI();
		
		globalHistoricalBestAI = ais[0];
	}
	
	public void repopulate() {
		// Generate ai
		
		for (int i = 0; i < population; i++) {
			ais[i] = randomAI(speciesCounter);
		}
		consoldateHistID();
		//speciesCounter++;
		updateNumSpeciesAndRecordData();

		stalenessCounter = 0;
		rollingHistoricalFarthestX = 0;
	}
	
	public NeatAI randomAI(int species) {
		Random rand = new Random();
		NeatAI ai = new NeatAI(sm, sm.getArea()+1, 4);
		
		int numLinks = rand.nextInt(3)+1;
		
		for (int j = 0; j < numLinks; j++) {
			addLink(ai);
		}
		if (rand.nextDouble() < 0.5) {
			addNode(ai);
		}
		
		ai.setSpecies(species);
		
		return ai;
	}
	
	/**
	 * 
	 * @return A boolean representing if the final AI is created or not.
	 */
	public boolean update() {
		if (displayingBestAI) {			
			interpretResults(AIdisplayed.get(sm.getFlatMap(true)));
			
			xFarthest = (int)player.getx()-xbeginning;
			return false;
		}
		
		if (!player.getDead()) {
			if (level.getWinState() != 1) {
				interpretResults(ais[currentAInum].get(sm.getFlatMap(true)));
			} 
			
			/*if (level.getWinState() == 1 || generation == 500) {
				System.out.println("gen: " + generation);
				System.exit(0);
				return true;//Stop. Enough of this. 500 generations is enough.
			}*/
		
			if (player.getx()-xbeginning > xFarthest) {
				xFarthest = (int)player.getx()-xbeginning;
				stillCounter = 0;
				moved = true;
			} else {
				stillCounter++;
				if (stillCounter >= stillTimeLimit*GamePanel.IDEAL_FPS) {
					//If the player is still for too long, restart.
					player.hit(999);
				}
			}
			
			if (!moved && stillCounter >= (int)(firstStillTimeLimit*GamePanel.IDEAL_FPS)) {
				//If the player is still for too long, restart.
				player.hit(999);
			}
		}
		
		return false;
	}
	
	private void interpretResults(double[] results) {
		boolean right = results[0] > 0;
		boolean left = results[1] > 0;
		boolean jumping = results[2] > 0;
		boolean down = results[3] > 0;
		player.setRight(right & !left);
		player.setLeft(left & !right);
		player.setJumping(jumping);
		player.setDown(down);
	}
	
	public void stateSet(Player player_) {
		player = player_;
		
		if (displayingBestAI) {
			return;
		}
		
		if (currentAInum >= 0) {
			distances[currentAInum] = xFarthest;
			////System.out.println(xFarthest);
			
			if (xFarthest > globalHistoricalFarthestX) {
				globalHistoricalBestAI = ais[currentAInum].clone();
				globalHistoricalFarthestX = xFarthest;
			}
		}
		
		xFarthest = 0;
		xbeginning = (int)player_.getx();
		
		currentAInum++;
		
		resetAI();
		
		Random rand = new Random();
		if (currentAInum == population) {			
			//System.out.println("mutate0");
			//System.out.println("mutate0");
			// Re-populate!!!
			currentAInum = 0;
			NeatAI[] newais = new NeatAI[population];
			
			//Note down some stats
			int distanceSum = 0;
			for (int distance : distances) {
				distanceSum += distance;
			}
			xFarthestAverage = distanceSum/population;
			historicalAverageDistance.add(xFarthestAverage);
			
			// Line up (aka index) species
			ArrayList<Integer> speciesNum = new ArrayList<Integer>();//What species do we have?
			ArrayList<ArrayList<Integer>> speciesLocations = new ArrayList<ArrayList<Integer>>();//Where do they appear?
			ArrayList<Integer> speciesFarthestX = new ArrayList<Integer>();//Farthest distance per species
			ArrayList<Double> speciesCollectiveFitness = new ArrayList<Double>();//What is the collective fitness per species?
			ArrayList<Integer> speciesPopAllocation = new ArrayList<Integer>();//How much population will be allocated to each species?
			int historicalFarthestX = 0;

			Integer species;
			int speciesIndex;
			ArrayList<Integer> temp;
			for (int i = 0; i < population; i++) {
				species = ais[i].getSpecies();
				speciesIndex = speciesNum.indexOf(species);
				if (speciesIndex != -1) {
					speciesLocations.get(speciesIndex).add(i);
				} else {
					speciesNum.add(species);
					
					temp = new ArrayList<Integer>();
					temp.add(i);
					speciesLocations.add(temp);
				}
			}
			numSpecies = speciesNum.size();
			
			// Create and adjust fitness scores for each individual
			for (int i = 0; i < population; i++) {
				species = ais[i].getSpecies();
				speciesIndex = speciesNum.indexOf(species);
				if (speciesIndex == -1) {
					throw new Error();
				}
				fitnesses[i] = (distances[i]*distances[i])/speciesLocations.get(speciesIndex).size();
			}
			
			//System.out.println("mutate1");
			//System.out.println("mutate1");
			
			//Generate Farthest x per species
			int highest;
			Integer distance;
			ArrayList<Integer> indexes;
			
			for (int i = 0; i < speciesNum.size(); i++) {
				indexes = speciesLocations.get(i);
				highest = 0;
				for (int j = 1; j < indexes.size(); j++) {
					if (distances[indexes.get(j)] > distances[indexes.get(highest)]) {
						highest = j;
					}
				}
				distance = distances[indexes.get(highest)];
				speciesFarthestX.add(distance);
				
				//Generate furthest x this generation
				if (distance > historicalFarthestX) {
					historicalFarthestX = distance;
				}
			}
			
			historicalSpeciesFarthestX.add(speciesFarthestX);
			
			// Sum fitnesses between species;
			double sum;
			int bestSpeciesIndex = 0;
			for (int i = 0; i < speciesNum.size(); i++) {
				sum = 0;
				for (Integer j : speciesLocations.get(i)) {
					sum += fitnesses[j];
				}
				speciesCollectiveFitness.add(sum);
				if (sum > speciesCollectiveFitness.get(bestSpeciesIndex)) {
					bestSpeciesIndex = i;
				}
			}
			
			//System.out.println("mutate2");
			//System.out.println("mutate2");
			
			//Update staleness
			//System.out.println(stalenessCounter);
			if (generation > 0) {
				if (historicalFarthestX > rollingHistoricalFarthestX) {
					rollingHistoricalFarthestX = historicalFarthestX;
					stalenessCounter = 0;
				} else {
					stalenessCounter++;
				}
				
				if (stalenessCounter == absoluteStaleLimit && absoluteStaleLimit > 0) {
					repopulate();
					
					generation++;
					
					return;
				} else if (stalenessCounter%staleLimit == 0 && stalenessCounter != 0 && staleLimit > 0) {
					//We have a stale population. Find the top performing species, and get rid of the rest.
					ArrayList<Integer> bestSpeciesIndeces = new ArrayList<Integer>();
					for (int i = 0; i < speciesNum.size(); i++) {
						bestSpeciesIndeces.add(i);
					}
					//Order the species by fitness.
					for (int i = 0; i < numSpeciesToProtect && i < bestSpeciesIndeces.size(); i++ ) {
						int highestIndex = i;
						for (int index = i+1; index < bestSpeciesIndeces.size(); index++) {
							if (speciesCollectiveFitness.get(bestSpeciesIndeces.get(index)) > speciesCollectiveFitness.get(bestSpeciesIndeces.get(highestIndex))) {
								highestIndex = index;
							}
						}
						int tempD = i;
						bestSpeciesIndeces.set(i, highestIndex);
						bestSpeciesIndeces.set(highestIndex, tempD);
					}
					
					/*System.out.print("{");
					for (int i = 0; i < bestSpeciesIndeces.size(); i++) {
						System.out.print(speciesCollectiveFitness.get(bestSpeciesIndeces.get(i)) + ", ");
					}
					System.out.println("}");*/
					
					if (numSpeciesToProtect >= bestSpeciesIndeces.size()) {
						//All species are protected. Continue!
						bestSpeciesIndeces.clear();
					} else {
						numSpecies = numSpeciesToProtect;
						//Remove the best species.
						for (int i = 0; i < numSpeciesToProtect; i++) {
							bestSpeciesIndeces.remove(0);
						}
						//Iterate over the worst species.
						for (int index : bestSpeciesIndeces) {
							for (int loc : speciesLocations.get(index)) {
								fitnesses[loc] = 0;
							}
							speciesCollectiveFitness.set(index, 0.0);
						}
					}
				}
			}
			
			//Calculate total population fitness
			double totalFitness = 0;
			for (Double d : speciesCollectiveFitness) {
				totalFitness += d;
			}
			if (totalFitness == 0) {
				repopulate();
				
				generation++;
				
				return;
			}
			
			//Allocate next generation population
			int size;
			int populationUnallocated = population;
			double totalFitnessLeft = totalFitness;
			for (int i = 0; i < speciesNum.size(); i++) {
				if (i == speciesNum.size()-1) {
					size = populationUnallocated;
				} else {
					size = (int)(populationUnallocated*(speciesCollectiveFitness.get(i)/totalFitnessLeft));
				}
				speciesPopAllocation.add(size);
				populationUnallocated -= size;
				totalFitnessLeft -= speciesCollectiveFitness.get(i);
			}

			//Check for errors.
			if (populationUnallocated != 0) {
				//System.out.println("" + population);
				for (double fit : fitnesses) {
					System.out.print(" " + fit);
				}
				throw new Error();
			}

			// Mutate, crossover, create the next generation of ai's.
			int firstAI_ID;
			int secondAI_ID;
			double fitness1;
			double fitness2;
			int individual = 0;
			ArrayList<Integer> specieLocations;
			int specieSize;
			Double[] specieFitnesses;
			
			//System.out.println("mutate3");
			//System.out.println("mutate3");
			
			for (int i = 0; i < speciesNum.size(); i++) {
				//Iterate over species
				//System.out.println("mutate3.1");
				
				specieLocations = speciesLocations.get(i);
				specieSize = specieLocations.size();
					
				specieFitnesses = new Double[specieSize];
				for (int k = 0; k < specieSize; k++) {
					specieFitnesses[k] = fitnesses[specieLocations.get(k)];
				}

				//System.out.println("species1");
				RouletteWheel rw = new RouletteWheel(specieLocations.toArray(), specieFitnesses);
				
				if (specieSize > minSizeForRemove) {
					rw.subtractLowest((int)(specieSize*removeWeakRate));
				}
				
				int bias = 0;
				if(speciesPopAllocation.get(i) >= minNetworkSizeForChamp) {
					firstAI_ID = (int)rw.getHighestWeightedObject();
					newais[individual] = ais[firstAI_ID].clone();
					individual += 1;
					bias = 1;
				}
				//System.out.println("sp2");
				
				//System.out.println("sp:" + speciesPopAllocation.get(i));
				for (int j = bias; j < speciesPopAllocation.get(i); j++) {
					//Iterate over new species individuals
					//System.out.println("ind1:" + individual);
					
					firstAI_ID = (int)rw.pickRandomItem();
					
				 	if (rand.nextDouble() < crossOverRate) {
						fitness1 = fitnesses[firstAI_ID];
						if (rand.nextDouble() < interSpeciesRate) {
							//System.out.println("inter");
							//pick random ai
							secondAI_ID = rand.nextInt(ais.length);
							fitness2 = fitnesses[secondAI_ID];
							newais[individual] = ais[firstAI_ID].crossOver(ais[secondAI_ID].clone(), fitness1, fitness2, disableRate);
						} else {							
							//System.out.println("cross");
							//pick second ai, and crossover
							secondAI_ID = (int)rw.pickRandomItem();
							
							fitness2 = fitnesses[secondAI_ID];
							//System.out.println("cross2");
							newais[individual] = ais[firstAI_ID].crossOver(ais[secondAI_ID].clone(), fitness1, fitness2, disableRate);
						} 
					} else {
						//System.out.println("clone" +  individual);
						newais[individual] = ais[firstAI_ID].clone();
					}
					//System.out.println("ind2");
				 	
					if (rand.nextDouble() < mutateRate) {
						newais[individual].perturbe(uniformPerturbeRate, perturbeMagnitude);
					}
					if (rand.nextDouble() < newNodeRate) {
						addNode(newais[individual]);
					}
					for (double chance = newLinkRate; chance > rand.nextDouble(); chance--) {
						addLink(newais[individual]);
					}
					
					individual += 1;
				}
			}
			
			// Correct species of new generation.
			//System.out.println("mutate4");
			//System.out.println("mutate4");
			boolean speciesFound;
			NeatAI tempAI;
			int tempLoc;
			for (int i = 0; i < population; i++) {
				speciesFound = false;
				for (int j = 0; j < speciesNum.size(); j++) {
					tempLoc = speciesLocations.get(j).get(0);
					if (tempLoc < population) {
						tempAI = ais[tempLoc];
					} else {
						tempAI = newais[tempLoc - population];
					}
					if (newais[i].compatibilityDistance(tempAI, c1, c2, c3, cMinNetworkSize) < compatibilityThreshold) {
						newais[i].setSpecies(tempAI.getSpecies());
						speciesFound = true;
						continue;
					}
				}
				if (!speciesFound) {
					//This creature is unique enough to warrant a new species.
					newais[i].setSpecies(speciesCounter);
					speciesCounter++;

					//Add the new species to the list of existing species to prevent fragmentation.
					speciesNum.add(speciesCounter);
					
					temp = new ArrayList<Integer>();
					temp.add(i + population);
					speciesLocations.add(temp);
				}
			}
			
			// Push changes.
			ais = newais;
			consoldateHistID();
			
			//Only increase generation when the generation is completed. Otherwise there can be graphing errors!
			generation++;
			
			updateNumSpeciesAndRecordData();
		}

	}
	
	public void updateNumSpeciesAndRecordData() {
		//System.out.println("update");
		//System.out.println("update");
		// Count num species and indexes.
		ArrayList<Integer> speciesNum = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> speciesIndexes= new ArrayList<ArrayList<Integer>>();

		Integer species;
		int index;
		ArrayList<Integer> temp;
		for (int i = 0; i < population; i++) {
			species = ais[i].getSpecies();
			index = speciesNum.indexOf(species);
			if (index == -1) {
				speciesNum.add(species);
				
				temp = new ArrayList<Integer>();
				temp.add(i);
				speciesIndexes.add(temp);
			} else {
				speciesIndexes.get(index).add(i);
			}
		}
		numSpecies = speciesNum.size();
		
		//Sort species
		int min;
		for (int i = 0; i < speciesNum.size(); i++) {
			min = i;
			for (int j = i+1; j < speciesNum.size(); j++) {
				if (speciesNum.get(j) < speciesNum.get(min)) {
					min = j;
				}
			}
			
			int temp2 = speciesNum.get(i);
			int temp3 = speciesNum.get(min);
			speciesNum.set(i, temp3);
			speciesNum.set(min, temp2);
			
			ArrayList<Integer> temp4 = speciesIndexes.get(i);
			ArrayList<Integer> temp5 = speciesIndexes.get(min);
			speciesIndexes.set(i, temp5);
			speciesIndexes.set(min, temp4);
		}
		
		//Count up species
		ArrayList<Integer> speciesCount = new ArrayList<Integer>();
		for (ArrayList<Integer> indexes : speciesIndexes) {
			speciesCount.add(indexes.size());
		}
		
		//Record species info
		if (historicalSpeciesNum.size() <= generation) {
			historicalSpeciesNum.add(speciesNum);
			historicalSpeciesNumCount.add(speciesCount);
		}
		
		if (generation > 1 && writeGraphs) {
			//System.out.println("graph");
			//System.out.println("graph");
			writeGraph(0);
		}
	}
	
	public void checkForErrors() {
		if (historicalSpeciesNum.size() != generation) {
			throw new Error("generation: " + generation + "histSpecNum: " + historicalSpeciesNum.size());
		}
	}
	
	public void writeGraph(int chopOff) {
		//System.out.println("write graph");
		generation -= chopOff;
		//Make a speciation graph!!
		int leftBuffer = 60;
		int topBuffer = 80;
		int cellLength = 3 + (70/population);
		int cellHeight = 9 + population/10;
		int width = Math.max(leftBuffer + cellLength*population, 250)+30;
		int height = Math.max(topBuffer + cellHeight*(generation-1)+10, 200);
		BufferedImage imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gc =  setUpGraph(imageRepresentation, "Speciation Map", "Population", "Generation", leftBuffer, topBuffer, true);

		////System.out.println("base");
		//Draw axis markings
		Font font = gc.getFont();
		gc.setFont(font.deriveFont(10f));
		int i;
		String s;
		for (i = 1; i < generation; i += 2) {
			s = padLeft("" + i, 4);
			gc.drawString(s, leftBuffer-24, topBuffer+cellHeight*(i-1)+5);
		}
		i = generation;
		s = padLeft("" + i, 4);
		gc.drawString(s, leftBuffer-20, topBuffer+cellHeight*(i-1)+5);
		for (i = 1; i < generation; i += 2) {
			gc.drawString("" + i, leftBuffer+population*cellLength+3, topBuffer+cellHeight*(i-1)+5);
		}
		i = generation;
		gc.drawString("" + i, leftBuffer+population*cellLength+3, topBuffer+cellHeight*(i-1)+5);
		gc.drawString("0", leftBuffer+2, topBuffer-5);
		s = padLeft("" + population, 4);
		gc.drawString(s, leftBuffer + cellLength*population - 24, topBuffer-5);
		gc.fillRect(leftBuffer, topBuffer, cellLength*population, cellHeight*(generation-1));
		
		////System.out.println("axis");
		//Draw graph lines
		for (int gen = 0; gen < generation-1; gen++) {
			////System.out.println("for");
			boolean done = false;
			int boti = 0;
			int topi = 0;
			int botSize;
			int topSize;
			int botSpecies = 0;
			int topSpecies = 0;
			int botPopCount = 0;
			int topPopCount = 0;
			Point previousBotPoint = new Point(leftBuffer, topBuffer + (gen)*cellHeight);
			Point previousTopPoint = new Point(leftBuffer, topBuffer + topBuffer + (gen+1)*cellHeight);
			Point botPoint = new Point(leftBuffer, topBuffer + (gen)*cellHeight);
			Point topPoint = new Point(leftBuffer, topBuffer + (gen+1)*cellHeight);
			Polygon tempTri;
			
			////System.out.println("for" + historicalSpeciesNum.size());
			botSize = historicalSpeciesNum.get(gen).size();
			////System.out.println("for" + historicalSpeciesNum.size());
			////System.out.println("gen" + gen);
			////System.out.println("gener" + generation);
			////System.out.println("what" + historicalSpeciesNum.get(0).size());
			topSize = historicalSpeciesNum.get(gen+1).size();
			////System.out.println("initialized");
			do {
				////System.out.println("species0");
				gc.setColor(Color.WHITE);
				if (boti < botSize) {
					botSpecies = historicalSpeciesNum.get(gen).get(boti);
				}
				if (topi < topSize) {
					topSpecies = historicalSpeciesNum.get(gen+1).get(topi);
				}
				////System.out.println("species1");
				
				if ((botSpecies <= topSpecies || topi >= topSize) && boti < botSize) {
					botPopCount += historicalSpeciesNumCount.get(gen).get(boti); 
					boti += 1;
				}
				if ((topSpecies <= botSpecies || boti >= botSize) && topi < topSize) {
					topPopCount += historicalSpeciesNumCount.get(gen+1).get(topi);
					topi += 1;
				}
				
				//if (!(previousBotPoint == botPoint)) {
					previousBotPoint = botPoint;
				//}
				//if (!(previousTopPoint == topPoint)) {
					previousTopPoint = topPoint;
				//}
				botPoint = new Point(cellLength*botPopCount + leftBuffer, topBuffer + (gen)*cellHeight);
				topPoint = new Point(cellLength*topPopCount + leftBuffer, topBuffer + (gen+1)*cellHeight);
				
				if (botSpecies != topSpecies ) {
					
					tempTri = new Polygon();
					tempTri.addPoint(botPoint.x, botPoint.y);
					tempTri.addPoint(topPoint.x, topPoint.y);

					if (!historicalSpeciesNum.get(gen+1).contains(botSpecies)) {
						tempTri.addPoint(previousBotPoint.x, previousBotPoint.y);
						if (enableColor) {
							gc.setColor(Color.GREEN);
						}
						gc.fillPolygon(tempTri);
					} else if (drawYellow) {
						tempTri.addPoint(previousTopPoint.x, previousTopPoint.y);
						if (enableColor) {
							gc.setColor(Color.YELLOW);
						}
						gc.fillPolygon(tempTri);
					} else {
						gc.drawLine(botPoint.x, botPoint.y, topPoint.x, topPoint.y);
					}
				} else {
					gc.drawLine(botPoint.x, botPoint.y, topPoint.x, topPoint.y);
				}
				
				if (boti >= botSize && topi >= topSize) {
					done = true;
				}
				////System.out.println("species4");
			} while(!done);
		}
		//gc.dispose();
		
		////System.out.println("graph1.5");
		saveImage(imageRepresentation, "Speciation Map.png");
		////System.out.println("graph2");
		////System.out.println("graph2");
		
		//Make a distance graph!
		topBuffer += 30;
		cellLength = 15;
		int graphWidth = cellLength*generation;
		int graphHeight = 400;
		width = Math.max(leftBuffer + graphWidth + 40, 350);
		height = topBuffer + graphHeight;
		imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		topBuffer -= 50;
		gc = setUpGraph(imageRepresentation, "Distance Map", "Generation", "Farthest x", leftBuffer, topBuffer, false);
		
		// Draw axis
		gc.drawLine(leftBuffer, topBuffer, leftBuffer, topBuffer + graphHeight);
		gc.drawLine(leftBuffer, topBuffer + graphHeight, leftBuffer + graphWidth, topBuffer + graphHeight);
		if (graphWidth >= 200) {
			gc.drawLine(leftBuffer + graphWidth, topBuffer, leftBuffer + graphWidth, topBuffer + graphHeight);
		}
		////System.out.println("axis");
		
		// Draw axis markings
		font = gc.getFont();
		gc.setFont(font.deriveFont(10f));
		for (i = 0; i < generation; i += 2) {
			gc.drawString("" + i, leftBuffer + i*cellLength - 2, topBuffer + graphHeight + 16);
		}
		i = generation;
		gc.drawString("" + i, leftBuffer + i*cellLength - 2, topBuffer + graphHeight + 16);
		for (i = 0; i < 9; i += 1) {
			s = padLeft("" + (int)(((8 - i)/8.0)*globalHistoricalFarthestX), 5);
			gc.drawString(s, leftBuffer-26, (int)(topBuffer+(i/8.0)*graphHeight+5));
		}
		if (graphWidth >= 200) {
			for (i = 0; i < 9; i += 1) {
				s = "" + (int)(((8 - i)/8.0)*globalHistoricalFarthestX);
				gc.drawString(s, leftBuffer + graphWidth+5, (int)(topBuffer+(i/8.0)*graphHeight+5));
			}
		}
		
		// Draw graph data
		double bias;
		if (globalHistoricalFarthestX == 0) {
			bias = 1.0;
		} else {
			bias = (double)graphHeight/globalHistoricalFarthestX;
		}

		//Draw graph key
		gc.drawString("Black lines - Specie Best", 215, 15);
		int bottomOfGraph = (int)(topBuffer + graphHeight);
		gc.drawLine((int)(leftBuffer + (1)*cellLength), bottomOfGraph, (int)(leftBuffer + (1)*cellLength), bottomOfGraph - 2);//Draw x axis tick for gen 1.
		gc.drawLine((int)(leftBuffer + (generation)*cellLength), bottomOfGraph, (int)(leftBuffer + (generation)*cellLength), bottomOfGraph - 2);//Draw x axis tick for last gen.
		for (int gen = 0; gen < generation-2; gen++) {
			ArrayList<Integer> genFarthestX = historicalSpeciesFarthestX.get(gen);
			ArrayList<Integer> nextGenFarthestX = new ArrayList<Integer>();
			if (generation > 2) {
				nextGenFarthestX = historicalSpeciesFarthestX.get(gen+1);
			}
			
			gc.drawLine((int)(leftBuffer + (gen+2)*cellLength), bottomOfGraph, (int)(leftBuffer + (gen+2)*cellLength), bottomOfGraph - 2);//Draw x axis ticks.

			for (int sp = 0; sp < genFarthestX.size(); sp++) {
				Integer speciesID =  historicalSpeciesNum.get(gen).get(sp);
				int index = historicalSpeciesNum.get(gen+1).indexOf(speciesID);
				
				if (gen == 0) {
					gc.drawLine((int)(leftBuffer + gen*cellLength), bottomOfGraph, (int)(leftBuffer + (gen+1)*cellLength), (int)(topBuffer + ((double)graphHeight - genFarthestX.get(sp)*bias)));
				}
				if (index != -1 && generation > 2) {
					gc.drawLine((int)(leftBuffer + (gen+1)*cellLength), (int)(topBuffer + ((double)graphHeight - genFarthestX.get(sp)*bias)), (int)(leftBuffer + (gen+2)*cellLength), (int)(topBuffer + ((double)graphHeight - nextGenFarthestX.get(index)*bias)));
				}
			}
		}
		saveImage(imageRepresentation, "Farthest Distance Map.png");
		
		//Add average to the distance map.
		//Draw more of the graph key.
		gc.drawString("Blue line - Generation Average", 215, 30);
		gc.drawString("Blue circle - Specie die out", 215, 45);
		gc.setStroke(new BasicStroke(2));
		gc.setColor(new Color(80, 183, 217));
		for (int gen = 0; gen < generation-2; gen++) {
			if (gen == 0) {
				gc.drawLine((int)(leftBuffer + gen*cellLength), (int)(topBuffer + graphHeight), (int)(leftBuffer + (gen+1)*cellLength), (int)(topBuffer + ((double)graphHeight - historicalAverageDistance.get(gen)*bias)));
				gc.drawLine((int)(leftBuffer + (gen+1)*cellLength), (int)(topBuffer + ((double)graphHeight - historicalAverageDistance.get(gen)*bias)), (int)(leftBuffer + (gen+2)*cellLength), (int)(topBuffer + ((double)graphHeight - historicalAverageDistance.get(gen+1)*bias)));
			} else if (generation > 2) {
				gc.drawLine((int)(leftBuffer + (gen+1)*cellLength), (int)(topBuffer + ((double)graphHeight - historicalAverageDistance.get(gen)*bias)), (int)(leftBuffer + (gen+2)*cellLength), (int)(topBuffer + ((double)graphHeight - historicalAverageDistance.get(gen+1)*bias)));
			}
			
			//Get any species that died out.
			ArrayList<Integer> genFarthestX = historicalSpeciesFarthestX.get(gen);
			for (int sp = 0; sp < genFarthestX.size(); sp++) {
				Integer speciesID =  historicalSpeciesNum.get(gen).get(sp);
				int index = historicalSpeciesNum.get(gen+1).indexOf(speciesID);
				
				if (index == -1) {
					//Species died out. Mark it.
					gc.drawOval((int)(leftBuffer + (gen+1)*cellLength)-3, (int)(topBuffer + ((double)graphHeight - genFarthestX.get(sp)*bias))-3, 6, 6);
				}
			}
		}
		 
		gc.dispose();
		
		saveImage(imageRepresentation, "Farthest Distance Map Detailed.png");
		////System.out.println("Success");
		
		generation += chopOff;
	}
	
	public Graphics2D setUpGraph(BufferedImage img, String title, String x_axis, String y_axis, int leftBuffer, int topBuffer, boolean x_axisLabelIsTop) {
		Graphics2D gc = img.createGraphics();
		int width = img.getWidth();
		int height = img.getHeight();
		
		//Set the background color
		gc.setColor(Color.WHITE);
		gc.fillRect(0, 0, width, height);
		
		//Put in header and x-axis label
		gc.setColor(Color.BLACK);
		Font font = gc.getFont();
		gc.setFont(font.deriveFont(30f));
		gc.drawString(title, 10, 30);
		gc.setFont(font.deriveFont(20f));
		if (x_axisLabelIsTop) {
			gc.drawString(x_axis, width/2 + leftBuffer/2-60, 55);
		} else {
			gc.drawString(x_axis, width/2 + leftBuffer/2-60, height-10);
		}
		
		//Put in y-axis label
		//The code is bloated due to the text being vertical
		font = gc.getFont();
		gc.setFont(font.deriveFont(30f));
		
		AffineTransform oldXForm = gc.getTransform();
		AffineTransform at = new AffineTransform();
		at.rotate(-Math.PI / 2, 0, 0);
		gc.setTransform(at);
		
		//Create a second buffered image with the y-axis label text
		BufferedImage genText = new BufferedImage(120, 20, BufferedImage.TYPE_INT_RGB);
		Graphics2D gc2 = genText.createGraphics();
		
		gc2.setColor(Color.WHITE);
		gc2.fillRect(0, 0, 120, 20);
		gc2.setColor(Color.BLACK);
        gc2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		font = gc2.getFont();
		gc2.setFont(font.deriveFont(20f));
		gc2.drawString(y_axis, 0, 20);
		
		gc2.dispose();
		
		gc.drawImage(genText, null, -(topBuffer + (height/2 + 20)), 10);

		gc.setTransform(oldXForm);
		
		return gc;
	}
	
	public static String padLeft(String s, int n) {
	    return String.format("%1$" + n + "s", s);  
	}
	
	private void saveImage(BufferedImage img, String title) {
		try {
		    // retrieve image
		    BufferedImage bi = img;
		    File outputfile = new File(title);
		    ImageIO.write(bi, "png", outputfile);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private void addLink(NeatAI n) {
		historicalCounter = n.addLink(historicalCounter);
	}
	
	private void addNode(NeatAI n) {
		historicalCounter = n.addNode(historicalCounter);
	}
	
	public void resetAI() {
		moved = false;
		stillCounter = 0;
	}
	
	/*
	 * This method is required to be called when the ai's are done being edited.
	 * If any two ai develop the same link in a generation, the links' historicalIDs will be made the same.
	 */
	private void consoldateHistID() {
		
	}
	
	public int getGeneration() { return generation; }
	public int getCurrentAInum() { return currentAInum; }
	public int getPopulation() { return population; }
	public int getFurthestDistance() { return globalHistoricalFarthestX; }
	
	public void draw(Graphics2D g) {
		if (currentAInum == -1) {
			return;
		}
		
		if (displayingBestAI) {
			globalHistoricalBestAI.draw(g);
		} else {
			ais[currentAInum].draw(g);
		}
		
		g.setColor(new Color(255, 255, 255, 180));
		g.fillRect(0, 0, GamePanel.WIDTH, sm.getCornerOffset());
		g.fillRect(0, GamePanel.HEIGHT - 2*sm.getCornerOffset(), GamePanel.WIDTH, sm.getCornerOffset()*2);
		g.setColor(Color.BLACK);
		Font font = g.getFont();
		g.setFont(font.deriveFont(sm.getCornerOffset()-2));
		g.drawString("Controls: H - create help txt file", 0, sm.getCornerOffset()-1);
		if (displayingBestAI) {
			g.drawString("Displaying Best AI", 0, GamePanel.HEIGHT - 1 - sm.getCornerOffset());
			g.drawString("Final Distance: " + globalHistoricalFarthestX + " Current Distance: " + xFarthest, 0, GamePanel.HEIGHT - 1);
		} else {
			g.drawString("Gen: " + generation + " ai: " + currentAInum + "/" + population + " numSpc: " + numSpecies + " Spc: #" + ais[currentAInum].getSpecies() + " Stl: " + stalenessCounter, 0, GamePanel.HEIGHT - 1 - sm.getCornerOffset());
			g.drawString("prvAvrDist: " + (int)xFarthestAverage +  " distance:" + xFarthest  + " Best dist: " + globalHistoricalFarthestX, 0, GamePanel.HEIGHT - 1);
		}
	}

	public void toggleBestAIDisplay() {
		displayingBestAI = !displayingBestAI;
		AIdisplayed = globalHistoricalBestAI;
		player.hit(999);
		if (!displayingBestAI) {
			currentAInum--;
		}
	}
}
