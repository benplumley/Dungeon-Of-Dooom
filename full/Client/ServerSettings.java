import java.awt.*;
import javax.swing.*;

public class ServerSettings extends JPanel  {

	/**
	 * This class extends JPanel and provides text fields and buttons for connecting to and
	 * disconnecting from the server.
	 */

	public JButton connectButton;
	public JButton disconnectButton;
	public JButton help;
	public TextField hostnameField;
	public TextField portNumField;
	public TextField playerNameField;

	/**
	 * This constructor creates the panel and makes it visible.
	 */
	public ServerSettings() {
		setupPanel();
		populatePanel();
		this.setVisible(true);
	}

	/**
	 * This method sets up the layout of the panel to use a GridLayout with one column and eight rows.
	 */
	private void setupPanel() {
		this.setLayout(new GridLayout(9, 1));
	}

	/**
	 * This method creates and sets up the components to go in this panel and adds them to it.
	 */
	private void populatePanel() {
		JLabel hostnameLabel = new JLabel("Enter hostname:");
		hostnameLabel.setOpaque(true);
		hostnameField = new TextField(20);
		JLabel portNumLabel = new JLabel("Enter port number:");
		portNumLabel.setOpaque(true);
		portNumField = new TextField(20);
		JLabel playerNameLabel = new JLabel("Enter optional player name:");
		playerNameLabel.setOpaque(true);
		playerNameField = new TextField(20);
		connectButton = new JButton("Connect");
		disconnectButton = new JButton("Disconnect");
		help = new JButton("Help");
		disconnectButton.setEnabled(false);
		this.add(hostnameLabel);
		this.add(hostnameField);
		this.add(portNumLabel);
		this.add(portNumField);
		this.add(playerNameLabel);
		this.add(playerNameField);
		this.add(connectButton);
		this.add(disconnectButton);
		this.add(help);
	}

	/**
	 * This method flips the state of all the components depending on whether the client is connected
	 * to a server or not.
	 */
	public void flipButtonStates(boolean isConnected) {
		connectButton.setEnabled(!isConnected);
		disconnectButton.setEnabled(isConnected); // the disconnect button can only be used whilst
		// connected
		hostnameField.setEditable(!isConnected);
		portNumField.setEditable(!isConnected);
		playerNameField.setEditable(!isConnected);
	}

	/**
	 * This method sets the Name box to contain the name. This is only visible if the player didn't
	 * pick a name.
	 */
	public void setName(String name) {
		playerNameField.setText(name);
	}

}
