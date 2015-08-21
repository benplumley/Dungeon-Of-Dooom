import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.event.*;
import java.awt.geom.*;

public class DungeonPanelOverlay extends DungeonViewport {

	/**
	 * This class extends DungeonViewPort and displays all information relating to the game that isn't
	 * part of the game, and some decorative elements. It sits on top of the DungeonPanel in a
	 * JLayeredPane.
	 */

	private JLabel overlayWithLantern;
	private JLabel overlayNoLantern;
	private JLabel healthbar;
	private JLabel actionbar;
	private JLabel playerSplash;
	public JLabel endTurn;
	private ImageIcon endTurnIcon;
	private ImageIcon enemyTurnIcon;
	private boolean oldHasLantern = false;
	private boolean oldOurTurn = true;
	private int health = 0;
	private int actionPoints = 0;
	private int goldHeld = 0;
	private int goldNeeded = 0;
	private boolean drawGold = false;


	/**
	 * This constructor loads the images from file into JLabels and calls methods to set up the panel.
	 */
	public DungeonPanelOverlay() {
		overlayWithLantern = new JLabel(getImageFromFileName("overlayWithLantern"));
		overlayNoLantern = new JLabel(getImageFromFileName("overlayNoLantern"));
		healthbar = new JLabel(getImageFromFileName("healthbar"));
		actionbar = new JLabel(getImageFromFileName("actionbar"));
		playerSplash = new JLabel(getImageFromFileName("playerSplash"));
		endTurnIcon = getImageFromFileName("endTurn");
		enemyTurnIcon = getImageFromFileName("enemyTurn");
		endTurn = new JLabel(endTurnIcon);
		endTurn.setName("endTurn");
		setupPanel();
		populatePanel();
	}

	/**
	 * This method sets up the panel to use a GridBagLayout.
	 */
	private void setupPanel() {
		this.setOpaque(false);
		this.setLayout(new GridBagLayout());
		toggleGameStarted(false);
	}

	/**
	 * This method adds all the components to the panel at the appropriate positions in the
	 * GridBagLayout.
	 */
	private void populatePanel() {
		GridBagConstraints layoutConstraints = new GridBagConstraints();
		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridheight = 7;
		layoutConstraints.gridwidth = 7;
		overlayWithLantern.setVisible(false);
		this.add(overlayWithLantern, layoutConstraints, JLayeredPane.PALETTE_LAYER);
		this.add(overlayNoLantern, layoutConstraints, JLayeredPane.PALETTE_LAYER);
		this.add(playerSplash, layoutConstraints, JLayeredPane.PALETTE_LAYER);
		layoutConstraints.gridheight = 1;
		layoutConstraints.gridwidth = 3;
		this.add(healthbar, layoutConstraints, JLayeredPane.DEFAULT_LAYER);
		layoutConstraints.gridx = 4;
		layoutConstraints.anchor = GridBagConstraints.EAST;
		this.add(actionbar, layoutConstraints, JLayeredPane.DEFAULT_LAYER);
		layoutConstraints.gridx = 5;
		layoutConstraints.gridy = 6;
		layoutConstraints.gridwidth = 2;
		layoutConstraints.anchor = GridBagConstraints.LAST_LINE_END;
		layoutConstraints.ipadx = 5;
		layoutConstraints.ipady = 5;
		this.add(endTurn, layoutConstraints, JLayeredPane.DEFAULT_LAYER);
		this.revalidate();
	}

	/**
	 * This method changes the appearance of the panel depending on whether the player is currently
	 * in a game or not.
	 */
	public void toggleGameStarted(boolean started) {
		playerSplash.setVisible(!started);
		overlayNoLantern.setVisible(started);
		overlayWithLantern.setVisible(false); // they will never have this at the start of the game
		endTurn.setVisible(started);
		healthbar.setVisible(started);
		actionbar.setVisible(started);
		drawGold = started;
		if (!started) { // hide the health and action bars
			health = 0;
			actionPoints = 0;
		}
	}

	/**
	 * Updates the panel depending on factors that change the appearance of the overlay.
	 */
	public void updateOverlay(boolean hasLantern, boolean ourTurn, int goldHeld, int goldNeeded) {
		if (hasLantern && (hasLantern != oldHasLantern)) {
			// the player just picked up a lantern
			overlayWithLantern.setVisible(true);
			overlayNoLantern.setVisible(false);
			this.revalidate();
		} // doesn't need code for losing the lantern because items can't be dropped
		oldHasLantern = hasLantern;
		if (ourTurn && (ourTurn != oldOurTurn)) { // our turn just started
			endTurn.setIcon(endTurnIcon);
		} else if (!ourTurn && (ourTurn != oldOurTurn)) {
			endTurn.setIcon(enemyTurnIcon);
		}
		oldOurTurn = ourTurn;
		this.goldHeld = goldHeld;
		this.goldNeeded = goldNeeded;
	}

	/**
	 * Updates the health and action bars with new values.
	 */
	public void updateBars(String healthString, String actionPointsString) {
		health = Integer.parseInt(healthString);
		actionPoints = Integer.parseInt(actionPointsString);
	}

	/**
	 * Paints the health and action bars, and the gold held and needed.
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		final int maxWidth = 171; // the width of each bar
		final int thirds = 57; // a third of a bar
		final int sixths = 28; // a sixth of a bar
		final int barHeight = 8; // the bars are 8 pixels tall
		final int barTop = 4; // the bars sit 4 pixels from the top of the overlay
		final int healthLeft = 18; // the health bar sits 18 pixels from the left of the overlay
		final int apLeft = 259; // the AP bar sits 259 pixels from the left of the overlay
		final int goldLeft = 218; // the gold counter sits 218 pixels from the left of the overlay
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.RED);
		int width = (health > 2) ? maxWidth : health * thirds;
    g2.fillRect(healthLeft, barTop, width, barHeight);
		g2.setColor(new Color(0x11CBFF));
		width = (actionPoints > 5) ? maxWidth : actionPoints * sixths;
    g2.fillRect(apLeft + (maxWidth - width), barTop, width, barHeight);
		if (drawGold) {
			g2.setColor(new Color(0xFFD800));
			g2.drawString(goldHeld + "/" + goldNeeded, goldLeft, barTop + barHeight);
		}
  }

}
