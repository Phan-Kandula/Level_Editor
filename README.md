# Level Editor #

This program is intended to help you create level scenery data easily so you don't have to hard-code arrays to represent levels.  You should modify the code to suit your needs and build the kind of levels you need.  You can do this by either cloning this repository and pushing it to a different remote or you can fork the repository.  *Forking is best way.*

## Setup ##

1. Fork the project and clone it to your local computer
2. Create a new Eclipse project in the folder you cloned into
3. Place all tile images you want to use into the images/ folder (they can be any size and will scale automatically to the grid size)

## Tools ##

* **Paint mode** Left and right buttons paint the tiles chosen in the dropdown menus
* **Erase mode** Either button paints a null image into the chosen location
* **Select mode** Draws a selection to be copied
* **Move mode** Moves the current selection as a copy of the selection
* ***Copy** Copies a selection to the clipboard
* **Paste** Pastes whatever is in the clipboard and changes to **Move mode**

## Controls and Features ##
* Mouse Wheel zooms in/out (or use the menu commands)
* The clipboard is shared among all windows so you can copy and paste from one editor window to another
* Ctrl + left_click sets a tile's isWalkable() property.  This property is recorded for each tile when saving a level.
* You can select a group of tiles and then use Ctrl + left_click to change the isWalkable() property for the entire selection
