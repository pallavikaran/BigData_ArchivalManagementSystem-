package dataAccessObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class TeradataDBDAO {
	public boolean teraDataDBRegistrationSave(String oracleDriver, String connectionString){
		Connection connect=null;
		Statement statement=null;
		PreparedStatement preparedStatement =null;
		ResultSet resultset =null;
		//MySqlFormBean msqlBean = new MySqlFormBean();
		
		try{
			Class.forName("oracle.jdbc.OracleDriver");
			//connect = (Connection) DriverManager.getConnection("jdbc:oracle:thin:@amsdatabase1.cqszpumnfyqd.us-east-1.rds.amazonaws.com:1521:nmsrvc",uname,passw);
			System.out.println("connectionString "+connectionString);
			connect = (Connection) DriverManager.getConnection(connectionString);
			statement =connect.createStatement();
			resultset =statement.executeQuery("select * from information_schema.tables");
			System.out.println("***********"+statement.getMaxRows());
			
			/*while(resultset.next()){
				ArrayList<String> tempArrayList=new ArrayList<String>();
				tempArrayList.add(resultset.getString(resultset.getString(1)));	
				msqlBean.setArrayList(tempArrayList);
				}*/
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
		
		
		return true;
	}
}
