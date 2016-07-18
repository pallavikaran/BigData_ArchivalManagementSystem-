/*//package com.nielsen.ap.ndm.service.impl;
package Sqoop;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.nielsen.ap.common.enumeration.NRETaskTypes;
import com.nielsen.ap.ndm.common.exception.util.ExceptionUtil;
import com.nielsen.ap.ndm.common.util.Constants;
import com.nielsen.ap.ndm.common.util.SSHUtils;
import com.nielsen.ap.ndm.constants.CommonConstants;
import com.nielsen.ap.ndm.domain.Job;
import com.nielsen.ap.ndm.domain.Task;
import com.nielsen.ap.ndm.domain.TaskType;
import com.nielsen.ap.ndm.dto.JobDTO;
import com.nielsen.ap.ndm.dto.ResponseDTO;
import com.nielsen.ap.ndm.service.CreateOozieWorkflowService;
import com.nielsen.ap.ndm.service.TaskManagerService;

@Service
public class CreateOozieWorkflowServiceImpl implements
CreateOozieWorkflowService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateOozieWorkflowServiceImpl.class);

	@Value("${automated.workflow_base_path}")
	private String workflow_base_path;

	@Value("${nameNodeServer}")
	private String nameNodeURL;

	@Value("${secondaryNameNodeServer}")
	private String secondaryNameNodeURL;

	@Value("${ndmelementrecognitionjarpath}")
	private String ndmElementRecognitionJarPath;

	@Value("${commonframeworkjarpath}")
	private String commonFrameWorkJarPath;

	@Value ("${tempWorkflowFileLocation}")
	private String tempWorkflowFileLocation;

	@Value("${destFilePath}")
	private String destFilePath;

	@Value("${modelingFilePath}")
	private String modelingFilePath;

	@Autowired
	SSHUtils sshUtil;

	@Value("${edgenode.userName}")
	private String edgeNodeUserName;

	@Autowired
	ExceptionUtil exceptionUtil;


	static FileSystem fs = null;


	@Autowired
	TaskManagerService taskManagerService;
	TreeMap<Integer, ArrayList<Job>> allActions = new TreeMap<Integer, ArrayList<Job>>();

	HashMap<String, String> taskTypeActionXMLMap = new HashMap<String, String>();

	private static final String success_action = "\t<action name=\"success\">"
			+ "\n<java>"
			+ "\n<job-tracker>${jobTracker}</job-tracker>"
			+ "\n<name-node>${nameNode}</name-node>"
			+ "\n<main-class>com.nielsen.ap.nre.workflow.WorkFlowPostAction</main-class>"
			+ "\n<arg>${jsonMessage}</arg>" + "\n<capture-output />"
			+ "\n</java>" + "\n<ok to=\"end\"/>" + "\n<error to=\"fail\" />"
			+ "\n</action>";

	private static final String failure_action = "\t<action name=\"failure\">"
			+ "	<java>"
			+ "\n<job-tracker>${jobTracker}</job-tracker>"
			+ "\n<name-node>${nameNode}</name-node>"
			+ "\n<main-class>com.nielsen.ap.nre.workflow.WorkFlowPostAction</main-class>"
			+ "\n<arg>${jsonMessage}</arg>"
			+ "\n<arg>${wf:lastErrorNode()}</arg>"
			+ "\n<arg><![CDATA[${wf:errorMessage(wf:lastErrorNode())}]]></arg>"
			+ "\n<capture-output />" + "\n</java>" + "\n<ok to=\"fail\" />"
			+ "\n<error to=\"fail\" />" + "\n</action>";

	private static final String findnext_action = "<action name=\"$ACTIONNAME$\">\n"
			+ "<java>\n"
			+ "<job-tracker>${jobTracker}</job-tracker>\n"
			+ "<name-node>${nameNode}</name-node>\n"
			+ "<main-class>com.nielsen.ap.nre.workflow.FindNextTasktoExecute</main-class>\n"
			+ "<arg>${wf:actionData(\"prepare_properties\")[\"jobId\"]}</arg>\n"
			+ "<arg>${wf:actionData(\"prepare_properties\")[\"jobInstanceId\"]}</arg>\n"
			+ "<arg>${wf:actionData(\"prepare_properties\")[\"isUIReq\"]}</arg>\n"
			+ "<arg>${wf:actionData(\"prepare_properties\")[\"taskIdToExecute\"]}</arg>\n"
			+ "<capture-output/>\n"
			+ "</java>\n"
			+ "<ok to=\"$NEXTACTION$\"/>\n"
			+ "<error to=\"failure\"/>\n"
			+ "</action>\n";

	BufferedWriter outputwriter = null;
	FileInputStream fstrm = null;
	StringBuilder wf = new StringBuilder();
	HashMap<String, String> actionNmActionEleMap = new HashMap<String, String>();
	FsPermission fspermission = new FsPermission(FsAction.ALL,FsAction.ALL, FsAction.ALL);


	@Override
	public void createwf(Integer jobId) {

		String fileName = "workflow.xml";
		String jobWorkflowPath 		= "";//Path.SEPARATOR+workflow_base_path + jobId.intValue() +Path.SEPARATOR+CommonConstants.OOZIE_WF;
		String workflowLocationForDB = workflow_base_path+ jobId.intValue()+Path.SEPARATOR+CommonConstants.OOZIE_WF;

		LOGGER.debug("Workflow Creation started for jobId:" + jobId );


		allActions.clear();
		taskTypeActionXMLMap.clear();
		actionNmActionEleMap.clear();
		List<Job> jobList   = null;
		List<Integer> jobTaskIds=new ArrayList<Integer>();
		Map<Integer, Task> currJobTaskMap=new HashMap<Integer, Task>();
		long serviceId = 0;
		try {

			jobList = taskManagerService.getJobDetailsById(jobId);
			List<TaskType> taskTypes = taskManagerService.getTaskTypes();

			for (int i = 0; i < taskTypes.size(); i++) {
				taskTypeActionXMLMap.put(taskTypes.get(i).getName(), taskTypes.get(i).getActionElement());
			}

			if (jobList != null && !jobList.isEmpty()) {
				Iterator<Job> jobListIt = jobList.iterator();

				Job job;
				ArrayList<Job> sameTasks;
				while (jobListIt.hasNext()) {
					job = jobListIt.next();
					sameTasks = allActions.get(job.getTaskOrder());
					if (sameTasks == null) {
						sameTasks = new ArrayList<Job>();
						allActions.put(job.getTaskOrder(), sameTasks);
					}
					sameTasks.add(job);
					serviceId = job.getServiceId();
					jobTaskIds.add(job.getTaskId());
					LOGGER.debug("Workflow Creation : found tasks for :  jobId:" + job.getJobId() + " serviceId: "+ job.getServiceId() + " taskName:" + job.getTaskName() + " taskid: " + job.getTaskId() + " taskorder: " +job.getTaskOrder() );

				}
				
				
				currJobTaskMap=taskManagerService.getTasksByIds(jobTaskIds);
				wf.setLength(0);
				wf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				wf.append("<workflow-app xmlns=\"uri:oozie:workflow:0.4\" name=\"${wfName}\">\n");
				wf.append("\t<global>\n");
				wf.append("<job-tracker>${jobTracker}</job-tracker>\n");
				wf.append("<name-node>${nameNode}</name-node>\n");
				wf.append("<configuration>\n");
				wf.append("\t<property>\n");
				wf.append("<name>mapreduce.job.queuename</name>\n");
				wf.append("<value>${poolname}</value>\n");
				wf.append("\t</property>\n");
				wf.append("\t<property>\n");
				wf.append("<name>oozie.launcher.mapred.fairscheduler.pool</name>\n");
				wf.append("<value>${poolname}</value>\n");
				wf.append("\t</property>\n");
				wf.append("</configuration>\n");
				wf.append("\t</global>\n");
				wf.append("\t<start to=\"").append("prepare_properties").append("\"/>\n");
				wf.append("\t<action name=\"prepare_properties\">\n");
				wf.append("		<java>\n");
				wf.append("			<job-tracker>${jobTracker}</job-tracker>\n");
				wf.append("			<name-node>${nameNode}</name-node>\n");
				wf.append("			<main-class>com.nielsen.ap.nre.workflow.NREWorkflowPrepAction\n");
				wf.append("			</main-class>\n");
				wf.append("				<arg>${jsonMessage}</arg>\n");
				wf.append("			<capture-output />\n");
				wf.append("		</java>\n");
				wf.append("		<ok to=\"findNextActionAfter_prepare_properties\"/>\n");
				wf.append("		<error to=\"failure\"/>\n");
				wf.append("\t</action>\n");

				String findNextAction = this.findnext_action;
				findNextAction = findNextAction.replace("$ACTIONNAME$","findNextActionAfter_prepare_properties");
				findNextAction = findNextAction.replace("$NEXTACTION$","nextActionAfter_prepare_properties");
				wf.append(findNextAction).append("\n");
				wf.append(buildDecisionAction(-1, "prepare_properties",null,currJobTaskMap)).append("\n");

				Iterator<Integer> orderedTaskListIt = allActions.keySet().iterator();
				Integer taskOrder;
				int i = 0;
				ArrayList<Job> sameTaskOrderJobs;
				while (orderedTaskListIt.hasNext()) {

					i++;
					taskOrder = orderedTaskListIt.next();
					sameTaskOrderJobs = allActions.get(taskOrder);
					Collections.sort(sameTaskOrderJobs, new Comparator<Job>() {
						@Override
						public int compare(Job o1, Job o2) {

							int cmp = o1.getTaskOrder().compareTo(o2.getTaskOrder());

							if(cmp != 0) return cmp;
							return o1.getTaskId().compareTo(o2.getTaskId());
						}
					});

					for(int jobIndx=0;jobIndx<sameTaskOrderJobs.size();jobIndx++){

						job = allActions.get(taskOrder).get(jobIndx);

						String actionName = currJobTaskMap.get(job.getTaskId()).getTaskName().toLowerCase();// @todo need to work on
						// standardizing the action name

						LOGGER.debug(" Calling build Action for action " + actionName + " taskOrder " + taskOrder + " subindx " + jobIndx);

						boolean isLastTask = false;
						if( (i == (allActions.size()))  && (jobIndx == (sameTaskOrderJobs.size()-1)))
							isLastTask = true;
						wf.append(buildJavaActionfromTemplate(job,isLastTask,currJobTaskMap));


					}

				}

				wf.append(success_action).append("\n");

				wf.append(failure_action).append("\n");

				wf.append("\t<kill name=\"fail\">\n");
				wf.append("<message>Error encountered,message[${wf:errorMessage(wf:lastErrorNode())}]</message>\n");
				wf.append("\t</kill>\n");
				wf.append("\t<end name=\"end\" />\n");
				wf.append("</workflow-app>\n");

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(new InputSource(
						new StringReader(wf.toString())));

				Transformer xformer = TransformerFactory.newInstance()
						.newTransformer();
				xformer.setOutputProperty(OutputKeys.METHOD, "xml");
				xformer.setOutputProperty(OutputKeys.INDENT, "yes");
				xformer.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "4");
				xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
						"yes");
				Source source = new DOMSource(document);

				StringWriter strwriter = new StringWriter();
				Result result = new StreamResult(strwriter);
				xformer.transform(source, result);

				strwriter.flush();

				jobWorkflowPath = Path.SEPARATOR+workflow_base_path +  serviceId +Path.SEPARATOR_CHAR+jobId.intValue() +Path.SEPARATOR+CommonConstants.OOZIE_WF;

				writeFilestoHDFSUsingSSH(jobWorkflowPath, fileName, strwriter.toString(),jobId,serviceId);

				if (jobList != null && !jobList.isEmpty()) {
					jobListIt = jobList.iterator();

					Date currDate = new Date(System.currentTimeMillis());

					workflowLocationForDB = workflow_base_path+serviceId+Path.SEPARATOR+ jobId.intValue()+Path.SEPARATOR+CommonConstants.OOZIE_WF;
					while (jobListIt.hasNext()) {

						job = jobListIt.next();
						job.setWorkflowLocation(workflowLocationForDB);

						job.setModifiedDate(currDate);

					}
				}

				
				//update the job table with the new workflow location
				//taskManagerService.createOrUpdateJob(jobList.get(0),jobList, false);

				LOGGER.debug("Workflow creation successful for job id " + jobId);
			}


		} catch (Exception ex) {
			ex.printStackTrace();

			throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" creating workflow xml for the job!"+ex.getMessage()});
		} 
		finally {
			try {
				//update the job table with the new workflow location
				if(jobList != null && jobList.size()>0){
					workflowLocationForDB = workflow_base_path+serviceId+Path.SEPARATOR+ jobId.intValue()+Path.SEPARATOR+CommonConstants.OOZIE_WF;
					LOGGER.debug("Workflow creation : Finally calling update method to update Job table with workflow location : "  + jobWorkflowPath);
					taskManagerService.updateOozieWfLocationForJob(jobId, workflowLocationForDB);
				}
					
			} catch (Exception ex) {
				LOGGER.error("Exception occurred creating workflow..."+ex);
				ex.printStackTrace();
				throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" creating workflow xml for the job!"});
			}
		}
	}

	@SuppressWarnings("null")
	private String buildDecisionAction(Integer curSeqNbr,
			String incomingActionName,Task curTask, Map<Integer, Task> currJobTaskMap) {

		StringBuilder sb = new StringBuilder();

		String decisionActionName = "nextActionAfter_" + incomingActionName;
		LOGGER.debug(" CreateWorkflow building decision for action : " + incomingActionName);

		sb.append("\t<decision name=\"").append(decisionActionName)
		.append("\">\n");
		sb.append("<switch>\n");

		int seqNbr;
		Iterator<Integer> actionIt = allActions.keySet().iterator();
		String actionName = "end";
		ArrayList<Job> sameTaskOrderJobs;

		Job tempJob;
		while (actionIt.hasNext()) {
			seqNbr = actionIt.next();

			sameTaskOrderJobs= allActions.get(seqNbr);

			Collections.sort(sameTaskOrderJobs, new Comparator<Job>() {

				@Override
				public int compare(Job o1, Job o2) {

					int cmp = o1.getTaskOrder().compareTo(o2.getTaskOrder());

					if(cmp != 0) return cmp;
					return o1.getTaskId().compareTo(o2.getTaskId());
				}
			});

			int indexInArrayForthisTask = 0;
			if(curTask != null && (seqNbr == curSeqNbr)){
				for(int sametaskIndx=0;sametaskIndx < sameTaskOrderJobs.size();sametaskIndx++){

					tempJob = sameTaskOrderJobs.get(sametaskIndx);
					if(tempJob == null)
						LOGGER.debug(" sameTaskOrderJobs.get(sametaskIndx) is null  at index : "  + sametaskIndx + " list size " +sameTaskOrderJobs.size() + " taskOrder  " +seqNbr + " taskId " + tempJob.getTaskId());
					Task task = currJobTaskMap.get(sameTaskOrderJobs.get(sametaskIndx).getTaskId());

					if(task.getTaskId().longValue() == curTask.getTaskId().longValue()){

						break;
					}
					indexInArrayForthisTask++;

				}
			}
			for(int sametaskIndx=indexInArrayForthisTask;sametaskIndx < sameTaskOrderJobs.size();sametaskIndx++){

				//				if(curTask != null){
				//					if( (sameTaskOrderJobs.size() > 1) && (sametaskIndx < indexInArrayForthisTask))
				//						continue;
				//				}
				//				Task task = taskManagerService.getTaskById(allActions.get(seqNbr).get(0).getTaskId());
				Task task = currJobTaskMap.get(sameTaskOrderJobs.get(sametaskIndx).getTaskId());
				actionName = task.getTaskName();

				String actionElement = taskTypeActionXMLMap.get(task.getTaskType());
				int begin_indx = actionElement.indexOf("name=\"");
				int end_indx = actionElement.indexOf("\"", begin_indx + 7);

				actionName = actionElement.substring(begin_indx + 6, end_indx);

				actionName = actionName + "_"
						+ allActions.get(seqNbr).get(sametaskIndx).getTaskId();
				boolean appendNextActionName = false;
				if (seqNbr > curSeqNbr) {
					appendNextActionName = true;
				}else
					if(seqNbr == curSeqNbr){
						if(curTask.getTaskId().longValue() != task.getTaskId().longValue())
							appendNextActionName = true;
					}

				actionName = actionName.toLowerCase();

				if(appendNextActionName){
					sb.append("\t<case to=\"")
					.append(actionName)
					.append("\">")
					.append("${wf:actionData(\"")
					.append("findNextActionAfter_" + incomingActionName)
					.append("\")[\"nextActionName\"] == \"" + actionName
							+ "\"}").append("</case>\n");
					
//					if(actionName.startsWith("import_data") && incomingActionName.equalsIgnoreCase("prepare_properties") ){
					if(incomingActionName.equalsIgnoreCase("prepare_properties") ){
						String updateTaskStatusActionName = "updatetaskstatusaction_"+actionName;
						
						sb.append("\t<case to=\"")
						.append(updateTaskStatusActionName)
						.append("\">")
						.append("${wf:actionData(\"")
						.append("findNextActionAfter_" + incomingActionName)
						.append("\")[\"nextActionName\"] == \"" + updateTaskStatusActionName
								+ "\"}").append("</case>\n");
					}
					
				}
				
				
			}

			//			Task task = taskManagerService.getTaskById(allActions.get(seqNbr).get(0).getTaskId());
			//			actionName = task.getTaskName().toLowerCase();
			//
			//			String actionElement = taskTypeActionXMLMap.get(task.getTaskType());
			//			int begin_indx = actionElement.indexOf("name=\"");
			//			int end_indx = actionElement.indexOf("\"", begin_indx + 7);
			//
			//			actionName = actionElement.substring(begin_indx + 6, end_indx);
			//
			//			actionName = actionName + "_"
			//					+ allActions.get(seqNbr).get(0).getTaskId();
			//
			//			if (seqNbr > curSeqNbr) {
			//				sb.append("\t<case to=\"")
			//				.append(actionName)
			//				.append("\">")
			//				.append("${wf:actionData(\"")
			//				.append("findNextActionAfter_" + incomingActionName)
			//				.append("\")[\"nextActionName\"] == \"" + actionName
			//						+ "\"}").append("</case>\n");
			//			}

		}
		sb.append("\t<default to=\"success\"/>\n");

		sb.append("</switch>\n");
		sb.append("\t</decision>\n");

		return sb.toString();
	}

	private String buildJavaActionfromTemplate(Job job,boolean isLastTask, Map<Integer, Task> currJobTaskMap) {

		try{

			Task task = currJobTaskMap.get(job.getTaskId());

			StringBuilder sb = new StringBuilder();
			LOGGER.debug("Workflow creation : Building JavaAction for " + task.getTaskName() + " : "+ task.getTaskType());

			String actionElement = taskTypeActionXMLMap.get(task.getTaskType());
			String basePropName=(getBaseTaskPropertyName(task.getTaskId().intValue(),task.getTaskType()));
			String jobInstancePropName=basePropName+"JobInstanceId";
			String taskIdPropName=basePropName+"TaskId";
			String taskNamePropName=basePropName+"TaskName";
			String dataSourceIdPropName=basePropName+"DataSourceId";
			String taskTypePropName=basePropName+"TaskType";
			String factDictionaryLocation = basePropName+"DictionaryLocation";
			String isReverseAttrInheritance	 = basePropName+"ReverseAttrInheritance";
			String pubDimNA = basePropName+"DimNA";
			String pubisOnlyMasters = basePropName+"isOnlyMasters";
			String pubisAlias   = basePropName+"isAlias";
			String isPubQCTrue  = basePropName+"isPubQCTrue";
			String unitSalesFactId=basePropName+"UnitSalesFactId";
			String valueSalesFactId=basePropName+"ValueSalesFactId";
			String promoFlagFactId=basePropName+"PromoFlagFactId";
			String itemisonpromoFactId=basePropName+"ItemisonpromoFactId";
			String noOfInputDays=basePropName+"NoOfInputDays";
			String enforceRestatementFlag=basePropName+"EnforceRestatementFlag";
			String runPromoCalculation=basePropName+"RunPromoCalculation";
			String simulationMode=basePropName+"SimulationMode";
			String validTimeField=basePropName+"ValidTimeField";
			String clearMasterReprocess=basePropName+"ClearMasterReprocess";
			String processEverything=basePropName+"ProcessEverything";
			String autoFlagLatestDate=basePropName+"AutoFlagLatestDate";  
			String forceRestatementOverrideList=basePropName+"ForceRestatementOverrideList";
			String underScoreTaskId = Constants.UNDER_SCORE+String.valueOf(task.getTaskId());

			actionElement = actionElement.replace(wrapWithBrackets(jobInstancePropName), wrapWithBrackets(jobInstancePropName+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(taskIdPropName), wrapWithBrackets(taskIdPropName+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(taskNamePropName), wrapWithBrackets(taskNamePropName+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(dataSourceIdPropName), wrapWithBrackets(dataSourceIdPropName+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(taskTypePropName), wrapWithBrackets(taskTypePropName+underScoreTaskId));
			//comment out the below lines
			//fact calculation specific
			actionElement = actionElement.replace(wrapWithBrackets(factDictionaryLocation), wrapWithBrackets(factDictionaryLocation+underScoreTaskId));

			actionElement = actionElement.replace(wrapWithBrackets(isReverseAttrInheritance), wrapWithBrackets(isReverseAttrInheritance+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(pubDimNA), wrapWithBrackets(pubDimNA+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(pubisOnlyMasters), wrapWithBrackets(pubisOnlyMasters+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(pubisAlias), wrapWithBrackets(pubisAlias+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(isPubQCTrue), wrapWithBrackets(isPubQCTrue+underScoreTaskId));

			actionElement = actionElement.replace(wrapWithBrackets(unitSalesFactId), wrapWithBrackets(unitSalesFactId+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(valueSalesFactId), wrapWithBrackets(valueSalesFactId+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(promoFlagFactId), wrapWithBrackets(promoFlagFactId+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(itemisonpromoFactId), wrapWithBrackets(itemisonpromoFactId+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(noOfInputDays), wrapWithBrackets(noOfInputDays+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(enforceRestatementFlag), wrapWithBrackets(enforceRestatementFlag+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(runPromoCalculation),wrapWithBrackets( runPromoCalculation+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(simulationMode), wrapWithBrackets(simulationMode+underScoreTaskId));	
			
			actionElement = actionElement.replace(wrapWithBrackets(validTimeField), wrapWithBrackets(validTimeField+underScoreTaskId));	
			actionElement = actionElement.replace(wrapWithBrackets(clearMasterReprocess), wrapWithBrackets(clearMasterReprocess+underScoreTaskId));	
			actionElement = actionElement.replace(wrapWithBrackets(processEverything), wrapWithBrackets(processEverything+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(autoFlagLatestDate), wrapWithBrackets(autoFlagLatestDate+underScoreTaskId));
			actionElement = actionElement.replace(wrapWithBrackets(forceRestatementOverrideList), wrapWithBrackets(forceRestatementOverrideList+underScoreTaskId));
			
			String actionNm = "";

			if (!StringUtils.isBlank(actionElement)) {

				actionElement = "\t" + actionElement;

				int begin_indx = actionElement.indexOf("name=\"");
				int end_indx = actionElement.indexOf("\"", begin_indx + 7);

				actionNm = actionElement.substring(begin_indx + 6, end_indx).toLowerCase();
				actionNm = actionNm + "_" + job.getTaskId();
				StringBuffer sbf = new StringBuffer(actionElement);
				sbf.replace(begin_indx + 6, end_indx, actionNm);

				actionElement = sbf.toString();

				int okbegin_indx = actionElement.toString().indexOf("<ok");
				int tobegin_indx = actionElement.toString().indexOf("to=\"",
						okbegin_indx + 1);
				int toend_indx = actionElement.indexOf("\"", tobegin_indx + 4);

				// if this action is not the last action in the sequence
				if(!isLastTask){
					sbf.replace(tobegin_indx + 4, toend_indx,
							"findNextActionAfter_" + actionNm.toLowerCase());
				} else {
					sbf.replace(tobegin_indx + 4, toend_indx, "success");
				}
				actionElement = sbf.toString();
				sb.append(actionElement).append("\n");

				//				if (job.getTaskOrder() < allActions.size()) {
				if(!isLastTask){

					String findNextAction = this.findnext_action;
					findNextAction = findNextAction.replace("$ACTIONNAME$",
							"findNextActionAfter_" + actionNm);
					findNextAction = findNextAction.replace("$NEXTACTION$",
							"nextActionAfter_" + actionNm);
					//				findNextAction = findNextAction.replace("$TASK_ID$", ""+task.getTaskId());

					sb.append(findNextAction).append("\n");
					sb.append(buildDecisionAction(job.getTaskOrder(), actionNm,task,currJobTaskMap))
					.append("\n");
					
				}
//				if(task.getTaskType().equalsIgnoreCase(NRETaskTypes.IMPORT.getDesc())){
					sb.append(buildUpdateTaskStatusAction(job,actionNm,task)).append("\n");
//				}

			}
			return sb.toString();
		}catch(Exception ex){
			ex.printStackTrace();
			throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" Exception building java action for workflow!"+ex.getMessage()});
		}
	}
	
	
	private String buildUpdateTaskStatusAction(Job job,String incomingActionName,Task task){
		
		
		StringBuffer updateTaskStatucActionEle = new StringBuffer();
		String fullIncomingActionName = "findNextActionAfter_prepare_properties";//"findNextActionAfter_" + incomingActionName;
		String actionName = "updatetaskstatusaction_"+incomingActionName;
				
		updateTaskStatucActionEle.append("<action name=\""+actionName+"\">\n"); 
		updateTaskStatucActionEle.append("<java>\n");
		updateTaskStatucActionEle.append("<job-tracker>${jobTracker}</job-tracker> \n"); 
		updateTaskStatucActionEle.append("<name-node>${nameNode}</name-node> \n"); 
		updateTaskStatucActionEle.append("<main-class>com.nielsen.ap.nre.workflow.UpdateTaskStatus \n"); 
		updateTaskStatucActionEle.append("</main-class> \n"); 
		updateTaskStatucActionEle.append("<arg>${wf:actionData('"+fullIncomingActionName+"')['jobInstanceId']}</arg> \n"); 
		updateTaskStatucActionEle.append("<arg>${wf:actionData('"+fullIncomingActionName+"')['taskName']}</arg> \n"); 
		updateTaskStatucActionEle.append("<arg>${wf:actionData('"+fullIncomingActionName+"')['taskId']}</arg> \n"); 
		updateTaskStatucActionEle.append("<arg>${wf:actionData('"+fullIncomingActionName+"')['taskType']}</arg> \n"); 
		updateTaskStatucActionEle.append("<arg>${wf:actionData('"+fullIncomingActionName+"')['dataSourceId']}</arg> \n"); 
		updateTaskStatucActionEle.append("<arg>${wf:actionData('"+fullIncomingActionName+"')['serviceId']}</arg> \n"); 
		updateTaskStatucActionEle.append("<arg>${wf:actionData('"+fullIncomingActionName+"')['jobId']}</arg> \n"); 
		updateTaskStatucActionEle.append("<arg>${wf:actionData('"+fullIncomingActionName+"')['countryCode']}</arg> \n"); 
		updateTaskStatucActionEle.append("<capture-output /> \n"); 
		updateTaskStatucActionEle.append("</java> \n"); 
		 
		updateTaskStatucActionEle.append("<ok to=\"success\" /> \n"); 
		updateTaskStatucActionEle.append("<error to=\"failure\" /> \n"); 
		updateTaskStatucActionEle.append("</action>\n"); 
		
		return updateTaskStatucActionEle.toString();
	}



	public ResponseDTO copyJob(Integer jobId,String newJobName){	
		try{


			JobDTO existingJob = taskManagerService.getJobDetailsByJobId(jobId);
			JobDTO newJob      =(JobDTO)deepClone(existingJob);

			newJob.setJobId(null);
			newJob.setJobName(newJobName);
			newJob.setWorkflowLocation(null);

			return taskManagerService.saveOrUpdateJob(newJob);
		}catch(Exception ex){
			ex.printStackTrace();
			throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" Exception copying job!"+ex.getMessage()});
		}
	}

	public void deleteJobById(Integer jobId){	
		try{
			Job job = taskManagerService.getJobById(jobId);
			Long serviceId = job.getServiceId();
			String workflowPath = Path.SEPARATOR+workflow_base_path +serviceId+"/"+ jobId;
			
			
			StringBuffer commandbf =new StringBuffer();
			commandbf.append("hadoop fs -rm -r " + workflowPath);
			
			sshUtil.executeSSHCommand(commandbf.toString());
//			getHDFSFileSystem();
			
			

//			fs.delete(new Path(workflowPath), true);

		}catch(Exception ex){
			ex.printStackTrace();
			throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" Exception deleting job!"+ex.getMessage()});
		}
	}
	public Object deepClone(Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


//	private FileSystem getHDFSFileSystem(){
//
//		try{
//			String workflowbasepath = Path.SEPARATOR+workflow_base_path;
//			Configuration conf = new Configuration();
//			conf.set("fs.defaultFS", nameNodeURL+":8020");
//
//			if(fs != null){
//				if(!fs.getConf().get("fs.defaultFS").equalsIgnoreCase(conf.get("fs.defaultFS"))){
//					fs = FileSystem.newInstance(conf);
//				}
//			}else{
//
//				FileStatus[] fstats = null;
//				try{
//
//					fs = FileSystem.newInstance(conf);
//					fstats = fs.listStatus(new Path(workflowbasepath));
//					LOGGER.debug( " Connection to hdfs filesystem at nameNode:  " + nameNodeURL + " successful");
//				}catch(Exception ex){
//					try{
//						LOGGER.debug( " Could not connect to  HDFS FileSystem at nameNode:  " + nameNodeURL + " so, trying standbyNameNode" + secondaryNameNodeURL);
//						conf.set("fs.defaultFS", secondaryNameNodeURL+":8020");
//						fs = FileSystem.get(conf);
//						fstats = fs.listStatus(new Path(workflowbasepath));
//					}catch(Exception exe){
//						exe.printStackTrace();
//
//						LOGGER.debug( " Could not connect to  HDFS FileSystem at standbyNameNode: "+ secondaryNameNodeURL);
//						throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" Exception creating HDFSFileSystem!",exe.getMessage()});
//					}
//
//				}
//
//			}
//		}catch(Exception ex){
//			throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" Exception creating HDFSFileSystem!"+ex.getMessage()});
//		}
//		return fs;
//	}

	*//**
	 * Gives the string representation of taskId
	 * @param taskId
	 * @param string 
	 * @return
	 *//*
	private String getBaseTaskPropertyName(Integer taskId, String taskType){
	    String baseName="";
	    if(taskType.equalsIgnoreCase(NRETaskTypes.IMPORT.getDesc())){
	        baseName="import";
	    }else  if(taskType.equalsIgnoreCase(NRETaskTypes.MANUAL_IMPORT.getDesc())){
	        baseName="manualimport";
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.AUTO_CROSS_CODE.getDesc()) ){
	        baseName="acc"; 
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.ATTRIBUTION.getDesc())){
	        baseName="attr"; 
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.PREDICTED_SALES_CALCULATION.getDesc())){
	        baseName="oos"; 
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.FACT_CALCULATION.getDesc())){
	        baseName="fact"; 
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.PUBLISHING.getDesc()) || taskType.equalsIgnoreCase(NRETaskTypes.PUBLISHING_TO_QC.getDesc()) ){
	        baseName="pub"; 
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.CROSS_CODE_BY_ELEMENT_AND_ATTRIBUTE.getDesc())){
	        baseName="accElAttr"; 
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.CLEAR_ATTRIBUTE_VALUES.getDesc())){
	        baseName="clearAttrVal"; 
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.MASTER_RENAMING.getDesc())){
	        baseName="renameMaster"; 
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.DELETE_DATA.getDesc())){
	        baseName="deleteData";
	    }
	    else if(taskType.equalsIgnoreCase(NRETaskTypes.DELETE_ELEMENTS.getDesc())){
	        baseName="deleteElements";
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.CROSS_CODE_AND_ATTRIBUTE_REPORT.getDesc())){
	        baseName="ccAttrRpt";
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.VIEW_DATA.getDesc())){
	        baseName="viewData";
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.HISTORY_LOAD.getDesc())){
	        baseName="historyLoad";
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.CLEAR_PROCESS.getDesc())){
	        baseName="clearProcess";
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.DELETE_ELEMENTS_WITHOUT_DATA_POINTS.getDesc())){
		baseName="deleteElWDPs";
	    }else if(taskType.equalsIgnoreCase(NRETaskTypes.EMAIL_NOTIFICATIONS.getDesc())){
		baseName="notify";
	    }
	    return baseName;
	}



//	public boolean copyJarFilestoLib(Integer jobId){
//		try{
//
//			String workflowLibPath = Path.SEPARATOR+workflow_base_path + jobId +Path.SEPARATOR+CommonConstants.OOZIE_WF+Path.SEPARATOR+ "lib";
//			Path libPath = new Path(workflowLibPath);
//			Path ndmElejarPath 		= new Path(ndmElementRecognitionJarPath);
//			Path commonFrwkjarPath  = new Path(commonFrameWorkJarPath);
//
//			getHDFSFileSystem();
//
//			if(!fs.exists(libPath))
//				fs.mkdirs(libPath,fspermission);
//
//			FileUtil.copy(fs, ndmElejarPath, fs, libPath, false, true, fs.getConf());
//			FileUtil.copy(fs, commonFrwkjarPath, fs, libPath, false, true, fs.getConf());
//
//			FsShell fsshell = new FsShell(fs.getConf());
//			try {
//
//				fsshell.run(new String[]{"-chmod","-R","777",Path.SEPARATOR+workflow_base_path+Path.SEPARATOR+jobId});
//				//				 fsshell.run(new String[]{"-chown","-R",edgeNodeUserName,Path.SEPARATOR+workflow_base_path+Path.SEPARATOR+jobId});
//			}
//			catch (  Exception e) {
//				LOGGER.error("Couldnt change the file permissions ",e);
//				throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" setting permissions - creating workflow xml for the job!"+e.getMessage()});
//			}
//
//
//		}catch(Exception ex){
//			LOGGER.error("Couldnt copy jar files to the lib directory ", ex);
//			throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" exception copying jars files to lib !"+ex.getMessage()});
//		}
//		return false;
//	}

	private String wrapWithBrackets(String unwrapped){
		return "[\""+unwrapped+"\"]";
	}


	private boolean writeFilestoHDFSUsingSSH(String jobWorkflowPath,String fileName,String fileContent,Integer jobId,Long serviceId){
		try{
			long startTime = System.currentTimeMillis();

//			String nasLocationforwf = destFilePath+Path.SEPARATOR+tempWorkflowFileLocation;
			String nasLocationforwf = destFilePath+tempWorkflowFileLocation;

			String wfLocalFileNm  = "workflow_"+serviceId+"_"+jobId+".xml";

//			String fullPathToWorkflowxml = nasLocationforwf+Path.SEPARATOR+wfLocalFileNm;
			String fullPathToWorkflowxml = nasLocationforwf+Path.SEPARATOR+wfLocalFileNm;

			File pathtodir = new File(nasLocationforwf);
			if((!pathtodir.exists())) {
				pathtodir.mkdirs();
				Process p = null;
				try {
					p = Runtime.getRuntime().exec("chmod -R 777 nasLocationforwf");
				}catch(Exception ex){
					
					throw exceptionUtil.createNDMException("common.dbexception", new Object[]{"Writing workflow and changing permission in NAS -" + jobWorkflowPath+ex.getMessage()});
				}
//				sshUtil.executeSSHCommand("chmod -R 777 destFilePath");
			}
			File wfLocalfile = new File(fullPathToWorkflowxml);

			FileUtils.writeStringToFile(wfLocalfile, fileContent);

			if(wfLocalfile.exists()){
				LOGGER.debug("successfully created temp workflow xml file : " + fullPathToWorkflowxml + " - "+wfLocalfile.getPath());
			}else{
				LOGGER.debug("workflow xml file creation had issues ****** : " + fullPathToWorkflowxml);
			}


			
			long sshstartTime = System.currentTimeMillis();
			String hdfsPathtoFileStr = jobWorkflowPath+Path.SEPARATOR+fileName;
			
//			String fullPathToWorkflowxmlonEN = modelingFilePath+Path.SEPARATOR+tempWorkflowFileLocation+Path.SEPARATOR+wfLocalFileNm;
			String fullPathToWorkflowxmlonEN = modelingFilePath+tempWorkflowFileLocation+Path.SEPARATOR+wfLocalFileNm;

			StringBuffer commandbf =new StringBuffer();
			commandbf.append("hadoop fs -mkdir -p " +jobWorkflowPath).append(";");
			commandbf.append("hadoop fs -copyFromLocal -f ").append(fullPathToWorkflowxmlonEN).append(" ").append(hdfsPathtoFileStr).append(";");
			commandbf.append("hadoop fs -chmod -R 777 ").append(jobWorkflowPath).append(";");
			
//			sshUtil.executeSSHCommand("hadoop fs -mkdir -p " +jobWorkflowPath);			
//			sshUtil.executeSSHCommand("hadoop fs -copyFromLocal -f " + fullPathToWorkflowxmlonEN + " "+hdfsPathtoFileStr);
//			sshUtil.executeSSHCommand("hadoop fs -chmod -R 777 " + jobWorkflowPath);

			sshUtil.executeSSHCommand(commandbf.toString());
			
			wfLocalfile.delete();
			long endTime = System.currentTimeMillis();
			LOGGER.debug(" SSH Time taken to copy workflow xml from nas "+ fullPathToWorkflowxmlonEN +"  to HDFS at " + hdfsPathtoFileStr + " is " + ((endTime-sshstartTime)/1000) + " seconds ");
			
			LOGGER.debug(" Total Time taken to copy workflow xml to HDFS at " + hdfsPathtoFileStr + " is " + ((endTime-startTime)/1000) + " seconds ");

		}catch(Exception ex){
			ex.printStackTrace();

			throw exceptionUtil.createNDMException("common.dbexception", new Object[]{"writing workflow xml using SSH to HDFS for the job -" + jobWorkflowPath+ex.getMessage()});
		}

		return true;
	}

//	private boolean writeFilestoHDFS(String jobWorkflowPath,String fileName,String fileContent){
//
//
//		if(!jobWorkflowPath.startsWith("/"))
//			jobWorkflowPath = Path.SEPARATOR+jobWorkflowPath;
//		if(!jobWorkflowPath.endsWith("/"))
//			jobWorkflowPath = jobWorkflowPath + Path.SEPARATOR;
//
//
//		String hdfsPathtoFileStr = jobWorkflowPath+fileName;
//
//
//
//		try{
//
//			getHDFSFileSystem();
//
//			Path hdfsPathtoFile = new Path(hdfsPathtoFileStr);
//
//			if(fs.exists(hdfsPathtoFile))
//				fs.delete(hdfsPathtoFile, false);
//
//			outputwriter = new BufferedWriter(new OutputStreamWriter(fs.create(hdfsPathtoFile, true)));
//
//			outputwriter.write(fileContent);
//			fs.setPermission(hdfsPathtoFile, fspermission);
//			//
//			outputwriter.close();
//
//			FsShell fsshell = new FsShell(fs.getConf());
//			try {
//
//				fsshell.run(new String[]{"-chmod","-R","777",jobWorkflowPath});
//			}
//			catch (  Exception e) {
//				LOGGER.error("Couldnt change the file permissions for  " + jobWorkflowPath,e);
//				throw exceptionUtil.createNDMException("common.dbexception", new Object[]{" setting permissions - creating workflow xml for the job - "+ jobWorkflowPath+e.getMessage()});
//			}
//		}catch(Exception ex){
//			throw exceptionUtil.createNDMException("common.dbexception", new Object[]{"writing workflow xml to HDFS for the job -" + jobWorkflowPath+ex.getMessage()});
//		}finally {
//			try {
//				if (outputwriter != null)
//					outputwriter.close();
//
//			} catch (Exception ex) {
//				LOGGER.error("Exception occurred creating workflow..."+ex);
//				ex.printStackTrace();
//				throw exceptionUtil.createNDMException("common.dbexception", new Object[]{"writing workflow xml to HDFS for the job -" + jobWorkflowPath+ex.getMessage()});
//			}
//		}
//
//		LOGGER.debug("Successfully written " + fileName + " to location " + jobWorkflowPath);
//
//		return true;
//
//	}
}*/