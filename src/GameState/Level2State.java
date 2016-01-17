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
public class Level2State extends PlatformerLevelState {

	public Level2State(GameStateManager gsm_) {
		super(gsm_);
		
		//Initialize variables
		deathSceneLength = 1;
		endingTilesFromEnd = 3;

		//Other stuff
		tileMap = new TileMap(20);
		tileMap.loadTiles("/Tilesets/grasstileset.png", "/Tilesets/grasstileset_extrainfo.txt");
		ArrayList<String> mapFiles = new ArrayList<String>();
		mapFiles.add("/Maps/level1-2-1.txt");
		mapFiles.add("/Maps/level1-2-2.txt");
		mapFiles.add("/Maps/level1-2-3.txt");
		mapFiles.add("/Maps/level1-2-4.txt");
		mapFiles.add("/Maps/level1-2-5.txt");
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
		player.setPosition(36, 230);

		enemies.add(new Purple(tileMap, getColX(39), getRowY(10), this));
		enemies.add(new Purple(tileMap, getColX(40), getRowY(10), this));
		enemies.add(new Purple(tileMap, getColX(41), getRowY(10), this));
		
		enemies.add(new Spitter(tileMap, getColX(69), getRowY(13), false, this));
		enemies.add(new PlantSpitter(tileMap, getColX(65.5), getRowY(11), this));
		enemies.add(new PlantSpitter(tileMap, getColX(73.5), getRowY(7), this));
		
		MovingPlatform temp = new MovingPlatform(tileMap, rm, 2, new int[]{getColX(94.5), getRowY(18)}, new int[]{getColX(94.5), getRowY(-1)}, 8, 0, true);
		mpm.addMPS(new MovingPlatformSpawner(temp, 3, mpm));
	}
}

