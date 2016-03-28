package controller;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import model.BackgroundTile;
import model.LevelEditorModel;
import view.LevelEditorView;


public class LevelEditorController {

	// Tool palette modes
	public static final int PAINT_MODE = 0;
	public static final int ERASE_MODE = 1;
	public static final int SELECT_MODE = 2;
	public static final int MOVE_MODE = 3;

	// Which mouse mode we're in (paint, erase, select, move)
	private int toolMode;
	private Point selectionStartCoord;				// Upper left corner of selection in (row, col) form
	private Point selectionEndCoord;				// Lower right corner of selection in (row, col) form
	private Point selectionMouseStartCoord;			// Mouse pixel coords used to detect dragging direction
	private int clipboardLayerColPlacement;
	private int clipboardLayerRowPlacement;

	private BackgroundTile currentTile;				// Currently selected BackgroundTile for popup use

	private LevelEditorModel model;
	private LevelEditorView view;
	
	public LevelEditorController() {
		// Default tool mode
		toolMode = PAINT_MODE;

		model = new LevelEditorModel(10, 10);
		view = new LevelEditorView(this);
		
		// Create listeners
		MyMouseListener mouseListener = new MyMouseListener();
		MyMenuListener menuListener = new MyMenuListener();
		MyButtonListener buttonListener = new MyButtonListener();
		MyPopUpListener popUpListener = new MyPopUpListener();

		// Attach listeners
		view.addMyMouseListeners(mouseListener);
		view.addMyButtonListeners(buttonListener);
		view.addMyMenuListeners(menuListener);
		view.addMyPopUpMenuListeners(popUpListener);
	}
	
