package Entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Random;

import TileMap.TileMap;

public class Cloud {

	private double x;
	private double y;
	private double z;
	
	private ResourceManager rm;
	private BufferedImage image;
	private int width;
	private int height;
	
	private double xmap;
	private double ymap;
	
	private TileMap tileMap;
	private int midLine = 110;
	private int floatHeight = -900;
	private double windSpeed = 0.8;
	private int normalSize = 9;
	
	public Cloud(TileMap tm, ResourceManager rm_) {
		initY();
		
		tileMap = tm;
		rm = rm_;
	}
	
	public double getZ() { return z; }
	public void setZ(double z_) { z = z_; }
	
	public void init() {
		setImage();
		initX();
	}
	
	public void initX() {
		x = randInt((int)(-image.getWidth()*z + 1), (int)(z*(tileMap.getWidth() + image.getWidth() - 1)));
	}
	public void initY() {
		y = randInt(floatHeight, floatHeight + 40);
	}
	
	public void setImage() {
		image = rm.getImage("Clouds");
		
		Random rand = new Random();
		int cloudID = rand.nextInt(5);
		
		image = image.getSubimage(0, 50*cloudID, 100, 50);
		
		width = image.getWidth();
		height = image.getHeight();
		
		convertImage();
		
		// add alpha
        float[] scales = {1f, 1f, 1f, (float)((18-z)/12.0)};
        float[] offsets = new float[4];
        RescaleOp rop = new RescaleOp(scales, offsets, null);
		rop.filter(image, image);
	}
	
	public void setMapPosition() {
		xmap = tileMap.getx();
		ymap = tileMap.gety();
	}
	
	public void update() {
		x += windSpeed;
		if (notOnScreen()) {
			setImage();
			x = -image.getWidth()*z+1;
			initY();
		}
	}
	
	public void draw(Graphics2D g) {
		setMapPosition();
	
		g.drawImage(
				image,
				(int)((x - xmap)/z),
				(int)((y - ymap - midLine)/z + midLine),
				(int)(width*(normalSize/z)),
				(int)(height*(normalSize/z)),
				null);
	}
	
	public boolean notOnScreen() {
		return x < -width*z || x > (tileMap.getWidth() + width)*z;
	}
	
	public int randInt(int bot, int top) {
		Random rand = new Random();
		int randVar = rand.nextInt(top-bot);
		return randVar + bot;
	}
	
	public void convertImage() {
	    BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    convertedImg.getGraphics().drawImage(image, 0, 0, null);
	    image = convertedImg;
	}
}
