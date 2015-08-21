import javax.swing.*;
import java.awt.*;
import javax.swing.text.BadLocationException;
import java.io.IOException;


public class ChatPanel extends JPanel {

	/**
	 * This class extends JPanel and creates the chat box, send button and chat history panel. It
	 * allows HTML code and some Markdown to be entered in the chat box to appear rendered properly
	 * in the chat history panel.
	 */

	private String history = "<html><head><style>" +
		"body {font-family:consolas; line-height:80%; font-size:9px}</style></head><body>\n";
	private JTextPane textPane;
	public TextField message;
	private JScrollPane scrollPane;
	public JButton send;
	private String playerName;

	/**
	 * This constructor sets up the chat panel and calls a method to add its components.
	 */
	public ChatPanel() {
		setup();
		populate();
	}

	/**
	 * This method sets up the chat panel to use a GridBagLayout, and sets up the components that will
	 * be added to it
	 */
	private void setup() {
		this.setLayout(new GridBagLayout());
		textPane = new JTextPane();
		textPane.setPreferredSize(new Dimension(592, 100));
		textPane.setContentType("text/html"); // allows the panel to render HTML
		scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(592, 100));
		message = new TextField(71);
		message.setName("message");
		send = new JButton("Send");
		send.setName("send");
		textPane.setEditable(false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	}

	/**
	 * This method adds all the components to the panel in the appropriate positions.
	 */
	private void populate() {
		GridBagConstraints layoutConstraints = new GridBagConstraints();
		layoutConstraints.fill = GridBagConstraints.BOTH;
		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridwidth = 2;
		layoutConstraints.gridheight = 1;
		this.add(scrollPane, layoutConstraints);
		layoutConstraints.gridy = 1;
		layoutConstraints.gridwidth = 1;
		this.add(message, layoutConstraints);
		layoutConstraints.gridx = 1;
		send.setEnabled(false);
		this.add(send, layoutConstraints);
	}

	/**
	 * This method informs this ChatPanel object of the player's name so it can be coloured
	 * differently in chat to the names of other players
	 */
	public void setName(String name) {
		playerName = name;
	}

	/**
	 * This method provides an accessor to the text entered in the chat box, calls a method to replace
	 * markdown with HTML, and clears the chat box afterwards
	 */
	public String getMessage() {
		if (!message.getText().equals("")) {
			String messageText = message.getText();
			messageText = parseEffects(messageText);
			message.setText("");
			return messageText;
		}
		return "";
	}

	/**
	 * This method searches the message for markdown tags and replaces them with the appropriate HTML
	 * tags using Regex.
	 */
	public String parseEffects(String originalMessage) {
		String newMessage = originalMessage;
		String htmlTag;
		for (int i = 0; i < newMessage.length(); i++) { // replaces **bold** with <strong> tags
			htmlTag = (i % 2 == 0) ? "<strong>" : "</strong>"; // every even-numbered tag is an opening
			// tag and every odd-numbered tag is a closing tag
			newMessage = newMessage.replaceFirst("\\*\\*", htmlTag); // replaces the next instance of **
			// with the appropriate tag
		}
		for (int i = 0; i < newMessage.length(); i++) { // does the same for *italics* and <em>
			htmlTag = (i % 2 == 0) ? "<em>" : "</em>";
			newMessage = newMessage.replaceFirst("\\*", htmlTag);
		}
		return newMessage;
	}

	/**
	 * This method allows the DungeonGUI class to append all received messages to the end of the chat
	 * history.
	 */
	public void receiveMessage(String message) {
		appendMessage(message);
		textPane.setCaretPosition(textPane.getDocument().getLength()); // scrolls to the bottom
	}

	/**
	 * This method appends messages with the appropriate HTML formatting to the end of the text in the
	 * chat history.
	 */
	private void appendMessage(String message) {
		String[] parts = message.split(":", 2);
		String name = parts[0];
		String messageText = "<span>" + parts[1] + "</span><br>";
		if (name.equals(playerName)) { // messages by this player are blue
			name = "<span style=\"color:blue\">&#60;" + name + "&#62; </span>";
		} else { // messages by other players are red
			name = "<span style=\"color:red\">&#60;" + name + "&#62; </span>";
		}
		history = history + name + messageText;
		textPane.setText(history + "</body></html>");
	}

	/**
	 * This method switches the state of the send button depending on whether the client is connected
	 * to a server.
	 */
	public void setConnected(boolean isConnected) {
		send.setEnabled(isConnected);
	}

}
