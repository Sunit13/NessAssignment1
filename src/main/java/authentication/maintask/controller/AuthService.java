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


    /**
     * This map is used for storing the username as key and token as value for that username. and every user has unique token.
     */
    private final Map<String, String> userTokenMap = new HashMap<String, String>();

    private static final String id = UUID.randomUUID().toString();
    private static final String SECRET_KEY = "nessSunit451";
    private static final long EXPIRATION_TIME = 5 * 60 * 1000;

    //1:- This method is used for generating the token and checking for token expiration

    /**
     * This method is generate a new token for the every new user. While creating a token it sets the expiration time for that token and after expiring the token it generates a new unique token fo that user.
     * @param user:- We pass user as object and generate a new token for that user with help of jwt tokens.
     * @return :- It return a generated token.
     */
    public String generateToken(User user) {
        // Set expiration time
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        if(userTokenMap.containsKey(user.getUsername())) {
            String token = userTokenMap.get(user.getUsername());
            try {
                // Parse the token to check its expiration date
                Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
                Date expirationDate = claims.getExpiration();

                // If the expiration date is in the past, the token has expired
                if(expirationDate.before(now)) {
                    // Generate new token
                    token = generateNewToken(user, expiryDate);
                    return "{\"Your token has expired. Please generate new one.\"}";
                } else {
                    // Token is still valid, return it
                    return token;
                }
            } catch (ExpiredJwtException ex ) {
                // Token has expired, generate a new one
                token = generateNewToken(user, expiryDate);
                return "{\"Your token has  expired please generate new one.\"}";
            }
        }

        // Generate new token if the token doesn't exist in the map
        String tokens = generateNewToken(user, expiryDate);
        return tokens;
    }


    // This method is used for to validate the token
    /**
     * This method is used for validating a token. It checks the users original token is same as passed token when requesting.
     * @param token :- To check token is valid or not.
     * @return d
     */
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

    /**
     * With the help of this method we fetch the users working directory. Users see directory only when their token is valid if not valid then it displays message.
     * @param token:- to validate the token.
     * @return It return the users current working directory.
     */
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
                    SessionManager.setSessionData(getUsernameFromToken(token),path);
                    return "{\"cwd\": \"" + path + "\"}";
                }
            }
        }
        return null;
    }

    //3:- This method is used for fetching list of directory and files

    /**
     * In this method we list the files and directories of the users current working directory.
     * @param token:- For validation
     * @return it returns list of files and directories.
     */
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
    /**
     * This methods changes the directory for current user when api hits.It checks the directory exist or not if exist then only change the directory else display message.
     * and after changing the directory. The directory and username is stored into the session data map which is in Session Manager class.
     * @param token:- For validation we pass token
     * @param directory:- For changing the directory we pass the directory name.
     * @return changed directory from sessiondata map. this directory changed for only current session.
     */
    public String changeDirectory(String token, String directory) {
        if (validateToken(token)) {
            String currentDirectory = SessionManager.getSessionData(getUsernameFromToken(token));
            System.out.println("Current Directory: " + currentDirectory); // Log current directory

            String chStrPath = currentDirectory + "/" + directory;
            File chdir = new File(chStrPath);
            if(chdir.exists() && chdir.isDirectory()){
                SessionManager.setSessionData(getUsernameFromToken(token), chStrPath);
                return "{\"cwd\": \"" + chStrPath + "\"}";
            }else if(!chdir.exists() && !chdir.isDirectory()) {
                currentDirectory = System.getProperty("user.home");
                String newDirectoryPath = currentDirectory + "/" + directory;
                File newDirectory = new File(newDirectoryPath);

                if(newDirectory.exists() && newDirectory.isDirectory()) {
                    SessionManager.setSessionData(getUsernameFromToken(token), newDirectoryPath);
                    return "{\"cwd\": \"" + newDirectoryPath + "\"}";
                }

            }
        }
        return "{\"error\": \"Directory does not exist\"}";
    }

    //5:- This method is for removing the token and directory from map after applying logout api
    /**
     * THis method remove the token from the map and logout the session for that user when api hits.
     * @param user:- pass user as a object and remove token for that specific user.
     */
    public void removeToken(User user) {
        SessionManager.sessionData.remove(user.getUsername());
        userTokenMap.remove(user.getUsername());
    }

    // This method is  used for generate token.
    /**
     * This methods only generate a token and put into the map.
     * @param user:- passed User as object because store username as key
     * @param expiryDate:- to given expiry of that token pass this varaible.
     * In this method i create a token with help of jwt and their algorithm.
     * @return the token.
     */
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
    /**
     * For fetching the username
     * @param token:- token wise user.
     * @return the username .
     */
    private String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}