    /** Handle ctrl+click popUp menu */
    private class MyPopUpListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == view.tilePropertyWalkable) {
				// If we have something selected then change the property for all tiles in the selection
				if (toolMode == SELECT_MODE && selectionStartCoord != selectionEndCoord) {
					for (int r = (int)selectionStartCoord.getX(); r < (int)selectionEndCoord.getX(); r++)
						for (int c = (int)selectionStartCoord.getY(); c < (int)selectionEndCoord.getY(); c++)
							if (model.getBackgroundLayer()[r][c] != null)
								model.getBackgroundLayer()[r][c].setProertyWalkable(view.tilePropertyWalkable.isSelected());
				}
				// Otherwise just set the property of the tile that was clicked on
				else
					currentTile.setProertyWalkable(view.tilePropertyWalkable.isSelected());
			}			
		}
    }
    
    /** Handle tool pallete buttons */
    private class MyButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == view.brushButton) {
				// If we came from MOVE_MODE and there's something waiting in the clipboard
				// (copy layer) then paste the clipboard contents and reset the clipboard
				if (toolMode == MOVE_MODE && clipboardLayerHasData()) {
					model.pasteClipboardLayer((int)selectionStartCoord.getX(), (int)selectionStartCoord.getY());
					model.clearClipboard();
					model.setClipboardLayerVisible(false);
				}
				// User selected paint mode, so hide any selections
				toolMode = PAINT_MODE;
				model.setSelectionVisible(false);
				view.getCanvas().repaint();
			}
			else if (e.getSource() == view.eraserButton) {
				toolMode = ERASE_MODE;
				view.getCanvas().repaint();
			}
			else if (e.getSource() == view.selectionButton) {
				// If we came from MOVE_MODE and there's something waiting in the clipboard
				// (copy layer) then paste the clipboard contents and reset the clipboard
				if (toolMode == MOVE_MODE && clipboardLayerHasData()) {
					model.pasteClipboardLayer((int)selectionStartCoord.getX(), (int)selectionStartCoord.getY());
					model.clearClipboard();
					model.setClipboardLayerVisible(false);
				}
				// User selected "select mode" so reset the selection and make any future
				// selections visible
				toolMode = SELECT_MODE;
				model.setSelectionVisible(true);
				selectionStartCoord = new Point(0, 0);
				selectionEndCoord = selectionStartCoord;
				view.getCanvas().repaint();
			}
			else if (e.getSource() == view.moveButton) {
				// If we came from SELECT_MODE and there's a selection region defined
				// then copy the selection to the clipboard (copy layer) in case the
				// user wants to move the selection
				if (toolMode == SELECT_MODE && selectionStartCoord != selectionEndCoord) {
					model.setClipboardLayer(selectionStartCoord, selectionEndCoord);
					clipboardLayerRowPlacement = (int)selectionStartCoord.getX();
					clipboardLayerColPlacement = (int)selectionStartCoord.getY();
					model.setClipboardLayerVisible(true);
				}
				toolMode = MOVE_MODE;
				view.moveButton.setSelected(true);
				model.setSelectionVisible(true);
				view.getCanvas().repaint();
			}
			else if (e.getSource() == view.copyButton) {
				model.setClipboardLayer(selectionStartCoord, selectionEndCoord);
			}
			else if (e.getSource() == view.pasteButton) {
				// If we're in MOVE mode and there's a selection region defined then copy
				// the selection to the clipboard (copy layer) and paste it
				if (toolMode == MOVE_MODE && selectionStartCoord != selectionEndCoord && clipboardLayerHasData()) {
					model.pasteClipboardLayer((int)selectionStartCoord.getX(), (int)selectionStartCoord.getY());
					model.clearClipboard();
					model.setClipboardLayerVisible(false);
					model.setSelectionVisible(false);
					selectionStartCoord = new Point(0, 0);
					selectionEndCoord = selectionStartCoord;
					view.getCanvas().repaint();
				}
				// Otherwise if there's no selection region defined then paste the
				// copied layer into the upper left corner of the canvas and select it
				else if (toolMode != MOVE_MODE || selectionStartCoord == selectionEndCoord && clipboardLayerHasData()) {
					model.setClipboardLayerVisible(true);
					view.moveButton.setSelected(true);
					toolMode = MOVE_MODE;
					selectionStartCoord = new Point(0, 0);
					selectionEndCoord = new Point((int)model.getClipboardLayerDimensions().getHeight(), (int)model.getClipboardLayerDimensions().getWidth());
					model.setSelectionVisible(true);
					view.getCanvas().repaint();
				}
			}
		}
    }
    
    /** Handle menu items */
    private class MyMenuListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == view.loadMenuItem) {
				System.out.println("Insert your code to load a level");
			}
			else if (e.getSource() == view.saveMenuItem) {
				System.out.println("Modify this method to save levels differently");
				// The code given below is sample starter code.
				// It saves a level as a 2D array of BackgroundTiles that print themselves
				// and their properties using toString()

				// Pop up a JFileChooser and if user cancels, quit this method
				JFileChooser fChooser = new JFileChooser(".");
				int response = fChooser.showSaveDialog(null);
				if (response == JFileChooser.CANCEL_OPTION) {
					return;
				}
				
				FileWriter fWriter = null;

				// Open the selected file so we can write to it
				try {
					fWriter = new FileWriter(fChooser.getSelectedFile());
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}

				// Get our level data
				BackgroundTile[][] backgroundLayer = model.getBackgroundLayer();

				// Write the level data in row-major order
				try {
					for (int row = 0; row < numRows(); row++) {
						for (int col = 0; col < numCols(); col++) {
							if (backgroundLayer[row][col] != null)
								fWriter.write(backgroundLayer[row][col].toString() + " ");
							else
								fWriter.write(String.format("null "));
						}
						fWriter.write(String.format("\r\n"));
					}
					fWriter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showInternalMessageDialog(view.getContentPane(), "An error has occured :( See console for details.");
					return;
				}
				// If we reach this, everything went well.  Otherwise we quit the method earlier with an error.
				JOptionPane.showInternalMessageDialog(view.getContentPane(), "File saved!");
			}
			// Open a new EditorFrame (useful for copying and pasting between windows)
			else if (e.getSource() == view.newEditorMenuItem) {
				new LevelEditorController();
			}
			// Change the grid size
			else if (e.getSource() == view.gridSizeMenuItem) {
				int numRows = Integer.parseInt((String)JOptionPane.showInternalInputDialog(view.getContentPane(),
                        "How many rows?",
                        "Change Grid Size",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        numRows() + ""));
				int numCols = Integer.parseInt((String)JOptionPane.showInternalInputDialog(view.getContentPane(),
                        "How many columns?",
                        "Change Grid Size",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        numCols() + ""));                        
				model.setGridSize(numRows, numCols);
				int cellWidth = (int)(view.getCanvas().getWidth() / numCols());
				int cellHeight = cellWidth;
				view.getCanvas().setPreferredSize(new Dimension(cellWidth * numCols(), cellHeight * numRows()));
				view.getCanvas().revalidate();
				view.getCanvas().repaint();
			}
			// Zoom in 2x
			else if (e.getSource() == view.zoomInMenuItem) {
				int oldCellWidth = (int)(view.getCanvas().getWidth() / numCols());
				int cellWidth = (int)(view.getCanvas().getWidth() / numCols() * 2);
				if (cellWidth == oldCellWidth)
					cellWidth *= 2;
				int cellHeight = cellWidth;
				view.getCanvas().setPreferredSize(new Dimension(cellWidth * numCols(), cellHeight * numRows()));
				view.getCanvas().revalidate();
				view.getCanvas().repaint();
			}
			// Zoom out 2x
			else if (e.getSource() == view.zoomOutMenuItem) {
				int cellWidth = (int)(view.getCanvas().getWidth() / numCols() / 2);
				if (cellWidth < LevelEditorView.MIN_CELL_WIDTH)
					cellWidth = LevelEditorView.MIN_CELL_WIDTH;
				int cellHeight = cellWidth;
				view.getCanvas().setPreferredSize(new Dimension(cellWidth * numCols(), cellHeight * numRows()));
				view.getCanvas().revalidate();
				view.getCanvas().repaint();
			}
		}    	
    }
    
    /** Handle mouse movement, clicks, and wheel */
	private class MyMouseListener extends MouseAdapter {
		
    	@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);

			// Determine the grid location we click on
			int row = e.getY() / (view.getCanvas().getHeight() / numRows());
			int col = e.getX() / (view.getCanvas().getWidth() / numCols());
			
			// Left-click + Ctrl brings up the tile properties popup menu
			int popUpMask = MouseEvent.BUTTON1_MASK | MouseEvent.CTRL_MASK;			
			if ((e.getModifiers() & popUpMask) == popUpMask) {
				currentTile = model.getBackgroundLayer()[row][col];
				if (currentTile != null) {
					view.tilePropertyWalkable.setSelected(currentTile.getPropertyWalkable());
					view.popUpMenu.show(view.getCanvas(), e.getX(), e.getY());
				}
				return;
			}

			if (toolMode == SELECT_MODE) {
				selectionStartCoord = new Point(row, col);
				selectionEndCoord = new Point(row + 1, col + 1);
				selectionMouseStartCoord = new Point(e.getX(), e.getY());
			}
			else if (toolMode == MOVE_MODE) {
				// If the user has a region selected but there's nothing in the clipboard (copy layer),
				// we'll assume the user wants to copy and move the current selection.  We'll do this by
				// it to the clipboard and letting them move the selection copy.
				selectionMouseStartCoord = new Point(e.getX(), e.getY());
				// If there something in the clipboard (copy layer) then move it to mouse location
				if (isWithinBounds(e.getX(), e.getY()) && clipboardLayerHasData()) {
					int r = e.getY() / (view.getCanvas().getHeight() / numRows());
					int c = e.getX() / (view.getCanvas().getWidth() / numCols());
					clipboardLayerRowPlacement = r;
					clipboardLayerColPlacement = c;					
					selectionStartCoord = new Point(r, c);
					selectionEndCoord = new Point(r + (int)model.getClipboardLayerDimensions().getHeight(), c + (int)model.getClipboardLayerDimensions().getWidth());
				}
				view.getCanvas().repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);

			// Left-click + Ctrl brings up the tile properties popup menu
			int popUpMask = MouseEvent.BUTTON1_MASK | MouseEvent.CTRL_MASK;			
			if ((e.getModifiers() & popUpMask) == popUpMask)
				return;
			
			if (toolMode == PAINT_MODE) {

				// Determine the grid location we click on
				int row = e.getY() / (view.getCanvas().getHeight() / numRows());
				int col = e.getX() / (view.getCanvas().getWidth() / numCols());

				if (isWithinBounds(e.getX(), e.getY())) {
					if (view.images.length > 0 && view.foregroundBrush.getSelectedItem() != null) {
						if (e.getButton() == MouseEvent.BUTTON1) {
							
							ImageIcon image = view.images[(Integer)view.foregroundBrush.getSelectedItem()];
							model.setObjectAt(row, col, new BackgroundTile(
											image,
											view.listOfFiles[(Integer)view.foregroundBrush.getSelectedItem()].getName()
											));
						}
						else {
							ImageIcon image = view.images[(Integer)view.backgroundBrush.getSelectedItem()];
							model.setObjectAt(row, col, new BackgroundTile(
											image,
											view.listOfFiles[(Integer)view.backgroundBrush.getSelectedItem()].getName()
											));
						}
					}
					view.getCanvas().repaint();
				}
			}
			if (toolMode == ERASE_MODE) {

				// Determine the grid location we click on
				int row = e.getY() / (view.getCanvas().getHeight() / numRows());
				int col = e.getX() / (view.getCanvas().getWidth() / numCols());

				if (isWithinBounds(e.getX(), e.getY())) {
					model.setObjectAt(row, col, null);
					view.getCanvas().repaint();
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			super.mouseClicked(e);

			// Left-click + Ctrl brings up the tile properties popup menu
			int popUpMask = MouseEvent.BUTTON1_MASK | MouseEvent.CTRL_MASK;	
			if ((e.getModifiers() & popUpMask) == popUpMask)
				return;
			
			if (toolMode == SELECT_MODE) {
				selectionEndCoord = selectionStartCoord;
				view.getCanvas().repaint();
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			super.mouseDragged(e);

			// Left-click + Ctrl brings up the tile properties popup menu
			int popUpMask = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;	
			if ((e.getModifiersEx() & popUpMask) == popUpMask)
				return;

			if (toolMode == PAINT_MODE) {
				if (view.images.length > 0 && view.foregroundBrush.getSelectedItem() != null) {
					// Determine the grid location we click on
					int row = e.getY() / (view.getCanvas().getHeight() / numRows());
					int col = e.getX() / (view.getCanvas().getWidth() / numCols());
	
					if (isWithinBounds(e.getX(), e.getY())) {
						if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
							ImageIcon image = view.images[(Integer)view.foregroundBrush.getSelectedItem()];
							model.setObjectAt(row, col, new BackgroundTile(
											image,
											view.listOfFiles[(Integer)view.foregroundBrush.getSelectedItem()].getName()
											));
						}
						else {
							ImageIcon image = view.images[(Integer)view.backgroundBrush.getSelectedItem()];
							model.setObjectAt(row, col, new BackgroundTile(
											image,
											view.listOfFiles[(Integer)view.backgroundBrush.getSelectedItem()].getName()
											));
						}
						view.getCanvas().repaint();
					}
				}
			}
			else if (toolMode == ERASE_MODE) {

				// Determine the grid location we click on
				int row = e.getY() / (view.getCanvas().getHeight() / numRows());
				int col = e.getX() / (view.getCanvas().getWidth() / numCols());

				if (isWithinBounds(e.getX(), e.getY())) {
					model.setObjectAt(row, col, null);
					view.getCanvas().repaint();
				}
			}
			else if (toolMode == SELECT_MODE) {

				if (isWithinBounds(e.getX(), e.getY())) {

					// Dragging in a southeast direction
					if (e.getX() > selectionMouseStartCoord.getX() && e.getY() > selectionMouseStartCoord.getY()) {
						int r2 = 1 + e.getY() / (view.getCanvas().getHeight() / numRows());
						int c2 = 1 + e.getX() / (view.getCanvas().getWidth() / numCols());
						selectionEndCoord = new Point(r2, c2);

					}
					// Dragging in a northwest direction
					else if (e.getX() < selectionMouseStartCoord.getX() && e.getY() < selectionMouseStartCoord.getY()) {
						int r1 = e.getY() / (view.getCanvas().getHeight() / numRows());
						int c1 = e.getX() / (view.getCanvas().getWidth() / numCols());
						selectionStartCoord = new Point(r1, c1);
					}
					// Dragging in a northeast direction
					else if (e.getX() > selectionMouseStartCoord.getX() && e.getY() < selectionMouseStartCoord.getY()) {
						int r1 = e.getY() / (view.getCanvas().getHeight() / numRows());
						int c1 = (int)selectionMouseStartCoord.getX() / (view.getCanvas().getWidth() / numCols());
						int r2 = 1 + (int)selectionMouseStartCoord.getY() / (view.getCanvas().getHeight() / numRows());
						int c2 = 1 + e.getX() / (view.getCanvas().getWidth() / numCols());
						selectionStartCoord = new Point(r1, c1);
						selectionEndCoord = new Point(r2, c2);
					}
					// Dragging in a southwest direction
					else if (e.getX() < selectionMouseStartCoord.getX() && e.getY() > selectionMouseStartCoord.getY()) {
						int r1 = (int)selectionMouseStartCoord.getY() / (view.getCanvas().getHeight() / numRows());
						int c1 = e.getX() / (view.getCanvas().getWidth() / numCols());
						int r2 = 1 + e.getY() / (view.getCanvas().getHeight() / numRows());
						int c2 = 1 + (int)selectionMouseStartCoord.getX() / (view.getCanvas().getWidth() / numCols());
						selectionStartCoord = new Point(r1, c1);
						selectionEndCoord = new Point(r2, c2);
					}
				}
				view.getCanvas().repaint();
			}
			else if (toolMode == MOVE_MODE) {
				if (isWithinBounds(e.getX(), e.getY()) && clipboardLayerHasData()) {

					// Determine the grid location we click on
					int r = e.getY() / (view.getCanvas().getHeight() / numRows());
					int c = e.getX() / (view.getCanvas().getWidth() / numCols());
					clipboardLayerRowPlacement = r;
					clipboardLayerColPlacement = c;
					selectionStartCoord = new Point(r, c);
					selectionEndCoord = new Point(r + (int)model.getClipboardLayerDimensions().getHeight(), c + (int)model.getClipboardLayerDimensions().getWidth());
				}
				view.getCanvas().repaint();
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			super.mouseWheelMoved(e);
			
			if (e.getWheelRotation() < 0) {
				int oldCellWidth = (int)(view.getCanvas().getWidth() / numCols());
				int cellWidth = (int)(view.getCanvas().getWidth() / numCols() * 1.2);
				if (cellWidth == oldCellWidth)
					cellWidth *= 2;
				int cellHeight = cellWidth;
				view.getCanvas().setPreferredSize(new Dimension(cellWidth * numCols(), cellHeight * numRows()));
			}
			else if (e.getWheelRotation() > 0) {
				int cellWidth = (int)(view.getCanvas().getWidth() / numCols() / 1.2);
				if (cellWidth < LevelEditorView.MIN_CELL_WIDTH)
					cellWidth = LevelEditorView.MIN_CELL_WIDTH;
				int cellHeight = cellWidth;
				view.getCanvas().setPreferredSize(new Dimension(cellWidth * numCols(), cellHeight * numRows()));
			}
			view.getCanvas().revalidate();
			view.getCanvas().repaint();
		}

	    /** Helper function to ensure a pixel location (x, y) is within the canvas bounds */		
		private boolean isWithinBounds(int x, int y) {
			return x > 0 && x < view.getCanvas().getWidth() && y > 0 && y < view.getCanvas().getHeight();
		}
	}

	public boolean clipboardLayerHasData() {
		return model.getClipboardLayer() != null;
	}

	public boolean clipboardLayerVisible() {
		return model.clipboardLayerVisible();
	}

	public boolean selectionVisible() {
		return model.selectionVisible();
	}

	public int numRows() {
		return model.numRows();
	}

	public int numCols() {
		return model.numCols();
	}

	public int numClipboardRows() {
		return model.getClipboardLayer().length;
	}

	public int numClipboardCols() {
		return model.getClipboardLayer()[0].length;
	}

	public int getClipboardLayerColPlacement() {
		return clipboardLayerColPlacement;
	}

	public int getClipboardLayerRowPlacement() {
		return clipboardLayerRowPlacement;
	}

	/** Sets the upper-left corner of the current selection.
	 *  Point is stored as (row, col) rather than (x, y) */
	public void setSelectionStartCoord(Point p) {
		selectionStartCoord = p;
	}

	/** Sets the lower-right corner of the current selection.
	 *  Point is stored as (row, col) rather than (x, y) */
	public void setSelectionEndCoord(Point p) {
		selectionEndCoord = p;
	}

	/** Returns the upper-left coordinates of the current selection.
	 *  Point is stored as (row, col) rather than (x, y) */
	public Point getSelectionStartCoord() {
		return selectionStartCoord;
	}

	/** Returns the lower-right coordinates of the current selection.
	 *  Point is stored as (row, col) rather than (x, y) */
	public Point getSelectionEndCoord() {
		return selectionEndCoord;
	}

	/** Returns the Image at (r, c) in the model's background layer
	 *  or null if there is no image at location (r, c) */
	public Image getBackgroundImageAt(int row, int col) {
		return model.getBackgroundLayer()[row][col] == null ? null : model.getBackgroundLayer()[row][col].getImage();
	}

	/** Returns the Image at (r, c) in the model's clipboard layer
	 *  or null if there is no image at location (r, c) */
	public Image getCopyLayerImageAt(int row, int col) {
		return model.getClipboardLayer()[row][col] == null ? null : model.getClipboardLayer()[row][col].getImage();
	}
}