<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>CS 5300 Project 1a by Yihui Fu</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
<p>netID: yf263 <br> Session: ${sessionID} <br> Version:${sessionVersion} <br> Date:${currentDate}</p>
<h1>${info}</h1>

<form method="get">
	<div>
		<input type="submit" name="Replace" value="Replace">
		<input type="text" name="Message" maxlength="256">	
	</div>
	<div>
		<input type="submit" name="Refresh" value="Refresh">
	</div>
	<div>
		<input type="submit" name="Logout" value="Logout">
	</div>
</form>

<div>
	<p>Cookie:${cookieID}</p>
	<p>ExpirationTime:${expTime}</p>
</div>
</body>
</html>