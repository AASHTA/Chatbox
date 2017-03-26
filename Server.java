/* 
 * Importing necessary libraries
 */
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * For now Server can run as a console.
 */

public class Server
{
	// a unique id for each connection
	// 'static' keyword in java means that the variable or function is shared between all the instances of that class
	private static int uniqueID;

	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;

	// to display time
	private SimpleDateFormat sdf;

	// For GUI
	private ServerGUI sg;

	// the port number to listen for connection
	private int port;

	// the boolean that would be turned off to stop the server
	private boolean keepGoing;

	/*
	 * Server constructor that receive the port to listen for connection as parameter in console
	 */
	public Server(int port)
	{
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		al = new ArrayList<ClientThread>();
	}

	/*
	 * Server constructor that receives the port to listen for connection as parameter in GUI mode
	 */
	public Server(int port, ServerGUI sg)
	{
		// for GUI
		this.sg = sg;
		// the port
		this.port = port;
		// to display in hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		al = new ArrayList<ClientThread>();
	}

	public void start()
	{
		keepGoing = true;

		/* 
		 * Create a socket server and wait for connection request
		 */
		try
		{
			// socket used by the server
			ServerSocket serversocket = new ServerSocket(port);

			//infinite loop to wait for connections
			while(keepGoing)
			{
				// format message to indicate the server is waiting for the client
				display(" Server waiting for Clients on port " + port + ".");

				/*
				 *  ServerSocket invokes accept() method. This method doesn't return until the client is connected
				 *  Also the server obtains its socket object from the return value of accept() method.
				 */
				Socket socket = serversocket.accept();

				// If asked to stop
				if(!keepGoing)
					break;

				// Making a thread of it	
				ClientThread t = new ClientThread(socket);

				// Saving it in ArrayList
				al.add(t);
				t.start();
			}

			// If asked to stop
			try
			{
				serversocket.close();
				for(int i = 0; i < al.size(); ++i)
				{
					ClientThread tc = al.get(i);
					try
					{
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					}
					catch(IOException ioE)
					{

					}
				}
			}
			catch(IOException e)
			{
				display("Exception closing the server and clients:" + e);
			}
		}
		catch(IOException e)
		{
			String msg = sdf.format(new Date()) + "Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}

	/*
	 * For the GUI to stop the server
	 */
	private void stop()
	{
		keepGoing = false;

	}


	/*
	 * Display an event to the console
	 */
	private void display(String msg)
	{
		String time = sdf.format(new Date()) + " " + msg + "\n";
	 //       String m1 = time + " " + msg + "\n";
	//	if(sg == null)
		System.out.println(time);
	//	else
		//	sg.appendEvent(time + "\n");
	}

	/*
	 *  To broadcast a message to all Clients
	 */

	/*
	 * synchronized methods enable a simple strategy for preventing thread interference and memory consistency errors.
	 * If object is visible to more than one thread, all reads and writes to that objects variables are done through
	 * synchronized methods.
	 *
	 * When one thread is executing a synchronized method for an object, all other threads that invoke synchronized methods
	 * for the same object block until the first thread is done with the object.
	 * Also when the synchronized method exists, it automatically establishes a happens-before relationship with any 
	 * subsequent invocation of a synchronized method for the same object. It guarantees that changes to the state of object
	 * are visible to all threads.
	 */

	private synchronized void broadcast(String message)
	{
		// add HH:MM:SS and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";

		// display the message on console or GUI
		//if(sg = null)
		System.out.println(messageLf);
		//else
		//	sg.appendRoom(messageLf); // append in the room window


		/* 
		 * Looping in reverse order in case we would have to remove a Client
		 * Because it has been disconnected
		 */

		for(int i = al.size(); --i >=0;)
		{
			ClientThread ct = al.get(i);

			// try to write to a client if it fails to remove it from the list
			if(!ct.writeMsg(messageLf))
			{
				al.remove(i);
				display("Disconnected Client" + ct.username + "removed from list.");
			}
		}
	}

	// for Client who logoff using LOGOUT message
	synchronized void remove(int id)
	{
		// scan the arraylist until we found the id
		for(int i = 0; i < al.size(); ++i)
		{
			ClientThread ct = al.get(i);

			if(ct.id == id)
			{
				al.remove(i);
				return;
			}
		}
	}

	/*
	 *  To run as console application just open a console window and:
	 *  > java Server
	 *  > java Server portNumber
	 *  If the portNumber is not specified 1500 is used
	 */

	public static void main(String[] args)
	{
		// start the server on port 1500 until portNumber is specified
		int portNumber = 1500;

		switch(args.length)
		{
			case 1:
				try
				{
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e)
				{
					System.out.println("Invalid Port Number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is:java Server [portNumber]");
				return;

		}

		// Creating server object and starting it
		Server server =  new Server(portNumber);
		server.start();
	}

	/* 
	 * One instance of this the thread will run for each client
	 */

	class ClientThread extends Thread
	{
		// socket where to listen 
		Socket socket;

		/*
		 * ObjectInputStream and ObjectOutputStream classes are basically used to serialize objects
		 * And store them as file or any other storage accessible by output stream; read them again, deseralize it into
		 * object and use it.
		 */
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;

		// unique id which makes disconnection easier
		int id;

		// the Username of the Client
		String username;

		// date the Client is connected
		String date;
		
		ChatMessage cm;

		// Constructor
		ClientThread(Socket socket)
		{
			// a unique id
			id = ++uniqueID;

			this.socket = socket;

			/* 
			 * Creating both data stream
			 */

			System.out.println("Thread trying to create object Input/Output Streams");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput = new ObjectInputStream(socket.getInputStream());

				// read the use name
				username = (String)sInput.readObject();
				display(username + "just connected.");
			}
			catch(IOException e)
			{
				display("Exception creating new Input/Output Streams:"+e);
				return;
			}
			catch(ClassNotFoundException e){}
			date = new Date().toString() + "\n";
		}

		public void run()
		{
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing)
			{
				// read a String (which is an object)
				try
				{
					cm = (ChatMessage)sInput.readObject();
				}
				catch(IOException e)
				{
					display(username + " Exception reading Streams:" + e);
					break;
				}
				catch(ClassNotFoundException e2)
				{
					break;
				}

				// the message part of the ChatMessage
				String message = cm.getMessage();

				// Switch on the type of the message receive
				switch(cm.getType())
				{
					case ChatMessage.MESSAGE:
						broadcast(username + ": " + message);
						break;
					case ChatMessage.LOGOUT:
						display(username + " disconnected with a LOGOUT message.");
						keepGoing = false;
						break;
					case ChatMessage.WHOISIN:
						writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");

						// scan all the users connected
						for(int i = 0; i < al.size(); ++i)
						{
							ClientThread ct = al.get(i);
							writeMsg((i+1) + ")" + ct.username + " since " + ct.date);
						}
						break;
				}
			}
			// remove the Client from the arraylist Containing the list of the connected Clients
			remove(id);
			close();
		}
		// try to close everything
		private void close()
		{
			// try to close the connection
			try
			{
				if(sOutput != null)sOutput.close();
			}
			catch(Exception e){}
			try
			{
				if(sInput != null)sInput.close();
			}
			catch(Exception e){};
			try
			{
				if(socket != null)socket.close();
			}
			catch(Exception e){}
		}

		/*
		 * Write a String to the Client Output Stream
		 */

		private boolean writeMsg(String msg)
		{
			// if Client is connected, send this message to the Client
			if(!socket.isConnected())
			{
				close();
				return false;
			}

			// write message to the stream
			try
			{
				sOutput.writeObject(msg);
			}

			// if an error occurs, inform the user
			catch(IOException e)
			{
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}	
}

