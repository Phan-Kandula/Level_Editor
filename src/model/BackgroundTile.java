package model;
import javax.swing.ImageIcon;

/**
 * Represents a scenery tile intended to be used as a background tile in the game.
 * A BackgroundTile keeps track of whether or not it can be walked on.  If it can't
 * be walked on then the tile should act as a barrier.
 */
public class BackgroundTile extends Tile {

	private boolean walkable;	// Determines whether this tile can be walked on
	
	public BackgroundTile(ImageIcon icon, String fileName) {
		super(icon, fileName);
		walkable = true;
	}
	
	public boolean getPropertyWalkable() {
		return walkable;
	}
	
	public void setProertyWalkable(boolean newValue) {
		walkable = newValue;
	}
	
	@Override
	public String toString() {
		return "[" + getFileName() + " " + walkable + "]";
	}
}