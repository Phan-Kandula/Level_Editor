package view;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JPanel;

import controller.LevelEditorController;

public class CanvasPanel extends JPanel {
	
	private static final long serialVersionUID = -7643413096529405862L;
	private LevelEditorController controller;
	
	public CanvasPanel(LevelEditorController controller) {
		this.controller = controller;
	}	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		drawGridlines(g);
		drawBackground(g);

		if (controller.clipboardLayerHasData() && controller.clipboardLayerVisible())
			drawClipboardLayer(g);

		if (controller.selectionVisible())
			drawSelection(g);
	}

	/** Draws a dashed rectangle to show the user's current selection */
	private void drawSelection(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		float[] dash = {10, 10};
		g2.setStroke(new BasicStroke(5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, dash, 0));

		// Note: I store these Points as (row, col) rather than (x, y)
		int x1 = (int)controller.getSelectionStartCoord().getY();
		int y1 = (int)controller.getSelectionStartCoord().getX();
		int x2 = (int)controller.getSelectionEndCoord().getY();
		int y2 = (int)controller.getSelectionEndCoord().getX();

		g2.drawRect(x1*cellWidth(), y1*cellHeight(), (x2 - x1)*cellWidth(), (y2 - y1)*cellHeight());
	}

	/** Draws the board gridlines */
	private void drawGridlines(Graphics g) {
		int cellWidth = cellWidth();
		int cellHeight = cellHeight();
		
		// Vertical gridlines
		for (int c = 0; c < controller.numCols(); c++) {
			g.drawLine(c*cellWidth, 0, c*cellWidth, this.getHeight());
		}
		
		// Horizontal gridlines
		for (int r = 0; r < controller.numRows(); r++) {
			g.drawLine(0, r*cellHeight, this.getWidth(), r*cellHeight);
		}			
	}

	/** Draws the clipboard data wherever the user drags the mouse */
	private void drawClipboardLayer(Graphics g) {
		int cellWidth = cellWidth();
		int cellHeight = cellHeight();

		for (int r = 0; r < controller.numClipboardRows(); r++) {
			for (int c = 0; c < controller.numClipboardCols(); c++) {
				Image img = controller.getCopyLayerImageAt(r, c);
				if (img != null)
					g.drawImage(img, (c + controller.getClipboardLayerColPlacement())*cellWidth, (r + controller.getClipboardLayerRowPlacement())*cellHeight, cellWidth, cellHeight, null);
			}
		}
	}

	/** Draws the main background tiles */
	private void drawBackground(Graphics g) {
		int cellWidth = cellWidth();
		int cellHeight = cellHeight();
		
		for (int r = 0; r < controller.numRows(); r++) {
			for (int c = 0; c < controller.numCols(); c++) {
				Image img = controller.getBackgroundImageAt(r, c);
				if (img != null)
					g.drawImage(img, c*cellWidth, r*cellHeight, cellWidth, cellHeight, null);
			}
		}
	}
	
	/** Cell width in pixels */
	private int cellWidth() {
		return getWidth() / controller.numCols();
	}

	/** Cell height in pixels */
	private int cellHeight() {
		return getHeight() / controller.numRows();
	}
}