package TileMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import Main.GamePanel;

public class TileMap {
	
	int x;
	int y;
	int width;
	int height;
	
	// map
	private int[][] mapCopy;
	private int[][] map;
	int tileSize;
	int numRow;
	int numCol;
	
	// tiles
	private BufferedImage tileset;
	public Tile[] tiles; // RESSET TO PRIVATE LATER
	
	// Bounds. Set in loadMap()
	private int xmin;
	private int ymin;
	private int xmax;
	private int ymax;
	
	// drawing
	private int rowOffset;
	private int colOffset;
	private int numRowsToDraw;
	private int numColsToDraw;
	
	public TileMap(int tileSize_) {
		tileSize = tileSize_;
		numRowsToDraw = GamePanel.HEIGHT/tileSize;
		numColsToDraw = GamePanel.WIDTH/tileSize;
	}
	
	/*
	 * This method takes the location of the tileset and extra tile information as parameters,
	 */
	public void loadTiles(String tilesetImage, String extraTileInfoFile) {
		try {
			// Import information and images.
			tileset = ImageIO.read(getClass().getResourceAsStream(tilesetImage));
		    
			int[][] extraTileInfo = parseFile(extraTileInfoFile, 1);

		    
			// Put all information gathered into easily accessibly tiles array.
		    int tileCount = extraTileInfo.length;
		    if (extraTileInfo[0].length < 7) {
		    	throw new Error();
		    }
		    
		    tiles = new Tile[tileCount];
		    for (int i = 0; i < tileCount; i++) {
		    	int[] tileInfo = extraTileInfo[i];
		    	tiles[i] = new Tile(getImage(tileInfo[0], tileInfo[1], tileInfo[2], tileInfo[3]), tileInfo[4], tileInfo[5], tileInfo[6]);
		    }
		} catch (IOException e) {
			System.out.println("Err, cannot load images. The rebel attack succedded. (TileMap)");
		}
	}
	
	public BufferedImage getImage(int row, int col, int width_in_tiles, int height_in_tiles) {
		try {
			return tileset.getSubimage(col*tileSize, row*tileSize, width_in_tiles*tileSize, height_in_tiles*tileSize);
		} catch (RasterFormatException e) {
			throw new Error();
		}
	}
	
	/*
	 * This method loads a map file (secretly a txt file).
	 */
	public void loadMap(ArrayList<String> maps) {
		map = null;
		int[][][] tempMaps = new int[maps.size()][][];
		int length = 0;
		int breadth;
		
		for (int i = 0; i < maps.size(); i++) {
			tempMaps[i] = parseFile(maps.get(i), 1);
			length += tempMaps[i][0].length;
		}
		
		breadth = tempMaps[0].length;
		map = new int[breadth][length];
		int tempLength = 0;
		int[][] tempMap;
		
		for (int mapNum = 0; mapNum < tempMaps.length; mapNum++) {
			tempMap = tempMaps[mapNum];
			for (int col = 0; col < tempMap[0].length; col++) {
				for (int row = 0; row < breadth; row++) {
					map[row][tempLength] = tempMap[row][col];
				}
				tempLength++;
			}
		}
			
		numRow = map.length;
		numCol = map[0].length;
		width = tileSize * numCol;
		height = tileSize * numRow;
		
			
		xmin = 0;
		xmax = width - GamePanel.WIDTH;
		ymin = 0;
		ymax = height - GamePanel.HEIGHT;
		
		mapCopy = cloneArray(map);
	}
	
