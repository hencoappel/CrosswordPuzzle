package sockets;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CrosswordServer implements Runnable {
	ServerSocket serverSocket;
	final ArrayList<Socket> clientSockets = new ArrayList<Socket>();
	final Map<Socket, PrintWriter> outStreams = new HashMap<Socket, PrintWriter>();
	final Map<Socket, BufferedReader> inStreams = new HashMap<Socket, BufferedReader>();
	
	public CrosswordServer() {
		try {
			serverSocket = new ServerSocket(1292);
		} catch (IOException e) {
			System.out.println("Socket in use");
			System.exit(1);
		}
	}
	
	public static void main(String[] args) throws IOException {
		new Thread(new CrosswordServer()).start();
	}
	
	@Override
	public void run() {
		while (true) {
			Socket s = null;
			try {
				s = serverSocket.accept();
			} catch (IOException e) {
			}
			
			newClient(s);
			System.out.println("Socket added");
		}
	}
	
	void newClient(Socket s) {
		clientSockets.add(s);
		try {
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			outStreams.put(s, out);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			inStreams.put(s, in);
			new Thread(new ClientsRead(s)).start();
		} catch (IOException e) {
		}
	}
	
	class ClientsRead implements Runnable {
		BufferedReader in;
		Socket s;
		
		public ClientsRead(Socket s) {
			this.s = s;
			in = inStreams.get(s);
		}
		
		@Override
		public void run() {
			String line = "";
			try {
				while ((line = in.readLine()) != null) {
					System.out.println("recieved" + line);
					broadcast(line, s);
				}
				System.out.println("Stop");
			} catch (IOException e) {
			}
		}
	}
	
	private void broadcast(String line, Socket s2) {
		for (Socket s : outStreams.keySet()) {
			if (!s.equals(s2))
				outStreams.get(s).println(line);
		}
	}
	
}