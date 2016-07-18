package connectionConfiguration;

import pojo.ClusterConnectionFormBean;
import pojo.HiveFormBean;
import pojo.OutputDirectoryFormBean;
import pojo.RDBMSConnectionFormBean;
import pojo.SqoopJobFormBean;

public class BuildingSqoopCmd {
	public void buildTheSqoopCommand(SqoopJobFormBean objSqoopJobFormBean,RDBMSConnectionFormBean objRDBMSConnectionFormBean,HiveFormBean objHiveFormBean,
			ClusterConnectionFormBean objClusterConnectionFormBean){
	
	String sqoopTypeSelection=objSqoopJobFormBean.getSqoopTypeSelection();
	if (sqoopTypeSelection.equalsIgnoreCase("sqoop1")){
	//Building Sqoop statement 
		//RDBMSConnectionFormBean objRDBMSConnectionFormBean=new RDBMSConnectionFormBean();
		//HiveFormBean objHiveFormBean=new HiveFormBean();
		
	String SqoopCmd=null;
	SqoopCmd =" sqoop import --connect "+ objRDBMSConnectionFormBean.getConnectionString()+ " "+
			  "--username="+objRDBMSConnectionFormBean.getUsername()+ " "+
			  "--password="+objRDBMSConnectionFormBean.getPassword()+ " "+
			  "--table " +objRDBMSConnectionFormBean.getTableName()+ " "+
			  "--hive-import -m "+objHiveFormBean.getMapTaskNo()+ " "+
			  "--hive-table="+objHiveFormBean.gethTablename();
	System.out.println("SqoopCmd: "+SqoopCmd);
	objSqoopJobFormBean.setSqoopCommand(SqoopCmd);
	}
	if (sqoopTypeSelection.equalsIgnoreCase("sqoop2")){
		
	}
	}
}
