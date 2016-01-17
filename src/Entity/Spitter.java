package Entity;

import java.awt.Graphics2D;
import java.util.ArrayList;

import GameState.GameState;
import Main.GamePanel;
import TileMap.TileMap;

public class Spitter extends Enemy{

	private boolean viewed = false;
	
	private int direction; // 1 = right, -1 = left
	private double moveSpeed = 1;
	private boolean moving;
	private double shootRate = 2;
	
	private int frame = 0;
	
	public Spitter(TileMap tm, int x_, int y_, int FPSoffset, boolean moving_, GameState level_) {
		super(tm, "Spitter", 20, 16, -2, 1, false, level_);
		setPosition(x_, y_);
		falling = false;
		moving = moving_;
		frame = FPSoffset * GamePanel.IDEAL_FPS;
	}
	
	public Spitter(TileMap tm, int x_, int y_, boolean moving_, GameState level_) {
		super(tm, "Spitter", 20, 16, -2, 1, false, level_);
		setPosition(x_, y_);
		falling = false;
		moving = moving_;
	}
	
	private void getNextPosition() {
		frame++;
		
		if (!notOnScreen() && !viewed) {
			viewed = true;
			if (level.getPlayer().getx() > x) {
				direction = 1;
			} else {
				direction = -1;//-1
			}
		}
		
		if (viewed) {
			if (moving) {
				dx = direction * moveSpeed;
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
			
			if (moving) {
				calculateCorners(x + dx, y);
				if (topLeft || topRight || x <= cwidth/2 || x >= tileMap.getWidth() - width/2 - 10) {
					direction *= -1;//-1
					dx *= -1;
				}
			} else {
				if (x > player.getx()) {
					direction = -1;//-1
				} else {
					direction = 1;
				}
			}
			
			if (frame%(GamePanel.IDEAL_FPS*shootRate) == 0) {
				frame = 0;
				ArrayList<Projectile> projectiles = level.getProjectiles();
				projectiles.add(new Fireball(tileMap, x + direction*(width/2), y+3, direction*3.5, 0, 0, level));
			}
			
			if (moving) {
				facingRight = dx <= 0;
			} else {
				facingRight = player.getx() < x;
			}
			
		}
		
		if (dy >= Player.MAX_FALL_SPEED) {
			dy = Player.MAX_FALL_SPEED;
		} else {
			dy += Player.GRAVITY;
		}
		
		super.update();
	}
	
	public void update() {
		getNextPosition();
		checkTileMapCollision();
		setPosition(xtemp, ytemp);
		
		setMapPosition();
	}
	
	public void draw(Graphics2D g) {
		super.draw(g);
	}
}
