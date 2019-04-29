package server;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.channels.*;
import java.security.*;
import java.util.*;

/**
 * HTTPrequest.java - a class to handle HTTP creating and sending
 * 
 * @author knownastron
 *
 */
public class HTTPresponse {
	
	/**
	 * Creates a HTTP response to a GET request
	 * 
	 * @param socket SocketChannel to send response to
	 * @param pathToFile path to the file to be sent back to client
	 * @throws Exception
	 */
	public static void handleResponse(SocketChannel socket, String pathToFile) throws Exception {
		StringBuilder response = new StringBuilder();
		OutputStream out = socket.socket().getOutputStream();
		
		File file = new File(pathToFile);
		
		// to ensure path to requested file is in the resources folder only
		File resourceFile = new File("resources");
		String canonicalPath = file.getCanonicalPath();
		if (!canonicalPath.contains(resourceFile.getCanonicalPath())) {
			throw new Exception("Access denied");
		}

		if (file.exists()) {
			response.append("HTTP/1.1 200 OK" + "\r\n");
			response.append("Content-Length: " + file.length() + "\r\n");
			response.append("\r\n");

			InputStream fileInput = new FileInputStream(pathToFile);
			out.write(response.toString().getBytes());
			out.write(fileInput.readAllBytes());
			
			fileInput.close();
		} else {
			response.append("HTTP/1.1 404 Not Found" + "\r\n");
			out.write(response.toString().getBytes());
		}
		
		out.flush();
	}
	
	/**
	 * Takes the Sec-WebSocket-Key from the change protocol request and computes an appropriate Sec-WebSocket-Accept response
	 * @param clientSocket the socket that is connected
	 * @param key the Sec-WebSocket-Key from the change protocol request
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void handleHandshakeResponse(SocketChannel clientSocket, String key) throws NoSuchAlgorithmException, IOException {
		MessageDigest messageDigest;
		
		messageDigest = MessageDigest.getInstance("SHA-1");
		String appendedKey = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		byte[] data = appendedKey.getBytes("UTF-8");  
		byte[] digest = messageDigest.digest(data);
		String base64String = Base64.getEncoder().encodeToString(digest);
		
		StringBuilder response = new StringBuilder();
		OutputStream out = clientSocket.socket().getOutputStream();
		
		response.append("HTTP/1.1 101 Switching Protocols" + "\r\n" +
						"Upgrade: websocket" + "\r\n" +
						"Connection: Upgrade" + "\r\n" +
						"Sec-WebSocket-Accept: " + base64String + "\r\n" +
						"\r\n");
		
		out.write(response.toString().getBytes());
		out.flush();
		System.out.println("WEBSOCKET CONNECTED");
	}
	
	/**
	 * Sends a "400 BAD REQUEST" HTTP response to socket
	 * 
	 * @param socket
	 */
	private void sendBadOutput(Socket socket) {
		try {
			StringBuilder response = new StringBuilder();
			OutputStream out = socket.getOutputStream();
			response.append("400 BAD REQUEST\n\n");
			out.write(response.toString().getBytes());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


