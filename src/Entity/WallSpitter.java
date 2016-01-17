package Entity;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.ArrayList;

import GameState.GameState;
import Main.GamePanel;
import TileMap.TileMap;

public class WallSpitter extends Enemy {	

	private int frame = 0;
	private double shootRate = 3;
	
	/*
	 * 0 = up
	 * 90 = right
	 * ect...
	 */
	private int direction;
	
	private static final int horOffset = 10;
	private static final int vertOffset = 9;
	private static final int vertOffset2 = 4;
	
	public WallSpitter(TileMap tm, int x_, int y_, int direction_, double FPSoffset, double frequency, GameState level_) {
		super(tm, "WallSpitter", 20, 13, -4, 1, false, level_);
		x_ = x_ - (int)Math.sin(Math.toRadians(direction_))*horOffset;
		y_ = y_ + (int)Math.cos(Math.toRadians(direction_))*vertOffset + vertOffset2;
		setPosition(x_, y_);
		direction = direction_;
		falling = false;
		frame = (int) (FPSoffset * GamePanel.IDEAL_FPS);
		shootRate = frequency;
	}
	
	public WallSpitter(TileMap tm, int x_, int y_, int direction_, double FPSoffset, GameState level_) {
		super(tm, "WallSpitter", 20, 13, -4, 1, false, level_);
		x_ = x_ - (int)Math.sin(Math.toRadians(direction_))*horOffset;
		y_ = y_ + (int)Math.cos(Math.toRadians(direction_))*vertOffset + vertOffset2;
		setPosition(x_, y_);
		direction = direction_;
		falling = false;
		frame = (int) (FPSoffset * GamePanel.IDEAL_FPS);
	}
	
	public WallSpitter(TileMap tm, int x_, int y_, int direction_, GameState level_) {
		super(tm, "WallSpitter", 20, 13, -4, 1, false, level_);
		x_ = x_ - (int)Math.sin(Math.toRadians(direction_))*horOffset;
		y_ = y_ + (int)Math.cos(Math.toRadians(direction_))*vertOffset + vertOffset2;
		setPosition(x_, y_);
		direction = direction_;
		falling = false;
	}
	
	public void update() {
		
		frame = (frame+1)%((int)(GamePanel.IDEAL_FPS*shootRate));
		if (frame == 0) {
			ArrayList<Projectile> projectiles = level.getProjectiles();
			//xvel, yvel
			int xvel;
			int yvel;
			switch (direction) {
				case 0:
					xvel = 0;
					yvel = -5;
					break;
				case 90:
					xvel = 5;
					yvel = 0;
					break;
				case 180:
					xvel = 0;
					yvel = 5;
					break;
				case 270:
					xvel = -5;
					yvel = 0;
					break;
				default:
					xvel = 2;
					yvel = -2;
			}
			projectiles.add(new Fireball(tileMap, (int)(x + xvel), (int)(y + yvel), xvel, yvel, 0, level));
		}
		
		Player player = level.getPlayer();
		if (intersects(player)) {
			player.hit(1);
		}
		
		setMapPosition();
	}
	
	@Override
	public void draw(Graphics2D g) {
		double rotationRequired = Math.toRadians (direction);
		double locationX = image.getWidth() / 2;
		double locationY = image.getHeight() / 2;
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

		// Drawing the rotated image at the required drawing locations
		g.drawImage(op.filter(image, null), (int)(x - xmap - width / 2), (int)(y - ymap - height / 2 + yOffset), null);
	}
}