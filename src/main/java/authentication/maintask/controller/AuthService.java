package authentication.maintask.controller;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;



@Service
public class AuthService {
	
	
    private final Map<String, String> userTokenMap = new HashMap<String, String>();
    
    private static final String id = UUID.randomUUID().toString(); 
    private static final String SECRET_KEY = "nessSunit451";
    private static final long EXPIRATION_TIME = 5 * 60 * 1000;
    
    //1:- This method is used for generating the token and checking for token expiration
    public String generateToken(User user) {
        // Set expiration time
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        if (userTokenMap.containsKey(user.getUsername())) {
            String token = userTokenMap.get(user.getUsername());
            try {
                // Parse the token to check its expiration date
                Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
                Date expirationDate = claims.getExpiration();

                // If the expiration date is in the past, the token has expired
                if (expirationDate.before(now)) {
                    // Generate new token
                    token = generateNewToken(user, expiryDate);
                    return "Your token has expired. A new token has been generated.";
                } else {
                    // Token is still valid, return it
                    return token;
                }
            } catch (ExpiredJwtException ex ) {
                // Token has expired, generate a new one
                token = generateNewToken(user, expiryDate);
                return "Your token has expired. A new token has been generated.";
                }
        }

        // Generate new token if the token doesn't exist in the map
        String token = generateNewToken(user, expiryDate);
        return token;
    }
    
    
    // This method is used for to validate the token
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            String username = claims.getSubject();
            return userTokenMap.containsKey(username) && userTokenMap.get(username).equals(token);
        } catch (NullPointerException e) {
            return false;
        }
    }
    
    
    //2:- This method is used for fetching the current working directory
    public String getDirectory(String token) {
    	if(validateToken(token)) {
    		String username = getUsernameFromToken(token);
    		if(username != null) {
    	        String directory = SessionManager.getSessionData(username);
    	        if (directory != null) {
    	            return "{\"cwd\": \"" + directory + "\"}";
    	        } else {
    	            // If directory is not found in session data, then showing current directory
    	            String path = System.getProperty("user.dir");
    	            return "{\"cwd\": \"" + path + "\"}";
    	        }
    		}
    	}
    	String str= "This is a testing current working directory";
    	return str;
    }
    
    //3:- This method is used for fetching list of directory and files
    public String listFilesInfo(String token) {
    	if(validateToken(token)) {
    		String username = getUsernameFromToken(token);
    		if(username != null) {
    			String dir = SessionManager.getSessionData(username);
    			if(dir != null) {
    				File changedir = new File(dir);
    				File[] chfiles = changedir.listFiles();
    				
    				if(chfiles == null) {
    					return "{can not acess current directory\"}";
    				}
    				List<FileInformation> chfileinfolist = new ArrayList<FileInformation>();
    				
    				for(File file1 : chfiles) {
    					FileInformation chfiledata = new FileInformation();
    					chfiledata.setName(file1.getName());
    					chfiledata.setType(file1.isDirectory() ? "DIRECTORY" : "FILE");
    					chfileinfolist.add(chfiledata);
    				}
    				return "{\"ls\": " + chfileinfolist.toString() + "}";
    			}
    		else {
    			String directory = System.getProperty("user.dir");
    			File currentDirectory = new File(directory);
    			File[] files = currentDirectory.listFiles();
    			if (files == null) {
    				return "{can not access the current directory\"}";
    				}
    			List<FileInformation> fileinfoList = new ArrayList<FileInformation>();
    			for (File file : files) {
    				FileInformation metadata = new FileInformation();
    				metadata.setName(file.getName());
    				metadata.setType(file.isDirectory() ? "DIRECTORY" : "FILE");
    				fileinfoList.add(metadata);
    				}
    			return "{\"ls\": " + fileinfoList.toString() + "}";
    			}
    	}
    	}
    	return null;
    	}
    
    //4:- This method is used for changing the directory for current session
    public String changeDirectory(String token, String directory) {
        if (validateToken(token)) {
            String homeDirectory = System.getProperty("user.home");
            String absolutePath = homeDirectory+ "/" + directory;
            // Create a File object representing the new directory
            File newDirectory = new File(absolutePath);

            // Check if the directory exists and is a directory
            if (newDirectory.exists() && newDirectory.isDirectory()) {
                // Update the session data with the new directory path
                String username = getUsernameFromToken(token);
                SessionManager.setSessionData(username, absolutePath);
                return "{\"cwd\": \"" + absolutePath + "\"}";
            }
        }
        return "{\"error\": \"Directory does not exist\"}";
    }
    
    //5:- This method is for removing the token and directory from map after applying logout api
    public void removeToken(User user) {
    	SessionManager.removeSessionData();
    	userTokenMap.remove(user.getUsername());
    }
    

    
    // This method is  used for generate token.
    private String generateNewToken(User user, Date expiryDate) {
        // Generate random token
        String token = Jwts.builder().setId(id)
                .setSubject(user.getUsername())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        // Store username and token in map
        userTokenMap.put(user.getUsername(), token);

        return token;
    }
    
    // This method is used for fetch username from token and add into the new map which is sessionData.
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}