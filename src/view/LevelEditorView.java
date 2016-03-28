package view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import controller.LevelEditorController;

public class LevelEditorView extends JFrame {

	private static final long serialVersionUID = 6652774452907126316L;
	
	public static final int MIN_CELL_WIDTH = 5;		// Minimum pixel width of cells when zooming in

	public JComboBox<Integer[]> foregroundBrush;	// Left-click ImageIcon selection
	public JComboBox<Integer[]> backgroundBrush;	// Right-click ImageIcon selection
	public Integer[] intArray;						// Used to build the JComboBoxes for tile selection
	public File[] listOfFiles;						// List of image Files to load
	public ImageIcon[] images;						// List of ImageIcons to display in the JComboBoxes
	public JMenuItem newEditorMenuItem;				// MenuItem for opening an additional window
	public JMenuItem loadMenuItem;					// MenuItem for loading a file
	public JMenuItem saveMenuItem;					// MenuItem for saving a file
	public JMenuItem gridSizeMenuItem;				// MenuItem for changing the grid size
	public JMenuItem zoomInMenuItem;				// MenuItem for zooming in
	public JMenuItem zoomOutMenuItem;				// MenuItem for zooming out
	public JPopupMenu popUpMenu;					// Menu that pops up when Ctrl+clicking a cell
	public JRadioButton brushButton;				// Paint tool button
	public JRadioButton eraserButton;				// Eraser tool button
	public JRadioButton selectionButton;			// Select tool button
	public JRadioButton moveButton;					// Move tool button
	public JButton copyButton;						// Copy tool button
	public JButton pasteButton;						// Paste tool button
	public JCheckBox tilePropertyWalkable;			// Tile property: walkable

	private JPanel contentPane;						// A custom content pane used for this JPanel
	private CanvasPanel canvasPanel;				// Where the user draws the level
	private JPanel canvasParent;					// This is what the canvasPanel sits inside of
	private JScrollPane scrollPane;					// Scrollbars attached to canvasParent

