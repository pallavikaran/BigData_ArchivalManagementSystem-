package controller;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
/* This controller is the main class which navigates between views and model */ 
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import FormBean.MySqlFormBean;
import connectionConfiguration.ClusterConnection;
import pojo.ClusterConnectionFormBean;
import pojo.HiveFormBean;
import pojo.RDBMSConnectionFormBean;
import pojo.SqoopJobFormBean; 
import pojo.LoginBean;
import pojo.OozieJobFormBean;
import dataAccessObject.MysqlDAO;
import pojo.DBToArchiveFormBean;
import dataAccessObject.*;
import FormBean.OracleFormBean;
import Sqoop.Workflow1;
import Sqoop.OozieJAVAAPI;


@Controller
@Scope("session")
public class MainController {
	
	//Object Declaration
	RDBMSConnectionFormBean objRDBMSConnectionFormBean =new RDBMSConnectionFormBean();
	ClusterConnectionFormBean objClusterConnectionFormBean=new ClusterConnectionFormBean();
	HiveFormBean objHiveFormBean= new HiveFormBean();
	LoginBean objLoginBean =new LoginBean();
	DBToArchiveFormBean objDBToArchiveFormBean =new DBToArchiveFormBean();
	OracleDBDAO objOracleDBDAO=new OracleDBDAO();
	MysqlDAO objMySqlDAO=new MysqlDAO();
	MySqlFormBean objMySqlFormBean = new MySqlFormBean();
	OracleFormBean objOracleFormBean=new OracleFormBean();
	Workflow1 objWorkflow1=new Workflow1();
	OozieJobFormBean objOozieJobFormBean=new OozieJobFormBean();
	OozieJAVAAPI objOozieJAVAAPI=new OozieJAVAAPI();
	
	String oracleDriver="oracle.jdbc.OracleDriver";
	String teradataDriver="com.teradata.jdbc.TeraDriver";
	String mysqlDriver="com.mysql.jdbc.Driver";
	
