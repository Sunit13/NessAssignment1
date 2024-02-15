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
public class MainController {
	
	@Autowired
	private AuthService authservice;
	
	// 1:- LOGIN API
	@RequestMapping(method=RequestMethod.POST, value="/restapi/login")
	public String login(@RequestBody User user) {
		String token = authservice.generateToken(user);
		return "{\"token\": \"" + token + "\"}";
	}
	
	// 2:- Fetching the CWD with help of API
	@RequestMapping(method=RequestMethod.GET, value="/restapi/cwd")
	public ResponseEntity<String> getDirectory(HttpServletRequest request){
		String token = request.getHeader("Authorization");
		
		if(token == null || !token.startsWith("Bearer")) {
			return new ResponseEntity<String>("Authorization header is not valid ", HttpStatus.UNAUTHORIZED);
		}
		
		String authtoken = token.substring(7);
		
		try {
			if(!authservice.validateToken(authtoken)) {
				return new ResponseEntity<String>("Invalidate token ", HttpStatus.UNAUTHORIZED);
				}
			}catch (io.jsonwebtoken.MalformedJwtException e) {
	            return new ResponseEntity<String>("Invalid token", HttpStatus.UNAUTHORIZED);
	        }catch(io.jsonwebtoken.SignatureException e) {
	        	return new ResponseEntity<String>("Invalid token", HttpStatus.UNAUTHORIZED);
	        }
		
		String dir = authservice.getDirectory(authtoken);
		return new ResponseEntity<String>(dir,HttpStatus.OK);
	}
	
	// 3:- This api is for list of directories
	@RequestMapping(method=RequestMethod.GET, value="/restapi/ls")
	public ResponseEntity<String> getFileInformation(HttpServletRequest request){
		String token = request.getHeader("Authorization");
		
		if(token == null || !token.startsWith("Bearer")) {
			return new ResponseEntity<String>("Authorization header is not valid ", HttpStatus.UNAUTHORIZED);
		}
		
		String authtoken = token.substring(7);
		
		try {
			if(!authservice.validateToken(authtoken)) {
				return new ResponseEntity<String>("Invalidate token ", HttpStatus.UNAUTHORIZED);
				}
			}catch (io.jsonwebtoken.MalformedJwtException e) {
	            return new ResponseEntity<String>("Invalid token", HttpStatus.UNAUTHORIZED);
	        }catch(io.jsonwebtoken.SignatureException e) {
	        	return new ResponseEntity<String>("Invalid token", HttpStatus.UNAUTHORIZED);
	        }
		String finfo = authservice.listFilesInfo(authtoken);
		return new ResponseEntity<String>(finfo,HttpStatus.OK);
	}
	
	//4:- The api for changing the directory
	@RequestMapping(method=RequestMethod.GET, value="/restapi/cd/{directory}")
	public ResponseEntity<String> changeDirectory(HttpServletRequest request, @PathVariable String directory) {
		String token = request.getHeader("Authorization");
		if(token == null || !token.startsWith("Bearer")) {
			return new ResponseEntity<String>("Authorization header is not valid ", HttpStatus.UNAUTHORIZED);
		}
		
		String authtoken = token.substring(7);
		
		try {
			if(!authservice.validateToken(authtoken)) {
				return new ResponseEntity<String>("Invalidate token ", HttpStatus.UNAUTHORIZED);
				}
			}catch (io.jsonwebtoken.MalformedJwtException e) {
	            return new ResponseEntity<String>("Invalid token", HttpStatus.UNAUTHORIZED);
	        }catch(io.jsonwebtoken.SignatureException e) {
	        	return new ResponseEntity<String>("Invalid token", HttpStatus.UNAUTHORIZED);
	        }
		
		String newdir = authservice.ChangeDirectory(authtoken, directory);
		return new ResponseEntity<String>(newdir,HttpStatus.OK);
	}
	
	// 5:- LOGOUT API
	@RequestMapping(method=RequestMethod.POST, value="/restapi/logout")
	public void logout(@RequestBody User user) {
		authservice.removeToken(user);
		String token = user.getToken();
		System.out.println("Session loggedout sucessfully");
		System.out.println(token);
	}
}