	/**
	 * Create the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LevelEditorView(LevelEditorController controller) {
				
		// Set up window and content pane properties
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Level Editor");
		setBounds(300, 100, 620, 670);
		setMinimumSize(new Dimension(620, 670));
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		// Load image files in the "images" subfolder into listOfFiles array
		File folder = new File("images");
		if (folder.isDirectory() || folder.mkdir()) {
			listOfFiles = folder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					String[] extensions = {".gif", ".jpg", ".png", ".bmp"};
					for (String s : extensions) {
						if (s.toLowerCase().endsWith(".gif"))
							return true;
					}
					return false;
				}
			});
		} else {
			JOptionPane.showMessageDialog(this, "could not find or create images directory!");
			listOfFiles = new File[0];
		}
		
		
		// Create an index array to be used by the JComboBox
        intArray = new Integer[listOfFiles.length];

        // Load the images and initialize the index array
        images = new ImageIcon[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            intArray[i] = new Integer(i);
            images[i] = createImageIcon("images/" + listOfFiles[i].getName());
            images[i].setDescription(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().lastIndexOf('.')));
        }

        // Create GUI elements

        // A custom renderer that draws labeled ImageIcons
        ComboBoxRenderer renderer = new ComboBoxRenderer(images);
        
        // A drop down that selects what ImageIcon to paint when left-clicking
        foregroundBrush = new JComboBox(intArray);
        foregroundBrush.setRenderer(renderer);
        foregroundBrush.setMaximumRowCount(10);
        foregroundBrush.setBorder(new TitledBorder("Left click"));

        // A drop down that selects what ImageIcon to paint when right-clicking
        backgroundBrush = new JComboBox(intArray);
        backgroundBrush.setRenderer(renderer);
        backgroundBrush.setMaximumRowCount(10);
        backgroundBrush.setBorder(new TitledBorder("Right click"));

        // A JPanel to put the left-click and right-click dropdowns into
        JPanel brushPanel = new JPanel();
        brushPanel.setBackground(Color.GRAY);
        brushPanel.add(foregroundBrush);
        brushPanel.add(backgroundBrush);
        
        // Where the level is painted
		canvasPanel = new CanvasPanel(controller);
		canvasPanel.setPreferredSize(new Dimension(16*30, 16*30));
		canvasPanel.setBorder(new LineBorder(Color.BLACK, 1));
		canvasParent = new JPanel();
		canvasParent.setBackground(Color.DARK_GRAY);
		
		// Wrap the canvasPanel in a parent and then add scroll bars to the parent
		canvasParent.add(canvasPanel);
		scrollPane = new JScrollPane(canvasParent, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Toolbar panel
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setBackground(Color.GRAY);
		toolbarPanel.setPreferredSize(new Dimension(60, 60));
		JLabel label = new JLabel("Tools");
		label.setForeground(Color.white);
		toolbarPanel.add(label);
		ImageIcon brushSelected = createImageIcon("resources/" + "brush_selected.png");
		ImageIcon brushUnselected = createImageIcon("resources/" + "brush_unselected.png");
		ImageIcon eraserSelected = createImageIcon("resources/" + "eraser_selected.png");
		ImageIcon eraserUnselected = createImageIcon("resources/" + "eraser_unselected.png");
		ImageIcon selectionSselected = createImageIcon("resources/" + "selection_selected.png");
		ImageIcon selectionUnselected = createImageIcon("resources/" + "selection_unselected.png");
		ImageIcon moveSelected = createImageIcon("resources/" + "move_selection_selected.png");
		ImageIcon moveUnselected = createImageIcon("resources/" + "move_selection_unselected.png");
		brushButton = new JRadioButton(brushUnselected, true);
		eraserButton = new JRadioButton(eraserUnselected, false);
		selectionButton = new JRadioButton(selectionUnselected, false);
		moveButton = new JRadioButton(moveUnselected, false);
		brushButton.setSelectedIcon(brushSelected);
		eraserButton.setSelectedIcon(eraserSelected);
		selectionButton.setSelectedIcon(selectionSselected);
		moveButton.setSelectedIcon(moveSelected);
		ButtonGroup bGroup = new ButtonGroup();
		bGroup.add(brushButton);
		bGroup.add(eraserButton);
		bGroup.add(selectionButton);
		bGroup.add(moveButton);
		toolbarPanel.add(brushButton);
		toolbarPanel.add(eraserButton);
		toolbarPanel.add(selectionButton);
		toolbarPanel.add(moveButton);

		copyButton = new JButton("Copy");
		pasteButton = new JButton("Paste");
		toolbarPanel.add(copyButton);
		toolbarPanel.add(pasteButton);
		
		// Menubar
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu optionsMenu = new JMenu("Options");
		newEditorMenuItem = new JMenuItem("New Window", 'N');
		loadMenuItem = new JMenuItem("Load", 'L');
		saveMenuItem = new JMenuItem("Save", 'S');
		gridSizeMenuItem = new JMenuItem("Change Grid Size", 'g');
		zoomInMenuItem = new JMenuItem("Zoom In (mouse wheel)", 'i');
		zoomOutMenuItem = new JMenuItem("Zoom Out (mouse wheel)", 'o');
		fileMenu.add(newEditorMenuItem);
		fileMenu.add(loadMenuItem);
		fileMenu.add(saveMenuItem);
		optionsMenu.add(gridSizeMenuItem);
		optionsMenu.add(zoomInMenuItem);
		optionsMenu.add(zoomOutMenuItem);
		menubar.add(fileMenu);
		menubar.add(optionsMenu);

		// Popup menu (when ctrl+clicking a cell)
		popUpMenu = new JPopupMenu();
		popUpMenu.add(new JLabel("Tile Properties"));
		popUpMenu.addSeparator();
		tilePropertyWalkable = new JCheckBox("Walkable", true);
		popUpMenu.add(tilePropertyWalkable);
		
		// Add GUI components to our content pane
		contentPane.add(brushPanel, BorderLayout.PAGE_START);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(toolbarPanel, BorderLayout.LINE_START);
		this.setJMenuBar(menubar);
		this.setContentPane(contentPane);
		this.setVisible(true);
	}

	/** The main drawing canvas where the level is drawn */
	public CanvasPanel getCanvas() {
		return canvasPanel;
	}
	
	public void addMyMouseListeners(MouseAdapter m) {
		canvasPanel.addMouseListener(m);
		canvasPanel.addMouseMotionListener(m);
		canvasParent.addMouseWheelListener(m);
	}
	
	public void addMyButtonListeners(ActionListener b) {
		brushButton.addActionListener(b);
		eraserButton.addActionListener(b);
		selectionButton.addActionListener(b);
		moveButton.addActionListener(b);
		copyButton.addActionListener(b);
		pasteButton.addActionListener(b);		
	}

	public void addMyMenuListeners(ActionListener m) {
		newEditorMenuItem.addActionListener(m);
		loadMenuItem.addActionListener(m);
		saveMenuItem.addActionListener(m);
		gridSizeMenuItem.addActionListener(m);
		zoomInMenuItem.addActionListener(m);
		zoomOutMenuItem.addActionListener(m);
	}
	
	public void addMyPopUpMenuListeners(ActionListener a) {
		tilePropertyWalkable.addActionListener(a);		
	}
	
    /** Returns an ImageIcon, or null if the path was invalid. */
    private static ImageIcon createImageIcon(String path) {
        if (path != null) {
            return new ImageIcon(path);
        } else {
            System.err.println("Couldn't find file: " + path);
                return null;
        }
    }	
}