import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class DungeonListener implements ActionListener, KeyListener, MouseListener {

	/**
	 * This class implements the listeners and calls the appropriate methods whenever the listener is
	 * triggered. This class represents the Controller part of the MVC architecture.
	 */

	private DungeonGUI gui;

	/**
	 * This method gives this class a pointer to the GUI object so it can call methods in DungeonGUI.
	 */
	public void setGUI(DungeonGUI localGUI) {
		gui = localGUI;
	}

	/**
	 * This method responds to button presses in the GUI.
	 */
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "Connect":
				gui.connectToServer();
				break;
			case "Disconnect":
				gui.disconnectFromServer();
				break;
			case "Send":
				gui.sendMessage();
				break;
			case "Help":
				gui.showHelp();
				break;
		}
	}

	/**
	 * This method responds to keypresses in the GUI.
	 */
	public void keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		int keyCode = e.getKeyCode();
		if (e.getComponent().getName().equals("dungeonPanel")) {
			switch (key) {
				case 'w': gui.move('N');
				break;
				case 'a': gui.move('W');
				break;
				case 's': gui.move('S');
				break;
				case 'd': gui.move('E');
				break;
				case 'e': gui.sendPickup();
				break;
			}
		} else if (e.getComponent().getName().equals("message") && (keyCode == KeyEvent.VK_ENTER)) {
			gui.sendMessage(); // the user can send messages by pressing return from the message box
		}
	}

	/**
	 * This method responds to mouse clicks in the GUI.
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getComponent().getName().equals("endTurn")) {
			gui.endTurn();
		} else if (e.getComponent().getName().equals("dungeonPanel")) {
			e.getComponent().requestFocusInWindow(); // if the user clicks the dungeon panel, focus it.
		}
	}

	/**
	 * The following methods aren't implemented because this GUI doesn't use them. They're included
	 * because this class implements interfaces that define them.
	 */
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

}
