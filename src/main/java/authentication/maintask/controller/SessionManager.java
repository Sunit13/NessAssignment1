package authentication.maintask.controller;

import java.util.HashMap;
import java.util.Map;

// In this class I can store the username and directory of that user. 

public class SessionManager {
	   public static Map<String, String> sessionData = new HashMap<String, String>();

	    public static void setSessionData(String token, String directory) {
	        sessionData.put(token, directory);
	    }

	    public static String getSessionData(String token) {
	        return sessionData.get(token);
	    }
	    
	    public static void removeSessionData() {
			sessionData.clear();
	    }
}
