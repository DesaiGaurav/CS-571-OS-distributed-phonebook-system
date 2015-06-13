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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPClient {
	
    public static void main(String[] args){
        
        String results = null;
        String textToSearch = null;
        int clientPort = args[0].equalsIgnoreCase("null")?0:Integer.parseInt(args[0]);
        
        try {

	        Socket client = new Socket(InetAddress.getLocalHost(), clientPort);
	        System.out.println("\n@TCP Client: Started...\n ");
	
	        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
	        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

	        Scanner scanner = new Scanner(System.in);
	        do {
	        	System.out.println("\n---------------------------------------------------------------------------------------------------------------------------\n");
	        	System.out.println("@TCP Client: Enter the query in following format or type 'exit' to exit: \n");
	        	System.out.println("             Format: '<First Name><space><Last Name>' (you can use '*' as wildcard character for first name or last name):\n");
	        	System.out.print("             Search: ");
	        	
		        textToSearch = scanner.nextLine();
		        oos.writeObject(textToSearch);
		        
		        System.out.println("\n             Search results for the keyword '"+textToSearch+"':\n");
		        System.out.print("\t\t---------------------\n\t\t");
		        results = (String)ois.readObject();
		        System.out.println(results.equals("")?"\t\tNo records found\n\t\t---------------------":results);
		        
	        }while(!textToSearch.equalsIgnoreCase("exit"));
	        
	        oos.writeObject(textToSearch);
	        System.out.println("@TCP Client: closing in 3 seconds...");
			Thread.sleep(3000);
			System.out.println("@TCP Client: closed...");
			System.exit(0);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}