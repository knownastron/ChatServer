package server;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.channels.*;
import java.util.*;

/**
 * This class represents a connected WebSocket with associated methods that a connected
 * WebSocket would need
 * 
 * @author knownastron
 *
 */

public class ConnectedWebSocket {
	/**
	 * Decodes a WebSocket header and returns message in String format
	 *
	 * @param clientSocket the client socket
	 * @return the decoded message
	 * @throws IOException
	 */
	public static String decodeMessage(SocketChannel clientSocket) throws IOException {
		System.out.println("Entered decodeMessage function");
		DataInputStream in = new DataInputStream(clientSocket.socket().getInputStream());
		byte byte0 = in.readByte();
		byte byte1 = in.readByte();
		
		int payloadLen =  byte1 & 127;
		long messageLength;
		String decodedString = "";
		
		if (payloadLen <= 125) {
			messageLength = payloadLen;
		} else {
			int mask = ~(0xFFFFFF00);
			byte byte2 = in.readByte();
			byte byte3 = in.readByte();
			messageLength = mask & byte3;
			messageLength = messageLength | (byte2 << 8);
		} 
		
		byte[] decoded = new byte[(int)messageLength];
		byte[] encoded = new byte[(int)messageLength];
		byte[] key = new byte[4];
	
		// reads in next 4 bytes to set up the keys/masks
		for (int i = 0; i < 4; i++) {
			key[i] = in.readByte();
		}
	
		// reads in the rest of the bytes and stores them in encode
		in.read(encoded);
	
		// decodes the message
		for (int i = 0; i < encoded.length; i++) {
		    decoded[i] = (byte)(encoded[i] ^ key[i%4]);
		}
		
		// concatenates all the decoded letters
		for (int i = 0; i < decoded.length; i++) {
			decodedString += (char) decoded[i];
		}

		return decodedString;
	}

	/**
	 * Converts a message post into JSON format and sends it through the web socket
	 * 
	 * @param clientSocket
	 * @param messagePost
	 * @throws IOException
	 */
	public static void sendMessage(SocketChannel clientSocket, MessagePost messagePost) throws IOException {
		String message = messagePost.toJSON();		
		encodeAndSend(clientSocket, message);
	}

	/**
	 * Encodes the message into a WebSocket header and sends it through the connected websocket
	 * 
	 * @param clientSocket
	 * @param message
	 * @throws IOException
	 */
	private static void encodeAndSend(SocketChannel clientSocket, String message) throws IOException {
		OutputStream out = clientSocket.socket().getOutputStream();
		
		byte[] rawData = message.getBytes();
		int frameCount = 0;
		byte[] frame = new byte[10];
		
		frame[0] = (byte)129;
		
		if (rawData.length <= 125) {
			frame[1] = (byte) rawData.length;
			frameCount = 2;
		} else if (rawData.length >= 126 && rawData.length <= 65535) {
			frame[1] = (byte) 126;
			int length = rawData.length;
			frame[2] = (byte) (length >> 8 & ((byte) 255));
			frame[3] = (byte) (length & (byte)255);
			frameCount = 4;
		}
		
		int totalMessageLength = frameCount + rawData.length;
		
		byte[] replyMessage = new byte[totalMessageLength];
		
		int msgIndex = 0;
		for (int i = 0; i < frameCount; i++) {
			replyMessage[msgIndex] = frame[i];
			msgIndex++;
		}
		
		for (int i = 0; i < rawData.length; i++) {
			replyMessage[msgIndex] = rawData[i];
			msgIndex++;
		} 
		
		out.write(replyMessage);
		out.flush();
	}
}
