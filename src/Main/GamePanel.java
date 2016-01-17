package Main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;









// Imports from project files.
import GameState.GameStateManager;

@SuppressWarnings({ "serial", "unused" })
public class GamePanel extends JPanel implements Runnable, KeyListener  {
	
	// GameState
	GameStateManager gsm;
	
	// graphics
	private BufferedImage image;
	private Graphics2D g;
	
	// game thread
	private Thread thread;
	private boolean running;
	public static final int IDEAL_FPS = 30;
	public static final int FAST_FPS = 1000;
	public static int FPS = IDEAL_FPS;
	private double targetTime = 1000.0 / FPS;
	
	// window info
	public static final int WIDTH = 240;
	public static final int HEIGHT = 180;
	private static final int SCALE = 2;

	// extra game info
	private static boolean EXTRA_INFO = true;
	private static double UPDATE_FREQUENCY = 5.0;
	
	private static boolean showWindow = true;
	private JFrame game;
	
	public GamePanel(JFrame game_) {
		super();
		
		game = game_;
				
		setPreferredSize(
			new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		setFocusable(true);
		requestFocus();
		
		gsm = new GameStateManager(this);
	}
	
	public void addNotify() {
		super.addNotify();
		if(thread == null) {
			thread = new Thread(this);
			addKeyListener(this);
			thread.start();
		}
	}

	// Initialize anything.
	private void init() {
		running = true;
		
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		init();
		
		long start;
		long elapsed;
		long wait;
		long currentTime;
		long lastUpdateTime = System.nanoTime();
		int frameCounter = 0;
		
		//game loop
		while(running) {
			// 1000 is a second.

			start = System.nanoTime();
			
			// Do the brunt of things.
			update();
			draw();
			drawToScreen();
			
			// Update times, and wait if needed.
			currentTime = System.nanoTime();
			
			elapsed = currentTime - start;
			
			wait = (long) (targetTime - elapsed / 1000000.0); 
			
			
			// Print out a game info readout.
			frameCounter++;
			if (EXTRA_INFO && (System.nanoTime() - lastUpdateTime)/1000000 > UPDATE_FREQUENCY*1000) {
				System.out.println("fps:" + (frameCounter/UPDATE_FREQUENCY));
				gsm.printGAData();
				lastUpdateTime = System.nanoTime();
				frameCounter = 0;
			}

			
			if (wait > 0) {
				try {
					Thread.sleep(wait);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private void update() {
		gsm.update();
	}
	
	private void draw() {
		if (showWindow) {
			gsm.draw(g);
		}
	}
	
	/*
	 * This method pushes all graphical information to the screen. Aka it draws everything.
	 */
	private void drawToScreen() {
		if (showWindow) {
			Graphics g2 = getGraphics();
			g2.drawImage(image,  0,  0, WIDTH * SCALE, HEIGHT * SCALE, null);
			g2.dispose();
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		gsm.keyPressed(e.getKeyCode());
		

		//Option for switching between window and fast mode.
		if (e.getKeyCode() == KeyEvent.VK_Q) {
			toggleWindow();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		gsm.keyReleased(e.getKeyCode());
	}
	
    public void toggleWindow() {
    	showWindow = !showWindow;
    	if (showWindow) {
    		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
    		setFPS(IDEAL_FPS);
    	} else {
    		setPreferredSize(new Dimension(80, 50));
    		setFPS(FAST_FPS);
    	}
    	game.pack();
    	gsm.toggleWindowUpdated();
    }
    
    private void setFPS(int num) {
    	FPS = num;
    	targetTime = 1000.0 / FPS;
    }

	/*
	 * This method runs when the game window closes. Use to wrap up application. 
	 */
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
