import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class DungeonGUI implements Runnable {

	/**
	 * This class creates and displays the user interface in its own thread. It sets up and adds all
	 * the components used in the GUI. It also provides methods for interfacing between messages
	 * received from the server in the Client class and the on-screen components such as the chat box
	 * and the panel showing the map. This class represents the Model part of the MVC architecture.
	 */

	private Client client;
	private ServerSettings serverSettings;
	private DungeonPanel dungeonPanel;
	private DungeonPanelOverlay dungeonPanelOverlay;
	private ChatPanel chatPanel;
	private boolean isAttacking = false;
	private DungeonListener listener = new DungeonListener();
	private String playerName;

	/**
	 * The main method in this class creates and initialises an instance of itself. This class is
	 * where the whole client-side program is started from.
	 */
	public static void main(String[] args) {
		DungeonGUI dungeonGUI = new DungeonGUI();
	}

	/**
	 * This constructor creates and starts a thread so that the GUI doesn't become unresponsive when
	 * the program is performing other operations.
	 */
	public DungeonGUI() {
		Thread thread = new Thread(this);
		thread.start();
	}

	/**
	 * The run method sets up the GUI and calls methods to add its components.
	 */
	public void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// matches the operating system's look and feel
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {}
		setupPanels();
		setupFrame();
		listener.setGUI(this); // sets the DungeonListener object's reference to this class
	}

	/**
	 * This method sets up the window and displays it.
	 */
	private void setupFrame() {
		JFrame window = new JFrame("Dungeon of Dooom");
		window.setIconImage(Toolkit.getDefaultToolkit().getImage("graphics/icon.png"));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		populateFrame(window.getContentPane());
		window.pack();
		window.setLocationByPlatform(true);
		window.setVisible(true);
		Dimension windowSize = new Dimension(608, 606);
		window.setSize(windowSize);
		window.setResizable(false);
	}

	/**
	 * This method sets up the panels used in this GUI and adds the appropriate listeners to the
	 * components that require them.
	 */
	private void setupPanels() {
		serverSettings = new ServerSettings();
		serverSettings.connectButton.addActionListener(listener);
		serverSettings.disconnectButton.addActionListener(listener);
		serverSettings.help.addActionListener(listener);
		dungeonPanel = new DungeonPanel();
		dungeonPanel.addKeyListener(listener);
		dungeonPanel.addMouseListener(listener);
		dungeonPanelOverlay = new DungeonPanelOverlay();
		dungeonPanelOverlay.endTurn.addMouseListener(listener);
		chatPanel = new ChatPanel();
		chatPanel.send.addActionListener(listener);
		chatPanel.message.addKeyListener(listener);
	}

	/**
	 * This method adds the panels to the window and organises them using a GridBagLayout and a
	 * JLayeredPane.
	 */
	private void populateFrame(Container frame) {
		frame.setLayout(new GridBagLayout());
		GridBagConstraints layoutConstraints = new GridBagConstraints();
		JLayeredPane dungeonArea = new JLayeredPane();
		dungeonArea.setPreferredSize(new Dimension(448, 448));
		dungeonArea.add(dungeonPanel, JLayeredPane.DEFAULT_LAYER);
		// the DungeonPanel is added underneath
		dungeonArea.add(dungeonPanelOverlay, JLayeredPane.PALETTE_LAYER);
		// the mostly transparent DungeonPanelOverlay is added on top in the same position
		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridwidth = 7;
		layoutConstraints.gridheight = 7;
		frame.add(dungeonArea, layoutConstraints);
		layoutConstraints.gridx = 7;
		layoutConstraints.gridwidth = 1;
		frame.add(serverSettings, layoutConstraints);
		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 7;
		layoutConstraints.gridwidth = 8;
		layoutConstraints.gridheight = 1;
		frame.add(chatPanel, layoutConstraints);
	}

	/**
	 * This method handles the client pressing the Connect button. It catches the possible errors if
	 * there were any, and connects the player to the server if there were none.
	 */
	public void connectToServer() {
		String hostname = serverSettings.hostnameField.getText();
		String portNum = serverSettings.portNumField.getText();
		String playerName = serverSettings.playerNameField.getText();
		try {
			client = new Client(hostname, portNum, this);
			if (!playerName.equals("")) {
				client.sendHello(playerName);
			} // if no name was entered, the server will pick a default so we don't send HELLO
			toggleGUIStates(true); // let the GUI know we're now connected
			dungeonPanel.requestFocusInWindow(); // set the focus on the DungeonPanel so its KeyListeners
			// work without a click from the user
		} catch (NumberFormatException e) {
			showErrorMessage("Could not connect", "Invalid port number.");
		} catch (IOException e) { // if the server isn't available, because eg it is currently offline
			// or a mistake in the hostname/port number was made
			String errorMessage = "The server at " + hostname + ":" + portNum + " could not be reached.";
			showErrorMessage("Could not connect", errorMessage);
		}
	}

	/**
	 * Handles the client pressing the Disconnect button.
	 */
	public void disconnectFromServer() {
		client.closeConnection();
		toggleGUIStates(false); // lets the GUI know we've disconnected
	}

	/**
	 * Interfaces between the Listener class and the Client class, allowing the move direction to be
	 * sent.
	 */
	public void move(char direction) {
		client.sendMove(direction);
	}

	/**
	 * Handles the game ending, and provides an appropriate message depending on whether we won or
	 * lost.
	 */
	public void gameOver(boolean winner) {
		toggleGUIStates(false); // lets the GUI know we've disconnected
		if (winner) {
			showInfoMessage("You won!", "You have won the game.");
		} else {
			showInfoMessage("You lost!", "You have lost the game.");
		}
	}

	/**
	 * This method changes all of the GUI components depending on whether the player is connected to
	 * a server, because different functionality is needed when we're connected from when we're not.
	 */
	public void toggleGUIStates(boolean gameRunning) {
		serverSettings.flipButtonStates(gameRunning);
		chatPanel.setConnected(gameRunning);
		dungeonPanelOverlay.toggleGameStarted(gameRunning);
		dungeonPanel.setVisible(gameRunning);
	}

	/**
	 * This method interfaces between the event listener for the player pressing End Turn and the
	 * client sending the appropriate messages.
	 */
	public void endTurn() {
		client.sendEndTurn();
		client.sendLook();
	}

	/**
	 * This method interfaces between the key listener for the player pressing E and the client
	 * sending the appropriate message.
	 */
	public void sendPickup() {
		client.sendPickup();
	}

	/**
	 * This method updates the relevant sections of the GUI when a new LookReply and RenderHint are
	 * received from the server.
	 */
	public void updateGUI(char[][] newCells, String[] newRenderHint, boolean ourTurn) {
		char[] playerDirections = processRenderHint(newRenderHint);
		boolean hasLantern = (newCells[0].length == 7); // the size of the lookreply determines whether
		// the player has a lantern
		dungeonPanel.updateCells(newCells, hasLantern, playerDirections); // updates the map panel
		int goldHeld = client.getGoldHeld();
		int goldNeeded = client.getGoldNeeded();
		dungeonPanelOverlay.updateOverlay(hasLantern, ourTurn, goldHeld, goldNeeded);
		// updates the overlay
	}

	/**
	 * This method takes in the raw renderHint and splits it into usable information, returning an
	 * array of the player directions and updating the bars in the overlay when the line referring
	 * to this player is read.
	 */
	public char[] processRenderHint(String[] renderHint) {
		int numberOfLines = renderHint.length;
		char[] playerDirections = new char[numberOfLines];
		for (int i = 0; i < numberOfLines; i++) {
			String[] components = renderHint[i].split(" ");
			if (renderHint[i].startsWith("0 0") && (components.length == 5)) {
				// this refers to the current player
				dungeonPanelOverlay.updateBars(components[3], components[4]);
			}
			playerDirections[i] = components[2].charAt(0);
		}
		return playerDirections;
	}

	/**
	 * This method makes the client send LOOK whenever the server sends the CHANGE message. This
	 * allows the client to be able to see changes caused by other players in real time.
	 */
	public void mapChanged() {
		client.sendLook();
	}

	/**
	 * This method turns a title and error message into a full error message dialogue and displays it.
	 */
	public void showErrorMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * This method turns a title and info message into a full informational message dialogue and
	 * displays it.
	 */
	public void showInfoMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * This method passes incoming chat messages from the Client class to the ChatPanel to display.
	 */
	public void receiveMessage(String message) {
		chatPanel.receiveMessage(message);
	}

	/**
	 * This method sends the message typed into the chat box to the Client class to be sent to the
	 * server as a SHOUT.
	 */
	public void sendMessage() {
		String message = chatPanel.getMessage();
		if (!message.equals("")) {
			client.sendShout(message);
		}
	}

	/**
	 * This method informs the ChatPanel and ServerSettings classes of the player's name, so it can be
	 * displayed in the appropriate places.
	 */
	public void setName(String localName) {
		playerName = localName;
		chatPanel.setName(playerName);
		serverSettings.setName(playerName);
	}

	/**
	 * This method creates the help text and passes it to a method to show it.
	 */
	public void showHelp() {
		String helpText = "Connect to a server by entering a hostname, port number and optionally \n";
		helpText = helpText + "a player name. \n\nMove around the map using the keys W, A, S and D \n";
		helpText = helpText + "for north, west, south and east respectively. Stand on an object and \n";
		helpText = helpText + "press E to pick it up. \n\nRemember to keep an eye on how much gold\n";
		helpText = helpText + "you have and need (top middle) and how many action points you have\n";
		helpText = helpText + "left. End your turn early by pressing End Turn. You can't do anything\n";
		helpText = helpText + "if it's not your turn.\n\nChat to other players using the chat box,\n";
		helpText = helpText + "pressing Enter or Send to send each message. Click on the map again\n";
		helpText = helpText + "to move.\n\nYou can use *single asterisks* for italic text, **double\n";
		helpText = helpText + "asterisks** for bold text, and ***triple asterisks*** for both.";
		showInfoMessage("Help", helpText);
	}

}
