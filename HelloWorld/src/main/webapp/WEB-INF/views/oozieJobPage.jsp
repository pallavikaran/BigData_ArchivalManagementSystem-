<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>   
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>oozieJobPage.jsp</title>
</head>
<body>
<form action="checkStatus" method="post">
<center>
<br><br><br><br><br><br><br><br><br>
 <table border="1"> 
 	<tr>    
 		<td>YOUR OOZIE JOB has been submitted:</td> 
 	</tr>
 	<tr>	 
  		<td>JOB ID: <input type=text value="${jobID}" name="jobID" size="50"></td>	 
 	</tr>
 	<tr>	 
  		<td>JOB Status: <input type=text value="${jobStatus}" name="jobStatus" size="50"></td>	 
 	</tr>
 	<tr>
 		<td>Submit Job Request Status<input type="submit" value="Submit" name="submit" size="50"></td>
 	</tr>
 </table> 
</center>        
<%-- ${msg} --%>
 </form>
</body>
</html>