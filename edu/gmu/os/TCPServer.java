/*
 *  Name: Gaurav Desai
 *  G number: G00851337
 */

package edu.gmu.os;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TCPServer {
	
	public static void main(String[] args) throws IOException{
		
		String phoneBookName = args[0];
        int clientPort = args[1].equalsIgnoreCase("null")?0:Integer.parseInt(args[1]);
        int childrenPort = args[2].equalsIgnoreCase("null")?0:Integer.parseInt(args[2]);
        int parentPort = args[3].equalsIgnoreCase("null")?0:Integer.parseInt(args[3]);
        int[] ports = {clientPort,childrenPort};
        String currentNode = null;

        Selector selector = Selector.open();
		ServerSocketChannel serverSocketChannel = null;
		
		for (int port:ports) {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); 
		}
		System.out.println("\n@TCP Server: Started... \n");
		System.out.println("@TCP Server: Waiting for connections...\n\n");
		System.out.println("----------------------------------------------------------------");
        while (true) {
			selector.select();
			Iterator it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey selKey = (SelectionKey) it.next();
				it.remove();
				if (selKey.isAcceptable()) {
					ServerSocketChannel ssChannel = (ServerSocketChannel) selKey.channel();
					SocketChannel client = ssChannel.accept();
					Socket socket = null;
					if (client != null) {
						socket = client.socket();
						if(socket.getLocalPort()==childrenPort)
							currentNode = "child";
						else
							currentNode = "client";
						
						System.out.println("@TCP Server: Connected to "+ currentNode + " (port:"+ socket.getLocalPort() + ")...\n");
						if (socket != null) {
							Thread t = new Thread(new ClientSocketThreadUtility(socket, phoneBookName,parentPort,childrenPort));
							t.start();
						}
					}
				}
			}
		}
	 }
}

class ClientSocketThreadUtility implements Runnable{
	
	private Socket clientSocket;
	private String phoneBookName;
	private int parentPort;
	private int childrenPort;
	
	public ClientSocketThreadUtility(Socket socket, String fileName, int port, int cPort){
		clientSocket = socket;
		phoneBookName = fileName;
		parentPort = port;
		childrenPort = cPort;
	}
	
	public void run(){
        String results = null;
        String textToSearch = "";
        String currentNode = null;
        
		try{
	        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
	        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
			
			do {
				textToSearch = (String)ois.readObject();
				
				if(clientSocket.getLocalPort()==childrenPort)
					currentNode = "child";
				else
					currentNode = "client";
				
				System.out.println("@TCP Server: Received query from "+ currentNode + " (port:"+ clientSocket.getLocalPort() + ")...\n");
				System.out.println("@TCP Server: Processing query from "+ currentNode + " (port:"+ clientSocket.getLocalPort() + ")...\n");
		        
				results = checkPhonebookAndReturnResults(textToSearch, phoneBookName);
				results = results.trim();
				Socket pclientSocket = null;
				
		        if(results.equals("")){
		        	if(parentPort!=0){
		        		
		        		pclientSocket = new Socket(InetAddress.getLocalHost(), parentPort);
				        ObjectOutputStream poos = new ObjectOutputStream(pclientSocket.getOutputStream());
				        ObjectInputStream pois = new ObjectInputStream(pclientSocket.getInputStream());
	
				        //System.out.println("----------------------------------------------------------------");
				        System.out.println("@TCP Server: Records not found in current server (port:"+ clientSocket.getLocalPort() +")... \n");
				        System.out.println("@TCP Server: Query forwarded to parent (port:"+ parentPort +")... \n");
				        poos.writeObject(textToSearch);
				        
				        results = (String) pois.readObject();
				        System.out.println("@TCP Server: Reply received from parent (port:"+ parentPort +")... \n");
				        System.out.println("@TCP Server: Reply sent back to client (port:"+ clientSocket.getLocalPort() +")... \n");
				        oos.writeObject(results);
						System.out.println("----------------------------------------------------------------");
					
		        	}else{
		        		
		        		System.out.println("@TCP Server: Records not found for query("+textToSearch+") in current server (port:"+ clientSocket.getLocalPort() +")... \n");
		        		System.out.println("@TCP Server: Root node reached...Query cant be forwarded to parent... \n");
			        	System.out.println("@TCP Server: Reply sent to port "+clientSocket.getLocalPort()+"...\n");
			        	oos.writeObject(results);
			        	System.out.println("----------------------------------------------------------------");
		        	}

		        }else{
		        	System.out.println("@TCP Server: Records found for query("+textToSearch+")...\n");
		        	System.out.println("@TCP Server: Reply sent to port "+clientSocket.getLocalPort()+"...\n");
		        	oos.writeObject(results.toString());
		        	System.out.println("----------------------------------------------------------------");

		        }
		        
		        
			} while(!textToSearch.equalsIgnoreCase("exit"));
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static String checkPhonebookAndReturnResults(String textToSearch, String phoneBookName) throws IOException{
		String[] strArr;
		String firstName = null;
		String lastName = null;
		
		if(textToSearch.contains(" ")){
			strArr = textToSearch.split(" ");
			firstName = strArr[0];
			lastName = strArr[1];
		}
		
        BufferedReader br = new BufferedReader(new FileReader(phoneBookName));
        StringBuffer results = new StringBuffer();
        String lastLine = null;
        
        try {
            String line = br.readLine();
            while (line != null) {
                	if(!line.equals("")){
                		if(lastName!=null){
	                		if(firstName.equalsIgnoreCase(line) || firstName.equals("*")){
	                			String nextLine = br.readLine();
	                			if(lastName.equalsIgnoreCase(nextLine) || lastName.equals("*")){
			                		results.append("\t\tFirst Name: " + line + "\n"); 
			                		results.append("\t\tLast Name : " + nextLine + "\n"); 
			                		line = br.readLine();
			                		results.append("\t\tYear      : " + line + "\n");
			                		results.append("\t\t---------------------\n");
	                			}else{
	                				line = br.readLine();
	                			}
	                		}else{
	                			line = br.readLine();
	                			line = br.readLine();
	                		}
                		}else{
                			if(line.equalsIgnoreCase(textToSearch)){
                            	if(lastLine.equals("")){
                            		results.append("\t\tFirst Name: " + line + "\n"); line = br.readLine();
                            		results.append("\t\tLast Name : " + line + "\n"); line = br.readLine();
                            		results.append("\t\tYear      : " + line + "\n");
                            		results.append("\t\t---------------------\n");
                            	}else{
                            		results.append("\t\tFirst Name: " + lastLine + "\n");
                            		results.append("\t\tLast Name : " + line + "\n"); line = br.readLine();
                            		results.append("\t\tYear      : " + line + "\n");
                            		results.append("\t\t---------------------\n");
                            	}
                            }
                		}
                	}
                    lastLine = line;
                    line = br.readLine();
            }
        } finally {
            br.close();
        }
		return results.toString();
	}
}