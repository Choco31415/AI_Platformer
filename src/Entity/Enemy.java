package Entity;

import java.awt.Graphics2D;

import GameState.GameState;
import TileMap.TileMap;

public class Enemy extends MapObject {
	
	protected int health;
	protected int maxHealth;
	protected boolean dead;
	protected int damage;
	
	protected boolean flinching;
	protected long flinchTimer;
	
	public Enemy(TileMap tm, String image_, int cwidth_, int cheight_, int yOffset_, int health_, boolean player_, GameState level_) {
		super(tm, image_, cwidth_, cheight_, yOffset_, player_, true, level_);
		health = health_;
	}
	
	public boolean isDead() { return dead; }
	
	public int getDamage() { return damage; }
	
	public void hit(int damage) {
		if(dead || flinching) return;
		health -= damage;
		if(health < 0) health = 0;
		if(health == 0) dead = true;
		flinching = true;
		flinchTimer = System.nanoTime();
	}
	
	public void update() {
		if (y >= tileMap.getHeight() - cheight/2) {
			dead = true;
		}
		
		setMapPosition();
	}
	
	public void draw(Graphics2D g) {
		super.draw(g);
	}
	
}