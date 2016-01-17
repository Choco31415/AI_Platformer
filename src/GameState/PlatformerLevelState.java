package GameState;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import Audio.AudioPlayer;
import Entity.Cloud;
import Entity.Enemy;
import Entity.Projectile;
import Entity.MovingPlatform;
import Entity.MovingPlatformManager;
import Entity.MovingPlatformSpawner;
import Entity.PlantSpitter;
import Entity.Player;
import Entity.Purple;
import Entity.ResourceManager;
import Entity.Spitter;
import Main.GamePanel;
import TileMap.TileMap;

@SuppressWarnings("unused")
public abstract class PlatformerLevelState extends GameState {
	
	protected TileMap tileMap;
	protected BufferedImage background;
	
	protected ResourceManager rm;
	
	protected Player player;
	protected boolean readFromKeyboard;
	
	protected ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	protected ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
	
	protected ArrayList<Cloud> clouds = new ArrayList<Cloud>();
	
	protected MovingPlatformManager mpm = new MovingPlatformManager();
	
	protected AudioPlayer bgMusic;
	
	protected int endingTilesFromEnd;
	
	public int winState = 0; // 0 = playing, -1 = dead, 1 = winnerz
	public int frame = 0;
	public double deathSceneLength;
	
	//Resource reference sheet
	BufferedImage fireballImage;
	
	public PlatformerLevelState(GameStateManager gsm_) {
		gsm = gsm_;
		
		if (gsm.getAudioAllowed()) {
			bgMusic = new AudioPlayer("/Music/Jaunty Gumption.wav");
			bgMusic.play();
		}
		
		rm = new ResourceManager();
	}
	
	public void spawnClouds() {
		Random rand = new Random();
		int mapTileLength = tileMap.getWidth()/tileMap.getTileSize();
		for (int i = 0; i < rand.nextInt(mapTileLength/6) + mapTileLength/2; i++) {
			clouds.add(new Cloud(tileMap, rm));
		}
		
		int[] zDepths = new int[clouds.size()];
		for (int i = 0; i < clouds.size(); i++) {
			zDepths[i] = randInt(7, 13);
		}
		
		Arrays.sort(zDepths);
		
		for (int i = 0; i < clouds.size(); i++) {
			clouds.get(i).setZ(zDepths[clouds.size() - i - 1]);
			clouds.get(i).init();
		}
	}

