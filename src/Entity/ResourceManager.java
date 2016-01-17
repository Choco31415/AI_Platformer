package Entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ResourceManager {

	ArrayList<String> imageNames = new ArrayList<String>();
	ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	
	public ResourceManager() {
		loadImage("/Sprites/Background/Clouds.png", "Clouds");
		loadImage("/Sprites/Enemy/Fireball.png", "Fireball");
		loadImage("/Tilesets/MovingPlatforms.png", "MovingPlatforms");
		loadImage("/Sprites/Enemy/PlantSpitter.png", "PlantSpitter");
		loadImage("/Sprites/Player/player.png", "Player");
		loadImage("/Sprites/Enemy/Enemy.png", "Purple");
		loadImage("/Sprites/Enemy/Shooter.png", "Spitter");
		loadImage("/Sprites/Enemy/Wall Spitter.png", "WallSpitter");
	}
	
	public void loadImage(String path, String name) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(getClass().getResourceAsStream(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Err");
			e.printStackTrace();
		}
		
		imageNames.add(name);
		images.add(image);
	}
	
	public BufferedImage getImage(String name) {
		int index = imageNames.indexOf(name);
		if (index == -1) {
			throw new Error();
		}
		return images.get(index);
	}
	
	public BufferedImage getImage(int index) {
		if (index < 0 || index >= images.size()) {
			throw new Error();
		}
		return images.get(index);
	}
}
