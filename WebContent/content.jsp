<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>CS 5300 Project 1a by Yihui Fu</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
<p>netID: yf263, gk256, sz428
	<br> Currently, Server ID ${serverID} with reboot number ${rebootNum} is executing the client request.
	<br> The session data was found on server ID ${sourceServerID}
	<br> Session: ${sessionID} 
	<br> Version: ${sessionVersion} 
	<br> Date: ${currentDate}</p>
<h1>${info}</h1>

<!-- For a request in an existing session, report the SvrID where the session data was found. -->

<form method="get">
	<div>
		<input type="submit" name="Replace" value="Replace">
		<input type="text" name="Message" maxlength="512">	
	</div>
	<div>
		<input type="submit" name="Refresh" value="Refresh">
	</div>
	<div>
		<input type="submit" name="Logout" value="Logout">
	</div>
</form>

<div>
	<p>Cookie: ${cookieID}
	<br>Cookie meta data: ${cookieMetadata}
	<br>Cookie domain: ${cookieDomain}
	<br>ExpirationTime: ${expTime}</p>
</div>
</body>
</html>