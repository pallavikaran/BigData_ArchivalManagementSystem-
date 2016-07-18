package FormBean;

import java.util.ArrayList;

public class MySqlFormBean {

	private  String url = null;
	private  String connectionString = null;
	private  String username = null;
	private  String password = null;
	private  String schemaName = null;
	private  String tableName = null;
	private  String columns = null;
	private  String partitionColumn = null;
	private  String sessionSystemUsername = null;
	private ArrayList<String> arrayList=null;
	
	private  String selectedMysqlDBConString = null;
	private ArrayList<String> arrayMysqlTableList=null;
	
	public String getSelectedMysqlDBConString() {
		return selectedMysqlDBConString;
	}
	public void setSelectedMysqlDBConString(String selectedMysqlDBConString) {
		this.selectedMysqlDBConString = selectedMysqlDBConString;
	}
	public ArrayList<String> getArrayMysqlTableList() {
		return arrayMysqlTableList;
	}
	public void setArrayMysqlTableList(ArrayList<String> arrayMysqlTableList) {
		this.arrayMysqlTableList = arrayMysqlTableList;
	}
	public String getSessionSystemUsername() {
		return sessionSystemUsername;
	}
	public void setSessionSystemUsername(String sessionSystemUsername) {
		this.sessionSystemUsername = sessionSystemUsername;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getConnectionString() {
		return connectionString;
	}
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColumns() {
		return columns;
	}
	public void setColumns(String columns) {
		this.columns = columns;
	}
	public String getPartitionColumn() {
		return partitionColumn;
	}
	public void setPartitionColumn(String partitionColumn) {
		this.partitionColumn = partitionColumn;
	}
	public ArrayList<String> getArrayList() {
		return arrayList;
	}
	public void setArrayList(ArrayList<String> arrayList) {
		this.arrayList = arrayList;
	}
}
