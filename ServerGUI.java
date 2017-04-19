/* 
 * The GUI is simple GUI using jTextArea
 * It is a BorderLayout with in NORTH region an editable jTextField containing the portNumber and the Server should listen.
 * It also contains 2 buttons to Start/Stop the sever.
 * The CENTER region contains two jScrollPane both jTextArea. 
 * The first JTextArea contains the messages exchanged in Chatbox, basically what Client sees.
 * The seond JTextArea contains event messages: WHOISIN, LOGOUT, error messages
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * The sever as GUI
 *
 * The class ServerGUI is based on JFrame.
 * Some commonly used container classes are
 * JFrame :  It is the basic Java application window. It has title bar and a provision for adding a menu
 * JDialog : This usually appears as a prompt for inputting data as well as displaying them
 * JApplet : This is designed to run embedded in a web page. You can draw and add menu components here
 * JPanel : It is used as a container that can be created within window.
 *
 * WindowListener is one of the window related event handlers. It handles the WindowEvent objects.
 * When the appropriate listener has been registered on a window (such as Frame or dialog), window events are fired 
 * just after the window activity or state has ocurred.
 * The 'WindowListener' interface defines methods that handle most window events, such as the events for
 * opening and closing the window, activation and deactivation of the window,and iconification and deiconification of the window.
 *
 * 'ActionListener' defines what should be done when user performs certain operation.
 * An action event occurs, whenever an action is performed by the user. Example:
 * > When the user clicks a button, chooses a menu item, presses Enter in the text field. 
 * > Then the result is that an actionPerformed message is sent to all action listeners that are registered on the relevant 
 * component.
 *
 */
public class ServerGUI extends JFrame implements ActionListener, WindowListener
{
	private static final long serialVersionUID = 1L;
         
	/*
	 * JButton is an implementation of a push button. This component has a label and generates an event when pressed.
	 */

	// the stop and start buttons
	private JButton stopStart;

	/*
	 * JTextArea is a multiline area to display plain text.
	 */

	// JTextArea for the chat messages and the events
	private JTextArea chat, event;

	/*
	 * JTextField is a component which allows the editing of a single line of text
	 */

	// The portNumber
	private JTextField tPortNumber;

	// my server
	private Server server;

	// server constructor that receive the port to listen to for connection as parameter
	ServerGUI(int port)
	{       
		/*
		 * 'super' is used to invoke immediate parent class constructor.
		 */

		super("Chat Server");
		server = null;

		/*
		 * Jpanel is a generic light weight container.
		 * It is used to group a set of compoents together.
		 *
		 * In the NorthPanel, there is portNumber and the Start and stop buttons.
		 */

		// Creating a new JPanel object
		JPanel north = new JPanel();

		/*
		 * JLabel class can display either text, an image or both.
		 * Label's contents are aligned by setting vertical or horizontal alignment in display area.
		 * By default, labels are vertically centerd in display area.
		 * Text-only labels are leading edge aligned, by default 
		 * And image-only labels are horizontally centered, by default.
		 */
		north.add(new JLabel("Port Number: "));

		tPortNumber = new JTextField(" " + port);
		north.add(tPortNumber);

		// to stop or start the server, we start with "Start"
		
		// Creates a button with the text.
		stopStart = new JButton("Start");

		/*
		 * The line ' stopStart.addActionListener(this);' registers 'this' as a listener to receive events 
		 * from the button name 'stopStart'.
		 * Different types of components require different kinds of listener classes to handle the events they fire.
		 * A button fires events known as 'action events', which are handled by the listeners knows as action listeners.
		 */
		stopStart.addActionListener(this);
		north.add(stopStart);

		/*
		 * The class 'BorderLayout' arranges the components to fit in the five regions:
		 * east, west, north, south and center. Each region can contain only one component and each component in
		 * each region is identified by the corresponding constant NORTH, SOUTH, EAST, WEST and CENTER.
		 */
		add(north, BorderLayout.NORTH);

		// the event and chatroom

		/*
		 * 'GridLayout' class arranges the components in a rectangular grid.
		 * GridLayout(int rows, int cols) creates a grid layout with specified number of rows and columns.
		 */
		JPanel center = new JPanel(new GridLayout(2,1));

		/*
		 * 'JTextArea(int rows, int columns)' constructs a new empty TextArea with the specified number of rows 
		 * and columns.
		 */
		chat = new JTextArea(80,80);
		chat.setEditable(false);
		appendRoom("Chat room. \n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(80,80);
		event.setEditable(false);
		appendEvent("Events log.\n");
		center.add(new JScrollPane(event));
		add(center);

		// Needs to be informed when user click the close button on the frame
		addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
	}

	// Append the message to the two JtextArea
	// position at the end
	void appendRoom(String str)
	{
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}

	void appendEvent(String str)
	{
		event.append(str);
		event.setCaretPosition(chat.getText().length() - 1);
	}

	// start or stop when clicked
	public void actionPerformed(ActionEvent e)
	{
		// if running, then stop
		if(server != null)
		{
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			stopStart.setText("Start");
			return;
		}

		// Start the server
		int port;
		try
		{
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er)
		{
			appendEvent("Invalid Port Number");
			return;
		}

		// Create a new server
		server = new Server(port, this);

		// Start it at as Thread
		new ServerRunning().start();
		stopStart.setText("Stop");
		tPortNumber.setEditable(false);
	}

	// Entry Point to start a new Server
	public static void main(String[] arg)
	{
		// start server default port 1500
		new ServerGUI(1500);

	}

	/* 
	 * If the user clicks the 'X' button to close the application
	 * Then we need to close the connection with the server to free the port
	 */

	public void WindowClosing(WindowEvent e)
	{
		// if server exists
		if(server != null)
		{
			try
			{
				// Ask the server to close the connection
				server.stop();
			}
			catch(Exception eClose)
			{
				
			}
			server = null;
		}

		// dispose the frame
		dispose();
		System.exit(0);
	}

	// WindowListener method
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	/*
	 * A thread to run the server
	 */

	class ServerRunning extends Thread
	{
		public void run()
		{
			server.start();

			// if server failed
			stopStart.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent("Server crashed \n");
			server = null;
		}
	}
}
 
