package Entity;

import GameState.GameState;
import TileMap.TileMap;

public class Fireball extends Projectile {
	
	public Fireball(TileMap tm, double x_, double y_, double dx_, double dy_, double gravity_, GameState level_) {
		super(tm, "Fireball", x_, y_, dx_, dy_, 8, 8, gravity_, level_);
	}
}
