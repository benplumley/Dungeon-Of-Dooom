import java.net.*;
import java.io.*;
import java.util.regex.Pattern;

public class Client implements Runnable {

	/**
	 * This class is extended by Bot and PlayGame. It provides methods that are used by both, and
	 * every method used to send commands to the server. It creates a thread to listen for methods
	 * coming back from the server, and acts on them when they are received.
	 */

	private static int portNumber;
	private static String hostname; // these are the defaults and will be overwritten
	protected int goldNeeded;
	protected int currentGold = 0;
	private int hitpoints = 3;
	protected boolean gameClosing = false;
	private static PrintWriter out;
	private static BufferedReader in;
	private static char[][] lastLookReply = new char[1][1]; // this invalidates every time the map
	// changes
	private static String[] lastRenderHint;
	private static Socket socket;
	protected boolean playerTurn = false;
	private static DungeonGUI gui = null;
	private boolean cleanExit = false;

	/**
	 * The constructor takes two strings for the hostname and port number to be used, and opens a
	 * socket and in/out streams to the server at this address. It then creates and starts a thread to
	 * handle all received communications from the server.
	 */
	public Client(String localHostname, String localPortNumber) {
		hostname = localHostname;
		portNumber = Integer.parseInt(localPortNumber);
		try {
			socket = new Socket(hostname, portNumber); // this socket is used to connect to the server
			out = new PrintWriter(socket.getOutputStream(), true); // this stream is used to write
			// messages to the server
			in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // this stream is
			// used to read incoming messages from the server
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		Thread thread = new Thread(this); // create a thread and pass this object in to the constructor
		// because Client implements Runnable
		thread.start(); // start the new thread to handle incoming commands
	}

	/**
	 * The constructor takes two strings for the hostname and port number to be used, and opens a
	 * socket and in/out streams to the server at this address. It then creates and starts a thread to
	 * handle all received communications from the server. This overloaded method accepts a GUI for
	 * displaying output to the client.
	 */
	public Client(String localHostname, String localPortNumber, DungeonGUI localGUI) throws NumberFormatException, IOException {
		hostname = localHostname;
		portNumber = Integer.parseInt(localPortNumber);
		socket = new Socket(hostname, portNumber); // this socket is used to connect to the server
		out = new PrintWriter(socket.getOutputStream(), true); // this stream is used to write
		// messages to the server
		in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // this stream is
		// used to read incoming messages from the server
		gui = localGUI;
		Thread thread = new Thread(this); // create a thread and pass this object in to the constructor
		// because Client implements Runnable
		thread.start(); // start the new thread to handle incoming commands
	}

	/**
	 * Handles receiving the HELLO response from the server. Prints the message to inform the user
	 * that the server has acknowledged their connection.
	 */
	private void receiveHello(String name) {
		gui.setName(name);
	}

	/**
	 * Handles receiving the GOLD message from the server, and prints the amount of gold needed.
	 */
	private void receiveGold(String goldString) {
		goldNeeded = Integer.parseInt(goldString);
	}

	/**
	 * Handles receiving the WIN message from the server, prints a human-readable message, sets the
	 * gameClosing flag and then exits the program.
	 */
	private void receiveWin() {
		cleanExit = true;
		gui.gameOver(true);
		closeConnection();
	}

	/**
	 * Handles receiving the LOSE message from the server, prints a human-readable message, sets the
	 * gameClosing flag and then exits the program.
	 */
	private void receiveLose() {
		cleanExit = true;
		gui.gameOver(false);
		closeConnection();
	}

	/**
	 * Handles receiving the CHANGE message from the server and calls a method to invalidate the
	 * current lookReply.
	 */
	private void receiveChange() {
		mapChanged();
	}

	/**
	 * Handles receiving the STARTTURN message from the server, prints a human-readable message, sets
	 * the playerTurn flag to true to allow the player or bot to perform actions again.
	 */
	private void receiveStartTurn() {
		playerTurn = true;
	}

	/**
	 * Handles receiving the END message from the server, prints a human-readable message, sets
	 * the playerTurn flag to false to prevent the player or bot from performing actions.
	 */
	private void receiveEndTurn() {
		playerTurn = false;
	}

	/**
	 * Handles receiving the HITMOD message from the server, prints a human-readable message, and
	 * increments or decrements the hitpoints by the appropriate amount.
	 */
	private void receiveHitMod(String stringToAdd) {
		int toAdd = Integer.parseInt(stringToAdd);
		hitpoints += toAdd;
	}

	/**
	 * Handles receiving the TREASUREMOD message from the server, prints a human-readable message, and
	 * increments or decrements the gold held by the appropriate amount.
	 */
	private void receiveTreasureMod(String stringToAdd) {
		int toAdd = Integer.parseInt(stringToAdd);
		currentGold += toAdd;
	}

	/**
	 * Handles receiving the MESSAGE message from the server, and prints the message that was sent
	 * to the screen.
	 */
	private void receiveMessage(String message) {
		if (gui != null) {
			gui.receiveMessage(message);
		}
	}

	/**
	 * Handles receiving the SUCCEED message from the server, and prints to the screen.
	 */
	private void receiveSucceed() {

	}

	/**
	 * Handles receiving the SUCCEED message from the server, and prints to the screen.
	 */
	private void receiveFail(String reason) {
		
	}

	/**
	 * Handles receiving the LOOKREPLY message from the server. Reads the length of the first line to
	 * determine whether the client is holding a lantern. Iterates the appropriate number of times to
	 * read the whole lookreply, and saves the result in a character array.
	 */
	private void receiveLookReply() throws IOException {
		String nextLine = in.readLine();
		int replyDimension = nextLine.length(); // because lookreplies are square
		lastLookReply = new char[replyDimension][replyDimension];
		for (int col = 0; col < replyDimension; col++) {
			lastLookReply[col][0] = nextLine.charAt(col); // this first line has already been read
		}
		for (int row = 1; row < replyDimension; row++) {
			nextLine = in.readLine(); // the remaining lines must be read from the server
			for (int col = 0; col < replyDimension; col++) {
				lastLookReply[col][row] = nextLine.charAt(col);
			}
		}
		synchronized (this) { // so the lookreply can't be simultaneously read and written to
			notifyAll(); // notify the getLookReply method that the lookreply has been updated
		}
	}

	/**
	 * This method will use the data received in the renderhint response to allow the GUI to update
	 * as the client moves. It is implemented in CW3. TODO this comment
	 */
	private void receiveRenderHint(String lineAmount) throws IOException {
		// TODO this is implemented in CW3
		int numberOfLines = Integer.parseInt(lineAmount);
		String[] lines = new String[numberOfLines];
		for (int i = 0; i < numberOfLines; i++) {
			lines[i] = in.readLine();
		}
		lastRenderHint = lines;
		updateGUI();
	}

	/**
	 * Sends HELLO and the name the client has chosen to the server as the first command. The server
	 * will generate a name if this command is not received first.
	 */
	protected void sendHello(String username) {
		doOutputMessage("HELLO " + username);
	}

	/**
	 * Sends SETPLAYERPOS and a string representing the position to the server.
	 */
	protected void sendSetPlayerPos(String positionString) {
		doOutputMessage("SETPLAYERPOS " + positionString);
	}

	/**
	 * Sends the LOOK request to the server.
	 */
	protected void sendLook() {
		doOutputMessage("LOOK");
	}

	/**
	 * Sends the MOVE command and a string representing the direction to the server. Error handling in
	 * the direction string is handled on the server side, provided a direction was given.
	 */
	protected void sendMove(char direction) {
		doOutputMessage("MOVE " + direction);
	}

	/**
	 * Sends the ATTACK command and a string representing the direction to the server. Error handling
	 * in the direction string is handled on the server side, provided a direction was given.
	 * TODO This command is implemented in CW3.
	 */
	protected void sendAttack(char direction) {
		doOutputMessage("ATTACK " + direction); // TODO implemented in CW3
	}

	/**
	 * Sends the PICKUP command to the server. Handling pickup being unavailable is handled on the
	 * server side.
	 */
	protected void sendPickup() {
		doOutputMessage("PICKUP");
	}

	/**
	 * Sends the SHOUT command to the server, with a string representing the message to be shouted to
	 * all players.
	 */
	protected void sendShout(String message) {
		doOutputMessage("SHOUT " + message);
	}

	/**
	 * Sends the ENDTURN message to the server.
	 */
	protected void sendEndTurn() {
		doOutputMessage("ENDTURN");
	}

	/**
	 * Prints the message string over the network connection to the server using a PrintWriter, which
	 * the server will read and act upon in a thread.
	 */
	private void doOutputMessage(String message) {
		out.println(message);
	}

	/**
	 * This method runs in a separate thread and waits for commands to come in from the server. When a
	 * command is received, the switch statement executes the appropriate handler method, with any
	 * arguments if necessary.
	 */
	public void run() {
		try {
			String[] command;
			while (!gameClosing) {
				command = in.readLine().trim().split(" ", 2); // the second part contains all the arguments
				// for the command
				switch (command[0]) {
					case "HELLO": receiveHello(command[1]);
						break;
					case "GOLD": receiveGold(command[1]);
						break;
					case "WIN": receiveWin();
						break;
					case "LOSE": receiveLose();
						break;
					case "CHANGE": receiveChange();
						break;
					case "STARTTURN": receiveStartTurn();
						break;
					case "ENDTURN": receiveEndTurn();
						break;
					case "HITMOD": receiveHitMod(command[1]);
						break;
					case "TREASUREMOD": receiveTreasureMod(command[1]);
						break;
					case "MESSAGE":
						try {
							receiveMessage(command[1]);
						} catch (ArrayIndexOutOfBoundsException e) {} // if the MESSAGE was empty
						break;
					case "SUCCEED": receiveSucceed();
						break;
					case "FAIL": receiveFail(command[1]);
						break;
					case "LOOKREPLY": receiveLookReply();
						break;
					case "RENDERHINT": receiveRenderHint(command[1]);
						break;
					default: break;
				}
			}
		} catch (IOException e) {
		} catch (NullPointerException e) {
			// Tried to read from closed inputstream
		} finally {
			if (gui != null) {
				if (!cleanExit) {
					gui.showErrorMessage("Connection lost", "Disconnected from server.");
				}
				gui.toggleGUIStates(false); // lets the GUI know we've disconnected
			}
			try {
				socket.close();
			} catch (IOException e) {}
		}
	}

	/**
	 * This method returns the lookreply to the Bot class, handling the network latency using the wait
	 * method. This is woken in the receiveLookReply method.
	 */
	protected char[][] getLookReply() {
		sendLook(); // sends the LOOK command to the server
		synchronized (this) {
			while (lastLookReply.length == 1) { // in case the thread was woken for the wrong reason, keep
				// calling wait until this condition no longer holds
				try {
					wait();
				} catch (InterruptedException e) {}
			}
			return lastLookReply;
		}
	}

	/**
	 * Invalidates the currently held lookreply, because it no longer accurately reflects the map.
	 */
	protected void mapChanged() {
		lastLookReply = new char[1][1];
		if (gui != null) {
			gui.mapChanged();
		}
	}

	/**
	 * Updates the GUI with the latest LookReply, RenderHint and whether or not it is this player's
	 * turn so this information can be displayed
	 */
	private void updateGUI() {
		if (gui != null) {
			gui.updateGUI(lastLookReply, lastRenderHint, playerTurn);
		}
	}

	/**
	 * Closes the connection to the server and the PrintWriter and BufferedReader using this
	 * connection.
	 */
	public void closeConnection() {
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {}
	}

	/**
	 * Returns the amount of gold needed to win.
	 */
	public int getGoldNeeded() {
		return goldNeeded;
	}

	/**
	 * Returns the amount of gold currently held.
	 */
	public int getGoldHeld() {
		return currentGold;
	}

}
