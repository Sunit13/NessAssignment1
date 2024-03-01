package authentication.maintask.authdtoclasses;

import authentication.maintask.authpojoclasses.FileInformation;

import java.util.List;

public class FileInformationListDTO {

    private List<FileInformation> fileInformationList;

//    private String message;

    private Status status;

    public FileInformationListDTO(){

    }


    public FileInformationListDTO(Status status) {
        this.status = status;
    }

//    public FileInformationListDTO(String message){
//        this.message = message;
//    }

    public List<FileInformation> getFileInformationList() {
        return fileInformationList;
    }

    public void setFileInformationList(List<FileInformation> fileInformationList) {
        this.fileInformationList = fileInformationList;
    }


    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


}