	@Override
	public boolean update() {
		frame++;
		
		for (Cloud cloud : clouds) {
			cloud.update();
		}
		
		if (winState != -1) {
			player.update();
		}
		
		mpm.update();
		
		if (player.getDead() && winState == 0) {
			winState = -1;
			frame = 0;
			player.setJumping(false);
			player.setDown(false);
		}
		if (winState == -1 && frame >= deathSceneLength*GamePanel.IDEAL_FPS) {
			return true;
		}
		
		for (int i = 0; i < enemies.size(); i++) {
			Enemy e = enemies.get(i);
			e.update();
			if (e.isDead()) {
				enemies.remove(e);
				i--;
			}
		}
		
		boolean collided;
		ArrayList<Projectile> toRemove = new ArrayList<Projectile>();
		for (Projectile fb : projectiles) {
			collided = fb.update();
			if (collided) {
				toRemove.add(fb);
			}
		}
		for (Projectile p : toRemove) {
			projectiles.remove(p);
		}
		
		if (player.getx() >= tileMap.getWidth() - endingTilesFromEnd*20 + 10 && winState == 0) {
			player.setx(tileMap.getWidth() - endingTilesFromEnd*20 + 10);
			winState = 1;
			player.setLeft(false);
			player.setRight(false);
			player.setJumping(false);
			player.setDown(false);
		}
		if (winState == 1) {
			if (player.getdy() == 0) {
				player.setRight(true);
			}
			if (player.getx() > tileMap.getWidth() - tileMap.getTileSize()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void draw(Graphics2D g) {
		draw(g, false);
	}

	public void draw(Graphics2D g, boolean doingScreenshot) {
		double oldX = tileMap.getx();
		double oldY = tileMap.gety();
		
		//If doing a screenshot, set everything up.
		if (!doingScreenshot) {
			g.drawImage(background, 0, 0, null);

			for (Cloud cloud : clouds) {
				cloud.draw(g);
			}
		} else {
			tileMap.setPosition(0, 0);
		}
		
		g.setColor(new Color(255, 255, 0, 80));
		if (doingScreenshot) {
			g.fillRect(tileMap.getWidth() - endingTilesFromEnd*20 - (int)tileMap.getx(), 0, 20, 1000);
		} else {
			g.fillRect(tileMap.getWidth() - endingTilesFromEnd*20 - (int)tileMap.getx(), 0, 20, GamePanel.HEIGHT);
		}
		
		if (!doingScreenshot) {
			tileMap.draw(g);
		}
		mpm.draw(g);
		if (winState != -1 && !doingScreenshot) {
			player.draw(g);
		}
		
		for (Projectile fb : projectiles) {
			if (doingScreenshot) {
				fb.setMapPosition();
			}
			fb.draw(g);
		}
		
		for (Enemy enemy : enemies) {
			if (doingScreenshot) {
				enemy.setMapPosition();
			}
			enemy.draw(g);
		}
		
		//If done with a screenshot, undo changes made to obtain the screenshot.
		if (doingScreenshot) {
			tileMap.setPosition(oldX, oldY);
		}
	}

	@Override
	public void keyPressed(int k) {
		if (player == null || winState != 0 || !readFromKeyboard) {
			return;
		}
		if(k == KeyEvent.VK_LEFT) player.setLeft(true);
		if(k == KeyEvent.VK_RIGHT) player.setRight(true);
		if(k == KeyEvent.VK_UP) player.setJumping(true);
		if(k == KeyEvent.VK_DOWN) player.setDown(true);
		if(k == KeyEvent.VK_SPACE) player.setJumping(true);
	}

	@Override
	public void keyReleased(int k) {
		if (player == null || winState != 0 || !readFromKeyboard) {
			return;
		}
		if(k == KeyEvent.VK_LEFT) player.setLeft(false);
		if(k == KeyEvent.VK_RIGHT) player.setRight(false);
		if(k == KeyEvent.VK_UP) player.setJumping(false);
		if(k == KeyEvent.VK_DOWN) player.setDown(false);
		if(k == KeyEvent.VK_SPACE) player.setJumping(false);
		
	}
	
	public void setReadFromKeyboard(boolean bool) {
		readFromKeyboard = bool;
	}
	
	public ArrayList<Enemy> getEnemies() {
		return enemies;
	}
	
	public ArrayList<Projectile> getProjectiles() {
		return projectiles;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public MovingPlatformManager getMPM() {
		return mpm;
	}
	
	public BufferedImage getFireballImage() {
		return fireballImage;
	}
	
	public TileMap getTM() {
		return tileMap;
	}
	
	public int randInt(int bot, int top) {
		Random rand = new Random();
		int randVar = rand.nextInt(top-bot);
		return randVar + bot;
	}
	
	protected int getColX(double col) {
		return (int) (col*tileMap.getTileSize() + 10);
	}
	
	protected int getRowY(double row) {
		return (int) (row*tileMap.getTileSize() + 10);
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		//bgMusic.close();
	}
	
	public void toggleWindow() {
		gsm.toggleWindow();
	}
	
	public void toggleWindowUpdated() {
		
	}
	
	public void reset() {
		enemies = new ArrayList<Enemy>();
		projectiles = new ArrayList<Projectile>();
		mpm = new MovingPlatformManager();
		
		tileMap.reset();
		
		createLevel();
		
		frame = 0;
		winState = 0;
	}
	
	public int getWinState() {
		return winState;
	}
	
	public abstract void createLevel();
	
	public ResourceManager getRM() {
		return rm;
	}

	public TileMap getTileMap() {
		return tileMap;
	}
	
	public void screenshot() {
		BufferedImage imageRepresentation  = tileMap.screenshotMap();
		Graphics2D g = imageRepresentation.createGraphics();
		draw(g, true);
		g.setColor(Color.black);
		g.drawString(tileMap.getWidth() + "x" + tileMap.getHeight(), 5, 25);
		saveImage(imageRepresentation, "Map Screenshot.png");
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
}
