package GameState;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import Entity.Enemy;
import Entity.MovingPlatformManager;
import Entity.Player;
import Entity.Projectile;
import Entity.ResourceManager;
import Main.GamePanel;
import TileMap.TileMap;

public class MenuState extends GameState {

	protected BufferedImage background;
	protected int level = 0;
	protected boolean isGAactive;
	protected Object preferences = null;
	protected int selection = 0;
	protected String[] options;
	protected String menuName;
	
	public MenuState(GameStateManager gsm_) {
		gsm = gsm_;
		
		try {
			background = ImageIO.read(getClass().getResourceAsStream("/Backgrounds/sky.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		menuName = "Level";
		options = new String[]{"Level1", "Level2", "Level3"};
	}
	
	public void reset() {
		return;
	}

	public void end() {
		return;
	}

	public boolean update() {
		return false;
	}

	public void draw(Graphics2D g) {
		g.drawImage(background, 0, 0, null);
		drawCenteredString(g, "Platformer Tales", GamePanel.WIDTH/2, 30, new Font("Times New Roman", 0, 20), Color.BLACK);
		
		int y = 60;
		for (int i = 0; i < options.length; i++) {
			Color buttonColor;
			if (i == selection) {
				buttonColor = Color.red;
			} else {
				buttonColor = Color.black;
			}
			
			drawCenteredString(g, options[i], GamePanel.WIDTH/2, y, new Font("Arial", 0, 12), buttonColor);
			y += 18;
		}
	}
	
	private void drawCenteredString(Graphics2D g, String text, int x, int y, Font font, Color color) {
		Font oldFont = g.getFont();
		
        //Set font
		g.setFont(font);
		
		drawCenteredString(g, text, x, y, color);
        
        g.setFont(oldFont);
	}
	
	
	private void drawCenteredString(Graphics2D g, String text, int x, int y, Color color) {
		
		//Center text
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(text, g);
        int newX = (GamePanel.WIDTH - (int) r.getWidth()) / 2;
        
		//Set color
		g.setColor(color);
		
		//Draw
        g.drawString(text, newX, y);
	}

	public void keyPressed(int k) {
		if (k == KeyEvent.VK_UP) {
			selection--;
			if (selection < 0) {
				selection = options.length-1;
			}
		} else if (k == KeyEvent.VK_DOWN) {
			selection++;
			if (selection == options.length) {
				selection = 0;
			}
		} else if (k == KeyEvent.VK_ENTER) {
			selectionMade();
		}
	}
	
	private void selectionMade() {
		switch (menuName) {
			case "Level":
				level = selection+1;
				if (level == 3) {
					menuName = "Difficulty";
					options = new String[]{"Normal", "Hard"};
				} else {
					playerMenu();
				}
				break;
			case "Difficulty":
				preferences = selection == 1;
				playerMenu();
				break;
			case "Player":
				isGAactive = (selection == 1);
				startLevel();
				break;
		}
		
		selection = 0;
	}
	
	public void playerMenu() {
		menuName = "Player";
		options = new String[]{"Player", "Computer"};
	}
	
	public void startLevel() {
		gsm.setIsGAactive(isGAactive);
		gsm.setState(level, preferences);
		gsm.initGA();
	}

	public void keyReleased(int k) {
		
	}
}
