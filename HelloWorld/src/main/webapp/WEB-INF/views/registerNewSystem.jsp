<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>registerNewSystem.jsp</title>
</head>
<body>
<form action="submitNewSystemEntry" method="post">
<center>
<br><br><br><br><br><br><br><br><br>Register a New System for Archival
<table border="1">
 	<tr>
 		<td>Select Database Type</td>
		<td><input type="radio" name="radiogroup" value="rOracle" checked="checked">Oracle</td>
		<td><input type="radio" name="radiogroup" value="rTeraData">TeraData</td>
		<td><input type="radio" name="radiogroup" value="rmysql">MySql</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td>Host Name:<input type="text" name="rHostName" value=""></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td>Port Number:<input type="text" name="rPortNumber" value=""></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td>Service Name:<input type="text" name="rServiceName" value=""></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
 	<tr>
		<td>UserName: <input type="text" name="rUsername" value=""/></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td>Password: <input type="text" name="rPassword" value=""/></td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr> 		
	<tr>
		<td>For Oracle only/JS validation</td>
		<td><input type="radio" name="radiogroup1" value="clintThin" checked="checked">JDBC Thin client-side driver</td>
		<td><input type="radio" name="radiogroup1" value="serverThin">JDBC Thin server-side driver</td>
		<td><input type="radio" name="radiogroup1" value="oci">JDBC OCI client-side driver</td>
		<td><input type="radio" name="radiogroup1" value="ss">JDBC Server-Side Internal driver</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td><input type="submit" value="Submit" name="submit">
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
</table>
	</center>
	</form>
</body>
</html>