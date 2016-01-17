package Entity;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class MovingPlatformManager {
	
	private ArrayList<MovingPlatform> movingPlatforms = new ArrayList<MovingPlatform>();
	private ArrayList<MovingPlatformSpawner> mps = new ArrayList<MovingPlatformSpawner>();
	
	public MovingPlatformManager() {
		
	}
	
	public void addPlatform(MovingPlatform mp) {
		movingPlatforms.add(mp);
	}
	
	public void addMPS(MovingPlatformSpawner mps_) {
		mps.add(mps_);
	}
	
	public void update() {
		boolean remove;
		for (int i = 0; i < movingPlatforms.size(); i++) {
			remove = movingPlatforms.get(i).update();
			if (remove) {
				movingPlatforms.remove(i);
			}
		}
		
		for (MovingPlatformSpawner temp_mps: mps) {
			temp_mps.update();
		}
	}
	
	public void draw(Graphics2D g) {
		for (int i = 0; i < movingPlatforms.size(); i++) {
			movingPlatforms.get(i).draw(g);
		}
	}
	
	public double intersects(double xcorner, double ycorner) {
		for (MovingPlatform mp : movingPlatforms) {
			if (mp.intersects(xcorner, ycorner)) {
				return mp.distanceAboveSurface(ycorner);
			}
		}
		
		return 999;
	}
	
	public ArrayList<MovingPlatform> getMovingPlatforms() {
		return movingPlatforms;
	}
	
}
