package server;

import java.util.ArrayList;
import java.util.Calendar;
import com.google.gson.Gson;

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
	
	public String getCommand() {
		return this.command;
	}
	public String getUsername() {
		return this.username;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public String toJSON() {
		Gson mygson = new Gson();
		return mygson.toJson(this);
	}
	
	public static MessagePost createMessagePost(String message, ArrayList<MessagePost> messageLog) throws Exception {
		String outputUsername, outputMessage;
		Gson gson = new Gson();
		
		try {
			MessagePost newMessagePost = gson.fromJson(message, MessagePost.class);
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
		messageLog.add(messagePost);
		return messagePost;
	}
}
