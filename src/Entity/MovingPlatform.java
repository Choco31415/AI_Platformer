package Entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Main.GamePanel;
import TileMap.TileMap;

public class MovingPlatform implements Cloneable {

	private BufferedImage image;
	
	private double x;
	private double y;
	private final double width;
	private final double height = 10;
	private final int yOffset = 0;
	
	private double xmap;
	private double ymap;
	private TileMap tileMap;
	
	// moving platform movement information
	private int[] startPos;
	private int[] endPos;
	private int speed;
	private int delay;
	private boolean selfDestruct;
	private int frame = 0;
	private int flaggedFrame = 0;
	private boolean waiting;
	
	public MovingPlatform(TileMap tileMap_, ResourceManager rm, int length_, int[] startPos_, int[] endPos_, int speed_, int delay_, boolean selfDestruct_) {
		tileMap = tileMap_;
		x = startPos_[0];
		y = startPos_[1];
		
		startPos = startPos_;
		endPos = endPos_;
		speed = speed_;
		delay = delay_;
		selfDestruct = selfDestruct_;
		
		image = rm.getImage("MovingPlatforms");
		
		if (length_ == 1) {
			image = image.getSubimage(0, 0, 20, 20);
		} else if (length_ == 2) {
			image = image.getSubimage(20, 0,  40,  20);
		} else {
			throw new Error();
		}
		width = image.getWidth();
		waiting = false;
	}
	
	public MovingPlatform(TileMap tileMap_, BufferedImage img, int length_, int[] startPos_, int[] endPos_, int speed_, int delay_, boolean selfDestruct_) {
		tileMap = tileMap_;
		x = startPos_[0];
		y = startPos_[1];
		
		startPos = startPos_;
		endPos = endPos_;
		speed = speed_;
		delay = delay_;
		selfDestruct = selfDestruct_;
		
		image = img;
	
		width = image.getWidth();
		waiting = false;
	}
	
	public double getx() { return x; }
	public double gety() { return y; }
	public double getWidth() { return width; }
	public double getHeight() { return height; }
	
	public boolean update() {
		setMapPosition();
		
		frame++;
		if (waiting) {
			if (frame - flaggedFrame - 1 == delay*GamePanel.IDEAL_FPS) {
				waiting = false;
				flaggedFrame = frame;
			}
		} else {
			double progress = ((frame-flaggedFrame)/(double)(speed*GamePanel.IDEAL_FPS));
			x = progress * endPos[0] + (1 - progress) * startPos[0] + 0.01;
			y = progress * endPos[1] + (1 - progress) * startPos[1] + 0.01;
			if (progress == 1) {
				if (selfDestruct) {
					return true;
				} else {
					flaggedFrame = frame;
					waiting = true;
					int[] temp = endPos;
					endPos = startPos;
					startPos = temp;
				}
			}
			

		}
		return false;
	}
	
	public boolean intersects(double x_, double y_) {
		return (int)x_ < x + width/2 - 1 && x_ >= x - width/2 && y_ <= y + height/2   && y_ >= y - height/2 - 4;
	}
	
	public double distanceAboveSurface(double y_) {
		return y - height/2 - y_;
	}
	
	public void setMapPosition() {
		xmap = tileMap.getx();
		ymap = tileMap.gety();
	}
	
	public void draw(Graphics2D g) {		
		g.drawImage(
				image,
				(int)(x - xmap - width / 2),
				(int)(y - ymap - height / 2 + yOffset),
				null
			);
	}
	
	public MovingPlatform clone() {
		BufferedImage clone = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D g2d = clone.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		return new MovingPlatform(tileMap, clone, (int)width/20, startPos, endPos, speed, delay, selfDestruct);
	}
}

