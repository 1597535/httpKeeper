/* ----------------------
 * Exstends the httpKeeperClose class.
 * last update: 17/3/2018
 * ---------------------- */

/* This class extends the httpKeeperClose,
 * and try to improve the connection time.
 */

/* HTTP implements:
 *  * Host: <host>
 *  * Connection: close
 *  * Accept-Encoding: gzip
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
import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

import java.io.IOException;
import java.lang.IndexOutOfBoundsException;

public class httpKeeperFaster extends httpKeeperClose
{
	// Constructor
	public httpKeeperFaster(String host, int port) {
		super (host, port);
	}
	
	// Sending HTTP GET request
	public void requestGET (String file, String[] headers) throws IOException {
		// If the user forgot to start with '/', we will help him
		if (!file.startsWith("/"))
			file = "/" + file;
			
		String GET = "GET " + file + " HTTP/1.1\r\n" +
					"Host: " + host + "\r\n" +
					"Connection: close\r\n" +
					"Accept-Encoding: gzip\r\n";
		
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
					"Connection: close\r\n" +
					"Accept-Encoding: gzip\r\n";
		
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
					"Content-Type: application/x-www-from-urlencoded\r\n" +
					"Accept-Encoding: gzip\r\n";
		
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
		
		InputStream inputStream = socket.getInputStream();
		
		long start = System.currentTimeMillis();
		while (inputStream.available() == 0)
			// Just in case
			if ((System.currentTimeMillis() - start) > 5000)
				throw new IOException("Time out");
		
		byte[] data = new byte[inputStream.available()];
		inputStream.read (data);
		
		String[] data_str = (new String(data)).split("\r\n");
		recvStatus = data_str[0];
		
		try {
			String headers = "";
			for (int i = 1; !data_str[i].isEmpty(); i++)
				headers += data_str[i] + "\r\n";
			
			recvHeaders = headers.split("\r\n");
		} catch (IndexOutOfBoundsException e) {
			throw new IOException ("Corupted HTTP respond");
		}
		
		// Content-Length: 18434
		// 123456789011234567890
		int msg_size = -1;
		for (String header: recvHeaders)
			if (header.length() > 15 && header.substring(0,15).equals("Content-Length:")) {
				msg_size = Integer.parseInt(header.substring(16));
				break;
			}
		
		if (msg_size == -1) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			data_str = (new String(data)).split("\r\n\r\n",2);
			recvBody = (data_str.length > 1) ? data_str[1] : "";
			for (String line = reader.readLine(); line != null; line = reader.readLine())
				recvBody += line + "\n";
		} else if (msg_size == 0) {
			recvBody = "";
		} else {
			int readed = recvStatus.length() + 2;
			for (String header: recvHeaders)
				readed += header.length() + 2;
			readed += 2;
			
			readed = data.length - readed;
			
			byte[] body = new byte[msg_size];
			for (int i = 0; i < readed; i++)
				body[i] = data[data.length - readed + i];
			
			start = System.currentTimeMillis();
			while (readed < msg_size) {
				if ((System.currentTimeMillis() - start) > 10000)
					throw new IOException("Time out");
				readed += inputStream.read(body, readed, msg_size - readed);
			}
			
			boolean zipped = false;
			// Content-Encoding: gzip
			for (String header: recvHeaders)
				if (!zipped)
					zipped = header.equals("Content-Encoding: gzip");
			
			if (zipped) {
				System.out.println(body.length);
				GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(body));
				
				BufferedReader br = new BufferedReader(new InputStreamReader(gz, "UTF-8"));
				recvBody = "";
				String line;
				while((line = br.readLine()) != null) {
					recvBody += line + "\n";
				}
			} else
				recvBody = new String(body);
		}
		
		socket.close();
	}
}