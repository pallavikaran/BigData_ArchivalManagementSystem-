<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>existingSystemLogin.jsp</title>
</head>
<body>
<form action="submit1" method="post">
<center>
 <table>
	<tr>
		<td>Select Database you want to Load</td>
	</tr>
	<!-- <tr>
		<td><input type="radio" name="radiogroup" value="mysql" checked="checked">MySQL</td>
		<td><input type="radio" name="radiogroup" value="oracle">Oracle</td>
	</tr> -->
	<tr>
		<td><input type="radio" name="radiogroup" value="mysql" checked="checked">MySQL</td>
		<td><input type="radio" name="radiogroup" value="oracle">Oracle</td>
		<td><input type="radio" name="radiogroup" value="teradata">TeraData</td>
	</tr>
	<tr>
		<td><input type="submit" value="Submit" name="submit">
	</tr>	
</table>
	</center>
	</form>
</body>
</html>