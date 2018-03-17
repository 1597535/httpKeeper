/* ----------------------
 * Implements the httpKeeper interface.
 * last update: 17/3/2018
 * ---------------------- */

/* This class implements the interface httpKeeper.
 * It use the HTTP connection type close.
 */

/* HTTP implements:
 *  * Host: <host>
 *  * Connection: close
 *
 * POST implements:
 *  * Content-Length: <length>
 *  * Content-Type: application/x-www-from-urlencoded
 */

import java.net.Socket;
import java.io.OutputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.IOException;

public class httpKeeperClose implements httpKeeper
{
	protected String host;
	protected int port;
	
	protected String recvStatus;
	protected String[] recvHeaders;
	protected String recvBody;
	
	// Constructor
	public httpKeeperClose(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	// Sending HTTP GET request
	public void requestGET (String file, String[] headers) throws IOException {
		// If the user forgot to start with '/', we will help him
		if (!file.startsWith("/"))
			file = "/" + file;
			
		String GET = "GET " + file + " HTTP/1.1\r\n" +
					"Host: " + host + "\r\n" +
					"Connection: close\r\n";
		
		for (String header: headers)
			GET += header + "\r\n";
		GET += "\r\n";
		
		formatIN_OUT (GET.getBytes());
	}

	// Sending HTTP HEAD request
	public void requestHEAD (String file, String[] headers) throws IOException {
		// If the user forgot to start with '/', we will help him
		if (!file.startsWith("/"))
			file = "/" + file;
			
		String HEAD = "HEAD " + file + " HTTP/1.1\r\n" +
					"Host: " + host + "\r\n" +
					"Connection: close\r\n";
		
		for (String header: headers)
			HEAD += header + "\r\n";
		HEAD += "\r\n";
		
		formatIN_OUT (HEAD.getBytes());
	}

	// Sending HTTP POST request
	public void requestPOST (String file, String[] headers, String[] parameters) throws IOException {
		// If the user forgot to start with '/', we will help him
		if (!file.startsWith("/"))
			file = "/" + file;
		
		String body = "";
		for (String parameter: parameters)
			body += (body.isEmpty() ? "" : "&") + parameter;
		
		String POST = "POST " + file + " HTTP/1.1\r\n" +
					"Host: " + host + "\r\n" +
					"Connection: close\r\n" +
					"Content-Length: " + body.length() + "\r\n" +
					"Content-Type: application/x-www-from-urlencoded\r\n";
		
		for (String header: headers)
			POST += header + "\r\n";
		POST += "\r\n";
		
		POST += body;
		
		formatIN_OUT (POST.getBytes());
	}
	
	// Sends the Data and format the output
	protected void formatIN_OUT (byte[] toSend) throws IOException {
		// Connect to the server
		Socket socket = new Socket(host, port);
		
		// Send the data
		OutputStream outputStream = socket.getOutputStream();
		outputStream.write (toSend);
		outputStream.flush ();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		recvStatus = reader.readLine();
		
		String headers = "";
		for (String line = reader.readLine(); line != null && !line.isEmpty(); line = reader.readLine())
			headers += line + "\r\n";
		
		recvHeaders = headers.split("\r\n");
		recvBody = "";
		for (String line = reader.readLine(); line != null; line = reader.readLine())
			recvBody += line + "\n";
		
		socket.close();
	}
	
	// Receive HTTP stutus respond
	public String respondStatus () {
		return recvStatus;
	}

	// Receive HTTP headers respond
	public String[] respondHeaders () {
		return recvHeaders;
	}

	// Receive HTTP body respond
	public String respondBody () {
		return recvBody;
	}
	
}