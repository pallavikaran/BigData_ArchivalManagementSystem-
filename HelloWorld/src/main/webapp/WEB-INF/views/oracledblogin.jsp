<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>oracledblogin.jsp</title>
</head>
<body>
<form action="oracleLogin" method="post">
<table align="center">
	<tr>
		<td>UserName: <input type="text" name="username" value=""/></td>
	</tr>
	<tr>
		<td>Password: <input type="text" name="password" value=""/></td>
	</tr>
	<tr>
		<td><input type="submit" value="Submit" name="submit"></td>
	</tr>
</table>
</form>
</body>
</html>