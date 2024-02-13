package authentication.maintask.controller;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
	
    private final Map<String, String> userTokenMap = new HashMap<String, String>();
    
    private static final String id = UUID.randomUUID().toString(); 
    private static final String SECRET_KEY = "nessSunit451";
    private static final long EXPIRATION_TIME = 5 * 60 * 1000;
    
    // This method is used for generating the token and checking for token expiration
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
            } catch (ExpiredJwtException ex) {
                // Token has expired, generate a new one
                token = generateNewToken(user, expiryDate);
                return "Your token has expired. A new token has been generated.";
            } catch (Exception ex) {
                // Other exceptions (e.g., invalid token format)
                return "Error generating token: " + ex.getMessage();
            }
        }

        // Generate new token if the token doesn't exist in the map
        String token = generateNewToken(user, expiryDate);
        return token;
    }
    
    
    // This method is for removing the token after applying logout api
    public void removeToken(User user) {
    	userTokenMap.remove(user.getUsername());
    }

    
    // This method is also used for generate token.
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
    
}