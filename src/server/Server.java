package server;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.io.*;
import com.google.gson.Gson;

/**
 * Server.java - class representing the main server
 * 
 * @author knownastron
 *
 */
public class Server {

	private ServerSocketChannel server = null;
	private int port;
	private HashMap<String, Room> chatRooms;
	private Gson gson = new Gson();
	
	
	public Server(int port) throws IOException {
		this.port = port;
		this.server = ServerSocketChannel.open();
		server.bind(new InetSocketAddress(this.port));
		this.chatRooms = new HashMap<>();
	}
	
	
	/**
	 * runs the server by creating a socket and listening for client requests
	 * returns HTTP GET requests or initializes handshake when header contains Sec-WebSocket-Key
	 */
	public void run()  {
		try {
			while (true) {
				SocketChannel clientSocket = acceptClient(server);
				new Thread(()-> {
					try {
						HashMap<String, String> headerMap = HTTPrequest.parseHeader(clientSocket);
						// assumes that if "Sec-WebSocket-Key" is in the header, it is a Websocket handshake request
						if (headerMap.containsKey("Sec-WebSocket-Key")) {
							
							// Handshakes with client and opens WebSocket
							HTTPresponse.handleHandshakeResponse(clientSocket, headerMap.get("Sec-WebSocket-Key"));
							String message = ConnectedWebSocket.decodeMessage(clientSocket);
							
							// turns the message into a MessagePost for easy handling
							MessagePost inMessagePost = gson.fromJson(message, MessagePost.class); 
							
							if (inMessagePost.getCommand().equals("join")) {
								Room.joinRoom(clientSocket, inMessagePost.getMessage(), chatRooms);
							} else {
								System.out.println("command is not join");
								clientSocket.close();
							}
						} else if (headerMap.containsKey("GET")) {
							// basic handling of HTTP get requests
							String desiredFile = HTTPrequest.handleGetRequest(headerMap.get("GET"));
							HTTPresponse.handleResponse(clientSocket, desiredFile);
							clientSocket.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		Server muhServer = new Server(8888);
		muhServer.run();
	}
	
	
	/**
	 * Listens to client connection on server
	 * 
	 * @param server a ServerSocketChannel to listen for client
	 * @return
	 * @throws IOException
	 */
	private SocketChannel acceptClient(ServerSocketChannel server) throws IOException {
		System.out.println("Server listening on port " + port);
		SocketChannel socket = server.accept();
		System.out.println("Client connected!");
		return socket;
	}
}
