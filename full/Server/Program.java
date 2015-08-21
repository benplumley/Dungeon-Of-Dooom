import java.text.ParseException;

/**
 * Class to handle command line arguments and initialise the correct instances.
 */
public class Program {

	private static final String mapDirectory = "Maps" + System.getProperty("file.separator");

	/**
	 * Main method, used to parse the command line arguments.
	 *
	 * @param args
	 *            Command line arguments
	 */
	public static void main(String[] args) {

		try {
			GameLogic game = null;
			Server server = null;

			switch (args.length) {
				case 0 :
					// No Command line arguments - default map
					System.out.println("Starting Game with Default Map");

					game = new GameLogic(mapDirectory + "defaultMap");
					break;

				case 1 :
					// Either -b or the name of the map
					if (args[0].equals("-b")) {
						System.out
								.println("Starting bot game with default Map");

						game = new GameLogic(mapDirectory + "defaultMap");
					} else {
						// Try to load the specified map
						System.out.println("Starting Game with Map " + args[0]);
						game = new GameLogic(mapDirectory + args[0]);
					}
					break;

				case 2 :
					// The first one needs to be -b
					if (args[0].equals("-b")) {
						game = new GameLogic(mapDirectory + args[1]);
					} else {
						System.err
								.println("The wrong number of arguments have been provided, you can either specify \"-b\" "
										+ "\n"
										+ "to play with a bot, the name of the map you want to play on, or the \"-b\" followed "
										+ "\n"
										+ "by the name of the map you want the bot to play on");
					}
					break;

				default :
					System.err
							.println("The wrong number of arguments have been provided, you can either specify \"-b\""
									+ "\n"
									+ "to play with a bot, the name of the map you want to play on, or the \"-b\" followed "
									+ "\n"
									+ "by the name of the map you want the bot to play on");
					break;
			}
			server = new Server(game);


		} catch (final ParseException e) {
			System.err.println("Syntax error on line " + e.getErrorOffset()
					+ ":" + "\n"
					+ e.getMessage());
			System.exit(2);
		} catch (final Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
