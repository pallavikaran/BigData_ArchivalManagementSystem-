<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>mysql.jsp</title>
</head>
<body>
<form action="MySqlData" method="post">
<table  border="1" style="width:100%">
	<tr>
		<td>MySQL</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td>URL:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		&nbsp;&nbsp;<input type="text" name="url" value=""></td>
		<td>Connection String:&nbsp;<input type="text" name="connectionString" value=""></td>
		<td>UserName:          <input type="text" name="username" value=""></td>
		<td>Password:          <input type="password" name="password" value=""></td>
	</tr>
	<tr>	
		<td>Schema Name:&nbsp;<input type="text" name="schemaName" value=""></td>
		<td>Table Name:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="tableName" value=""></td>
		<td>Column:            <input type="text" name="columns" value=""></td>
		<td>Partition Column:  <input type="text" name="partitionColumn" value=""></td>
	</tr>
	
</table>
<br>
<br>
<table  border="1" style="width:100%">
	<tr>
		<td>Cluster Info</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td>Host Name:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		&nbsp;&nbsp;<input type="text" name="hhostName" value=""></td>
		<td>UserName:          <input type="text" name="husername" value=""></td>
		<td>Password:          <input type="password" name="hpassword" value=""></td>
	</tr>
</table>
<br>
<br>
<table  border="1" style="width:100%">
	<tr>
		<td>Hive Info</td>
		<td>&nbsp;</td>
		
	</tr>
	<tr>
		<td>Table Name:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		&nbsp;&nbsp;<input type="text" name="htablename" value=""></td>
		<td>Map Task No:<input type="text" name="mapTaskNo" value=""></td>
	</tr>
	<tr>	
		<td><input type="submit" value="Submit" name="submit"></td>
		<td>&nbsp;</td>
	</tr>
	
</table>

</form>
</body>
</html>