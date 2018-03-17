
import java.io.IOException;

public class tester
{
	public static void main(String[] arg) throws IOException {
		httpKeeper hK = new httpKeeperFaster("www.example.org", 80);
		
		hK.requestGET ("/",new String[0]);
		System.out.println (hK.respondStatus());
		String[] headers = hK.respondHeaders();
		for (String header: headers)
			System.out.println (header);
		
		System.out.println (hK.respondBody());
		
		return;
	}
}