package authentication.maintask.authpojoclasses;

// We create username and token with help of this class. 

public class User{
	private String username;
	private String id;
	private String token;
	
	public User() {
		
	}
	
	public String getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void userDto(String token){
		this.token = token;
	}

	public User(String username, String id, String token) {
		this.username = username;
		this.id = id;
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	
}
