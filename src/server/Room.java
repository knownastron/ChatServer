package server;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.*;
import java.util.*;

import com.google.gson.Gson;

/**
 * This Room class represents a chat room within the chat app
 * 
 * @author knownastron
 *
 */
public class Room {
	private String roomName;
	private Selector sel;
	private ArrayList<SocketChannel> socketsToRegister;
	private ArrayList<SocketChannel> connectedClients;
	private ArrayList<MessagePost> messageLog;

	
	public Room(String name) throws IOException {
		this.roomName = name;
		this.sel = Selector.open();
		this.socketsToRegister = new ArrayList<>();
		this.connectedClients = new ArrayList<>();
		this.messageLog = new ArrayList<>();
	}
	
	
	/**
	 * Returns the name of the room
	 * 
	 * @return name of the current room
	 */
	public String getName() {
		return this.roomName;
	}

	
	/**
	 * Adds a new client to the current room
	 * @param clientSocket the client socket to connect to the room
	 * @throws IOException
	 */
	public synchronized void addClient(SocketChannel clientSocket) throws IOException {
		connectedClients.add(clientSocket);
		socketsToRegister.add(clientSocket);
		sendBacklog(clientSocket);
		sel.wakeup();
	}

	
	/**
	 * Listens for messages sent from the clients connected to the room
	 * 
	 * @throws Exception
	 */
	public void listen() throws Exception {
		while (true) {
			sel.select(); // blocks until at least one channel is ready for I/O 
			Set<SelectionKey> keySet = sel.selectedKeys();
			Iterator<SelectionKey> it = keySet.iterator();
			decodeMessageAndBroadcast(it);
			registerSockets();
		}
	}
	
	
	/**
	 * Decodes messages from channels that have sent a message, then broadcasts the decoded
	 * message to the other connected sockets in the room
	 * 
	 * @param it
	 * @throws IOException
	 */
	private void decodeMessageAndBroadcast(Iterator<SelectionKey> it) throws IOException {
		while (it.hasNext()) {
			SelectionKey key = it.next();
			it.remove();
			SocketChannel clientSocket = (SocketChannel) key.channel();
			key.cancel();
			clientSocket.configureBlocking(true);

			try {
				String message = ConnectedWebSocket.decodeMessage(clientSocket);
				System.out.println("MESSAGE RECEIVED! " + message);
				MessagePost newMessagePost = MessagePost.createMessagePost(message);
				MessagePost.addMessagePostToLog(newMessagePost, messageLog);
				clientSocket.configureBlocking(false);
				sel.selectNow();
				clientSocket.register(sel, SelectionKey.OP_READ);
				broadcastMessage(newMessagePost);
			} catch (Exception e) { // this exception happens when client leaves
				System.out.println("Client has disconnected");
				removeClient(clientSocket);
			} 
		}
	}
	
	
	/**
	 * Sends the previous messages in the room when a new client connects to the room
	 * 
	 * @param clientSocket the client that has connected to the room
	 * @throws IOException
	 */
	private void sendBacklog(SocketChannel clientSocket) throws IOException {
		for (MessagePost message : messageLog) {
			ConnectedWebSocket.sendMessage(clientSocket, message);
		}
	}
	
	
	/**
	 * removes a client socket from the room when the socket is disconnected
	 * @param clientSocket
	 */
	private synchronized void removeClient(SocketChannel clientSocket) {
		connectedClients.remove(clientSocket);
	}
	
	
	/**
	 * Registers sockets in the queue to the Channel Selector
	 * 
	 * @throws IOException
	 */
	private synchronized void registerSockets() throws IOException {
		for (SocketChannel sc : socketsToRegister) {
			sc.configureBlocking(false);
			sc.register(sel, SelectionKey.OP_READ);
		}
		socketsToRegister.clear();
	}
	
	
	/**
	 * Broadcasts a new message received from a socket to all other sockets registered to the Channel
	 * 
	 * @param newMessagePost a message in the format designated by the Message Post class
	 * @throws IOException
	 */
	private synchronized void broadcastMessage(MessagePost newMessagePost) throws IOException {
		for (SocketChannel socket : connectedClients) {
			SelectionKey curKey = socket.keyFor(sel);
			curKey.cancel();
			socket.configureBlocking(true);
			
			ConnectedWebSocket.sendMessage(socket, newMessagePost);
			
			socket.configureBlocking(false);
			sel.selectNow();
			socket.register(sel, SelectionKey.OP_READ);
		}
	}
	
	
	/**
	 * Activated when a join message is received from the socket.
	 * Adds the client socket to the roomName
	 * 
	 * @param clientSocket
	 * @param chatRooms
	 * @throws IOException
	 */
	public static void joinRoom(SocketChannel clientSocket, String roomName, HashMap<String, Room> chatRooms) throws IOException {
		boolean roomAlreadyExists = doesRoomExists(roomName, chatRooms); 
		
		if (roomAlreadyExists) {
			addClientToExistingRoom(roomName, clientSocket, chatRooms);
		} else {
			addClientToNewRoom(roomName, clientSocket, chatRooms);
		}
	}
	
	
	/**
	 * Checks if roomName already exists in chatRooms
	 * 
	 * @param roomName
	 * @param chatRooms
	 * @return true if roomName exists in chatRooms, false otherwise
	 */
	private static boolean doesRoomExists(String roomName, HashMap<String, Room> chatRooms) {
		if (chatRooms.containsKey(roomName)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Adds the clientSocket to the roomName in chatRooms
	 * 
	 * @param roomName
	 * @param clientSocket
	 * @param chatRooms
	 * @throws IOException
	 */
	private static synchronized void addClientToExistingRoom(String roomName, SocketChannel clientSocket, HashMap<String, Room> chatRooms ) throws IOException {
			System.out.println("joining existing room");
			chatRooms.get(roomName).addClient(clientSocket);
	}
	
	
	/**
	 * Creates a new Room and adds it to chatRooms. 
	 * Adds client socket to this newly created room. 
	 * 
	 * @param roomName
	 * @param clientSocket
	 * @param chatRooms
	 * @throws IOException
	 */
	private static synchronized void addClientToNewRoom(String roomName, SocketChannel clientSocket, HashMap<String, Room> chatRooms) throws IOException {
		System.out.println("creating new room: " + roomName);
		Room newRoom = new Room(roomName);
		chatRooms.put(roomName, newRoom);
		newRoom.addClient(clientSocket);
		new Thread(()-> {
			try {
				newRoom.listen();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
}
