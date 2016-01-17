package AI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import Entity.Enemy;
import Entity.MovingPlatform;
import Entity.MovingPlatformManager;
import Entity.Player;
import Entity.Projectile;
import GameState.GameState;
import GameState.PlatformerLevelState;
import TileMap.TileMap;

public class SimpleMap {
	
	private TileMap tileMap;
	private int width;
	private int height;
	private int smallTileSize = 4;
	private int tileSize;
	
	private Integer[][] map;
	
	private PlatformerLevelState level;
	
	private int cornerOffset = 10;
	private boolean drawBias = true;
	
	private int outputDistance = 24;
	private int outputSpacing = 4;
	
	public SimpleMap(TileMap tileMap_, int width_, int height_, GameState level_) {
		tileMap = tileMap_;
		width = width_;
		height = height_;
		level = (PlatformerLevelState) level_;
		tileSize = tileMap.getTileSize();
		map = new Integer[height][width];
	}
	
	public void setLevel(GameState level_) {
		level = (PlatformerLevelState) level_;
	}
	
	public void update() {
		
		Player player = level.getPlayer();
		int colOffset = (int)player.getx()/tileSize - width/2;
		int rowOffset = (int)player.gety()/tileSize - height/2;
		Integer tileType;
		//Read from the map.
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				tileType = tileMap.getType(row + rowOffset, col + colOffset);
				if (tileType != null && tileType != 0) {
					map[row][col] = 1;
				} else {
					map[row][col] = null;
				}
			}
		}
		
		//Read from enemies.
		ArrayList<Enemy> enemies = level.getEnemies();
		
		int row;
		int col;
		for (Enemy e: enemies) {
			col = (int)(e.getx()/tileSize) - (int)(player.getx()/tileSize) + (width/2);
			row = (int)(e.gety()/tileSize) - (int)(player.gety()/tileSize) + (height/2);
			if (row >= 0 && row < height && col >= 0 && col < width) {
				map[row][col] = 0;
			}
		}
		
		//Read from projectiles.
		ArrayList<Projectile> projectiles = level.getProjectiles();
		
		for (Projectile j: projectiles) {
			col = (int)(j.getx()/tileSize) - (int)(player.getx()/tileSize) + (width/2);
			row = (int)(j.gety()/tileSize) - (int)(player.gety()/tileSize) + (height/2);
			if (row >= 0 && row < height && col >= 0 && col < width) {
				map[row][col] = 0;
			}
		}
		
		//Read from moving platforms
		MovingPlatformManager mpm = level.getMPM();
		ArrayList<MovingPlatform> movingPlatforms = mpm.getMovingPlatforms();
		for (MovingPlatform mp : movingPlatforms) {
			col = (int)(mp.getx()/tileSize) - (int)(player.getx()/tileSize) + (width/2) - (int)mp.getWidth()/2/tileSize;
			row = (int)(mp.gety()/tileSize) - (int)(player.gety()/tileSize) + (height/2);
			if (row >= 0 && row < height && col >= 0 && col < width) {
				map[row][col] = 1;
			}
			int length = (int)mp.getWidth()/20-1;
			for (int i = 0; i < length; i++) {
				col++;
				if (row >= 0 && row < height && col >= 0 && col < width) {
					map[row][col] = 1;
				}
			}
		}
	}
	
	public void draw(Graphics2D g) {
		g.setColor(new Color(255, 255, 255, 120));
		g.fillRect(cornerOffset, cornerOffset, width*(smallTileSize + 2) + 1, height*(smallTileSize + 2) + 1);
		g.setColor(Color.BLACK);
		g.drawRect(cornerOffset, cornerOffset, width*(smallTileSize + 2) + 1, height*(smallTileSize + 2) + 1);
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (map[row][col] != null) {
					if (map[row][col] == 0) {
						drawTile(g, Color.BLACK, row, col);
					} else {
						if (map[row][col] == 1) {
							drawTile(g, Color.WHITE, row, col);
						} else {
						}
					}
				}
			}
		}
		
		//draw bias
		if (drawBias) {
			drawTile(g, Color.WHITE, height + 1, width - 1);
		}
		
		g.setColor(Color.BLACK);
		Font font = g.getFont();
		g.setFont(font.deriveFont(10.0f));
		g.drawString("R", cornerOffset + (width + outputDistance + 2)*(smallTileSize+2), (int) (cornerOffset + 1.5*(smallTileSize+2)));
		g.drawString("L", cornerOffset + (width + outputDistance + 2)*(smallTileSize+2), (int) (cornerOffset + (1.5 + outputSpacing)*(smallTileSize+2)));
		g.drawString("J", cornerOffset + (width + outputDistance + 2)*(smallTileSize+2), (int) (cornerOffset + (1.5 + outputSpacing*2)*(smallTileSize+2)));
		g.drawString("D", cornerOffset + (width + outputDistance + 2)*(smallTileSize+2), (int) (cornerOffset + (1.5 + outputSpacing*3)*(smallTileSize+2)));
	}
	
	public void drawTile(Graphics2D g, Color color, int row, int col) {
		Rectangle rect = new Rectangle(cornerOffset+1+(col)*(smallTileSize + 2), cornerOffset+1+(row)*(smallTileSize+2), smallTileSize + 1, smallTileSize + 1);
		g.setColor(new Color(0, 0, 0));
		g.draw(rect);
		rect.setSize((int)rect.getWidth()-1, (int)rect.getHeight()-1);
		rect.setLocation((int)rect.getX()+1, (int)rect.getY()+1);
		g.setColor(color);
		g.fill(rect);
	}
	
	public Integer getType(int row, int col) {
		return map[row][col];
	}
	
	public Integer[][] getMap() {
		return map;
	}
	
	/*
	 * Returns a single line version of the simple map. Rows are concatenated on each other.
	 * 0 0
	 * 1 2
	 * Becomes: 0 0 1 2
	 * 
	 * biasWeight: Should there be an extra 1 at the end of the array?
	 */
	public Integer[] getFlatMap(boolean biasWeight) {
		Integer[] flatMap;
		int size = width*height;
		if (biasWeight) {
			size++;
		}
		flatMap = new Integer[size];
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				flatMap[row*width + col] = map[row][col];
			}
		}
		
		if (biasWeight) {
			flatMap[flatMap.length - 1] = 1;
		}
		return flatMap;
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getArea() { return width*height; }
	public int getSmallTileSize() { return smallTileSize; }
	public int getCornerOffset() { return cornerOffset; }
	
	public double[] getTilePos(int row, int col) {
		double[] pos = new double[2];
		if (row < height) {
			pos[0] = cornerOffset+1+(col + 0.5)*(smallTileSize + 2);
			pos[1] = cornerOffset+1+(row + 0.5)*(smallTileSize + 2);
		} else {
			pos[0] = cornerOffset+1+(width-1+0.5)*(smallTileSize + 2);
			pos[1] = cornerOffset+1+(height+1+0.5)*(smallTileSize + 2);
		}
		return pos;
	}
	
	public double[] getOutputTilePos(int id) {
		double[] pos = new double[2];
		
		pos[0] = cornerOffset + (width + outputDistance + 0.5)*(smallTileSize+2);
		pos[1] = cornerOffset + (0.5 + outputSpacing*id)*(smallTileSize+2);
		
		return pos;
	}
	
	
	public void toggleWindow() {
		level.toggleWindow();
	}
}
