package FormBean;

import java.util.ArrayList;

public class OracleFormBean {

	private  String selectedOracleDBConString = null;
	private ArrayList<String> arrayOracleTableList=null;
	public String getSelectedOracleDBConString() {
		return selectedOracleDBConString;
	}
	public void setSelectedOracleDBConString(String selectedOracleDBConString) {
		this.selectedOracleDBConString = selectedOracleDBConString;
	}
	public ArrayList<String> getArrayOracleTableList() {
		return arrayOracleTableList;
	}
	public void setArrayOracleTableList(ArrayList<String> arrayOracleTableList) {
		this.arrayOracleTableList = arrayOracleTableList;
	}
		
}
