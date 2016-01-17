package Entity;

import java.awt.Graphics2D;

import GameState.GameState;
import Main.GamePanel;
import TileMap.TileMap;

public class Player extends MapObject {

	private int health;
	private int maxHealth;
	private boolean dead = false;
	
	private boolean flinching = false;
	private long flinchTimer;
	private double flinchLength = 1.0;
	
	// player properties
	private double moveSpeed = 3;
	private double jumpStart = 9.5;
	public final static int MAX_FALL_SPEED = 8;
	public final static double GRAVITY = 0.6;
	
	private TileMap tm;
	
	public Player(TileMap tm_, GameState level_) {
		super(tm_, "Player", 20, 20, 0, true, true, level_);
		tm = tm_;
		health = 1;
	}
	
	public int getHealth() { return health; }
	public int getMaxHealth() { return maxHealth; }
	public boolean getDead() { return dead; }
	public double getdy() { return dy; }
	
	public void hit(int damage) {
		if (flinching) {
			return;
		}
		health -= damage;
		if (health <= 0) {
			dead = true;
		}
		flinching = true;
		flinchTimer = System.nanoTime();
	}
	
	private void getNextPosition() {
		if (right) {
			dx = moveSpeed;
		} else if (left) {
			dx = -moveSpeed;
		} else {
			dx = 0;
		}
		
		// jumping
		if(jumping && !falling && dy==0) {
			dy = -jumpStart;
			falling = true;
		}
		
		if (falling) {
			if (dy >= MAX_FALL_SPEED) {
				dy = MAX_FALL_SPEED;
			} else {
				dy += GRAVITY;
			}
		}
	}
	
	public void update() {
		
		getNextPosition();
		checkTileMapCollision();
		setPosition(xtemp, ytemp);
		
		// check if done flinching
		if(flinching) {
			long elapsed =
				(System.nanoTime() - flinchTimer) / 1000000;
			if(elapsed > 1000*flinchLength) {
				// 1 sec
				flinching = false;
			}
		}
		
		int newX = (int)(x-GamePanel.WIDTH/2);
		int newY = (int)(y-GamePanel.HEIGHT/2 - 20);
		tm.setPosition(newX, newY);
		
		for (int[] tile : tilesToDestroy) {
			tileMap.setMapTile(tile[0], tile[1], 0);
			if (tileMap.getType(tile[0]-1, tile[1]) == 0) {
				tileMap.setMapTile(tile[0]-1, tile[1], 0);
			}
		}
		tilesToDestroy.clear();
		
		if (y+1 >= tileMap.getHeight() - cheight/2) {
			health = 0;
			hit(0);
		}
		
		setMapPosition();
	}
	
	public void draw(Graphics2D g) {
		
		// draw player
		if(flinching) {
			long elapsed =
				(System.nanoTime() - flinchTimer) / 1000000;
			if(elapsed / 100 % 2 == 0) {
				return;
				// Causes player to not be drawn, resulting in "flashing"
			}
		}
		
		super.draw(g);
		
	}
	
}