	/*Login to AMS System.
	  DB Type: mysql, localhost, root, cloudera
	  DB Name: metadata_storage
	  DB Table for this authentication: metadata_users */
	//@Scope("session")
	@RequestMapping("/amsLoginSubmit")
	public String LoginToAMSystem(@RequestParam String ams_username,String ams_password){
		String returnStr=null;
		try {
			if(ams_username!=null && !ams_username.equalsIgnoreCase("") && ams_password!=null && !ams_password.equalsIgnoreCase("")){
				objMySqlFormBean.setUsername(ams_username);
				objMySqlFormBean.setPassword(ams_password);
				if(objMySqlDAO.amsUserAuthetication(objMySqlFormBean)==true){//Fetch password and match with input password string
					returnStr="ams";
					//request.getSession().setAttribute("sessionUserName", objMySqlFormBean.getUsername());
				}
				else{
					returnStr="error";
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return returnStr;
	}
	
	
	//Selection between existing system and new registering System.
	@RequestMapping("/submitSystemType")
	public String doForwardForSystemType(@RequestParam String radiogroup){
		String returnStr=null;
		try {
			if(radiogroup!=null && !radiogroup.equalsIgnoreCase("")){
				objLoginBean.setDbselcted(radiogroup.toString());
			}
			else{
				objLoginBean.setDbselcted("");
			}
			
			if(objLoginBean.getDbselcted().equalsIgnoreCase("existSys")){
				returnStr="existingSystemLogin"; 
			}
			
			if(objLoginBean.getDbselcted().equalsIgnoreCase("newsys")){
				returnStr="registerNewSystem"; 
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return returnStr;
	}
	
	//Population/Selecting the connection string based on the DB Type
	@RequestMapping(value = "/submit1", method = RequestMethod.POST)
	public ModelAndView doForward(@RequestParam String radiogroup, HttpServletRequest request,
	        HttpServletResponse response){
		String returnStr=null;
		ModelAndView model=null; 
		try {
			if(radiogroup!=null && !radiogroup.equalsIgnoreCase("")){
				objLoginBean.setDbselcted(radiogroup.toString());
			}
			else{
				objLoginBean.setDbselcted("");
			}
			ArrayList conList=new ArrayList();
			//Map<Integer,String> metadata;
			if(objLoginBean.getDbselcted().equalsIgnoreCase("oracle")){
				model= new ModelAndView("mysqlConForOracle");
				conList=objMySqlDAO.mysqlMetaDataStoreFetch();//Fetch the metadata from MYSQL table metadata_store in metadata_storage DB				
				System.out.println("PPPPPPPPPPPPPP"+conList);
			    model.addObject("msg", conList);
			}
			
			//mysql
			if(objLoginBean.getDbselcted().equalsIgnoreCase("mysql")){
				model= new ModelAndView("mysqlConForMysql");
				conList=objMySqlDAO.mysqlMetaDataStoreFetch();	
				String text = conList.toString().replace("[", "").replace("]", "");
				System.out.println("MYSQLMMYSQLMYSQL: "+text);
			    model.addObject("msg", text);
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return model;
	}

		//Populate/Select the list of tables from oracle DB
		@RequestMapping(value = "/submitTheConnection", method = RequestMethod.POST)
		public ModelAndView selectConnectiondetail(@RequestParam String selectedConn, HttpServletRequest request,
		        HttpServletResponse response){
			String returnStr=null;
			ModelAndView model = new ModelAndView("listOfOracleTables");
			try {
				if(selectedConn!=null && !selectedConn.equalsIgnoreCase("")){
					objOracleFormBean.setSelectedOracleDBConString(selectedConn);
					System.out.println(objOracleFormBean.getSelectedOracleDBConString());
					ArrayList conList=new ArrayList();
					conList=objOracleDBDAO.oraclePopulateTables(objOracleFormBean);	//Fetch list of tables in Oracle based connection string			
					System.out.println("@@@@@@@@@@"+conList);
				    model.addObject("listOftables", conList);
					}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return model;
		}
		
		//After Table selection, building sqoop command & workflow.xml & Job properties file
		@RequestMapping(value = "/submitMysqlTableSelection", method = RequestMethod.POST)
		public ModelAndView buildSqoopCommandForMySql(@RequestParam String selectedTables, String connectionStr, HttpServletRequest request,
		        HttpServletResponse response){
			ModelAndView model=null;// = new ModelAndView("oozieJobPage");
			try {
				if(selectedTables!=null && !selectedTables.equalsIgnoreCase("") && connectionStr!=null && !connectionStr.equalsIgnoreCase("")){
					objClusterConnectionFormBean.setConnectionStringSelected(connectionStr);
					String tablename=StringUtils.substringBefore(selectedTables, "[").trim();
					objClusterConnectionFormBean.setTableselected(tablename);
					if(objWorkflow1.createWorkflowXML(objClusterConnectionFormBean)==true){ //for workflow.xml
						String appPath="${nameNode}/user/cloudera/oozieWorkflow/workflow.xml";
						objOozieJobFormBean=objOozieJAVAAPI.executeOzzieSqoopJob(appPath);//for Job.properties 
						model=new ModelAndView("oozieJobPage");
						model.addObject("jobID", objOozieJobFormBean.getOozieJobID());
						model.addObject("jobStatus", objOozieJobFormBean.getOozieJobStatus());
						
					}
					else{
						model=new ModelAndView("workflowFail");
						model.addObject("error", "Workflow.xml File Already Exists in that location.");
					}
					/*objOracleFormBean.setSelectedOracleDBConString(selectedConn);
					System.out.println(objOracleFormBean.getSelectedOracleDBConString());
					ArrayList conList=new ArrayList();
					conList=objOracleDBDAO.oraclePopulateTables(objOracleFormBean);				
					System.out.println("@@@@@@@@@@"+conList);
				    model.addObject("listOftables", conList);*/
					}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return model;
		}
		
				//Populate the tables from mySql DB based on connection string with database name
				@RequestMapping(value = "/submitTheConnectionMysql", method = RequestMethod.POST)
				public ModelAndView selectConnectiondetailMysql(@RequestParam String selectedConn, HttpServletRequest request,
				        HttpServletResponse response){
					String returnStr=null;
					ModelAndView model = new ModelAndView("listOfMysqlTables");
					try {
						if(selectedConn!=null && !selectedConn.equalsIgnoreCase("")){
							objMySqlFormBean.setSelectedMysqlDBConString(selectedConn);
							System.out.println(objMySqlFormBean.getSelectedMysqlDBConString());
							ArrayList conList=new ArrayList();
							conList=objMySqlDAO.mysqlPopulateTables(objMySqlFormBean);//Fetch the list of tables			
							System.out.println("@@@@@@@@@@"+conList);
						    model.addObject("listOftables", conList);
						    model.addObject("connectionStr",objMySqlFormBean.getSelectedMysqlDBConString());
							}
					}
					catch(Exception e){
						e.printStackTrace();
					}
					return model;
				}
		
	
	//Save new Archival registry System Details. for MYSQL, ORACLE, TERADATA and building connection string to save
	@RequestMapping("/submitNewSystemEntry")
	public String doForwardToregisterNewSystem(@RequestParam String radiogroup, String rHostName, String rPortNumber,
			String rServiceName, String rUsername, String rPassword, String radiogroup1){
		String returnStr=null;
		String strDBDriver=null;
		String strConnectionString=null;
		try {
			if(radiogroup!=null && !radiogroup.equalsIgnoreCase("")){
				objDBToArchiveFormBean.setrDbselcted(radiogroup.toString());
			}
			else{
				objDBToArchiveFormBean.setrDbselcted("");
			}
			
			//ORACLE
			if(objDBToArchiveFormBean.getrDbselcted().equalsIgnoreCase("rOracle")){
				objDBToArchiveFormBean.setrDBType("Oracle");
				objDBToArchiveFormBean.setrHostName(rHostName);
				objDBToArchiveFormBean.setrUsername(rUsername);
				objDBToArchiveFormBean.setrPassword(rPassword);
				objDBToArchiveFormBean.setrServiceName(rServiceName);
				objDBToArchiveFormBean.setrPortNumber(rPortNumber);
				objDBToArchiveFormBean.setrDriverName(oracleDriver);
				if(radiogroup1!=null && !radiogroup1.equalsIgnoreCase("")){
					strDBDriver=radiogroup1.toString();
					if(strDBDriver.equalsIgnoreCase("clintThin"))
					{
						objDBToArchiveFormBean.setrDBDriver("jdbc:oracle:thin");//jdbc:oracle:thin
					}
					else if(strDBDriver.equalsIgnoreCase("serverThin")){
						//objDBToArchiveFormBean.setrDBDriver("jdbc:oracle:thin");
					}
					else if(strDBDriver.equalsIgnoreCase("oci")){
						//objDBToArchiveFormBean.setrDBDriver("jdbc:oracle:thin");
					}
					else if(strDBDriver.equalsIgnoreCase("ss")){
						//objDBToArchiveFormBean.setrDBDriver("jdbc:oracle:thin");
					}
					else{
						//throws exception
					}
				}
				strConnectionString=objDBToArchiveFormBean.getrDBDriver()+":"+objDBToArchiveFormBean.getrDBType()+":@"+
						objDBToArchiveFormBean.getrHostName()+":"+objDBToArchiveFormBean.getrPortNumber()+":"+
						objDBToArchiveFormBean.getrServiceName()+":"+objDBToArchiveFormBean.getrUsername()+":"+
						objDBToArchiveFormBean.getrPassword();
				System.out.println(strConnectionString);
				objDBToArchiveFormBean.setrConnectionString(strConnectionString); 
				
				if(objMySqlDAO.mysqlMetaDataStoreSave(objDBToArchiveFormBean)){
					returnStr="ams";
				}
				else{
					//return error page
				}
			}
			
			//TERADATA
			else if(objDBToArchiveFormBean.getrDbselcted().equalsIgnoreCase("rTeraData")){
				objDBToArchiveFormBean.setrDBType("TeraData");
				objDBToArchiveFormBean.setrHostName(rHostName);
				objDBToArchiveFormBean.setrUsername(rUsername);
				objDBToArchiveFormBean.setrPassword(rPassword);
				objDBToArchiveFormBean.setrServiceName(rServiceName);
				objDBToArchiveFormBean.setrDriverName(oracleDriver);
				if(radiogroup1!=null && !radiogroup1.equalsIgnoreCase("")){
					strDBDriver=radiogroup1.toString();
					if(strDBDriver.equalsIgnoreCase("clintThin"))
					{
						objDBToArchiveFormBean.setrDBDriver("jdbc:oracle:thin");//jdbc:oracle:thin
					}
					else if(strDBDriver.equalsIgnoreCase("serverThin")){
						//objDBToArchiveFormBean.setrDBDriver("jdbc:oracle:thin");
					}
					else if(strDBDriver.equalsIgnoreCase("oci")){
						//objDBToArchiveFormBean.setrDBDriver("jdbc:oracle:thin");
					}
					else if(strDBDriver.equalsIgnoreCase("ss")){
						//objDBToArchiveFormBean.setrDBDriver("jdbc:oracle:thin");
					}
					else{
						//throws exception
					}
				}
				strConnectionString=objDBToArchiveFormBean.getrDBDriver()+":"+objDBToArchiveFormBean.getrDBType()+":"+
						objDBToArchiveFormBean.getrHostName()+":"+objDBToArchiveFormBean.getrPortNumber()+":"+
						objDBToArchiveFormBean.getrServiceName()+":"+objDBToArchiveFormBean.getrUsername()+":"+
						objDBToArchiveFormBean.getrPassword();
				
				objDBToArchiveFormBean.setrConnectionString(strConnectionString); 
				
				if(objOracleDBDAO.oracleDBRegistrationSave(oracleDriver,objDBToArchiveFormBean.getrConnectionString())==true){
					returnStr="oracleDBRegistered";
				}
				else{
					//return error page
				}
			}
			
			//MYSQLs
			else if(objDBToArchiveFormBean.getrDbselcted().equalsIgnoreCase("rmysql")){
				objDBToArchiveFormBean.setrDBType("mysql");
				objDBToArchiveFormBean.setrHostName(rHostName);
				objDBToArchiveFormBean.setrUsername(rUsername);
				objDBToArchiveFormBean.setrPassword(rPassword);
				objDBToArchiveFormBean.setrServiceName(rServiceName);
				objDBToArchiveFormBean.setrPortNumber(rPortNumber);
				objDBToArchiveFormBean.setrDriverName(mysqlDriver);
				
				strConnectionString="jdbc:"+objDBToArchiveFormBean.getrDBType()+"://"+
						objDBToArchiveFormBean.getrHostName()+"/"/*+objDBToArchiveFormBean.getrPortNumber()*/+
						objDBToArchiveFormBean.getrServiceName()+"?user="+objDBToArchiveFormBean.getrUsername()+"&password="+
						objDBToArchiveFormBean.getrPassword();
				System.out.println(strConnectionString);
				objDBToArchiveFormBean.setrConnectionString(strConnectionString); 
				
				if(objMySqlDAO.mysqlMetaDataStoreSave(objDBToArchiveFormBean)){
					returnStr="ams";
				}
				else{
					//return error page
				}
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		return returnStr;
	}

	@RequestMapping("/oracleLogin")
	public String doForwardOracle(@RequestParam String username, String password){
		String returnStr=null;
		if(username!=null && !username.equals("")){
			objRDBMSConnectionFormBean.setUsername(username.toString());
		}
		else{
			objRDBMSConnectionFormBean.setUsername("");
		}
		
		if(password!=null && !password.equals("")){
			objRDBMSConnectionFormBean.setPassword(password.toString());
		}
		else{
			objRDBMSConnectionFormBean.setPassword("");
		}
		return "mysql";
	}
	
	
 	@RequestMapping("/mysqllogin")
	public String doForward(@RequestParam String username, String password){
		MysqlDAO daoObj= new MysqlDAO();
		if(username!=null && !username.equals(" ")){
			objRDBMSConnectionFormBean.setUsername(username.toString());
		}
		else{
			objRDBMSConnectionFormBean.setUsername("");
		}
		
		if(password!=null && !password.equals(" ")){
			objRDBMSConnectionFormBean.setPassword(password.toString());
		}
		else{
			objRDBMSConnectionFormBean.setPassword("");
		}
		
		
		/*if(daoObj.mysqlLogin(objRDBMSConnectionFormBean.getUsername(), objRDBMSConnectionFormBean.getPassword())==true){
			
		}
		else{
			//error
		}*/
		
		return "oracleLogin";
	}
	
	@RequestMapping("/myslsubmit")
	public String doMysqlForward(@RequestParam String url, String connectionString, String username,
			String password, String schemaName, String tableName, String columns, String partitionColumn,
			String hhostName, String husername, String hpassword, String htablename, String mapTaskNo ){
		String strResp=null;
		
		try {
				if(url!=null && !url.equalsIgnoreCase(" ")){
					objRDBMSConnectionFormBean.setUrl(url.toString());
				}
				else{
					objRDBMSConnectionFormBean.setUrl("");
				}
				
				if(connectionString!=null && !connectionString.equals("")){
					objRDBMSConnectionFormBean.setConnectionString(connectionString.toString());
				}
				else{
					objRDBMSConnectionFormBean.setConnectionString("");
				}
				
				if(username!=null && !username.equals("")){
					objRDBMSConnectionFormBean.setUsername(username.toString());
				}
				else{
					objRDBMSConnectionFormBean.setUsername("");
				}
				
				if(password!=null && !password.equals("")){
					objRDBMSConnectionFormBean.setPassword(password.toString());
				}
				else{
					objRDBMSConnectionFormBean.setPassword("");
				}
				
				if(schemaName!=null && !schemaName.equals("")){
					objRDBMSConnectionFormBean.setSchemaName(schemaName.toString());
				}
				else{
					objRDBMSConnectionFormBean.setSchemaName("");
				}
				
				if(tableName!=null && !tableName.equals("")){
					objRDBMSConnectionFormBean.setTableName(tableName.toString());
				}
				else{
					objRDBMSConnectionFormBean.setTableName("");
				}
				
				if(columns!=null && !columns.equals("")){
					objRDBMSConnectionFormBean.setColumns(columns.toString());
				}
				else{
					objRDBMSConnectionFormBean.setColumns("");
				}
				
				if(partitionColumn!=null && !partitionColumn.equals("")){
					objRDBMSConnectionFormBean.setPartitionColumn(partitionColumn.toString());
				}
				else{
					objRDBMSConnectionFormBean.setPartitionColumn("");
				}
				
				if(hhostName!=null && !hhostName.equals("")){
					objClusterConnectionFormBean.setHostName(hhostName.toString());
				}
				else{
					objClusterConnectionFormBean.setHostName("");
				}
				if(husername!=null && !husername.equals("")){
					objClusterConnectionFormBean.setUsername(husername.toString());
				}
				else{
					objClusterConnectionFormBean.setUsername("");
				}
				if(hpassword!=null && !hpassword.equals("")){
					objClusterConnectionFormBean.setPassword(hpassword.toString());
				}
				else{
					objClusterConnectionFormBean.setPassword("");
				}
				if(htablename!=null && !htablename.equals("")){
					objHiveFormBean.sethTablename(htablename.toString());
				}
				else{
					objHiveFormBean.sethTablename("");
				}
				if(mapTaskNo!=null && !mapTaskNo.equals("")){
					objHiveFormBean.setMapTaskNo(Integer.parseInt(mapTaskNo.toString()));
				}
				else{
					objHiveFormBean.setMapTaskNo(0);
				}
				
				System.out.println("Set in Form Bean");
				
				ClusterConnection cc = new ClusterConnection();
				SqoopJobFormBean sjfb=new SqoopJobFormBean();
				sjfb.setSqoopTypeSelection("sqoop1");
					
				boolean resp=cc.sqoopExecutionMain(objRDBMSConnectionFormBean,objHiveFormBean,sjfb,objClusterConnectionFormBean);
				if(resp==true){
					strResp="success";
				}
					
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return strResp;
	}
	
}