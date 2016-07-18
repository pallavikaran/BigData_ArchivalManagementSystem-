package Sqoop;
import java.util.Properties;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.WorkflowJob;

public class OozieJAVAAPIForMR {

	public static void main(String[] args) {
		OozieClient wc = new OozieClient("http://quickstart.cloudera:11000/oozie/");

		Properties conf = wc.createConfiguration();

		conf.setProperty("nameNode", "hdfs://quickstart.cloudera:8020");
		conf.setProperty("jobTracker", "quickstart.cloudera:8032");
		conf.setProperty("queueName", "default");
		conf.setProperty("oozie.libpath", "${nameNode}/user/oozie/share/lib");
		conf.setProperty("oozie.use.system.libpath", "true");
		conf.setProperty("oozie.wf.rerun.failnodes", "true");
		conf.setProperty("examplesRoot", "examples");
		conf.setProperty("appPath","${nameNode}/user/cloudera/${examplesRoot}/apps/map-reduce");
		//conf.setProperty("appPath","${oozieProjectRoot}/");
		conf.setProperty(OozieClient.APP_PATH, "${appPath}");

		//conf.setProperty("inputDir", "${oozieProjectRoot}");
		conf.setProperty("outputDir", "${appPath}/map-reduce12");

		try {
			String jobId = wc.run(conf);
			System.out.println("Workflow job, " + jobId + " submitted");

			while (wc.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				System.out.println("*******************"+wc.getJobInfo(jobId));
				Thread.sleep(10 * 1000);
			}
			System.out.println("Workflow job completed ...");
			System.out.println(wc.getJobInfo(jobId));
		} catch (Exception r) {
			System.out.println("Errors " + r.getLocalizedMessage());
		}
	}
}