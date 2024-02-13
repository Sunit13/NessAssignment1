package authentication.maintask.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	@Autowired
	private AuthService authservice;
	
	@RequestMapping(method=RequestMethod.POST, value="/restapi/login")
	public String login(@RequestBody User user) {
		String token = authservice.generateToken(user);
		return "{\"token\": \"" + token + "\"}";
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/restapi/logout")
	public void logout(@RequestBody User user) {
		authservice.removeToken(user);
		String token = user.getToken();
		System.out.println("Session loggedout sucessfully");
		System.out.println(token);
	}
}
