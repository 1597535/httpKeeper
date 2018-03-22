/* ----------------------
 * Implements the httpKeeper interface.
 * last update: 22/3/2018
 * ---------------------- */

/* This class implements the interface HttpKeeper.
 * It use the HTTP connection type close.
 * Note that it only abstruct class that prepare the request data.
 */

/* HTTP implements:
 *  * Host: <host>
 *  * Connection: close
 */

import java.io.IOException;

public abstract class KeeperClose implements HttpKeeper
{
	protected String host;
	protected int port;
	
	// Constructor
	public KeeperClose(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	// ============================== Request Section ============================== //
	// Make basic HTTP request (without body).
	protected String basicRequest (String Request, String file, String[] headers) {
		// If the user forgot to start with '/', we will help him
		if (!file.startsWith("/")) file = "/" + file;
		
		StringBuilder request = new StringBuilder(Request);
		request.append(" " + file + " HTTP/1.1\r\n");
		request.append("Host: " + host + "\r\n");
		request.append("Connection: close\r\n");
		
		for (String header: headers)
			request.append(header + "\r\n");
		
		request.append("\r\n");
		
		return request.toString();
	}
	
	// Sending HTTP GET request
	public void requestGET (String file, String[] headers) throws IOException {
		String request = basicRequest("GET", file, headers);
		send (request.getBytes());
	}

	// Sending HTTP HEAD request
	public void requestHEAD (String file, String[] headers) throws IOException {
		String request = basicRequest("HEAD", file, headers);
		send (request.getBytes());
	}

	// Sending HTTP POST request
	public void requestPOST (String file, String[] headers, byte[] body) throws IOException {
		String request = basicRequest("POST", file, headers);
		
		// Connect two byte arrays
		byte[] requestB = request.getBytes();
		byte[] data = new byte[requestB.length + body.length];
		
		for (int i = 0; i < requestB.length; i++)
			data[i] = requestB[i];
		
		for (int i = 0; i < body.length; i++)
			data[requestB.length + i] = body[i];
		
		send (data);
	}
	
	
	// ============================== Network Section ============================== //
	// Send & recive the data
	public abstract void send (byte[] data) throws IOException;
	
	
	// ============================== Respond Section ============================== //
	// Receive HTTP stutus respond
	public abstract String respondStatus ();
	
	// Receive HTTP headers respond
	public abstract String[] respondHeaders ();
	
	// Receive HTTP body respond
	public abstract byte[] respondBody ();
	
}