/*
 * The ClientGUI class
 * It is the GUI for the client. 
 * It is also a BorderLayout. In the NORTH region, two JTextField to enter the host name of the Server and port number it is listening to
 * The CENTER region contains jScrollPane with a JtextArea that contains the messages exchanged.
 * The SOUTH region contains 3 buttons: "Login" "Logout", "Whoisin".
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * The client with its GUI
 */

public class ClientGUI extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;

	// JLabel first to hold "Username:", later on "Enter Message"
	private JLabel label;

	// JTextField to hold username and later on the messages
	private JTextField tf;

	// JTextField to hold Server address and the port number
	private JTextField tfServer, tfPort;

	// JButton to logout and get the list of users
	private JButton login, logout, whoIsIn;

	// For chat messages
	private JTextArea ta;

	// For Connection
	private boolean connected;

	// Client object
	private Client client;

	// the default port number
	private int defaultPort;

	private String defaultHost;

	// Constructor connection receiving a socket number
	ClientGUI(String host, int port)
	{
		super("Chat Client");
		defaultPort = port;
		defaultHost = host;

		// The NorthPanel with:
		JPanel northPanel = new JPanel(new GridLayout(3,1));

		// Server name and the port number
		JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));

		// The two JTextField with default value for server address and port number
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Server Address: "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number: "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));

		// Adds the Server and port field to GUI
		northPanel.add(serverAndPort);

		// the label and TextField
		label = new JLabel(" Enter the username below", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Anonymous");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);

		// The CenterPanel which is a Chat space
		ta = new JTextArea("Welcome to the Chat room \n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// 3 Buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);   // you have to login before being able to logout
		whoIsIn = new JButton("Who is is");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);  // you have to login before being able to who is in

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();
	
	}

	// Called by Client to append text in the TextArea
	void append(String str)
	{
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}

	// Called by GUI if the connection failed
	// we reset out buttons, label, textfield
	void connectionFailed()
	{
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		label.setText("Enter your username below");
		tf.setText("Anonymous");

		// Reset port number and host name as a construction time
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);

		// Let the user change them
		tfServer.setEditable(false);
		tfPort.setEditable(false);

		tf.removeActionListener(this);
		connected = false;
	}

	/*
	 *  When BUtton or JTextField is clicked
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object o = e.getSource();

		// if it is the Logout button
		if(o == logout)
		{
			 client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
			 return;
		}

		// if it is the whoisIn button
		if(o == whoIsIn)
		{
			 client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));         
			 return;
		}

		// ok it is comming from the JTextField
		if(connected)
		{
			// just have to send the message
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));
			tf.setText("");
			return;
		}

		if(o == login)
		{
			// ok it is a connection request
			String username = tf.getText().trim();

			// empty username ignore it
			if(username.length() == 0)
				return;

			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;

			// empty or invalid port number, ignore it
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try
			{
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en)
			{
				return; 
			}

			// try creating a new Client with GUI
			client = new Client(server, port, username, this);

			// test if we can start the client
			if(!client.start())
				return;

			tf.setText("");
			label.setText("Enter your message below");
			connected = true;

			// disable login button
			login.setEnabled(false);

			// enable the 2 buttons
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);

			// disable the Server and Port JTextField
			tfServer.setEditable(false);
			tfPort.setEditable(false);

			// Action listener for when the user enter the message
			tf.addActionListener(this);
		}
	}

	// To start the whole thing
	public static void main(String[] args)
	{
		new ClientGUI("localhost", 1500);

	}

}
