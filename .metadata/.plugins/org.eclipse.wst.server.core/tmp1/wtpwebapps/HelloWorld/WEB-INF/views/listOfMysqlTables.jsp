<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>  
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>listOfMysqlTables.jsp</title>
</head>
<body>
LIST OF TABLES
<form action="submitMysqlTableSelection" method="post">
<center>
<br><br><br><br><br><br><br><br><br>
 <table border="1"> 
 	<tr>    
 		<td>Select the MySql Table:</td>  
  		<td>
  			<select name="selectedTables">
    			<c:forEach var="line" items="${listOftables}">
        		<option><c:out value="${line}"/></option>
    			</c:forEach>
    		 </select>	
    	 </td>	 
 	</tr>
 	<tr>
 		<td><input type="hidden" value="${connectionStr}" name="connectionStr">&nbsp;</td>
 		<td><input type="submit" value="Submit" name="submit"></td>
 	</tr>
 </table> 
</center>        
</form>
</body>
</html>