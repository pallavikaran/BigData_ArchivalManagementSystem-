package dataAccessObject;

import java.sql.DriverManager;
import java.sql.ResultSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import FormBean.OracleFormBean;

public class OracleDBDAO {

		OracleFormBean objOracleFormBean=new OracleFormBean();
	
		//For populating the tables 
		public ArrayList oraclePopulateTables(OracleFormBean objOracleFormBean){
			Connection connect=null;
			Statement statement=null;
			PreparedStatement preparedStatement =null;
			ResultSet resultset =null;
			boolean strReturn=false;
			ArrayList Rows = new ArrayList();
			try{
				//Class.forName("oracle.jdbc.driver.OracleDriver");
				Class.forName("oracle.jdbc.OracleDriver");
				System.out.println("objOracleFormBean.getSelectedOracleDBConString(): "+objOracleFormBean.getSelectedOracleDBConString());
				connect = (Connection) DriverManager.getConnection(objOracleFormBean.getSelectedOracleDBConString(),"amsdatabase","ams_database");
				statement =connect.createStatement();
				resultset =statement.executeQuery("select table_name from all_tables WHERE owner='AMSDATABSE");
				while(resultset.next()){
					ArrayList row = new ArrayList();
				       for (int i = 1; i <= resultset.getRow() ; i++){
				               row.add(resultset.getString(i));
				               System.out.println("resultset.getString("+i+"): "+resultset.getString(i));
				       }

				       Rows.add(row);
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
			return Rows;
		}
	
	
	public boolean oracleDBRegistrationSave(String oracleDriver, String connectionString){
		Connection connect=null;
		Statement statement=null;
		PreparedStatement preparedStatement =null;
		ResultSet resultset =null;
		//MySqlFormBean msqlBean = new MySqlFormBean();
		boolean returnResult=false;
		
		try{
			Class.forName("oracle.jdbc.OracleDriver");
			//System.out.println(uname+"|"+passw);
			connect = (Connection) DriverManager.getConnection("jdbc:oracle:thin:@amsdatabase1.cqszpumnfyqd.us-east-1.rds.amazonaws.com:1521/ORCL","amsdatabase","ams_database");
			//connect = (Connection) DriverManager.getConnection(connectionString);
			statement =connect.createStatement();
			resultset =statement.executeQuery("select TITLE from BOOKS");
			System.out.println("***********"+statement.getMaxRows());
			while(resultset.next()){
				String rtnResult=resultset.getString(1);
				System.out.println(resultset.getString(1));
				returnResult=true;
				/*ArrayList<String> tempArrayList=new ArrayList<String>();
				tempArrayList.add(resultset.getString(resultset.getString(1)));	
				msqlBean.setArrayList(tempArrayList);*/
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
		
		
		return returnResult;
	}
	
}
