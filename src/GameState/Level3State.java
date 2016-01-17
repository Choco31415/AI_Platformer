package GameState;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import Audio.AudioPlayer;
import Entity.Cloud;
import Entity.Enemy;
import Entity.MovingPlatform;
import Entity.MovingPlatformManager;
import Entity.MovingPlatformSpawner;
import Entity.PlantSpitter;
import Entity.Player;
import Entity.Projectile;
import Entity.Purple;
import Entity.Spitter;
import Entity.WallSpitter;
import TileMap.TileMap;

@SuppressWarnings("unused")
public class Level3State extends PlatformerLevelState {

	private boolean difficult;
	
	public Level3State(GameStateManager gsm_, boolean difficult_) {
		super(gsm_);
		difficult = difficult_;
		
		//Initialize variables
		deathSceneLength = 1;
		endingTilesFromEnd = 3;

		//Other stuff
		tileMap = new TileMap(20);
		tileMap.loadTiles("/Tilesets/grasstileset.png", "/Tilesets/grasstileset_extrainfo.txt");
		ArrayList<String> mapFiles = new ArrayList<String>();
		mapFiles.add("/Maps/level1-3-1.txt");
		mapFiles.add("/Maps/level1-3-2.txt");
		mapFiles.add("/Maps/level1-3-3.txt");
		mapFiles.add("/Maps/level1-3-4.txt");
		mapFiles.add("/Maps/level1-3-5.txt");
		mapFiles.add("/Maps/level1-3-6.txt");
		tileMap.loadMap(mapFiles);
		tileMap.setPosition(100, 0);
		
		try {
			background = ImageIO.read(getClass().getResourceAsStream("/Backgrounds/sky.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fireballImage = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemy/Fireball.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		readFromKeyboard = true;
		
		createLevel();
		
		spawnClouds();
	}

	public void createLevel() {		
		
		player = new Player(tileMap, this);
		player.setPosition(36, getRowY(8));
		
		//	public Purple(TileMap tm, int x_, int y_, GameState level_) {
		enemies.add(new Purple(tileMap, getColX(9), getRowY(6), this));
		enemies.add(new Purple(tileMap, getColX(11), getRowY(6), this));
		enemies.add(new Purple(tileMap, getColX(13), getRowY(6), this));
		
		enemies.add(new Purple(tileMap, getColX(21), getRowY(8), this));
		
		//	public WallSpitter(TileMap tm, int x_, int y_, int direction_, int FPSoffset, double frequency, GameState level_) {
		if (difficult) {
			for (int i = 0; i < 16; i++) {
				enemies.add(new WallSpitter(tileMap, getColX(23 + i), getRowY(1), 180, i*0.5, 2, this));
			}
			enemies.add(new WallSpitter(tileMap, getColX(25), getRowY(8), 0, 2, this));
			enemies.add(new WallSpitter(tileMap, getColX(29), getRowY(8), 0, 2, this));
			enemies.add(new WallSpitter(tileMap, getColX(36), getRowY(8), 0, 2, this));
		} else {
			for (int i = 0; i < 16; i++) {
				enemies.add(new WallSpitter(tileMap, getColX(23 + i), getRowY(1), 180, i*0.6, 2, this));
			}
		}
		
		enemies.add(new Purple(tileMap, getColX(50), getRowY(8), this));
		enemies.add(new Purple(tileMap, getColX(51), getRowY(8), this));
		enemies.add(new Purple(tileMap, getColX(52), getRowY(8), this));
		
		enemies.add(new WallSpitter(tileMap, getColX(87), getRowY(12), 270, 0, 3, this));
		enemies.add(new WallSpitter(tileMap, getColX(87), getRowY(14), 270, 1.5, 3, this));
		
		enemies.add(new Purple(tileMap, getColX(101), getRowY(14), this));
		enemies.add(new Purple(tileMap, getColX(107), getRowY(14), this));
	}
}

