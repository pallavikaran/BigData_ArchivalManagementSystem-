<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>workflowFail.jsp</title>
</head>
<body>
<form action="workflowFileAlreadyExistsErr" method="post">
<center>
<br><br><br><br><br><br><br><br><br>
 <table border="1"> 
 	<tr>	 
  		<td><input type=text value="${error}" name="jobStatus" size="50"></td>	 
 	</tr>
 	<tr>
 		<td>Go Back: <input type="submit" value="Submit" name="submit"></td>
 	</tr>
 </table> 
</center>        
<%-- ${msg} --%>
 </form>
</body>
</html>