	public int getTileSize() { return tileSize; }
	public double getx() { return x; }
	public double gety() { return y; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public Integer getType(int row, int col) {
		if (row >= numRow || col >= numCol || row < 0 || col < 0) {
			return null;
		} else {
			int tileID = map[row][col];
			if (tileID == 0){
				return 0;
			} else {
				return tiles[tileID-1].getType();
			}
		}
	}
	
	public void setPosition(double oldX, double oldY) {
		x = (int) oldX;
		y = (int) oldY;
		
		fixBounds();
		
		colOffset = x/tileSize;
		rowOffset = y/tileSize;
	}
	
	private void fixBounds() {
		if (x < xmin) { x = xmin;}
		if (y < ymin) { y = ymin;}
		if (x > xmax) { x = xmax;}
		if (y > ymax) { y = ymax;}
	}
	
	public void draw(Graphics2D g) {
		for(
				int row = rowOffset;
				row <= rowOffset + numRowsToDraw;
				row++) {
			if (row >= numRow) break;
			
			for(
					int col = colOffset;
					col <= colOffset + numColsToDraw;
					col++) {
				if (col >= numCol) break;
				
				int tileID = map[row][col];
				
				if(tileID == 0) continue;
				
				Tile tile = tiles[tileID-1];
				BufferedImage image = tile.getImage();
				int xOffset = tile.getXOffset();
				int yOffset = tile.getYOffset();
				
				g.drawImage(
						image,
						(int)-x + col * tileSize + xOffset,
						(int)-y + row * tileSize + yOffset - image.getHeight() + tileSize,
						null
					);
			}
		}
	}
	
	public void setMapTile(int row, int col, int newType) {
		map[row][col] = newType;
	}
	
	/*
	 * This method takes a file formatted as a 2D array with format:
	 * First Line: Width
	 * Second Line: Height
	 * Other Lines: Array Items
	 * 
	 * Array Items are to be separated by spaces.
	 */
	private int[][] parseFile(String s, int commentBufferLineCount) {
		try {
			// Read in the file!
			InputStream in = getClass().getResourceAsStream(s);
			BufferedReader br = new BufferedReader(
						new InputStreamReader(in)
					);
			
			// Ignore the comment
			for (int i = 0; i < commentBufferLineCount; i++) {
				br.readLine();
			}
			
			// Gather array size
			int tempNumCol = Integer.parseInt(br.readLine());
			int tempNumRow = Integer.parseInt(br.readLine());
			int[][] returnArray = new int[tempNumRow][tempNumCol];
			
			// Parse file array into java int array
			String split_regex = "\\s+";
			for (int row = 0; row < tempNumRow; row++) {
				String line = br.readLine();
				String[] parsedLine = line.split(split_regex);
				for (int col = 0; col < tempNumCol; col++) {
					try {
						returnArray[row][col] = Integer.parseInt(parsedLine[col]);
					} catch (ArrayIndexOutOfBoundsException e) {
						returnArray[row][col] = 0;
					}
				}
			}
			
			in.close();
			br.close();
			
			return returnArray;
		} catch (IOException e) {
			System.out.println("Read N Parse Fries 2 Fried 4 Me (TileMap)");
		}
		return null;
	}
	
	public void reset() {
		map = cloneArray(mapCopy);
	}
	
	public int[][] cloneArray(int[][] array) {
		int height = array.length;
		int length = array[0].length;
		int[][] temp = new int[height][length];
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < length; col++) {
				temp[row][col] = array[row][col];
			}
		}
		
		return temp;
	}

	public BufferedImage screenshotMap() {
		BufferedImage imageRepresentation = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = imageRepresentation.createGraphics();
		
		//Set the background color
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		//Draw the map
		for(
				int row = 0;
				row < numRow;
				row++) {
			
			for(
					int col = 0;
					col < numCol;
					col++) {
				
				int tileID = map[row][col];
				
				if(tileID == 0) continue;
				
				Tile tile = tiles[tileID-1];
				BufferedImage image = tile.getImage();
				int xOffset = tile.getXOffset();
				int yOffset = tile.getYOffset();
				
				g.drawImage(
						image,
						(int) col * tileSize + xOffset,
						(int) row * tileSize + yOffset - image.getHeight() + tileSize,
						null
					);
			}
		}
		
		return imageRepresentation;
		
	}
}
