package GameState;

import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import Entity.MovingPlatform;
import Entity.PlantSpitter;
import Entity.Player;
import Entity.Purple;
import Entity.Spitter;
import Entity.WallSpitter;
import TileMap.TileMap;

public class Level1State extends PlatformerLevelState {

	public Level1State(GameStateManager gsm_) {
		super(gsm_);
		
		//Initialize variables
		deathSceneLength = 1;
		endingTilesFromEnd = 3;

		//Other stuff
		tileMap = new TileMap(20);
		tileMap.loadTiles("/Tilesets/grasstileset.png", "/Tilesets/grasstileset_extrainfo.txt");
		ArrayList<String> mapFiles = new ArrayList<String>();
		mapFiles.add("/Maps/level1-1-1.txt");
		mapFiles.add("/Maps/level1-1-2.txt");
		mapFiles.add("/Maps/level1-1-3.txt");
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
		player.setPosition(36, 310);

		enemies.add(new WallSpitter(tileMap, getColX(56), getRowY(14), 0, this));
		enemies.add(new WallSpitter(tileMap, getColX(57), getRowY(14), 0, this));
		
		enemies.add(new Purple(tileMap, 200, 290, this));

		enemies.add(new Purple(tileMap, 300, 290, this));
		
		enemies.add(new PlantSpitter(tileMap, 120, 270, this));
		enemies.add(new PlantSpitter(tileMap, 490, 110, this));
		//enemies.add(new PlantSpitter(tileMap, 510, 110, 1, this));
		enemies.add(new PlantSpitter(tileMap, 530, 110, 2, this));
		enemies.add(new Spitter(tileMap, 450, 290, true, this));
		enemies.add(new Spitter(tileMap, 550, 290, 1, false, this));
		enemies.add(new Spitter(tileMap, 570, 270, false, this));
		enemies.add(new Spitter(tileMap, 290, 10, false, this));

		mpm.addPlatform(new MovingPlatform(tileMap, rm, 2, new int[]{660, 120}, new int[]{740, 120}, 3, 0, false));
		mpm.addPlatform(new MovingPlatform(tileMap, rm, 2, new int[]{80, 240}, new int[]{80, 240}, 3, 0, false));
	}
}
