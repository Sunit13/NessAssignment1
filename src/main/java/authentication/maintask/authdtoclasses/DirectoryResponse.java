package authentication.maintask.authdtoclasses;

public class DirectoryResponse {
    private String cwd;
    private Status status;


    public DirectoryResponse(String cwd) {
        this.cwd = cwd;
    }

    public DirectoryResponse(Status status){
        this.status = status;
    }


    public  DirectoryResponse(String cwd, Status status){
        this.cwd = cwd;
        this.status = status;
    }


    public String getCwd() {
        return cwd;
    }

    public void setCwd(String cwd) {
        this.cwd = cwd;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
