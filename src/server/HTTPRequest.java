package server;
import java.util.*;
import java.io.IOException;
import java.net.*;
import java.nio.channels.SocketChannel;

/**
 * HTTPrequest.java - a class for decoding HTTP request headers and handling GET requests
 * 
 * @author knownastron
 *
 */
public class HTTPRequest {
	HashMap<String, String> headerMap;
	
	public HTTPRequest() {
		this.headerMap = new HashMap<String,String>();
	
	}
	
	/**
	 * 
	 * @param uri the URI in the HTTP request
	 * @return the path to the file requested in the URI
	 * @throws IOException
	 */
	public static String handleGetRequest(String uri) throws IOException{	
		String path = "";
		path = HTTPRequest.getFilePath(uri);
		return path;
	}
	
	/**
	 * Prepends the path to the desired file requested in the URI
	 * 
	 * @param uri the URI in the HTTP request
	 * @return
	 */
	private static String getFilePath(String uri) {
		if (uri.equals("/")) {
			uri = "./resources/index.html";
		} else {
			uri = "./resources" + uri;
		}
		
		return uri;
	}
	

	/**
	 * Parses a HTTP request header into a HashMap class instance object
	 * 
	 * @param clientSocket the SocketChannel to read the HTTP header from
	 * @throws IOException
	 */
	public void parseMessageFromSocket(SocketChannel clientSocket) throws IOException {
		Scanner in = new Scanner(clientSocket.socket().getInputStream());
		if (in.hasNext()) {
			String firstLine = in.nextLine();
			String[] splitLine = firstLine.split(" ");
			this.headerMap.put(splitLine[0], splitLine[1]);
			
			String scan;
			while ((scan = in.nextLine()) != null) {
				if (scan.isEmpty()) {
					break;
				}
				
				splitLine = scan.split(": ");
				this.headerMap.put(splitLine[0], splitLine[1]);
			}
		}
	}
	
	/**
	 * returns the web socket key that was read from the HTTP request
	 * 
	 * @return the web socket key
	 */
	public String getWebSocketKey() {
		return this.headerMap.get("Sec-WebSocket-Key");
	}
	
	/**
	 * returns the requested file from the HTTP GET request
	 * 
	 * @return the requested file
	 */
	public String getRequestedFilePath() {
		return this.headerMap.get("GET");
	}
	
	/**
	 * returns true if the HTTP request is a WebSocket handshake request, false otherwise
	 * assumes that if "Sec-WebSocket-Key" is in the HTTP header, it is a Websocket handshake request
	 * 
	 * @return 
	 */
	public boolean isWebSocketHandshakeRequest() {
		return this.headerMap.containsKey("Sec-WebSocket-Key");
	}
	
	/**
	 * returns true if the HTTP request is a HTTP "GET" request, false otherwise
	 * 
	 * @return 
	 */
	public boolean isGetRequest() {
		return this.headerMap.containsKey("GET");
	}
	
}
