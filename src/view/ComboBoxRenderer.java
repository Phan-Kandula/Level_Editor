package view;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * ComboBoxRender allows JComboBoxes to draw an ImageIcon with associated text.
 * Code modified from Oracle tutorial by Eric Ferrante.
 * 
 * How to use this renderer:
 * 
 * 1. Create a JComboBox from an Integer array that matches the number of items
 *    you want in the JComboBox. An example for 4 items would be
 *       Integer[] intArray = {0, 1, 2, 3};
 *       JComboBox myComboBox = new JComboBox(intArray);
 * 
 * 2. Create an array of ImageIcons (must be same length as the Integer array)
 *    that you want to display in the JComboBox and set the description of each
 *    ImageIcon in the array to the text you want to display in the JComboBox. You
 *    can set the description of an ImageIcon using the setDescription method. See
 *    ImageIcon API.
 * 
 * 3. Create a new ComboBoxRenderer and pass in your array of ImageIcons:
        ComboBoxRenderer renderer = new ComboBoxRenderer(iconList);
 * 
 * 4. Attached the renderer to the JComboBox using setRenderer:
 *      myComboBox.setRenderer(renderer);
 * 
 * Source:
 * http://docs.oracle.com/javase/tutorial/uiswing/components/combobox.html
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 */

public class ComboBoxRenderer extends JLabel implements ListCellRenderer<Object> {

	private static final long serialVersionUID = 1135802729975729492L;
	private Font uhOhFont;
	private ImageIcon[] images;

	public ComboBoxRenderer(ImageIcon[] images) {
		this.images = images;
		setOpaque(false);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	/*
	 * This method finds the image and text corresponding to the selected value
	 * and returns the label, set up to display the text and image.
	 */
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// Get the selected index. (The index param isn't
		// always valid, so just use the value.)
		if (value != null) {
			int selectedIndex = ((Integer) value).intValue();
	
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
	
			// Set the icon and text. If icon was null, say so.
			ImageIcon icon = new ImageIcon(images[selectedIndex].getImage().getScaledInstance(32, 32, Image.SCALE_DEFAULT));
			if (images[selectedIndex] != null) {
				setIcon(icon);
				setText(images[selectedIndex].getDescription());
				setFont(list.getFont());
			} else {
				setUhOhText("(null Icon)", list.getFont());
			}
		}
		return this;
	}

	// Set the font and text when no image was found.
	protected void setUhOhText(String uhOhText, Font normalFont) {
		if (uhOhFont == null) { // lazily create this font
			uhOhFont = normalFont.deriveFont(Font.ITALIC);
		}
		setFont(uhOhFont);
		setText(uhOhText);
	}
}