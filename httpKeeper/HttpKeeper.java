/* ----------------------
 * Interface for basic http communication.
 * last update: 22/3/2018
 * ---------------------- */

/* A interface for classes to follow for proper web HTTP communication.
 * The implemet classes can and should extend the HTTP features.
 */

package httpKeeper;

// The network communication should return Exception to the user.
import java.io.IOException;

public interface HttpKeeper
{
	/*
	 * The Constructor should specify the host and the port
	 * of the traget server.
	 */
	
	
	// ============================== Request Section ============================== //
	// Sending HTTP GET request
	public void requestGET (String file, String[] headers) throws IOException;
	/* GET request is the most basic and common HTTP request.
	 * It asks for file from the server, without any parameters.
	 */
	
	// Sending HTTP HEAD request
	public void requestHEAD (String file, String[] headers) throws IOException;
	/* HEAD request is the same as GET, but tell the server to
	 * exclude the body.
	 */
	
	// Sending HTTP POST request
	public void requestPOST (String file, String[] headers, byte[] body) throws IOException;
	/* POST request is also very common HTTP request, alike GET
	 * it also request a file from the server, but also got the
	 * ability to include parameters.
	 */
	
	
	// ============================== Network Section ============================== //
	// This function deals with the network layer.
	public void send (byte[] data) throws IOException;
	/* This function deals with the connection,
	 * and formats the recived data.
	 * Should be called by request functions,
	 * and before any respond function.
	 */
	
	
	// ============================== Respond Section ============================== //
	// Receive HTTP stutus respond
	public String respondStatus ();
	/* The HTTP status code indicates the server respond for
	 * the request.
	 */
	
	// Receive HTTP headers respond
	public String[] respondHeaders ();
	/* The HTTP headers that the server sends back.
	 */
	
	// Receive HTTP body respond
	public byte[] respondBody ();
	/* The Body of the file that the HTTP server Sended back.
	 * If there is no body, this function should return empty
	 * array.
	 */
	
}
