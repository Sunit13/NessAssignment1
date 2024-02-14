** Solution Design **

1] Overall Design:
    The application appears to be a RESTful service for user authentication.
    It utilizes JSON Web Tokens (JWT) for generating tokens upon user login.
    It maintains a map (userTokenMap) to store the mapping between usernames and tokens.

2] Endpoints:
    The main endpoint is /restapi/login, which handles user login.
    Upon receiving a POST request with a user object containing a username, it generates a token and returns it in a JSON response.
    The another endpoint is /restapi/logout, which handles user logout API.
  When User calls this api then with the help of username the token gets  removed from the data structure and return null.
  
3] Token Generation:
    Tokens are generated using JWT.
    Each token is associated with a username and has an expiration time.

4] User Token Map:
    The userTokenMap stores the mapping between usernames and tokens.
    It is used to check if a token exists for a given username and retrieve it if so.

5] Token Management:
    If a token already exists for a username, it returns the existing token.
    If not, it generates a new token, associates it with the username, and returns it.


** Instructions to build and run project **

1] Building the Project:
    Ensure that you have Maven installed on your system. If not, download and install Maven.
    Navigate to the root directory of your project in the command line.
    Run the following command to build the project:
        mvn clean install

2] Running the Application:
    After successfully building the project, you can run the application.
    You can run the application using the following command:
        java -jar target/your-application.jar

3] CURL Command
    With help of curl command you can run this application.
    eg:- If request is post:-  curl -X POST -d '{"username": "sunit"}' -H "Content-Type: application/json" http://localhost:8080/restapi/login
