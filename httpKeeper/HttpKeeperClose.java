/* ----------------------
 * Extends the KeeperClose class
 * and implements the HttpKeeper interface.
 * last update: 22/3/2018
 * ---------------------- */

/* This class add the network layer
 * to the abstract KeeperClose class.
 * It does not do any HTTP extends.
 */

/* HTTP implements (KeeperClose):
 *  * Host: <host>
 *  * Connection: close
 */

import java.net.Socket;
import java.util.*;
import java.io.*;

public class HttpKeeperClose extends KeeperClose implements HttpKeeper
{
	/* ~ Reminder from httpKeeperClose :
	 * protected String host;
	 * protected int port;
	 */
	
	protected String recvStatus;
	protected String[] recvHeaders;
	protected byte[] recvBody;
	
	protected long timeout;
	
	// Constructor
	public HttpKeeperClose(String host, int port) {
		super (host, port);
		
		recvStatus = "";
		recvHeaders = new String[0];
		recvBody = new byte[0];
		
		timeout = 100000L;	// 10sec.
	}
	
	// ============================== Network Section ============================== //
	// Send & recive the data
	public void send (byte[] data) throws IOException {
		/* Connect to the client */
		Socket socket = new Socket (host, port);
		
		/* Send the data (request) */
		OutputStream output = socket.getOutputStream();
		output.write (data);
		output.flush ();
		
		long startTime = System.currentTimeMillis();
		
		/* Reades the headers */
		InputStream input = socket.getInputStream();
		byte[] b = new byte[4096];
		int totalReaded = 0, readed = 0;
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		
		String headers = "";
		while (!headers.contains ("\r\n\r\n")) {	// double CRLF indicates the end of the message
			if ((readed = input.read(b)) > 0) {
				totalReaded += readed;
				headers += (new String(b, 0, readed));
			}
			// Just in case.
			if ((System.currentTimeMillis() - startTime) > timeout)
				throw new IOException ("Timeout while waiting for " + host + " to respond");
		}
		
		/* Not everything is a header, right? */
		headers = headers.substring(0, headers.indexOf("\r\n\r\n"));
		int isNotHeadar = totalReaded - headers.getBytes().length - 4;
		body.write (b, readed - isNotHeadar, isNotHeadar);
		
		/* Formats the status and the headers */
		recvStatus = headers.substring(0, headers.indexOf("\r\n"));
		recvHeaders = headers.substring(headers.indexOf("\r\n")).split("\r\n");
		
		/* check if the size specified */
		int dataSize = -1;
		for (String header: recvHeaders)
			// Content-Length: x
			if (header.contains(":") && header.substring(0, header.indexOf(":")).equals("Content-Length"))
				dataSize = Integer.parseInt(header.substring(header.indexOf(":") + 2));
		
		
		if (dataSize == -1) { // Not size specified :(
			while (!socket.isClosed()) { // wait like a good boy
				if ((readed = input.read(b)) > 0)
					body.write (b, 0, readed);
				// Just in case.
				if ((System.currentTimeMillis() - startTime) > timeout)
					throw new IOException ("Timeout while waiting for " + host + " to close");
			}
		} else if (dataSize > 0) { // Size specified
			while (body.size() < dataSize) { // till everything readed
				if ((readed = input.read(b)) > 0)
					body.write (b, 0, readed);
				// Just in case.
				if ((System.currentTimeMillis() - startTime) > timeout)
					throw new IOException ("Timeout while waiting for " + host + " to send the body");
			}
		}
		
		recvBody = body.toByteArray();
	}
	
	// ============================== Respond Section ============================== //
	// Receive HTTP stutus respond
	public String respondStatus () {
		// String is imortal
		return recvStatus;
	}
	
	// Receive HTTP headers respond
	public String[] respondHeaders () {
		// Client sould NOT change its recived data.
		String[] retn = new String[recvHeaders.length];
		int i = 0;
		
		for (String header: recvHeaders)
			retn[i++] = header;
		
		return retn;
	}
	
	// Receive HTTP body respond
	public byte[] respondBody () {
		// Client sould NOT change its recived data.
		byte[] retn = new byte[recvBody.length];
		int i = 0;
		
		for (byte b: recvBody)
			retn[i++] = b;
		
		return retn;
	}
	
	// ============================== *** TESTER *** ============================== //
	public static void main (String[] arg) throws IOException {
		HttpKeeperClose hKCH = new HttpKeeperClose("www.example.org", 80);
		hKCH.requestGET ("/", new String[0]);
		
		System.out.println (hKCH.respondStatus());
		
		for (String header: hKCH.respondHeaders())
			System.out.println (header);
		
		System.out.println (new String(hKCH.respondBody()));
	}
}
