/*
 *  Name: Gaurav Desai
 *  G number: G00851337
 */

package edu.gmu.os;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Scanner;

public class UDPClient {
	
    public static void main(String[] args){
        
        String results = "";
        String temp = "";
        String textToSearch = null;
        int clientPort = args[0].equalsIgnoreCase("null")?0:Integer.parseInt(args[0]);
        byte[] buffer = new byte[256];

        int MAX_PACKET_SIZE = 65507;
        ByteBuffer sendBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
        ByteBuffer receiveBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE);

        
        try {
            DatagramChannel datagramChannel = DatagramChannel.open();
            datagramChannel.connect(new InetSocketAddress(InetAddress.getLocalHost(), clientPort));
        	
	        System.out.println("\n@TCP Client: Started...\n ");
	        Scanner scanner = new Scanner(System.in);
	        do {
	        	System.out.println("\n---------------------------------------------------------------------------------------------------------------------------\n");
	        	System.out.println("@TCP Client: Enter the query in following format or type 'exit' to exit: \n");
	        	System.out.println("             Format: '<First Name><space><Last Name>' (you can use '*' as wildcard character for first name or last name):\n");
	        	System.out.print("             Search: ");
	        	
		        textToSearch = scanner.nextLine();

		        if(datagramChannel.isConnected()){
		        	sendBuffer = ByteBuffer.wrap(textToSearch.getBytes());
			        datagramChannel.write(sendBuffer);
			        
			        System.out.println("\n             Search results for the keyword '"+textToSearch+"':\n");
			        System.out.print("\t\t---------------------\n\t\t");
					do{
						datagramChannel.read(receiveBuffer);
						temp = new String(receiveBuffer.array()).trim();
				        results = results.concat(temp);
			        }while(temp.equals(""));
			        
			        System.out.println(results.equals("")?"\t\tNo records found\n\t\t---------------------":results);
		        }
	        }while(!textToSearch.equalsIgnoreCase("exit"));
	        
	        /*byte[] data = textToSearch.getBytes();
	        DatagramPacket sendPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), clientPort);
	        client.send(sendPacket);*/
	        
	        System.out.println("@TCP Client: closing in 3 seconds...");
			Thread.sleep(3000);
			System.out.println("@TCP Client: closed...");
			System.exit(0);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}