package Entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import GameState.GameState;
import TileMap.TileMap;

public class Projectile extends MapObject {
	
	private double gravity;
	
	private static int MAX_FALL_SPEED = 5;

	public Projectile(TileMap tm, String image, double x_, double y_, double dx_, double dy_, int cwidth_, int cheight_, double gravity_, GameState level_) {
		super(tm, image, cwidth_, cheight_, 0, false, false, level_);
		setPosition(x_, y_);
		dx = dx_;
		dy = dy_;
		gravity = gravity_;
	}
	
	public Projectile(TileMap tm, BufferedImage image, double x_, double y_, double dx_, double dy_, int cwidth_, int cheight_, double gravity_, GameState level_) {
		super(tm, image, cwidth_, cheight_, 0, false, false, level_);
		setPosition(x_, y_);
		dx = dx_;
		dy = dy_;
		gravity = gravity_;
	}
	
	public boolean update() {
		checkTileMapCollision();
		setPosition(xtemp, ytemp);
		
		setMapPosition();
		
		Player player = level.getPlayer();
		if (intersects(player)) {
			player.hit(1);
			return true;
		}
		
		if (dy >= MAX_FALL_SPEED) {
			dy = MAX_FALL_SPEED;
		} else {
			dy += gravity;
		}
		
		if (collided) {
			return true;
		}
		
		if (x <= width/2 || x >= tileMap.getWidth()-width/2 || y < 0) {
			return true;
		}
		
		return false;
	}
	
	public void draw(Graphics2D g) {
		super.draw(g);
	}
}
