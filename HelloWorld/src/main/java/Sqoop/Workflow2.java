package Sqoop;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

import java.io.File;
public class Workflow2 {
	
		
	   public static void main(String argv[]) {

	      try {
	         DocumentBuilderFactory dbFactory =
	         DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.newDocument();
	         // root element
	         Element rootElement = doc.createElement("workflow-app");
	         rootElement.setAttribute("name", "WorkflowWithSqoopAction");
	         rootElement.setAttribute("xmlns", "uri:oozie:workflow:0.1");
	         doc.appendChild(rootElement);
	         
	         Element start = doc.createElement("start");
	         start.setAttribute("to", "sqoopAction");
	         rootElement.appendChild(start);
	         
	         Element action = doc.createElement("action");
	         action.setAttribute("name", "sqoopAction");
	         //Attr attrType = doc.createAttribute("name");
	         //attrType.setValue("sqoopAction");
	         //action.setAttributeNode(attrType);
	         rootElement.appendChild(action);
	         
	         Element sqoop = doc.createElement("sqoop");
	         sqoop.setAttribute("xmlns", "uri:oozie:sqoop-action:0.2");
	         action.appendChild(sqoop);
	         
	         Element jobTracker = doc.createElement("job-tracker");
	         jobTracker.appendChild(doc.createTextNode("${jobTracker}"));
	         sqoop.appendChild(jobTracker);
	         
	         Element nameNode = doc.createElement("name-node");
	         nameNode.appendChild(doc.createTextNode("${nameNode}"));
	         sqoop.appendChild(nameNode);
	         
	         Element command = doc.createElement("command");
	         //command.appendChild(doc.createTextNode("import --connect jdbc:mysql://localhost/TEST  --username root --password cloudera --table pet --target-dir hdfs://quickstart.cloudera:8020/user/cloudera/oozieOutputFromJava  -m 1 "));
	         //command.appendChild(doc.createTextNode("import --connect jdbc:mysql://localhost/TEST --username=root --password=cloudera --table pet --hive-import -m 1 --hive-table=pet "));
	         String sqoopCmd="";
	         command.appendChild(doc.createTextNode("import --connect jdbc:mysql://localhost/retail_db --username=root --password=cloudera --table customers --hive-import -m 1 --hive-table=customers "));
	         sqoop.appendChild(command);
	         
	         Element ok = doc.createElement("ok");
	         ok.setAttribute("to", "end");
	         action.appendChild(ok);
	         
	         Element error = doc.createElement("error");
	         error.setAttribute("to", "fail");
	         action.appendChild(error);
	         
	         Element kill = doc.createElement("kill");
	         kill.setAttribute("name", "fail");
	         rootElement.appendChild(kill);
	         
	         Element message = doc.createElement("message");
	         message.appendChild(doc.createTextNode("Killed job due to error: ${wf:errorMessage(wf:lastErrorNode())}"));
	         kill.appendChild(message);
	         
	         Element end = doc.createElement("end");
	         end.setAttribute("name", "end");
	         rootElement.appendChild(end);
	         
	         // write the content into xml file
	         TransformerFactory transformerFactory =
	         TransformerFactory.newInstance();
	         Transformer transformer =
	         transformerFactory.newTransformer();
	         DOMSource source = new DOMSource(doc);
	         StreamResult result =
	         new StreamResult(new File("/home/cloudera/workflow.xml"));
	         transformer.transform(source, result);
	         // Output to console for testing
	         StreamResult consoleResult =
	         new StreamResult(System.out);
	         transformer.transform(source, consoleResult);
	         
	         	ConnBean cb = new ConnBean("quickstart.cloudera","cloudera","cloudera");
				SSHExec ssh = SSHExec.getInstance(cb);   
				ssh.connect();
	     		CustomTask task1 = new ExecCommand("echo $SSH_CLIENT"); 
	     		System.out.println(ssh.exec(task1));
	     		ssh.exec(task1);
	     		//CustomTask task3 = new ExecCommand("cd Desktop"); 
	     		//ssh.exec(task3);
	     		//System.out.println("*************** "+buildSqoopObj.getSqoopCommand());
	     		CustomTask task2 = new ExecCommand("hadoop fs -copyFromLocal workflow.xml oozieWorkflow");
	     		ssh.exec(task2);
	         
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	   
	}
}
