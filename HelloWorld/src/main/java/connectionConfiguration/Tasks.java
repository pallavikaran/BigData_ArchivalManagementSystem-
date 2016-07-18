package connectionConfiguration;

import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import pojo.SqoopJobFormBean;

public class Tasks {

	public void setTask(SSHExec ssh, SqoopJobFormBean buildSqoopObj) throws TaskExecFailException{
		CustomTask task1 = new ExecCommand("echo $SSH_CLIENT"); 
		System.out.println(ssh.exec(task1));
		ssh.exec(task1);
		System.out.println("*************** "+buildSqoopObj.getSqoopCommand());
		CustomTask task2 = new ExecCommand(buildSqoopObj.getSqoopCommand());
		ssh.exec(task2);
	}
	
}