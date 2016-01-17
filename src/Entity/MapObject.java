package Entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import GameState.GameState;
import GameState.PlatformerLevelState;
import Main.GamePanel;
import TileMap.TileMap;

public class MapObject {
	
	protected TileMap tileMap;
	protected PlatformerLevelState level;
	protected int tileSize;
	protected double xmap;
	protected double ymap;
	
	// position
	protected double x;//upper left corner
	protected double y;//upper left corner

	// velocities
	protected double dx = 0;
	protected double dy = 0;
	protected Double futuredy = null;
	
	// temp positions variables
	protected double xtemp;
	protected double ytemp;
	
	//image width and height
	protected int width;
	protected int height;
	
	// bounding box width and height
	protected int cwidth;
	protected int cheight;
	
	// are you going left, right, up, down, jumping, or falling?
	protected boolean left;
	protected boolean right;
	protected boolean up;
	protected boolean down;
	protected boolean jumping;
	protected boolean falling;
	protected boolean facingRight = true;
	
	// for collisions
	protected boolean topLeft;
	protected boolean topRight;
	protected boolean bottomLeft;
	protected boolean bottomRight;
	private int currCol;
	private int currRow;
	protected boolean collided;
	
	// for drawing
	protected BufferedImage image;
	protected Rectangle rect;
	protected int yOffset;
	protected int maxSlopeClimb = 9;
	protected double slopeDifference;
	protected double surfaceDifference;
	
	protected ArrayList<int[]> tilesToDestroy = new ArrayList<int[]>();
	private boolean player = false;
	private boolean checkingUp = false;
	private boolean respectsSemiPermiable;
	
	// constructor
	public MapObject(TileMap tm, String image_, int cwidth_, int cheight_, int yOffset_, boolean player_, boolean respectsSemiPermiable_, GameState level_) {
		level = (PlatformerLevelState) level_;
		
		tileMap = tm;
		tileSize = tm.getTileSize(); 

		image = level.getRM().getImage(image_);
		width = image.getWidth();
		height = image.getHeight();
		
		cwidth = cwidth_;
		cheight = cheight_;
		yOffset = yOffset_;
		player = player_;
		respectsSemiPermiable = respectsSemiPermiable_;

	}
	
	// constructor
	public MapObject(TileMap tm, BufferedImage image_, int cwidth_, int cheight_, int yOffset_, boolean player_, boolean respectsSemiPermiable_, GameState level_) {
		level = (PlatformerLevelState) level_;
		
		tileMap = tm;
		tileSize = tm.getTileSize(); 
		image = image_;
		width = image.getWidth();
		height = image.getHeight();
		
		cwidth = cwidth_;
		cheight = cheight_;
		yOffset = yOffset_;
		player = player_;
		respectsSemiPermiable = respectsSemiPermiable_;
	}


	public boolean intersects(MapObject o) {
		Rectangle rectangle1 = getRectangle();
		Rectangle rectangle2 = o.getRectangle();
		return rectangle1.intersects(rectangle2);
	}
	
	public Rectangle getRectangle() {
		return new Rectangle(
				(int)x - cwidth/2,
				(int)y - cheight/2,
				cwidth,
				cheight
				);
	}
	
	public void calculateCorners(double x_, double y_) {
		
		rect = getRectangle();
		
		double xleft = x_ - cwidth / 2;
		double xright = x_ + cwidth / 2 - 1;
		double ytop = y_ - cheight / 2;
		double ybot = y_ + cheight / 2 - 1;

		int leftTile = (int)((xleft) / tileSize);
		int rightTile = (int)((xright) / tileSize);
		int topTile = (int)((ytop) / tileSize);
		int bottomTile = (int)((ybot) / tileSize);
		
		slopeDifference = 999;
		topLeft = checkCorner(topTile, leftTile, (int)xleft, (int)ytop, false);
		topRight = checkCorner(topTile, rightTile, (int)xright, (int)ytop, false);
		surfaceDifference = 999;
		bottomLeft = checkCorner(bottomTile, leftTile, (int)xleft, (int)ybot, true);
		bottomRight = checkCorner(bottomTile, rightTile, (int)xright, (int)ybot, true);
		if (topLeft || topRight || bottomLeft || bottomRight) {
			collided = true;
		}
		
	}
	
