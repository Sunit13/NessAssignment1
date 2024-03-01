package authentication.maintask.authdtoclasses;

public class LogoutResponseDTo {
    private String message;

    public LogoutResponseDTo(){

    }

    public LogoutResponseDTo(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
