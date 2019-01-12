package server;

import java.util.ArrayList;
import java.util.Calendar;
import com.google.gson.Gson;

/**
 * MessagePost.java - a class that represents a message sent to/from a client
 * 
 * @author knownastron
 *
 */

public class MessagePost {
	private String command, username, message, time;
	
	public MessagePost() {
		this.command = "";
		this.username = "";
		this.message = "";
		this.time = "";
	}
	
	public MessagePost(String command, String username, String message, String time) {
		this.command = command;
		this.username = username;
		this.message = message;
		this.time = time;
	}
	
	/**
	 * @return the value of the MessagePost
	 */
	public String getCommand() {
		return this.command;
	}
	
	/**
	 * @return the username of the MessagePost
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * @return the message of the MessagePost
	 */
	public String getMessage() {
		return this.message;
	}
	
	
	/**
	 * 
	 * @return the time the MessagePost was received at the sever
	 */
	public String getTime() {
		return this.time;
	}
	
	/**
	 * returns a String of the fields of the MessagePost in JSON format
	 * 
	 * @return JSON format of the MessagePost
	 */
	public String toJSON() {
		Gson mygson = new Gson();
		return mygson.toJson(this);
	}
	
	/**
	 * Creates a MessagePost from the incomingMessage, adds the new MessagePost to the messageLog
	 * 
	 * @param message
	 * @param messageLog
	 * @return
	 * @throws Exception
	 */
	public static MessagePost createMessagePost(String incomingMessage) throws Exception {
		String outputUsername, outputMessage;
		Gson gson = new Gson();
		
		try {
			MessagePost newMessagePost = gson.fromJson(incomingMessage, MessagePost.class);
			outputUsername = newMessagePost.getUsername();
			outputMessage = newMessagePost.getMessage();
		} catch (Exception e) { // this error happens when a client leaves
			throw new Exception();
		}
		Calendar cal = Calendar.getInstance();
		String hour = cal.get(Calendar.HOUR) + "";
		String minute = cal.get(Calendar.MINUTE) + "";
		if (minute.length() == 1) {
			minute = "0" + minute;
		}
		String outputTime = hour + ":" + minute;

		MessagePost messagePost = new MessagePost("post", outputUsername, outputMessage, outputTime);
		return messagePost;
	}
	
	/**
	 * Adds a MessagePost to the ArrayList
	 * 
	 * @param messagePostToAdd MessagePost to add to the messageLog
	 * @param messageLog ArrayList to add the MessagePost to
	 */
	public static void addMessagePostToLog(MessagePost messagePostToAdd, ArrayList<MessagePost> messageLog) {
		messageLog.add(messagePostToAdd);
	}
}
