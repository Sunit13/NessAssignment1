package authentication.maintask.apiservices;

import authentication.maintask.authdtoclasses.AuthorizationResponseDTo;
import authentication.maintask.authdtoclasses.DirectoryResponse;
import authentication.maintask.authdtoclasses.FileInformationListDTO;
import authentication.maintask.authdtoclasses.Status;
import authentication.maintask.authpojoclasses.FileInformation;
import authentication.maintask.authpojoclasses.SessionManager;
import authentication.maintask.authpojoclasses.User;
import io.jsonwebtoken.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
    public AuthorizationResponseDTo generateToken(User user) {
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
                    Status error = new Status();
                    error.setCode(HttpStatus.FORBIDDEN.value());
                    error.setMessage("Your token has expired, please generate new one.");
                    return new AuthorizationResponseDTo(error);
                } else {
                    // Token is still valid, return it
                    Status successStatus = new Status();
                    successStatus.setCode(HttpStatus.OK.value());
                    successStatus.setMessage("Successful");
                    return new AuthorizationResponseDTo(token, successStatus);
                }
            } catch (JwtException ex ) {
                // Token has expired, generate a new one
                token = generateNewToken(user, expiryDate);
                Status error = new Status();
                error.setCode(HttpStatus.FORBIDDEN.value());
                error.setMessage("Your token has  expired please generate new one.");
                return new AuthorizationResponseDTo(error);
            }
        }

        // Generate new token if the token doesn't exist in the map
        String tokens = generateNewToken(user, expiryDate);
        Status successstatus = new Status();
        successstatus.setCode(HttpStatus.OK.value());
        successstatus.setMessage("Successful");
        return new AuthorizationResponseDTo(tokens,successstatus);
    }


    // This method is used for to validate the token
    /**
     * This method is used for validating a token. It checks the users original token is same as passed token when requesting.
     * @param token :- To check token is valid or not.
     * @return in the form of boolean if token is valid then return true else false.
     */
    public boolean validateToken(String token) {
        if(token == null){
            return false;
        }
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            if(claims==null){
                return false;
            }
            String username = claims.getSubject();

            if(username == null){
                return false;
            }
            return userTokenMap.containsKey(username) && userTokenMap.get(username).equals(token);
        } catch (JwtException e) {
            return false;
        }
    }


    //2:- This method is used for fetching the current working directory
    /**
     * With the help of this method we fetch the users working directory. Users see directory only when their token is valid if not valid then it displays message.
     * @param token:- to validate the token.
     * @return It return the users current working directory.
     */
    public DirectoryResponse getDirectory(String token) {
        if(validateToken(token)) {
            String username = getUsernameFromToken(token);
            if(username != null) {
                String directory = SessionManager.getSessionData(username);
                if (directory != null) {
                    Status successmessage = new Status();
                    successmessage.setCode(HttpStatus.OK.value());
                    successmessage.setMessage("Successful");
                    return new DirectoryResponse(directory, successmessage);
                } else {
                    // If directory is not found in session data, then showing current directory
                    String path = System.getProperty("user.dir");
                    SessionManager.setSessionData(getUsernameFromToken(token),path);

                    Status successmessage = new Status();
                    successmessage.setCode(HttpStatus.OK.value());
                    successmessage.setMessage("Successful");
                    return new DirectoryResponse(path, successmessage);
                }
            }else{
                Status error = new Status();
                error.setCode(HttpStatus.FORBIDDEN.value());
                error.setMessage("Directory does not exist");
                return new DirectoryResponse(error);
            }
        }else{
            Status error = new Status();
            error.setCode(HttpStatus.UNAUTHORIZED.value());
            error.setMessage("Invalid Token");
            return new DirectoryResponse(error);
        }
    }

    //3:- This method is used for fetching list of directory and files
    /**
     * In this method we list the files and directories of the users current working directory.
     * @param token:- For validation
     * @return it returns list of files and directories.
     */
    public FileInformationListDTO listFilesInfo(String token) {
        List<FileInformation> fileInformationList = new ArrayList<FileInformation>();
        Status status = new Status();

        if (validateToken(token)) {
            String username = getUsernameFromToken(token);
            if (username != null) {
                String dir = SessionManager.getSessionData(username);
                if (dir != null) {
                    File changedir = new File(dir);
                    File[] chfiles = changedir.listFiles();

                    if (chfiles != null) {
                        for (File file : chfiles) {
                            FileInformation fileInfo = new FileInformation();
                            fileInfo.setName(file.getName());
                            fileInfo.setType(file.isDirectory() ? "DIRECTORY" : "FILE");
                            fileInformationList.add(fileInfo);
                        }
                        status = new Status();
                        status.setCode(HttpStatus.OK.value());
                        status.setMessage("Successful");
                    }
                } else {
                    String directory = System.getProperty("user.dir");
                    File currentDirectory = new File(directory);
                    File[] files = currentDirectory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            FileInformation fileInfo = new FileInformation();
                            fileInfo.setName(file.getName());
                            fileInfo.setType(file.isDirectory() ? "DIRECTORY" : "FILE");
                            fileInformationList.add(fileInfo);
                        }
                        status = new Status();
                        status.setCode(HttpStatus.OK.value());
                        status.setMessage("Successful");
                    }
                }
            }
        }else{
            status = new Status();
            status.setCode(HttpStatus.UNAUTHORIZED.value());
            status.setMessage("Invalid token");
        }
        FileInformationListDTO responseDto = new FileInformationListDTO();
        responseDto.setStatus(status);
        responseDto.setFileInformationList(fileInformationList);
        return responseDto;
    }

    //4:- This method is used for changing the directory for current session
    /**
     * This methods changes the directory for current user when api hits.It checks the directory exist or not if exist then only change the directory else display message.
     * and after changing the directory. The directory and username is stored into the session data map which is in Session Manager class.
     * @param token:- For validation we pass token
     * @param directory:- For changing the directory we pass the directory name.
     * @return changed directory from sessiondata map. this directory changed for only current session.
     */
    public DirectoryResponse changeDirectory(String token, String directory) {
        if (validateToken(token)) {
            String currentDirectory = SessionManager.getSessionData(getUsernameFromToken(token));
            System.out.println("Current Directory: " + currentDirectory); // Log current directory

            String chStrPath = currentDirectory + "/" + directory;
            File chdir = new File(chStrPath);
            if(chdir.exists() && chdir.isDirectory()){
                SessionManager.setSessionData(getUsernameFromToken(token), chStrPath);
                Status successmessage = new Status();
                successmessage.setCode(HttpStatus.OK.value());
                successmessage.setMessage("Successful");
                return new DirectoryResponse(chStrPath,successmessage);
            }else if(!chdir.exists() && !chdir.isDirectory()) {
                currentDirectory = System.getProperty("user.home");
                String newDirectoryPath = currentDirectory + "/" + directory;
                File newDirectory = new File(newDirectoryPath);

                if(newDirectory.exists() && newDirectory.isDirectory()) {
                    SessionManager.setSessionData(getUsernameFromToken(token), newDirectoryPath);
                    Status successmessage = new Status();
                    successmessage.setCode(HttpStatus.OK.value());
                    successmessage.setMessage("Sucessful");
                    return new DirectoryResponse(newDirectoryPath,successmessage);
                }
                else{
                    Status error = new Status();
                    error.setCode(HttpStatus.FORBIDDEN.value());
                    error.setMessage("Directory does not exist");
                    return new DirectoryResponse(null,error);
                }
            }
        }
        Status error = new Status();
        error.setCode(HttpStatus.OK.value());
        error.setMessage("Invalid token");
        return new DirectoryResponse(null,error);
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