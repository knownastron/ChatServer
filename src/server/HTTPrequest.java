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
public class HTTPrequest {
	/**
	 * 
	 * @param uri the URI in the HTTP request
	 * @return the path to the file requested in the URI
	 * @throws IOException
	 */
	public static String handleGetRequest(String uri) throws IOException{	
		String path = "";
		path = HTTPrequest.getFilePath(uri);
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
	 * Parses a HTTP request header into a map of header:value
	 * 
	 * @param headerMap a map to store the map of the header to value
	 * @param clientSocket the SocketChannel to receive HTTP header
	 * @return the HTTP header put into a map
	 * @throws IOException
	 */
	public static HashMap<String, String> parseHeader(SocketChannel clientSocket) throws IOException {
		HashMap<String, String> headerMap = new HashMap<>();
		
		Scanner in = new Scanner(clientSocket.socket().getInputStream());
		if (in.hasNext()) {
			String firstLine = in.nextLine();
			String[] splitLine = firstLine.split(" ");
			headerMap.put(splitLine[0], splitLine[1]);
			
			String scan;
			while ((scan = in.nextLine()) != null) {
				if (scan.isEmpty()) {
					break;
				}
				
				splitLine = scan.split(": ");
				headerMap.put(splitLine[0], splitLine[1]);
			}
		}
		return headerMap;
	}
}
