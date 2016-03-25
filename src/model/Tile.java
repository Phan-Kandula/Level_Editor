package model;
import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * This class represents a graphical Tile in the game.
 * If you use it, you should EXTEND it or ADD MORE PROPERTIES
 */
public class Tile  {

	private ImageIcon icon;		// Graphical representation of this tile
	private String fileName;	// Filename should match the ImageIcon used
	
	public Tile(ImageIcon icon, String fileName) {
		this.icon = icon;
		this.fileName = fileName;
	}
	
	public ImageIcon getImageIcon() {
		return icon;
	}

	public Image getImage() {
		return icon.getImage();
	}
	
	public String getFileName() {
		return fileName;
	}

	@Override
	public String toString() {
		return "[" + fileName + "]";
	}
}