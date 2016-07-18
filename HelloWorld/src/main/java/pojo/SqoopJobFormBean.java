package pojo;

public class SqoopJobFormBean {

	private  String sqoopCommand=null;
	private  String sqoopTypeSelection=null;
	private  String jobId=null;
	private  String jobStatus=null;
	public String getSqoopCommand() {
		return sqoopCommand;
	}
	public void setSqoopCommand(String sqoopCommand) {
		this.sqoopCommand = sqoopCommand;
	}
	public String getSqoopTypeSelection() {
		return sqoopTypeSelection;
	}
	public void setSqoopTypeSelection(String sqoopTypeSelection) {
		this.sqoopTypeSelection = sqoopTypeSelection;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}
	
}
