Name Gaurav Desai
Course CS571


1) Problem statement can be found in 'Assignment.pdf'

2) Project Description can be found in 'Project Implementation Details.pdf'

3) Basic Instructions:

	- Programs should be tested on mason
	- Execution instruction:
		TCP Server: 
		javac edu/gmu/os/TCPServer.java
		java edu.gmu.os.TCPServer ./serverA.txt 3333 5553 NULL
		
		TCP Client: 
		javac edu/gmu/os/TCPClient.java
		java edu.gmu.os.TCPClient 3333
		
		UDP Server:
		javac edu/gmu/os/UDPServer.java
		java edu.gmu.os.UDPServer ./serverA.txt 3333 5553 NULL
		
		UDP Client:
		javac edu/gmu/os/UDPServer.java
		java edu.gmu.os.UDPServer 3333
		
	- Known Problems:
		- TCP server and client are working perfectly and according to me it works as expected in all scenarios
		- UDP server and client are not working as i expect them to be. I know the main problem but could not solve it.
		  In UDPServer, Selector which i used for supporting concurrency, waits for the channel to be readable and continue through which is
		  fine but it will always find the channel to be readable which is also true but that is making it to create bunch of thread
		  and failing. This was not the case with TCPServer as selector has an option to select a channel only when a new connection is ready 
		  whereas in UDPServer only readable and writable were the options and could not find any way to limit the thread creation to only
		  no of client/child request.
