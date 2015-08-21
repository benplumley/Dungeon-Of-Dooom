import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.event.*;

public class DungeonPanel extends DungeonViewport {

	/**
	 * This class displays the information received via LookReplies and RenderHints using images
	 * arranged in a GridLayout. This class represents the View part of the MVC architecture.
	 */

	private ImageIcon wall;
	private ImageIcon exit;
	private ImageIcon gold;
	private ImageIcon floor;
	private ImageIcon enemy;
	private ImageIcon sword;
	private ImageIcon armour;
	private ImageIcon health;
	private ImageIcon lantern;
	private ImageIcon unknown;
	private ImageIcon enemyNorth;
	private ImageIcon enemyEast;
	private ImageIcon enemySouth;
	private ImageIcon enemyWest;
	private ImageIcon playerNorth;
	private ImageIcon playerEast;
	private ImageIcon playerSouth;
	private ImageIcon playerWest;

	/**
	 * This constructor loads the images from file.
	 */
	public DungeonPanel() {
		this.setName("dungeonPanel");
		wall = getImageFromFileName("wall");
		exit = getImageFromFileName("exit");
		gold = getImageFromFileName("gold");
		floor = getImageFromFileName("floor");
		sword = getImageFromFileName("sword");
		armour = getImageFromFileName("armour");
		health = getImageFromFileName("health");
		lantern = getImageFromFileName("lantern");
		unknown = getImageFromFileName("unknown");
		enemyNorth = getImageFromFileName("enemyNorth");
		enemyEast = getImageFromFileName("enemyEast");
		enemySouth = getImageFromFileName("enemySouth");
		enemyWest = getImageFromFileName("enemyWest");
		playerNorth = getImageFromFileName("playerNorth");
		playerEast = getImageFromFileName("playerEast");
		playerSouth = getImageFromFileName("playerSouth");
		playerWest = getImageFromFileName("playerWest");
		setupPanel();
	}

	/**
	 * This method sets up the panel to use a GridLayout with seven rows and seven columns.
	 */
	private void setupPanel() {
		this.setLayout(new GridLayout(7, 7));
	}

	/**
	 * This method takes the LookReply and other information relating to the the display of the map
	 * and updates what is being displayed to reflect this.
	 */
	public void updateCells(char[][] cellArray, boolean hasLantern, char[] playerDirections) {
		this.removeAll(); // removes the current cells in the grid
		int offset = hasLantern ? 0 : -1; // all cell indices in the lookreply are relative (-1, -1) if
		// the player doesn't have a lantern to if they do
		int playerNumber = 0;
		for (int row = 0; row < 7; row++) {
			for (int col = 0; col < 7; col++) {
				try {
					char cellChar = cellArray[col + offset][row + offset];
					// the current cell of the LookReply
					JLabel background = getImageFromChar(cellChar);
					if ((row == 3) && (col == 3)) {
						JLayeredPane layeredTile = getLayeredTile(background, playerNumber, playerDirections, true);
						// creates a JLayeredPane to hold the player and the background
						this.add(layeredTile);
						playerNumber++;
					} else if (cellChar == 'P') {
						JLayeredPane layeredTile = getLayeredTile(background, playerNumber, playerDirections, false);
						this.add(layeredTile);
						playerNumber++;
					} else {
						this.add(background);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					this.add(new JLabel(unknown)); // adds black cells around the edge if no lantern is held
				}
			}
		}
		this.revalidate();
	}

	/**
	 * This method returns a JLayeredPane based on the JLabel, and which player's tile is being
	 * requested.
	 */
	private JLayeredPane getLayeredTile(JLabel background, int playerNumber, char[] playerDirections,  boolean centrePlayer) {
		if (!centrePlayer) { // because we don't know what's under enemy players
			background = new JLabel(floor);
		}
		JLayeredPane layeredTile = new JLayeredPane();
		background.setBounds(0, 0, 64, 64);
		layeredTile.add(background, JLayeredPane.DEFAULT_LAYER); // add the background behind
		JLabel client = getPlayerSprite(centrePlayer, playerDirections[playerNumber]);
		// playerNumber can be used as an array index because the tiles are parsed left to right, top
		// to bottom, the same order in which the renderhints are sent.
		client.setBounds(0, 0, 64, 64);
		layeredTile.add(client, JLayeredPane.PALETTE_LAYER); // add the player in front
		return layeredTile;
	}

	/**
	 * This method returns the correct JLabel depending on whether this player or an enemy is needed,
	 * and the direction that character is facing.
	 */
	private JLabel getPlayerSprite(boolean centrePlayer, char direction) {
		if (centrePlayer) {
			switch (direction) {
				case 'N': return new JLabel(playerNorth);
				case 'E': return new JLabel(playerEast);
				case 'S': return new JLabel(playerSouth);
				case 'W': return new JLabel(playerWest);
			}
		} else {
			switch (direction) {
				case 'N': return new JLabel(enemyNorth);
				case 'E': return new JLabel(enemyEast);
				case 'S': return new JLabel(enemySouth);
				case 'W': return new JLabel(enemyWest);
			}
		}
		return new JLabel(unknown);
	}

	/**
	 * This method returns a JLabel depending the character used to represent that cell in a
	 * lookreply.
	 */
	private JLabel getImageFromChar(char cellType) {
		switch (cellType) {
			case '#': return new JLabel(wall);
			case 'E': return new JLabel(exit);
			case 'G': return new JLabel(gold);
			case '.': return new JLabel(floor);
			case 'S': return new JLabel(sword);
			case 'A': return new JLabel(armour);
			case 'H': return new JLabel(health);
			case 'L': return new JLabel(lantern);
			case 'X': return new JLabel(unknown);
			case 'P': return null;
		}
		return new JLabel();
	}

}
