package server;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.*;
import java.util.*;

import com.google.gson.Gson;

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
	
	
	public String getName() {
		return this.roomName;
	}

	
	public synchronized void addClient(SocketChannel clientSocket) throws IOException {
		connectedClients.add(clientSocket);
		socketsToRegister.add(clientSocket);
		sendBacklog(clientSocket);
		sel.wakeup();
	}

	
	public void listen() throws Exception {
		while (true) {
			// this should happen after handshake
			sel.select();
			Set<SelectionKey> keySet = sel.selectedKeys();
			Iterator<SelectionKey> it = keySet.iterator();
			decodeMessageAndBroadcast(it);
			registerSockets();
		}
	}
	
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
				MessagePost newMessagePost = MessagePost.createMessagePost(message, messageLog);
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
	
	
	private void sendBacklog(SocketChannel clientSocket) throws IOException {
		for (MessagePost message : messageLog) {
			ConnectedWebSocket.sendMessage(clientSocket, message);
		}
	}
	
	
	private synchronized void removeClient(SocketChannel clientSocket) {
		connectedClients.remove(clientSocket);
	}
	
	
	private synchronized void registerSockets() throws IOException {
		for (SocketChannel sc : socketsToRegister) {
			sc.configureBlocking(false);
			sc.register(sel, SelectionKey.OP_READ);
		}
		socketsToRegister.clear();
	}
	
	
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
	
	
	public static void joinRoom(SocketChannel clientSocket, HashMap<String, Room> chatRooms) throws IOException {
		String message = ConnectedWebSocket.decodeMessage(clientSocket);
		System.out.println(message);
		Gson gson = new Gson();
		MessagePost inMessagePost = gson.fromJson(message, MessagePost.class);
		addClientToRoom(inMessagePost, clientSocket, chatRooms);
	}
	
	
	
	private static synchronized void addClientToRoom(MessagePost inMessagePost, SocketChannel clientSocket, HashMap<String, Room> chatRooms) throws IOException {
		String roomToJoin = inMessagePost.getMessage();
		boolean roomExists = false;
		
		// check if room exists, adds client to room if true
		if (inMessagePost.getCommand().equals("join")) {
			if (chatRooms.containsKey(roomToJoin)) {
				System.out.println("joining existing room");
				roomExists = true;
				chatRooms.get(roomToJoin).addClient(clientSocket);
			}
		
			// if room does not exist, create room, add client, and start thread	
			if (roomExists == false) {
				System.out.println("creating new room: " + roomToJoin);
				Room newRoom = new Room(roomToJoin);
				chatRooms.put(roomToJoin, newRoom);
				newRoom.addClient(clientSocket);
				new Thread(()-> {
					try {
						newRoom.listen();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
			}
		} else {
			System.out.println("command is not join");
			clientSocket.close();
		}
	}
}
