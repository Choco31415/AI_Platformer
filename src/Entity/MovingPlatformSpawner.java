package Entity;

import Main.GamePanel;

public class MovingPlatformSpawner {

	private MovingPlatform mp;
	private double frequency;
	private MovingPlatformManager mpm;
	
	private double frame;
	
	public MovingPlatformSpawner(MovingPlatform mp_, double frequency_, MovingPlatformManager mpm_) {
		mp = mp_;
		frequency = frequency_;
		mpm = mpm_;
	}
	
	public void update() {
		frame++;
		
		if (frame == GamePanel.IDEAL_FPS*frequency) {
			frame = 0;
			mpm.addPlatform(mp.clone());
		}
	}
}
