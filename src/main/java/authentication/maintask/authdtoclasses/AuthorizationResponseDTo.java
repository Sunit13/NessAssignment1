package authentication.maintask.authdtoclasses;

public class AuthorizationResponseDTo {
    private String token;

    private Status status;



    public AuthorizationResponseDTo(){

    }


    public AuthorizationResponseDTo(Status status) {
        this.status = status;
    }

    public AuthorizationResponseDTo(String token) {
        this.token = token;
    }

    public AuthorizationResponseDTo(String token, Status status){
        this.token = token;
        this.status = status;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
