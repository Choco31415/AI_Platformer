package GameState;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import AI.GeneticAlgorithm;
import AI.SimpleMap;
import Main.GamePanel;

public class GameStateManager {

	private GameState currentState;
	private int currentStateNumber;
	private boolean currentStateIsPlatformer = false;
	
	public static final int NUMGAMESTATES = 4;
	public static final int MENUSTATE = 0; //NOT USED
	public static final int LEVEL1STATE = 1;
	public static final int LEVEL2STATE = 2;
	public static final int LEVEL3STATE = 3;
	
	private boolean isGAactive = true;
	private boolean resetGA = true;
	private int GAresetsCount;
	
	private GeneticAlgorithm ga;
	
	SimpleMap sm;
	GamePanel gp;
	
	private boolean audioAllowed = false;
	
	private boolean stateSetThisFrame;
	
	public GameStateManager(GamePanel gp_) {
		gp = gp_;
		setState(MENUSTATE);
		
		initGA();
		
		//small minor things
		GAresetsCount = 0;
	}
	
	public void initGA() {
		if (isGAactive && currentStateIsPlatformer) {
			sm = new SimpleMap(((PlatformerLevelState)currentState).getTM(), 11, 11, currentState);
			((PlatformerLevelState)currentState).setReadFromKeyboard(false);
		
			ga = new GeneticAlgorithm(sm, currentState);
			
			notifyGA();
		}
	}
	
	public void setState(int state) {
		setState(state, null);
	}
	
	public void setState(int state, Object preferences) {
		currentStateIsPlatformer = false;
		if (state == MENUSTATE) {
			currentState = new MenuState(this);
		} else if (state == LEVEL1STATE) {
			currentState = new Level1State(this);
			currentStateIsPlatformer = true;
		} else if (state == LEVEL2STATE) {
			currentState = new Level2State(this);
			currentStateIsPlatformer = true;
		} else if (state == LEVEL3STATE) {
			currentState = new Level3State(this, (boolean)preferences);
			currentStateIsPlatformer = true;
		}
		
		if (state >= 0 && state < NUMGAMESTATES) {
			currentStateNumber = state;
		} else {
			throw new Error();
		}
		
		if (currentStateIsPlatformer) {
			if (ga != null) {
				notifyGA();
				sm.setLevel(currentState);
			}
			
			if (isGAactive) {
				((PlatformerLevelState)currentState).deathSceneLength = GeneticAlgorithm.deathSceneLength;
			}
		}
	}
	
	public void notifyGA() {
		ga.stateSet(((PlatformerLevelState)currentState).getPlayer());
		ga.checkForErrors();
	}
	
	public void update() {
		stateSetThisFrame = false;
		try {
			boolean refresh = currentState.update();
			
			if (!isGAactive && currentStateIsPlatformer && ((PlatformerLevelState)currentState).getWinState() == 1) {

			} else if (refresh) {
				resetLevel();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if (isGAactive && currentStateIsPlatformer && ga != null) {
			sm.update();
			boolean reset = ga.update();
			
			if (reset && resetGA) {
				ga = new GeneticAlgorithm(sm, currentState);
				resetLevel();
				
				//Only gather this many data points
				GAresetsCount++;
				if (GAresetsCount == 30) {
					System.exit(0);
				}
			}
		}
	}
	
	public void resetLevel() {
		currentState.reset();
		stateSetThisFrame = true;
		
		if (ga != null && currentStateIsPlatformer) {
			notifyGA();
			sm.setLevel(currentState);
		}
	}
	
	public void draw(java.awt.Graphics2D g) {
		if (!stateSetThisFrame) {
			try {
				currentState.draw(g);
			} catch(Exception e) {}
			
			if (isGAactive && currentStateIsPlatformer && ga != null) {
				sm.draw(g);
				ga.draw(g);
			}
		}
	}
	
	public void keyPressed(int k) {
		currentState.keyPressed(k);
		
		if (k == KeyEvent.VK_E) {
			ga.writeGraph(0);
		} else if (k == KeyEvent.VK_H) {
			String help = "Controls:";
			String[] lines = new String[]{"H - Help", "Q - Enter and exit fast mode", "E - Export graphs", "A - Go to the best AI", "G - Go to menu", "S - Screenshot the entire level"};
			Arrays.sort(lines);
			for (String line : lines) {
				help += "\n" + line;
			}
			
			System.out.println(help);
			writeFile(help, "Help.txt");
		} else if (k == KeyEvent.VK_A) {
			ga.toggleBestAIDisplay();
		} else if (k == KeyEvent.VK_G) {
			setState(MENUSTATE);
		} else if (k == KeyEvent.VK_S) {
			((PlatformerLevelState)currentState).screenshot();
		}
	}
	
	public void writeFile(String text, String location) {
		PrintWriter writer = null;
		try {
			System.out.println(location);
			writer = new PrintWriter(location, "UTF-8");
		} catch (FileNotFoundException e) {
			System.out.println("Err1 File Not Found");
			return;
		} catch (UnsupportedEncodingException e) {
			System.out.println("Err2 Unsupported file format");
			return;
		}
		writer.write(text);
		writer.close();
	}
	
	public void keyReleased(int k) {
		currentState.keyReleased(k);
	}

	public void printGAData() {
		if (isGAactive && currentStateIsPlatformer) {
			System.out.println("generation: " +  ga.getGeneration());
			System.out.println("current AI num: " + ga.getCurrentAInum() + "/" + ga.getPopulation());
			System.out.println("furthest distance: " + ga.getFurthestDistance());
		}
		
	}
	
	public void toggleWindow() {
		gp.toggleWindow();
	}
	
	public void toggleWindowUpdated() {
		if (currentStateIsPlatformer) {
			((PlatformerLevelState)currentState).toggleWindowUpdated();
		}
	}
	
	public int getCurrentStateNumber() { return currentStateNumber; }
	public boolean getAudioAllowed() { return audioAllowed; }
	public void setIsGAactive(boolean bool) { isGAactive = bool; }
	public boolean isGAactive() { return isGAactive; }
}
