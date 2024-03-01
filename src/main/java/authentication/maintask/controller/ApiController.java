package authentication.maintask.controller;

import javax.servlet.http.HttpServletRequest;

import authentication.maintask.apiservices.AuthService;
import authentication.maintask.authdtoclasses.*;
import authentication.maintask.authpojoclasses.User;
import io.jsonwebtoken.JwtException;
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
public class ApiController {

	@Autowired
	private AuthService authservice;

	/**
	 * Checks the validity of the token passed via HttpServletRequest and generates a response accordingly.
	 * This method is responsible for verifying the validity of the token passed in the HttpServletRequest.
	 * Based on the token's validity, it generates a response indicating success or failure of the token validation.
	 *
	 * @param request The HttpServletRequest containing the token for validation.
	 * @return ResponseEntity indicating the result of the token validation.
	 */
	private boolean handleAuthorization(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token == null || !token.startsWith("Bearer")) {
			return false;
		}
		String authtoken = token.substring(7);
		try {
			return authservice.validateToken(authtoken);
		} catch (JwtException e) {
			return false;
		}
	}

	/**
	 * Handle user login.
	 * This endpoint allows users to authenticate by providing their credentials.
	 * Upon successful authentication, a JWT token is returned for further requests.
	 * @param user:-  loginRequest The object containing user credentials (username and password).
	 * @return ResponseEntity containing JWT token if authentication is successful,
	 *         or an error message if authentication fails.
	 */
	@RequestMapping(method=RequestMethod.POST, value="/login")
	public AuthorizationResponseDTo login(@RequestBody User user) {
		return authservice.generateToken(user);
	}

	/**
	 * Retrieve the current working directory (CWD).
	 * This endpoint returns the current working directory of the server.
	 * @return ResponseEntity containing the current working directory path.
	 */
	@RequestMapping(method=RequestMethod.GET, value="/cwd")
	public ResponseEntity<DirectoryResponse> getDirectory(HttpServletRequest request) {
		boolean isAuthorized = handleAuthorization(request);
		if (!isAuthorized) {
			Status status = new Status();
			status.setCode(HttpStatus.UNAUTHORIZED.value());
			status.setMessage("Invalid Token");
			return new ResponseEntity<DirectoryResponse>(new DirectoryResponse(status), HttpStatus.UNAUTHORIZED);
		}

		String authtoken = request.getHeader("Authorization").substring(7);
		DirectoryResponse dir = authservice.getDirectory(authtoken);
		return new ResponseEntity<DirectoryResponse>(dir, HttpStatus.OK);
	}

	/**
	 * Retrieves the list of directories and files from the current working directory.
	 * This endpoint returns the list of directories and files located in the current working directory.
	 * @param request The HTTPServletRequest containing the authorization token in the headers for authentication purposes.
	 * @return ResponseEntity containing the list of directories and files in the current working directory.
	 *         If the request is unauthorized or if an error occurs, an appropriate error message is returned.
	 */
	@RequestMapping(method=RequestMethod.GET, value="/ls")
	public ResponseEntity<FileInformationListDTO> getFileInformation(HttpServletRequest request) {
		boolean isAuthorized = handleAuthorization(request);
        if (!isAuthorized) {
			Status status = new Status();
			status.setCode(HttpStatus.UNAUTHORIZED.value());
			status.setMessage("Invalid Token");
			return new ResponseEntity<FileInformationListDTO>(new FileInformationListDTO(status),HttpStatus.UNAUTHORIZED);
		}

		String authToken = request.getHeader("Authorization").substring(7);
		FileInformationListDTO fileInformationList = authservice.listFilesInfo(authToken);

		return new ResponseEntity<FileInformationListDTO>(fileInformationList, HttpStatus.OK);
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
	public ResponseEntity<DirectoryResponse> changeDirectory(HttpServletRequest request, @PathVariable String directory) {
		boolean isAuthorized = handleAuthorization(request);
		if (!isAuthorized) {
			Status status = new Status();
			status.setCode(HttpStatus.UNAUTHORIZED.value());
			status.setMessage("Invalid Token");
			return new ResponseEntity<DirectoryResponse>(new DirectoryResponse(status), HttpStatus.UNAUTHORIZED);
		}

		String authtoken = request.getHeader("Authorization").substring(7);
		DirectoryResponse newdir = authservice.changeDirectory(authtoken, directory);
		return new ResponseEntity<DirectoryResponse>(newdir, HttpStatus.OK);
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
	public LogoutResponseDTo logout(@RequestBody User user) {
		authservice.removeToken(user);
		return new LogoutResponseDTo("Session Logged out successfully.");
	}
}
