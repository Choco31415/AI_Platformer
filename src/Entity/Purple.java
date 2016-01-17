package Entity;

import java.awt.Graphics2D;

import Entity.Player;
import GameState.GameState;
import TileMap.TileMap;

public class Purple extends Enemy {

	private boolean viewed = false;
	
	private int direction; // 1 = right, -1 = left
	private int moveSpeed = 1;
	
	public Purple(TileMap tm, int x_, int y_, GameState level_) {
		super(tm, "Purple", 20, 16, -2, 1, false, level_);
		setPosition(x_, y_);
		falling = false;
	}
	
	private void getNextPosition() {
		if (!notOnScreen() && !viewed) {
			viewed = true;
			if (level.getPlayer().getx() > x) {
				direction = 1;
			} else {
				direction = -1;
			}
		}
		
		if (viewed) {
			dx = direction * moveSpeed;
			
			Player player = level.getPlayer();
			if (intersects(player)) {
				if (y - player.gety() - height/2 >= -player.getdy() && player.getdy() > 0) {
					hit(1);
					player.setYvelFuture(-6.0);
				} else {
					player.hit(1);
				}
			}
			
			calculateCorners(x + dx, y);
			if (topLeft || topRight || x <= cwidth/2 || x >= tileMap.getWidth() - width/2 - 10) {
				direction *= -1;
				dx *= -1;
			}
		}
		
		if (dy >= Player.MAX_FALL_SPEED) {
			dy = Player.MAX_FALL_SPEED;
		} else {
			dy += Player.GRAVITY;
		}
		
		facingRight = dx <= 0;
	}
	
	public void update() {
		getNextPosition();
		super.update();
		checkTileMapCollision();
		setPosition(xtemp, ytemp);
		
		setMapPosition();
	}
	
	public void draw(Graphics2D g) {
		super.draw(g);
	}

}