	public boolean checkCorner(int tile1, int tile2, double xcorner, double ycorner, boolean bottom) {
		Integer type = tileMap.getType(tile1, tile2);
		if (type == null) {
			return true;
		}
		
		Integer type2;
		try {
			type2 = tileMap.getType(tile1-1, tile2);
			if (type2 == null) {
				type2 = type;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			type2 = type;
		}
		Integer type3 = tileMap.getType(tile1+1, tile2);
		if (type3 == null) {
			type3 = type;
		}
		
		double xtemp2;
		double ytemp2;
		boolean underground;
		
		switch (type) {
			case 0:
				if (respectsSemiPermiable && bottom) {
					int temp = (int) level.getMPM().intersects(xcorner, ycorner);
					if (temp != 999 && !down && dy >= 0 && temp <= dy + 4) {
						surfaceDifference = (temp - 1);
						return true;
					}
					return false;
				}
				return false;
			case 1:
				if (type2 == 0 || type2 == 2 || type2 == 3) {
					double temp = (tile1*tileSize - ycorner - 1);
					surfaceDifference = temp;
				}
				return true;
			case 2:
				// slope: /

				if (bottom) {
					xtemp2 = xcorner%tileSize + 0;
					ytemp2 = tileSize - ycorner%tileSize;
					slopeDifference = ytemp2 - xtemp2;
					underground = slopeDifference < 0;
					if (!underground) {
						slopeDifference = 999;
					}
					return underground && bottom;
				} else {
					return false;
				}

			case 3:
				// slope: \
				if (bottom) {
					xtemp2 = tileSize - xcorner%tileSize + 0;
					ytemp2 = tileSize - ycorner%tileSize;
					slopeDifference = ytemp2 - xtemp2;
					underground = slopeDifference <= 0;
					if (!underground) {
						slopeDifference = 999;
					}
					return underground  && bottom;
				} else {
					return false;
				}
			case 4:
				// jump through able floor
				surfaceDifference = -(tile1*tileSize - ycorner);
				boolean result = dy>=0 && surfaceDifference <= dy+1 && surfaceDifference >= 0 && bottom && !down;
				surfaceDifference = 999;
				return result && respectsSemiPermiable;
				
			case 5:
				ytemp2 = tileSize - ycorner%tileSize;
				if (checkingUp && dy < 0 && ytemp2 < -dy + 1 && player && (type3 == 0 || type3 == 4)) {
					tilesToDestroy.add(new int[]{tile1, tile2});
				}
				return true;
			default:
				throw new Error();
		}
	}
	
	public void checkTileMapCollision() {
		if (futuredy != null) {
			dy = futuredy;
			futuredy = null;
		}
		
		collided = false;
		
		currCol = (int)x/tileSize;
		currRow = (int)y/tileSize;
		
		double xdest = x + dx;
		double ydest = y + dy;
		if (xdest < 0) {
			xdest = 0;
		} else if (xdest > tileMap.getWidth()) {
			xdest = tileMap.getWidth();
		}
		if (ydest < 0) {
			ydest = 0;
		} else if (ydest > tileMap.getHeight()) {
			ydest = tileMap.getHeight();
		}
		
		xtemp = x;
		ytemp = y;
		
		checkingUp = true;
		calculateCorners(x, ydest);
		checkingUp = false;
		// remember y is in reverse
		if (Math.abs(surfaceDifference) < maxSlopeClimb && dy >= 0) {
			ytemp += surfaceDifference;
			ytemp += dy;
			
			if (bottomLeft || bottomRight) {
				dy = 0;
			}
		} else {
			if(dy < 0) { 
				if(topLeft || topRight) {
					dy = 0;
					ytemp = currRow * tileSize + cheight / 2;
				}
				else {
					ytemp += dy;
				}
			}
			if(dy > 0) { 
				if(bottomLeft || bottomRight) {
					
					if (slopeDifference < maxSlopeClimb) {
						ytemp += slopeDifference;
						ytemp += dy;
						dy = 0;
					} else {
						ytemp = (currRow + 1) * tileSize - cheight / 2;
						dy = 0;
					}
				}
				else {
					ytemp += dy;
				}
			}
		}
		
		calculateCorners(xdest, ytemp);
		if (Math.abs(surfaceDifference) < maxSlopeClimb && dy >= 0) {
			ytemp += surfaceDifference;
			xtemp += dx;
		} else {
			if (Math.abs(slopeDifference) < maxSlopeClimb) {
				ytemp += slopeDifference;
				xtemp += dx;
				dy = 0;
			} else {
				if(dx < 0) {
					if(topLeft || bottomLeft) {
						dx = 0;
						xtemp = currCol * tileSize + cwidth / 2;
					}
					else {
						xtemp += dx;
					}
				} else if(dx > 0) {
					if(topRight || bottomRight) {	
						dx = 0;
						xtemp = (currCol + 1) * tileSize - cwidth / 2;
					}
					else {
						xtemp += dx;
					}
				}
			}
		}
		
		calculateCorners(x, ydest+1);
		if (!(bottomLeft || bottomRight)) {
			falling = true;
		} else {
			falling = false;
		}
	}
	
	public void setMapPosition() {
		xmap = tileMap.getx();
		ymap = tileMap.gety();
	}
	
	public void setLeft(boolean b) { left = b;}
	public void setRight(boolean b) { right = b; }
	public void setUp(boolean b) { up = b; }
	public void setDown(boolean b) { down = b; }
	public void setJumping(boolean b) { jumping = b; }
	
	public double getx() { return x; }
	public double gety() { return y; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getCWidth() { return cwidth; }
	public int getCHeight() { return cheight; }
	
	public void setPosition(double x_, double y_) {
		x = x_;
		y = y_;
		if (x < cwidth/2) {
			x = cwidth/2;
		} else if (x > tileMap.getWidth() - cwidth/2 - 4) {
			x = tileMap.getWidth() - cwidth/2 - 4;
		}
		
		if (y < cheight/2) {
			y = cheight/2;
		} else if (y >= tileMap.getHeight() - cheight/2) {
			y = tileMap.getHeight() - cheight/2;
		}
		
	}
	
	public void setVector(double dx_, double dy_) {
		dx = dx_;
		dy = dy_;
	}
	
	public void setx(double x_) {
		x = x_;
	}
	
	public void setYvelFuture(Double ddy_) {
		futuredy = ddy_;
	}
	
	public boolean notOnScreen() {
		return x - xmap + width < 0 ||
				x - xmap - width > GamePanel.WIDTH ||
				y - ymap + height < 0 ||
				y - ymap - height > GamePanel.HEIGHT;
	}
	
	public void draw(java.awt.Graphics2D g) {
		if (facingRight) {
			g.drawImage(
					image,
					(int)(x - xmap - width / 2),
					(int)(y - ymap - height / 2 + yOffset),
					null
				);
		} else {
			g.drawImage(
					image,
					(int)(x - xmap - width / 2 + width),
					(int)(y - ymap - height / 2 + yOffset),
					-width,
					height,
					null
				);
		}
	}
	
}
