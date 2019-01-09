package server;
import java.util.*;
import java.io.IOException;
import java.net.*;

public class HTTPrequest {
	public static String handleGetRequest(String uri) throws IOException, BadRequestException{	
		String path = "";
		path = HTTPrequest.getFilePath(uri);
		return path;
	}
	
	private static String getFilePath(String uri) {
		if (uri.equals("/")) {
			uri = "./resources/index.html";
		} else {
			uri = "./resources" + uri;
		}
		
		return uri;
	}
}
