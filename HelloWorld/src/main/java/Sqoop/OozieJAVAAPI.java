package Sqoop;
import java.util.Properties;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.WorkflowJob;
import pojo.OozieJobFormBean;

public class OozieJAVAAPI {
	OozieJobFormBean objOozieJobFormBean=new OozieJobFormBean();
	public OozieJobFormBean executeOzzieSqoopJob(String appPath) {
		
		OozieClient wc = new OozieClient("http://quickstart.cloudera:11000/oozie/");
		//OozieClient wc = new OozieClient("http://ec2-23-21-6-160.compute-1.amazonaws.com:11000/oozie/");
		Properties conf = wc.createConfiguration();

		conf.setProperty("nameNode", "hdfs://quickstart.cloudera:8020");
		conf.setProperty("jobTracker", "quickstart.cloudera:8032");
		conf.setProperty("queueName", "default");
		conf.setProperty("oozie.libpath", "${nameNode}/user/oozie/share/lib");
		conf.setProperty("oozie.use.system.libpath", "true");
		conf.setProperty("oozie.wf.rerun.failnodes", "true");

		conf.setProperty("appPath",appPath);
		//conf.setProperty("appPath","${oozieProjectRoot}/");
		conf.setProperty(OozieClient.APP_PATH, "${appPath}");

		//conf.setProperty("inputDir", "${oozieProjectRoot}");
		//conf.setProperty("outputDir", "${appPath}/oozieOutputFromJava");

		try {
			String jobId = wc.run(conf);
			System.out.println("Workflow job, " + jobId + " submitted");
			objOozieJobFormBean.setOozieJobID(jobId);
			while (wc.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
				objOozieJobFormBean.setOozieJobStatus(wc.getJobInfo(jobId).getStatus().toString());
				System.out.println("Workflow job running ...");
				System.out.println("*******************"+wc.getJobInfo(jobId));
				Thread.sleep(10 * 1000);
				//int time=10 * 1000;
				//objOozieJobFormBean.setOozieJobTimeTaken();
			}
			System.out.println("Workflow job completed ...");
			objOozieJobFormBean.setOozieJobStatus(wc.getJobInfo(jobId).getStatus().toString());
			System.out.println(wc.getJobInfo(jobId));
		} catch (Exception r) {
			System.out.println("Errors " + r.getLocalizedMessage());
		}
		return objOozieJobFormBean;
	}
}