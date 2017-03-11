/* 
 * The GUI is simple GUI using jTextArea
 * It is a BorderLayout with in NORTH region an editable jTextField containing the portNumber and the Serve should listen.
 * It also contains 2 buttons to Start/Stop the sever.
 * The CENTER region contains two jScrollPane both jTextArea. 
 * The first JTextArea contains the messages exchanged in Chatbox, basically what Client sees.
 * The seond JTextArea contains event messages: WHOISIN, LOGOUT, error messages
 */
import javax.swing.*;
import java,awt.*;
import java,awt.event.*;

/*
 * The sever as GUI
 */
public class ServerGUI extends JFrame implements ActionListener, WindowListener
{
	private static final long serialVersionUID = 1L;

	// the stop and start buttons
	private JButton stopStart;

	// JTextArea for the chat messages and the events
	private JTextArea chat, event;

	// The portNumber
	private JTextField tPortNumber;

	// my server
	private Server server;

	// server constructor that receive the port to listen to for connection as parameter
	ServerGUI(int port)
	{
		super("Chat Server");
		server = null;

		// in the NorthPanel the portNumber the Start and Stop buttons
		jPanel north = new Jpanel();

		north.add
	}
}
 *
