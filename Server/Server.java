import java.util.ArrayList;
import java.text.ParseException;
import java.net.*;
import java.io.*;
import java.util.Iterator;

public class Server implements Runnable {

	/**
	 * This class runs on the server side and creates threads and objects for clients as they join.
	 */

	private int portNumber = 59652;
	private ArrayList<ClientThread> clientList = new ArrayList<ClientThread>(); // contains threads
	// for all clients currently in the game
	private GameLogic game;

	/**
	 * Opens a server socket and accepts any incoming connections. For each new connection, a new
	 * socket is created and a new thread is started with that socket and a shared GameLogic.
	 */
	public Server(GameLogic game) {
		// Thread thread = new Thread(this);
		// thread.start();
		outputHeaders();
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			while (true) {
				Socket clientSocket = serverSocket.accept(); // creates a new socket for each client
				clientList.add(new ClientThread(clientSocket, game)); // creates a ClientThread with its own
				// socket and a shared game between all clients
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Prints the column headers for the server's screen output. These are printed here because they
	 * appear once per instance of a server, rather than once per new client.
	 */
	private void outputHeaders() {
		System.out.printf("%-10.10s %-30.30s %-30.30s %5.5s\n", "Type", "Command", "Player Name", "ID");
		System.out.println("------------------------------------------------------------------------------");
	}

	@Override
	public void run() {
	// 	boolean gameStarted = false;
	// 	int alivePlayers = 0;
	// 	while (!(gameStarted && (alivePlayers == 0))) {
	// 		alivePlayers = 0;
	// 		Iterator iterator = clientList.iterator();
	// 		while (iterator.hasNext()) {
	// 			ClientThread currentClient = (ClientThread) iterator.next();
	// 			if (currentClient.isRemoved()) {
	// 				clientList.remove(currentClient);
	// 			} else {
	// 				alivePlayers++;
	// 				gameStarted = true;
	// 			}
	// 		}
	// 	}
	// 	System.exit(0);
	}

}
