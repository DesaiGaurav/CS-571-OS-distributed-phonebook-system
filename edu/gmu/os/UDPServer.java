/*
 *  Name: Gaurav Desai
 *  G number: G00851337
 */

package edu.gmu.os;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UDPServer {
	
	public static void main(String[] args) throws IOException{
		
		String phoneBookName = args[0];
        int clientPort = args[1].equalsIgnoreCase("null")?0:Integer.parseInt(args[1]);
        int childrenPort = args[2].equalsIgnoreCase("null")?0:Integer.parseInt(args[2]);
        int parentPort = args[3].equalsIgnoreCase("null")?0:Integer.parseInt(args[3]);
        int[] allPorts = {clientPort,childrenPort};
        String currentNode = null;
        
        Selector selector = Selector.open();
        DatagramChannel datagramChannel = null;
		
		for (int port:allPorts) {
			datagramChannel = DatagramChannel.open();
			datagramChannel.configureBlocking(false);
			datagramChannel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(),port));
			datagramChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE); 
		}
		
		System.out.println("\n@UDP Server: Started... \n");
		System.out.println("@UDP Server: Waiting for connections...\n\n");
		System.out.println("----------------------------------------------------------------");
		
        while (true) {

        	selector.select();
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			
			while (it.hasNext()) {
				SelectionKey selKey = (SelectionKey) it.next();
				it.remove();
				
				if (selKey.isReadable()) {
					DatagramChannel ssChannel = (DatagramChannel) selKey.channel();
					DatagramSocket socket = ssChannel.socket();
					
					if (socket != null) {
						if(socket.getLocalPort()==childrenPort)
							currentNode = "child";
						else
							currentNode = "client";
						System.out.println("@UDP Server: Connected to "+ currentNode + " (port:"+ socket.getLocalPort() + ")...\n");
						
						Thread t = new Thread(new ClientDatagramThreadUtility(ssChannel, phoneBookName,parentPort,childrenPort, clientPort));
						t.start();
					}
				}
				
			}
			
		}
	 }
}

class ClientDatagramThreadUtility implements Runnable{

	private DatagramChannel datagramChannel;
	private String phoneBookName;
	private int parentPort;
	private int childrenPort;
	private int clientPort;
    byte[] buffer = new byte[256];
    byte[] data = new byte[256];
    int MAX_PACKET_SIZE = 65507;
    ByteBuffer sendBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
    ByteBuffer receiveBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
	
	public ClientDatagramThreadUtility(DatagramChannel socket, String fileName, int port, int cPort, int clPort){
		datagramChannel = socket;
		phoneBookName = fileName;
		parentPort = port;
		childrenPort = cPort;
		clientPort = clPort;
	}
	
	public void run(){
        String results = "";
        String textToSearch = "";
        String currentNode = null;
        String temp = null;
        SocketAddress address;
		try {
			address = new InetSocketAddress(InetAddress.getLocalHost(),clientPort);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
		try{
			do {
		        
				do{
					address = datagramChannel.receive(receiveBuffer);
			        textToSearch = new String(receiveBuffer.array()).trim();
			        
		        }while(textToSearch.equals("") || address==null);
		        
				DatagramSocket clientSocket = datagramChannel.socket();
				if (clientSocket != null)
					if(clientSocket.getLocalPort()==childrenPort)
						currentNode = "child";
					else
						currentNode = "client";
				System.out.println("@UDP Server: Received query from "+ currentNode + " (port:"+ clientSocket.getLocalPort() + ")...\n");
				System.out.println("@UDP Server: Processing query from "+ currentNode + " (port:"+ clientSocket.getLocalPort() + ")...\n");
		        
				results = checkPhonebookAndReturnResults(textToSearch, phoneBookName);
				results = results.trim();
				
				DatagramSocket pclientSocket = new DatagramSocket();
				
		        if(results.equals("")){
		        	if(parentPort!=0){
		        		
				        System.out.println("@UDP Server: Records not found in current server (port:"+ clientSocket.getLocalPort() +")... \n");
				        System.out.println("@UDP Server: Query forwarded to parent (port:"+ parentPort +")... \n");

				        byte[] data = textToSearch.getBytes();
				        DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), parentPort);
				        pclientSocket.send(sendPacket);

				        do{
				        	datagramChannel.receive(receiveBuffer);
				        	temp = new String(receiveBuffer.array()).trim();
				        	results = results.concat(temp);
				        }while(temp.length()>0);
				        
				        System.out.println("@UDP Server: Reply received from parent (port:"+ parentPort +")... \n");
				        System.out.println("@UDP Server: Reply sent back to client (port:"+ clientSocket.getLocalPort() +")... \n");
				        
				        data = results.getBytes();
				        sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), parentPort);
				        pclientSocket.send(sendPacket);

						System.out.println("----------------------------------------------------------------");
					
		        	}else{
		        		
		        		System.out.println("@UDP Server: Records not found for query("+textToSearch+") in current server (port:"+ clientSocket.getLocalPort() +")... \n");
		        		System.out.println("@UDP Server: Root node reached...Query cant be forwarded to parent... \n");
			        	System.out.println("@UDP Server: Reply sent to port "+clientSocket.getLocalPort()+"...\n");

			        	byte[] data = textToSearch.getBytes();
				        DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), parentPort);
				        pclientSocket.send(sendPacket);
				        
			        	System.out.println("----------------------------------------------------------------");
		        	}

		        }else{
		        	System.out.println("@UDP Server: Records found for query("+textToSearch+")...\n");
		        	System.out.println("@UDP Server: Reply sent to port "+clientSocket.getLocalPort()+"...\n");

		        	sendBuffer = ByteBuffer.wrap(results.getBytes());
			        datagramChannel.send(sendBuffer,address);
		        	sendBuffer.clear();
			        
		        	System.out.println("----------------------------------------------------------------");
		        }
			} while(!textToSearch.equalsIgnoreCase("exit"));
			datagramChannel.close();
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
