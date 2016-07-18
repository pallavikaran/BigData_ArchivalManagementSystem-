package connectionConfiguration;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;
import pojo.ClusterConnectionFormBean;
import pojo.SqoopJobFormBean;
import pojo.HiveFormBean;
import pojo.RDBMSConnectionFormBean;
import pojo.OutputDirectoryFormBean;
import connectionConfiguration.Tasks;
import connectionConfiguration.BuildingSqoopCmd;

public class ClusterConnection {

	public Boolean sqoopExecutionMain(RDBMSConnectionFormBean objRDBMSConnectionFormBean,HiveFormBean objHiveFormBean, SqoopJobFormBean objSqoopJobFormBean,
			ClusterConnectionFormBean objClusterConnectionFormBean) throws Exception{
		try{
			//RDBMSConnectionFormBean objRDBMSConnectionFormBean= new RDBMSConnectionFormBean();
			//HiveFormBean objHiveFormBean= new HiveFormBean();
			//ClusterConnectionFormBean objClusterConnectionFormBean=new ClusterConnectionFormBean();
			
			/*objHiveFormBean.sethTablename("pet");
			objHiveFormBean.setMapTaskNo(1);
			objRDBMSConnectionFormBean.setConnectionString("jdbc:mysql://localhost/TEST");
			objRDBMSConnectionFormBean.setUsername("root");
			objRDBMSConnectionFormBean.setPassword("cloudera");
			objRDBMSConnectionFormBean.setTableName("pet");*/
			
			//Building the sqoop command
			BuildingSqoopCmd buildSqoopObj = new BuildingSqoopCmd();
			//objSqoopJobFormBean.setSqoopTypeSelection("sqoop1");
			buildSqoopObj.buildTheSqoopCommand(objSqoopJobFormBean,objRDBMSConnectionFormBean,objHiveFormBean,objClusterConnectionFormBean);
			
			//Setting connection values
			/*objClusterConnectionFormBean.setHostName("quickstart.cloudera");
			objClusterConnectionFormBean.setUsername("cloudera");
			objClusterConnectionFormBean.setPassword("cloudera");*/
			System.out.println("Before sshxcute connection firing statement");
			//Connecting to the cluster
			if(!objClusterConnectionFormBean.getHostName().equalsIgnoreCase("") && !objClusterConnectionFormBean.getHostName().isEmpty() && objClusterConnectionFormBean.getHostName() !=null
				&& !objClusterConnectionFormBean.getUsername().equalsIgnoreCase("") && !objClusterConnectionFormBean.getUsername().isEmpty() && objClusterConnectionFormBean.getUsername() !=null
				&& !objClusterConnectionFormBean.getPassword().equalsIgnoreCase("") && !objClusterConnectionFormBean.getPassword().isEmpty() && objClusterConnectionFormBean.getPassword() !=null){
			
				//ConnBean cb = new ConnBean(objClusterConnectionFormBean.getHostName(), objClusterConnectionFormBean.getUsername(),objClusterConnectionFormBean.getPassword());
				ConnBean cb = new ConnBean("quickstart.cloudera","cloudera","cloudera");
				SSHExec ssh = SSHExec.getInstance(cb);   
				ssh.connect();
				//Executing the Task
				
				Tasks t=new Tasks();
				t.setTask(ssh, objSqoopJobFormBean);
				ssh.disconnect(); 
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}

}

