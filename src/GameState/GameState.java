package GameState;

import java.util.ArrayList;

import TileMap.TileMap;
import Entity.Enemy;
import Entity.MovingPlatformManager;
import Entity.Player;
import Entity.Projectile;
import Entity.ResourceManager;

public abstract class GameState {
	
	protected GameStateManager gsm;
	
	public abstract void reset();
	public abstract void end();
	public abstract boolean update();
	public abstract void draw(java.awt.Graphics2D g);
	public abstract void keyPressed(int k);
	public abstract void keyReleased(int k);
}
