package authentication.maintask.authpojoclasses;

import authentication.maintask.authdtoclasses.Status;

import java.util.List;
import java.util.ArrayList;
public class FileInformation{

	List<FileInformation> fileInformations = new ArrayList<FileInformation>();
	List<String> directories = new ArrayList<String>();
	List<String> files = new ArrayList<String>();
	private String name;
	private String type;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
