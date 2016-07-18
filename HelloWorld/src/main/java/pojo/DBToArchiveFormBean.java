package pojo;

import java.util.ArrayList;

public class DBToArchiveFormBean {
	private String rDriverName= null;
	private String rUsername= null;
	private String rPassword= null;
	private String rServiceName= null;
	private String rHostName= null;
	private String rPortNumber= null;
	private String rDBType= null;
	private String rConnectionString= null;
	private String rDBDriver= null;
	private String amsStatus= null;
	private String amsUserID= null;
	private String rDbselcted= null;
	private ArrayList<String> arrayList=null;
	
	
	public String getrDbselcted() {
		return rDbselcted;
	}
	public void setrDbselcted(String rDbselcted) {
		this.rDbselcted = rDbselcted;
	}
	public String getrDBDriver() {
		return rDBDriver;
	}
	public void setrDBDriver(String rDBDriver) {
		this.rDBDriver = rDBDriver;
	}
	public String getrDriverName() {
		return rDriverName;
	}
	public void setrDriverName(String rDriverName) {
		this.rDriverName = rDriverName;
	}
	public String getrUsername() {
		return rUsername;
	}
	public void setrUsername(String rUsername) {
		this.rUsername = rUsername;
	}
	public String getrPassword() {
		return rPassword;
	}
	public void setrPassword(String rPassword) {
		this.rPassword = rPassword;
	}
	public String getrServiceName() {
		return rServiceName;
	}
	public void setrServiceName(String rServiceName) {
		this.rServiceName = rServiceName;
	}
	public String getrHostName() {
		return rHostName;
	}
	public void setrHostName(String rHostName) {
		this.rHostName = rHostName;
	}
	public String getrPortNumber() {
		return rPortNumber;
	}
	public void setrPortNumber(String rPortNumber) {
		this.rPortNumber = rPortNumber;
	}
	public String getrDBType() {
		return rDBType;
	}
	public void setrDBType(String rDBType) {
		this.rDBType = rDBType;
	}
	public String getrConnectionString() {
		return rConnectionString;
	}
	public void setrConnectionString(String rConnectionString) {
		this.rConnectionString = rConnectionString;
	}
	public String getAmsStatus() {
		return amsStatus;
	}
	public void setAmsStatus(String amsStatus) {
		this.amsStatus = amsStatus;
	}
	public String getAmsUserID() {
		return amsUserID;
	}
	public void setAmsUserID(String amsUserID) {
		this.amsUserID = amsUserID;
	}
	
}
