/* 
 * This program has to start after the Server program has started.
 */

import java.net.*;
import java.io.*;
import java.util.*;

/*
 * Client that can run in console
 */

public class Client
{
	// for I/O
	
	// to read from socket 
	private ObjectInputStream sInput;

	// to write on socket
	private ObjectOutputStream sOutput;

	private Socket socket;

	// the server, the port and the username
	private String server, username;
	private int port;

	/* Constructor called in console mode
	 * server : the server address
	 * port : the port number
	 * username : the username
	 */
	Client(String server, int port, String username)
	{
		this.server = server;
		this.port = port;
		this.username = username;
	}

	/*
	 * To start the dialog
	 */

	public boolean start()
	{
		// try to connect to the server
		try
		{
			socket = new Socket(server, port);
		}
		// if failed
		catch(Exception ec)
		{
			display("Error connecing to server:" + ec);
			return false;
		}

		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);

		/*
		 * Creating both data stream
		 */
		try
		{
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch(IOException eIO)
		{
			display("Exception creating new Input/Output Streams:" + eIO);
			return false;
		}

		//Creates the threads to listen from Server
		new ListenFromServer().start();

		// Send the username to the server. 
		// This is the only message that we will send as String.
		// All the other message will be ChatMessage objects.
		
		try
		{
			sOutput.writeObject(username);
		}
		catch(IOException eIO)
		{
			display("Exception doing login:" + eIO);
			disconnect();
			return false;
		}

		// success we inform the caller it worked
		return true;
	}

	/*
	 * To send message to console
	 */

	private void display(String msg)
	{
		System.out.println(msg);
	}

	/*
	 *  To send the message to the server
	 */
	void sendMessage(ChatMessage msg)
	{
		try
		{
			sOutput.writeObject(msg);
		}
		catch(IOException e)
		{
			display("Exception writing to the server:" + e);
		}
	}

	/*
	 * When something goes wrong, close the input/output streams and disconnect 
	 */
	private void disconnect()
	{
		try
		{
			if(sInput != null)
				sInput.close();
		}
		catch(Exception e){}
		try
		{
			if(sOutput != null)
				sOutput.close();
		}
		catch(Exception e){}
		try
		{
			if(socket != null)
				socket.close();
		}
		catch(Exception e){}
	}

	/*
	 * To start the client in console mode
	 * > java Client
	 * > java Client username
	 * > java Client username portNumber
	 * > java Client username portNumber serverAddress
	 *
	 * If the portNumber is not specified then 1500 is used
	 * If the serverAddress is not specified then "localHost" is used
	 * If the username is not specified "Anonymous" is used
	 *
	 */

	public static void main(String[] args)
	{
		// default values
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";

		// depending on the number of arguments provided we fall through
		switch(args.length)
		{
			// java Client username portNumber serverAddr
			case 3:
				serverAddress = args[2];
			// java Client username portNumber
			case 2:
				try
				{
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e)
				{
					System.out.println("Invalid portNumber");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			
			// java Client username	
			case 1:
				userName = args[0];
			//java Client
			case 0:
				break;
			// invalid number of arguments
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
				return;
		}

		// Creating client Object
		Client client = new Client(serverAddress, portNumber, userName);

		// test if the connectin to the server can be started
		if(!client.start())
			return;

		// wait for message from user
		Scanner scan = new Scanner(System.in);

		// loop forever for the message from the user
		while(true)
		{
			System.out.println("> ");

			// read the message from the user
			String msg = scan.nextLine();

			// logout if the message is LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT"))
			{
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				break;
			}

			// message WHOISIN
			else if(msg.equalsIgnoreCase("WHOISIN"))
			{
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, "")); 
			}
			else
			{
				 client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		// done disconnect
		client.disconnect();
	}

	/*
	 * A class that waits for the message from the server
	 * And simply System.out.println() in console mode
	 */
	class ListenFromServer extends Thread
	{
		public void run()
		{
			while(true)
			{
				try
				{
					String msg = (String)sInput.readObject();

					// if console mode print the message and add back the promt
					System.out.println(msg);
					System.out.print("> ");
				}
				catch(IOException e)
				{
					display("Server has closed the connection: " + e);
					break;
				}

				catch(ClassNotFoundException e2)
				{

				}
			}
		}
	}
}
