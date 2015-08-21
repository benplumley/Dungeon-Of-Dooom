import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

public abstract class DungeonViewport extends JLayeredPane {

	/**
	 * This abstract class extends JLayeredPane and provides a method for loading files common to both
	 * DungeonPanel and DungeonPanelOverlay, to reduce code repetition.
	 */

	/**
	 * This constructor sets the dimensions of the JLayeredPane, without which it would not display.
	 */
	public DungeonViewport() {
		this.setPreferredSize(new Dimension(448, 448));
		this.setBounds(0, 0, 448, 448);
	}

	/**
	 * This method takes a filename and returns the corresponding ImageIcon having loaded it from
	 * file.
	 */
	protected ImageIcon getImageFromFileName(String fileName) {
		String filePath = qualifyGraphicFile(fileName);
		try {
			return new ImageIcon(ImageIO.read(new File(filePath)));
		} catch (IOException e) {
			System.err.println("File missing: " + filePath);
		}
		return null;
	}

	/**
	 * This method turns a filename into a relative path.
	 */
	private String qualifyGraphicFile(String fileName) {
		String filePath = "graphics/" + fileName + ".png";
		return filePath;
	}

}
