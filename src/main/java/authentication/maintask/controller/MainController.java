package authentication.maintask.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restapi")
public class MainController {

	@Autowired
	private AuthService authservice;

	// Method to handle authorization logic
	/**
	 * This method is create for checking the token with help of httpservlet request. We check token is valid or not according to that we generate a response.
	 * @param request :- Pass the token as a request
	 * @return result
	 */
	private ResponseEntity<String> handleAuthorization(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token == null || !token.startsWith("Bearer")) {
			return new ResponseEntity<String>("Authorization header is not valid ", HttpStatus.UNAUTHORIZED);
		}
		String authtoken = token.substring(7);
		try {
			if (!authservice.validateToken(authtoken)) {
				return new ResponseEntity<String>("Invalid token ", HttpStatus.UNAUTHORIZED);
			}
		}catch (io.jsonwebtoken.JwtException e) {
			return new ResponseEntity<String>("Invalid token", HttpStatus.UNAUTHORIZED);
		}
		return null; // Indicates authorization is successful
	}

	/**
	 * Handle user login.
	 * This endpoint allows users to authenticate by providing their credentials.
	 * Upon successful authentication, a JWT token is returned for further requests.
	 * @param :-  loginRequest The object containing user credentials (username and password).
	 * @return ResponseEntity containing JWT token if authentication is successful,
	 *         or an error message if authentication fails.
	 * @throws :-  UnauthorizedExceptions if the provided credentials are invalid.
	 */
	@RequestMapping(method=RequestMethod.POST, value="/login")
	public String login(@RequestBody User user) {
		String token = authservice.generateToken(user);
		return "{\"token\": \"" + token + "\"}";
	}

	/**
	 * Retrieve the current working directory (CWD).
	 * This endpoint returns the current working directory of the server.
	 * @return ResponseEntity containing the current working directory path.
	 */
	@RequestMapping(method=RequestMethod.GET, value="/cwd")
	public ResponseEntity<String> getDirectory(HttpServletRequest request){
		ResponseEntity<String> authorizationResponse = handleAuthorization(request);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}
		String authtoken = request.getHeader("Authorization").substring(7);
		String dir = authservice.getDirectory(authtoken);
		return new ResponseEntity<String>(dir, HttpStatus.OK);
	}

	/**
	 * Retrieves the list of directories and files from the current working directory.
	 * This endpoint returns the list of directories and files located in the current working directory.
	 * @param request The HTTPServletRequest containing the authorization token in the headers for authentication purposes.
	 * @return ResponseEntity containing the list of directories and files in the current working directory.
	 *         If the request is unauthorized or if an error occurs, an appropriate error message is returned.
	 */
	@RequestMapping(method=RequestMethod.GET, value="/ls")
	public ResponseEntity<String> getFileInformation(HttpServletRequest request){
		ResponseEntity<String> authorizationResponse = handleAuthorization(request);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}
		String authtoken = request.getHeader("Authorization").substring(7);
		String finfo = authservice.listFilesInfo(authtoken);
		return new ResponseEntity<String>(finfo, HttpStatus.OK);
	}

	/**
	 * Changes the current working directory for the user.
	 * This endpoint allows the user to change their current working directory by providing the directory name.
	 * @param request The HttpServletRequest containing the authorization token in the headers for authentication purposes.
	 * @param directory The name of the directory to navigate to.
	 * @return ResponseEntity indicating the success or failure of the directory change operation.
	 *         If the directory change is successful, an appropriate success message is returned.
	 *         If the request is unauthorized or if the specified directory does not exist or cannot be accessed, an appropriate error message is returned.
	 */
	@RequestMapping(method=RequestMethod.GET, value="/cd/{directory}")
	public ResponseEntity<String> changeDirectory(HttpServletRequest request, @PathVariable String directory) {
		ResponseEntity<String> authorizationResponse = handleAuthorization(request);
		if (authorizationResponse != null) {
			return authorizationResponse;
		}
		String authtoken = request.getHeader("Authorization").substring(7);
		String newdir = authservice.changeDirectory(authtoken, directory);
		return new ResponseEntity<String>(newdir, HttpStatus.OK);
	}


	/**
	 * Logs out the user by removing their token and ending the session.
	 * This endpoint allows users to log out by removing their authentication token and ending the session.
	 *
	 * @param user The user object representing the user to be logged out.
	 * @return ResponseEntity indicating the success or failure of the logout operation.
	 *         If the logout is successful, an appropriate success message is returned.
	 *         If an error occurs during logout, an appropriate error message is returned.
	 */
	@RequestMapping(method=RequestMethod.POST, value="/logout")
	public String logout(@RequestBody User user) {
		authservice.removeToken(user);
		return "{\"Session Logged out successfully.\"}";
	}
}
