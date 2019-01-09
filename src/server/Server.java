package server;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.io.*;

public class Server {

	private ServerSocketChannel server = null;
	private int port;
	private HashMap<String, Room> chatRooms;
	
	
	public Server(int port) throws IOException {
		this.port = port;
		this.server = ServerSocketChannel.open();
		server.bind(new InetSocketAddress(this.port));
		this.chatRooms = new HashMap<>();
	}
	
	public void run()  {
		
		try {
			while (true) {
				SocketChannel clientSocket = acceptClient(server);
				new Thread(()-> {
					HashMap<String, String> headerMap = new HashMap<>();
					try {
						parseHeader(headerMap, clientSocket);
						
						if (headerMap.containsKey("Sec-WebSocket-Key")) {
							// Handshakes with client and opens WebSocket
							HTTPresponse.handleHandshakeResponse(clientSocket, headerMap.get("Sec-WebSocket-Key"));
							System.out.println("About to enter Join room");
							Room.joinRoom(clientSocket, chatRooms);

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
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private SocketChannel acceptClient(ServerSocketChannel server) throws IOException {
		System.out.println("Server listening on port " + port);
		SocketChannel socket = server.accept();
		System.out.println("Client connected!");
		return socket;
	}
	
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
	
	private HashMap<String, String> parseHeader(HashMap<String, String> headerMap, SocketChannel clientSocket) throws IOException {
		Scanner in = new Scanner(clientSocket.socket().getInputStream());
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
		return headerMap;
	}
	

	
}
