package Sqoop;

	import net.neoremind.sshxcute.core.SSHExec;
	import net.neoremind.sshxcute.core.ConnBean;
	import net.neoremind.sshxcute.task.CustomTask;
	import net.neoremind.sshxcute.task.impl.ExecCommand;
public class SqoopJob2 {

	public static void main(String args[]) throws Exception{

	    // Initialize a ConnBean object, parameter list is ip, username, password

	    ConnBean cb = new ConnBean("quickstart.cloudera", "cloudera","cloudera");

	    // Put the ConnBean instance as parameter for SSHExec static method getInstance(ConnBean) to retrieve a singleton SSHExec instance
	    SSHExec ssh = SSHExec.getInstance(cb);          
	    // Connect to server
	    ssh.connect();
	    CustomTask sampleTask1 = new ExecCommand("echo $SSH_CLIENT"); // Print Your Client IP By which you connected to ssh server on Horton Sandbox
	    System.out.println(ssh.exec(sampleTask1));
	    //CustomTask sampleTask2 = new ExecCommand("sqoop import --connect jdbc:mysql://localhost/TEST --username=root --password=cloudera --table pet --hive-import -m 1 -- --schema default");
	    CustomTask sampleTask3 = new ExecCommand("sqoop import --connect jdbc:mysql://localhost/TEST --username=root --password=cloudera --table pet --hive-import -m 1 --hive-table=pet ");
	    //ssh.exec(sampleTask2);
	    ssh.exec(sampleTask3);
	    ssh.disconnect();   
	}
}
