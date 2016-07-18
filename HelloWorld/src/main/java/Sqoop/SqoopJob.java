package Sqoop;
	import java.util.List;

import org.apache.sqoop.client.SqoopClient;
import org.apache.sqoop.model.MConnection;
import org.apache.sqoop.model.MConnectionForms;
import org.apache.sqoop.model.MForm;
import org.apache.sqoop.model.MInput;
import org.apache.sqoop.model.MJob;
import org.apache.sqoop.model.MJobForms;
import org.apache.sqoop.model.MSubmission;
import org.apache.sqoop.submission.counter.Counter;
import org.apache.sqoop.submission.counter.CounterGroup;
import org.apache.sqoop.submission.counter.Counters;
import org.apache.sqoop.validation.Status;

public class SqoopJob {

		private static SqoopClient client;
		private static String url = null;
		private static String connectionString = null;
		private static String username = null;
		private static String password = null;
		private static String schemaName = null;
		private static String tableName = null;
		private static String columns = null;
		private static String partitionColumn = null;
		private static String outputDirectory = null;

		/**
		 * Print errors or warnings messages
		 * @param formList
		 * 
		 */
		private static void printMessage(List<MForm> formList) {
			for (MForm form : formList) {
				List<MInput<?>> inputlist = form.getInputs();
				if (form.getValidationMessage() != null) {
					System.out.println("Form message: "
							+ form.getValidationMessage());
				}
				for (MInput<?> minput : inputlist) {
					//In case of any UNACCEPTABLE AND ACCEPTABLE status, user has to iterate 
					//the connector part forms and framework part forms for getting actual error or warning message.
					if (minput.getValidationStatus() == Status.ACCEPTABLE) {
						System.out.println("Warning:"
								+ minput.getValidationMessage());
					} else if (minput.getValidationStatus() == Status.UNACCEPTABLE) {
						System.out
								.println("Error:" + minput.getValidationMessage());
					}
				}
			}
		}

		public static void main(String[] agrs) {
			try {
				 connectionString = "jdbc:mysql://localhost/TEST";
				 username = "root";
				 password = "cloudera";
				 //schemaName = "YourMysqlDB";
				 tableName = "pets";
				 columns = "name,owner,species,sex,birth,death"; //comma separated column names
				 //partitionColumn = "PersonID";
				 outputDirectory = "/output/Pets";
				 url = "http://quickstart.cloudera:12000/sqoop/";

				// First initialize the SqoopClient class with server URL as
				// argument.
				client = new SqoopClient(url);

				// ********************************************************************
				// 1. Create connection using Connector ID (cid) - Creates
				// connection and
				// returns connection ID (xid)
				// ********************************************************************

				// Create Connection
				// Dummy connection object
				MConnection newCon = client.newConnection(1);

				// Get connection and framework forms. Set name for connection
				MConnectionForms conForms = newCon.getConnectorPart();
				MConnectionForms frameworkForms = newCon.getFrameworkPart();
				newCon.setName("MyConnection");

				// Set connection forms values
				conForms.getStringInput("connection.connectionString").setValue(
						connectionString);
				conForms.getStringInput("connection.jdbcDriver").setValue(
						"com.mysql.jdbc.Driver");
				conForms.getStringInput("connection.username").setValue(username);
				conForms.getStringInput("connection.password").setValue(password);

				frameworkForms.getIntegerInput("security.maxConnections").setValue(
						0);

				Status status = client.createConnection(newCon);
				if (status.canProceed()) {
					System.out.println("Created. New Connection ID : "
							+ newCon.getPersistenceId());
				} else {
					System.out.println("Check for status and forms error ");
				}

				// Print errors or warnings
				printMessage(newCon.getConnectorPart().getForms());
				printMessage(newCon.getFrameworkPart().getForms());

				// ********************************************************************
				// 2. Create Job using Connection ID (xid) - Create
				// job and returns Job ID (jid)
				// ********************************************************************
				// Creating dummy job object
				MJob newjob = client.newJob(newCon.getPersistenceId(),
						org.apache.sqoop.model.MJob.Type.IMPORT);
				MJobForms connectorForm = newjob.getConnectorPart();
				MJobForms frameworkForm = newjob.getFrameworkPart();

				newjob.setName("ImportJob");
				// Database configuration
				connectorForm.getStringInput("table.schemaName").setValue(
						schemaName);
				// Input either table name or sql
				connectorForm.getStringInput("table.tableName").setValue(tableName);
				// connectorForm.getStringInput("table.sql").setValue("select id,name from table where ${CONDITIONS}");
				connectorForm.getStringInput("table.columns").setValue(columns);
				connectorForm.getStringInput("table.partitionColumn").setValue(partitionColumn);
				// Set boundary value only if required
				// connectorForm.getStringInput("table.boundaryQuery").setValue("");

				// Output configurations
				frameworkForm.getEnumInput("output.storageType").setValue("HDFS");
				frameworkForm.getEnumInput("output.outputFormat").setValue(
						"TEXT_FILE");// Other option: SEQUENCE_FILE
				frameworkForm.getStringInput("output.outputDirectory").setValue(
						outputDirectory + newCon.getPersistenceId());

				// Job resources
				frameworkForm.getIntegerInput("throttling.extractors").setValue(1);
				frameworkForm.getIntegerInput("throttling.loaders").setValue(1);

				Status jobStatus = client.createJob(newjob);
				if (jobStatus.canProceed()) {
					System.out.println("New Job ID: " + newjob.getPersistenceId());
				} else {
					System.out.println("Check for status and forms error ");
				}

				// Print errors or warnings
				printMessage(newjob.getConnectorPart().getForms());
				printMessage(newjob.getFrameworkPart().getForms());

				// ********************************************************************
				// 3. Job submission with Job ID (jid) - Submit
				// sqoop Job to server
				// ********************************************************************

				// Job submission start
				// For synchronous job submission, use startSubmission(jid,
				// callback, pollTime) method.
				// If user is not interested in getting submission status, then
				// invoke method with null
				// for callback parameter and returns final submission status.
				// Polltime is request interval
				// for getting submission status from sqoop server and value should
				// be greater than zero.
				// Frequently hit the sqoop server if the low value is set to
				// pollTime. When a synchronous job
				// is submission started with callback, first invokes the callback’s
				// submitted(MSubmission)
				// method on successful submission, after every poll time interval
				// invokes updated(MSubmission)
				// and finally on finished executing the job invokes
				// finished(MSubmission) method.
				
				MSubmission submission = client.startSubmission(newjob.getPersistenceId());
				System.out.println("Hadoop job id :" + submission.getExternalId());
				System.out.println("Job link : " + submission.getExternalLink());
				System.out.println("Final Status : " + submission.getStatus());
				//Show counters
					Counters counters = submission.getCounters();
					if(counters != null) {
					  System.out.println("Counters:");
					  for(CounterGroup group : counters) {
					    System.out.print("\t");
					    System.out.println(group.getName());
					    for(Counter counter : group) {
					      System.out.print("\t\t");
					      System.out.print(counter.getName());
					      System.out.print(": ");
					      System.out.println(counter.getValue());
					    }
					  }
					}
				// Trace error
				if (submission.getExceptionInfo() != null) {
					System.out.println("Exception info : "
							+ submission.getExceptionInfo());
				}
				// Stop a running job
				// client.stopSubmission(newjob.getPersistenceId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
