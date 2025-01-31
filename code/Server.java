import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
	// if I am in a GUI
	private ServerGUI sg;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;
	

	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */
	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerGUI sg) {
		// GUI or not
		this.sg = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		al = new ArrayList<>();
	}
	
	public void start() {
		keepGoing = true;
		/* create socket server and wait for connection requests */
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
                        outWhile:
			while(keepGoing) 
			{
				// format message saying we are waiting for users to connect:
				display("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();  	// accept connection
				// if I was asked to stop
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);  // make a thread of it
                                if(t.getuserName().contains("<")||t.getuserName().contains(">"))
					{
						t.getObjectOutputStream().writeObject("user name has > or < in the name. please remove them.\n");
						sg.appendEvent("cannot accept "+t.getuserName()+", user name have forbiden chars.\n");
						t.close();
						continue;
					}
                            for (ClientThread al1 : al) {
                                if (al1.getuserName().equals(t.getuserName())) {
                                    t.getObjectOutputStream().writeObject("user name is already taken, choose another one\n");
                                    sg.appendEvent("there is already someone that his nickname his "+t.getuserName()+", disconnect from the new one.\n");
                                    t.close();
                                    continue outWhile;
                                }
                            }
				al.add(t);									// save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
                                        tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						
					}
				}
			}
			catch(Exception e) {
				sg.appendEvent("Could not close all connections available: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
                       String msg = sdf.format(new Date()) + " Could not open the server: " + e + "\n";
			sg.appendEvent(msg);
		}
	}		
    /*
     * For the GUI to stop the server
     */
	protected void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement 
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			
		}
	}
	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	/*
	 *  to broadcast a message to all Clients
	 */
        
	private synchronized void broadcast(String message, ClientThread Source) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
                String sourceName = Source.username;
		// display message on console or GUI
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     // append in the room window
		
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
	if(message.charAt(sourceName.length()+2)=='<' && message.contains(">"))
		{

			String name;
			int i=sourceName.length()+3;
			while(message.charAt(i)!='>')
			{
				i++;
			}
			name = message.substring(sourceName.length()+3, i);
			for(int j = al.size(); --j >= 0;) {
				ClientThread ct = al.get(j);
				if(ct.username.equals(name)){
					if(!ct.writeMsg(messageLf)) {
						al.remove(j);
						sg.appendEvent("Disconnected Client " + ct.username + " removed from list.");
                                                
						if(!Source.writeMsg(name + " has disconnect"))
						{
							for(int z = al.size(); --z >= 0;)
								if(Source == al.get(z))
								{
									al.remove(z);
									sg.appendEvent("Disconnected Client " + Source.username + " removed from list.");
                                                                        
								}
						}
						return;
					}
					else
					{
						if(!Source.writeMsg(messageLf))
						{
							for(int z = al.size(); --z >= 0;)
								if(Source == al.get(z))
								{
									al.remove(z);
									sg.appendEvent("Disconnected Client " + Source.username + " removed from list.");
								}
						}
						return;
					}

				}
			}
			//in a case that a specific name does not exist
			if(!Source.writeMsg(name + " is not exist"))
			{
				for(int z = al.size(); --z >= 0;)
					if(Source == al.get(z))
					{
						al.remove(z);
						sg.appendEvent("Disconnected Client " + Source.username + " removed from list.");
					}
			}
			sg.appendRoom("~~no one recived ^ beacuse the user name is'nt exist~~\n");
			return;
		}
if(message.charAt(sourceName.length()+2)=='<' && message.contains(">"))
		{

			String name;
			int i=sourceName.length()+3;
			while(message.charAt(i)!='>')
			{
				i++;
			}
			name = message.substring(sourceName.length()+3, i);
			for(int j = al.size(); --j >= 0;) {
				ClientThread ct = al.get(j);
				if(ct.username.equals(name)){
					if(!ct.writeMsg(messageLf)) {
						al.remove(j);
						sg.appendEvent("Disconnected Client " + ct.username + " removed from list.\n");
						if(!Source.writeMsg(name + " has disconnect"))
						{
							for(int z = al.size(); --z >= 0;)
								if(Source == al.get(z))
								{
									al.remove(z);
									sg.appendEvent("Disconnected Client " + Source.username + " removed from list.");
                                                                        
								}
						}
						return;
					}
					else
					{
						if(!Source.writeMsg(messageLf))
						{
							for(int z = al.size(); --z >= 0;)
								if(Source == al.get(z))
								{
									al.remove(z);
									sg.appendEvent("Disconnected Client " + Source.username + " removed from list.");
                                                                       
								}
						}
						return;
					}

				}
			}
			//in a case that a specific name does not exist
			if(!Source.writeMsg(name + " is not exist"))
			{
				for(int z = al.size(); --z >= 0;)
					if(Source == al.get(z))
					{
						al.remove(z);
						sg.appendEvent("Disconnected Client " + Source.username + " removed from list.");
                                          
					}
			}
			sg.appendRoom("~~no one recived ^ beacuse the user name is'nt exist~~\n");
			return;
		}
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			if(!ct.writeMsg(messageLf)) {
				al.remove(i);
				sg.appendEvent("Disconnected Client " + ct.username + " removed from list.");
                                
			}
		}
	}


	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	
	/*
	 *  To run as a console application just open a console window and: 
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified 
		int portNumber = 4444;
//		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;
                
            public void setsInput(ObjectInputStream sInput) {
            this.sInput = sInput;
        }

        public void setsOutput(ObjectOutputStream sOutput) {
            this.sOutput = sOutput;
        }

        public ObjectInputStream getsInput() {
            return sInput;
        }

        public ObjectOutputStream getObjectOutputStream() {
            return sOutput;
        }

        @Override
        public long getId() {
            return id;
        }

        public String getuserName() {
            return username;
        }

		// Constructore
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("\nException creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}

		// what will run forever
                @Override
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage();
                                

				// Switch on the type of message receive
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message,this);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
                                        String name= username;
                                        for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
                                               ct.writeMsg(name + " Disconnected Client .");
                                               //broadcast(name + " Disconnected Client \n.", ct); //another option
					}
                                        keepGoing = false;
                                     
					break;
				case ChatMessage.WHOISIN:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// scan all the users connected
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}

