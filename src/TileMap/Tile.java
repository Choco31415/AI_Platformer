package TileMap;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Tile {
	
	private BufferedImage image;
	private int type;
	private int xOffset;
	private int yOffset;
	
	// tile types
	public static final int NORMAL = 0;
	public static final int BLOCKED = 1;
	public static final int SLANTED_LEFT = 2; // slope: /
	public static final int SLANTED_RIGHT = 3; // slope: \
	
	public Tile(BufferedImage image, int type, int xOffset, int yOffset) {
		this.image = image;
		this.type = type;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	public BufferedImage getImage() { return image; }
	public int getType() { return type; }
	public int getXOffset() { return xOffset; }
	public int getYOffset() { return yOffset; }
	
	public void draw(Graphics2D g) {
		g.drawImage(image, 0, 0, null);
	}
	
	@Override
	public String toString() {
		String tile = "";
		
		tile += "Type: " + type;
		tile += " xOffset: " + xOffset;
		tile += " yOffset: " + yOffset;
		
		return tile;
	}
}
