package model;

import java.awt.Dimension;
import java.awt.Point;

public class LevelEditorModel {

	private BackgroundTile[][] backgroundLayer;
	private static BackgroundTile[][] clipboardLayer;
	private int numRows;
	private int numCols;
	private boolean clipboardVisible;
	private boolean selectionVisible;

	public LevelEditorModel(int numRows, int numCols) {
		this.numRows = numRows;
		this.numCols = numCols;		
		backgroundLayer = new BackgroundTile[numRows][numCols];
		clipboardVisible = false;
		selectionVisible = false;
	}

	public void setObjectAt(int r, int c, BackgroundTile img) {
		backgroundLayer[r][c] = img;
	}

	public BackgroundTile[][] getBackgroundLayer() {
		return backgroundLayer;
	}

	public void setClipboardLayer(Point p1, Point p2) {
		int r1, c1, r2, c2;
		if (p1 == null || p2 == null) return;
		c1 = (int)Math.min(p1.getY(), p2.getY());
		r1 = (int)Math.min(p1.getX(), p2.getX());
		c2 = (int)Math.max(p1.getY(), p2.getY());
		r2 = (int)Math.max(p1.getX(), p2.getX());

		clipboardLayer = new BackgroundTile[r2 - r1][c2 - c1];
		
		for (int r = r1; r < r2; r++) {
			for (int c = c1; c < c2; c++) {
				clipboardLayer[r - r1][c - c1] = backgroundLayer[r][c];
			}
		}
	}
		
	public void pasteClipboardLayer(int startRow, int startCol) {
		for (int r = 0; r < clipboardLayer.length; r++) {
			for (int c = 0; c < clipboardLayer[0].length; c++) {
				if (clipboardLayer[r][c] != null && isWithinBounds(startRow + r, startCol + c))
					backgroundLayer[startRow + r][startCol + c] = clipboardLayer[r][c];
			}
		}
	}
	
	public boolean clipboardLayerVisible() {
		return clipboardVisible;
	}

	public void setClipboardLayerVisible(boolean state) {
		clipboardVisible = state;
	}

	public boolean selectionVisible() {
		return selectionVisible;
	}

	public void setSelectionVisible(boolean state) {
		selectionVisible = state;
	}
	
	// Returned as width, height
	public Dimension getClipboardLayerDimensions() {
		return clipboardLayer == null || clipboardLayer.length == 0 ? new Dimension(0, 0) : new Dimension(clipboardLayer[0].length, clipboardLayer.length);
	}

	public BackgroundTile[][] getClipboardLayer() {
		return clipboardLayer;
	}
	
	public void clearClipboard() {
		clipboardLayer = null;
	}

	public void setGridSize(int numRows, int numCols) {

		// Temporary holding place for new data
		BackgroundTile newLayer[][] = new BackgroundTile[numRows][numCols];

		int newNumRows = Math.min(numRows, this.numRows);
		int newNumCols = Math.min(numCols, this.numCols);		

		// Copy old data into new array
		for (int r = 0; r < newNumRows; r++)
			for (int c = 0; c < newNumCols; c++)
				newLayer[r][c] = backgroundLayer[r][c];

		// Set new attributes
		backgroundLayer = newLayer;
		this.numRows = numRows;
		this.numCols = numCols;
	}

	private boolean isWithinBounds(int r, int c) {
		return r >= 0 && r < numRows() && c >= 0 && c < numCols();
	}	
	
	public int numRows() {
		return numRows;
	}	

	public int numCols() {
		return numCols;
	}	
}