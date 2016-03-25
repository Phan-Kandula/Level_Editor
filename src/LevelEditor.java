import java.awt.EventQueue;

import controller.LevelEditorController;

/**
 * Basic Level Editor by Mr. Ferrante
 * @author eric_ferrante
 */
public class LevelEditor {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new LevelEditorController();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
