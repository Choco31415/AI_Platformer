package Entity;

import java.awt.Graphics2D;
import java.util.ArrayList;

import GameState.GameState;
import Main.GamePanel;
import TileMap.TileMap;

public class PlantSpitter extends Enemy {
	private boolean viewed = false;
	

	private int frame = 0;
	private double shootRate = 3;
	
	public PlantSpitter(TileMap tm, int x_, int y_, int FPSoffset, double frequency, GameState level_) {
		super(tm, "PlantSpitter", 20, 13, -4, 1, false, level_);
		setPosition(x_, y_);
		falling = false;
		frame = FPSoffset * GamePanel.IDEAL_FPS;
		shootRate = frequency;
	}
	
	public PlantSpitter(TileMap tm, int x_, int y_, int FPSoffset, GameState level_) {
		super(tm, "PlantSpitter", 20, 13, -4, 1, false, level_);
		setPosition(x_, y_);
		falling = false;
		frame = FPSoffset * GamePanel.IDEAL_FPS;
	}
	
	public PlantSpitter(TileMap tm, int x_, int y_, GameState level_) {
		super(tm, "PlantSpitter", 20, 13, -4, 1, false, level_);
		setPosition(x_, y_);
		falling = false;
	}
	
	public void getNextPosition() {
		if (dy >= Player.MAX_FALL_SPEED) {
			dy = Player.MAX_FALL_SPEED;
		} else {
			dy += Player.GRAVITY;
		}
	}
	
	public void update() {
		getNextPosition();
		checkTileMapCollision();
		setPosition(xtemp, ytemp);
		
		if (!notOnScreen() && !viewed) {
			viewed = true;
		}
		
		frame = (frame+1)%((int)(GamePanel.IDEAL_FPS*shootRate));
		if (frame == 0 && viewed) {
			ArrayList<Projectile> projectiles = level.getProjectiles();
			projectiles.add(new Fireball(tileMap, (int)x, (int)y, -2, -5, 0.5, level));
			projectiles.add(new Fireball(tileMap, (int)x, (int)y, 0, -5, 0.5, level));
			projectiles.add(new Fireball(tileMap, (int)x, (int)y, 2, -5, 0.5, level));
			
		}
		
		Player player = level.getPlayer();
		if (intersects(player)) {
			if (y - player.gety() - height/2 >= -player.getdy() && player.getdy() > 0) {
				hit(1);
				player.setYvelFuture(-6.0);
			} else {
				player.hit(1);
			}
		}
		
		setMapPosition();
	}
	
	public void draw(Graphics2D g) {
		super.draw(g);
	}
}
