/* 
 * This class is used to send message (java object) between two communicating java application.
 * In this class all the message sent from Server to Client are String Objects.
 * All the message sent from Client to Server are ChatMessage. ChatMessage has a type and a String that contains actual message.
 */

import java.io.*;

/*
 * 'implements' is for implementing an interface. 
 *  The main difference between implement and regular class is that in an interface we cannot implement any of the declared methods
 *  The class that implements the interface can implement the methods.
 *
 *  Serializable interface is implemented when conversion of an instance of class into series of bytes is necessary.
 *  Serializable interface makes storing and sending of objects over network easy.
 */
public class ChatMessage implements Serializable{

	/*
	 *  serialVersionUID is a universal version identifier for a 'Serializable' class. 
	 * 'Deserialization' uses this number to ensure that a loaded class corresponds exactly to serialized object.
	 *
	 */
	protected static final long serialVersionUID = 1112122200L;

	// WHOISIN to receive the list of users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from Server
	
	static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
	private int type;
	private String message;

	//constructor
	
	ChatMessage(int type, String message)
	{
		this.type = type;
		this.message = message;
	}

	// methods
	int getType()
	{
		return type;
	}

	String getMessage()
	{
		return message;
	}

}

