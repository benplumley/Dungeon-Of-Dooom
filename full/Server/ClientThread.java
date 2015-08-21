import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

public class ClientThread extends CommandLineUser {

	/**
	 * This class extends CommandLineUser and starts a new thread for each client that connects. This
	 * allows clients to send commands to the server even when it's not their turn, such as LOOK and
	 * SHOUT. It handles all inputs from the client and passes these on to the processCommand method
	 * in CommandLineUser.
	 */

	private Socket socket;
	private Thread thread;
	private GameLogic game;

	/**
	 * Constructs the ClientThread class using the socket and game provided. The socket is unique to
	 * this instance of ClientThread, but the GameLogic is shared between all instances, hence the
	 * need for synchronisation.
	 */
	public ClientThread(Socket localSocket, GameLogic localGame) {
		super(localGame);
		game = localGame;
		socket = localSocket;
		thread = new Thread(this); // creates a new thread and passes this in. This is runnable because
		// CommandLineUser implements Runnable.
		thread.start(); // starts the new thread to listen for commands.
	}

	/**
	 * Runs in a thread to accept input from the client and passes it to CommandLineUser to be
	 * processed. This is where the main synchronisation happens, because by making ClientThread
	 * objects have a lock on the shared game object, only one at a time can have their command
	 * processed.
	 */
	@Override
	public void run() {
		boolean firstTurn = true;
		addPlayer(); // adds the new player
		broadcastChange();
		try (
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// opens a reader from the client socket
		) {
			serverOutput("SERVER", "CONNECTED");
			String command;
			while (((command = in.readLine()) != null)) { // constantly reads from
				// client until the connection is closed or the game is over
				serverOutput("RECEIVED", command);
				synchronized (game) {
					processCommand(command);
					// this only allows one client at a time to have their command processed. If another
					// client is already in this synchronized block, the first client must wait until the
					// other client has released their lock on game. This works because all ClientThread
					// objects share the same GameLogic object, game.
				}
				if (firstTurn && !command.startsWith("HELLO")) { // HELLO must be sent on the first turn
					String autoName = Long.toHexString(UUID.randomUUID().getLeastSignificantBits());
					autoName = autoName.toUpperCase();
					try {
						Thread.sleep(100); // this is needed to allow long responses such as lookreply to be
						// sent entirely before the HELLO is sent
					} catch (InterruptedException e) {}
					synchronized (game) {
						processCommand("HELLO HUMAN-" + autoName); // picks a random name if the human doesn't
					}
				}
				firstTurn = false;
			}

		} catch (IOException e) {
		} finally {
			removePlayer();
			try {
				socket.close();
			} catch (IOException e) {}
			serverOutput("SERVER", "DISCONNECTED");
			broadcastChange();
		}
	}

	/**
	 * Sends a string over the network to the client. This is synchronised so that two server messages
	 * taking different amounts of time to reach this method don't send at the same time. This can
	 * happen when the client enters LOOK as their first command, and the server must send both LOOK
	 * and HELLO in immediate succession.
	 */
	@Override
	protected void doOutputMessage(String message) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // opens an output stream
			// to the client socket
			serverOutput("SENT", message);
			synchronized (game) {
				out.println(message);
				// synchronized to stop two messages from being sent simultaneously
			}
		} catch (IOException e) {}
	}

	/**
	 * This logs all actions taken by the server to the server's standard output. This is the screen
	 * in demos, but in a real server this could be a log file. Actions are recorded along with other
	 * fields.
	 */
	private void serverOutput(String direction, String command) {
		if (command.startsWith("LOOKREPLY")) {
			command = "LOOKREPLY"; // don't print the actual reply because it has multiple lines
		} else if (command.startsWith("RENDERHINT")) {
			command = "RENDERHINT";
		}
		System.out.printf("%-10.10s %-30.30s %-30.30s %5.5s\n", direction, command, getPlayerName(), playerID);
		// lines longer than the columns allocated to them are automatically truncated rather than being
		// allowed to break the formatting of the table
	}

}
