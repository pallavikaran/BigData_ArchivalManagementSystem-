package metadata;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pojo.ClusterConnectionFormBean;
import pojo.HiveFormBean;
import pojo.RDBMSConnectionFormBean;
import pojo.SqoopJobFormBean;
import connectionConfiguration.ClusterConnection;

/**
 * Servlet implementation class MySqlData
 */
@WebServlet("/MySqlData")
public class MySqlData extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MySqlData() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doGet(&nbsp;, response);
		RDBMSConnectionFormBean sqlFormBean =new RDBMSConnectionFormBean();
		ClusterConnectionFormBean ccfb=new ClusterConnectionFormBean();
		HiveFormBean hfb= new HiveFormBean();
		
		try {
				if(request.getParameter("url")!=null && !request.getParameter("url").equals(" ")){
					sqlFormBean.setUrl(request.getParameter("url").toString());
				}
				else{
					sqlFormBean.setUrl("");
				}
				
				if(request.getParameter("connectionString")!=null && !request.getParameter("connectionString").equals(" ")){
					sqlFormBean.setConnectionString(request.getParameter("connectionString").toString());
				}
				else{
					sqlFormBean.setConnectionString("");
				}
				
				if(request.getParameter("username")!=null && !request.getParameter("username").equals(" ")){
					sqlFormBean.setUsername(request.getParameter("username").toString());
				}
				else{
					sqlFormBean.setUsername("");
				}
				
				if(request.getParameter("password")!=null && !request.getParameter("password").equals(" ")){
					sqlFormBean.setPassword(request.getParameter("password").toString());
				}
				else{
					sqlFormBean.setPassword("");
				}
				
				if(request.getParameter("schemaName")!=null && !request.getParameter("schemaName").equals(" ")){
					sqlFormBean.setSchemaName(request.getParameter("schemaName").toString());
				}
				else{
					sqlFormBean.setSchemaName("");
				}
				
				if(request.getParameter("tableName")!=null && !request.getParameter("tableName").equals(" ")){
					sqlFormBean.setTableName(request.getParameter("tableName").toString());
				}
				else{
					sqlFormBean.setTableName("");
				}
				
				if(request.getParameter("columns")!=null && !request.getParameter("columns").equals(" ")){
					sqlFormBean.setColumns(request.getParameter("columns").toString());
				}
				else{
					sqlFormBean.setColumns("");
				}
				
				if(request.getParameter("partitionColumn")!=null && !request.getParameter("partitionColumn").equals(" ")){
					sqlFormBean.setPartitionColumn(request.getParameter("partitionColumn").toString());
				}
				else{
					sqlFormBean.setPartitionColumn("");
				}
				
				if(request.getParameter("hhostName")!=null && !request.getParameter("hhostName").equals(" ")){
					ccfb.setHostName(request.getParameter("hhostName").toString());
				}
				else{
					ccfb.setHostName("");
				}
				if(request.getParameter("husername")!=null && !request.getParameter("husername").equals(" ")){
					ccfb.setUsername(request.getParameter("husername").toString());
				}
				else{
					ccfb.setUsername("");
				}
				if(request.getParameter("hpassword")!=null && !request.getParameter("hpassword").equals(" ")){
					ccfb.setPassword(request.getParameter("hpassword").toString());
				}
				else{
					ccfb.setPassword("");
				}
				if(request.getParameter("htablename")!=null && !request.getParameter("htablename").equals(" ")){
					hfb.sethTablename(request.getParameter("htablename").toString());
				}
				else{
					hfb.sethTablename("");
				}
				if(request.getParameter("mapTaskNo")!=null && !request.getParameter("mapTaskNo").equals(" ")){
					hfb.setMapTaskNo(Integer.parseInt(request.getParameter("mapTaskNo").toString()));
				}
				else{
					hfb.setMapTaskNo(0);
				}
				
				ClusterConnection cc = new ClusterConnection();
				SqoopJobFormBean sjfb=new SqoopJobFormBean();
				sjfb.setSqoopTypeSelection("sqoop1");
					
				boolean resp=cc.sqoopExecutionMain(sqlFormBean,hfb,sjfb,ccfb);
				if(resp==true){
					response.sendRedirect("success.jsp");
				}
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

}
