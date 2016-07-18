package dataAccessObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import FormBean.MySqlFormBean;

public class UtilityDAO {
	public int getMaxIdOfTable(String tablename, String columnName){
		Connection connect=null;
		Statement statement=null;
		PreparedStatement preparedStatement =null;
		ResultSet resultset =null;
		MySqlFormBean msqlBean = new MySqlFormBean();
		int maxIDInTable=0;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			//System.out.println(uname+"|"+passw);
			String conStr="jdbc:mysql://localhost/metadata_storage?user=root&password=cloudera";//Log in to mySql
			connect = (Connection) DriverManager.getConnection(conStr);
			statement =connect.createStatement();
			String query="select max("+columnName+") from "+tablename;
			System.out.println(query);
			resultset =statement.executeQuery(query);
			System.out.println("***********"+statement.getMaxRows());
			while(resultset.next()){
				String maxID=resultset.getString(1);
				System.out.println("maxID: "+maxID);
				if(maxID==null){
					maxIDInTable=0;
				}
				else{
					maxIDInTable=Integer.parseInt(maxID);
				}
				
			}	
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			try{ 
				if(resultset!=null){
					resultset.close();
				}
				if(statement!=null){
					statement.close();
				}
				if(connect!=null){
					connect.close();
				}
			}
			catch(Exception exx){
				exx.printStackTrace();
			}
		}
		return maxIDInTable;
	}
	
	public String getSingleColumnValues(String tablename, String columnName, String whereclause){
		Connection connect=null;
		Statement statement=null;
		PreparedStatement preparedStatement =null;
		ResultSet resultset =null;
		MySqlFormBean msqlBean = new MySqlFormBean();
		int maxIDInTable=0;
		ArrayList Rows = new ArrayList();
		String result=null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			//System.out.println(uname+"|"+passw);
			String conStr="jdbc:mysql://localhost/metadata_storage?user=root&password=cloudera";//Log in to mySql
			connect = (Connection) DriverManager.getConnection(conStr);
			statement =connect.createStatement();
			String query="select "+columnName+" from "+tablename+ " where "+whereclause;
			System.out.println(query);
			resultset =statement.executeQuery(query);
			while(resultset.next()){
				//Rows.add(resultset.getString(1));
				 result=resultset.getString(1);
	            System.out.println("resultset.getString(): "+resultset.getString(1));
	           // Rows.add(Rows);
				
			}	
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			try{ 
				if(resultset!=null){
					resultset.close();
				}
				if(statement!=null){
					statement.close();
				}
				if(connect!=null){
					connect.close();
				}
			}
			catch(Exception exx){
				exx.printStackTrace();
			}
		}
		return result;
	}
	
}
