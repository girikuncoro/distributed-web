<!DOCTYPE html>
<html>
<head>
<title>CS 5300 Project 1b</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
<script src="https://code.jquery.com/jquery-2.2.3.min.js" integrity="sha256-a23g1Nt4dtEYOj7bR+vTu7+T8VP13humZFBJNIYoEJo=" crossorigin="anonymous"></script>
</head>
<body>

<!-- For a request in an existing session, report the SvrID where the session data was found. -->

<p>netID: yf263, gk256, sz428
	<br> Currently, Server ID <span id="server-id"></span> with reboot number <span id="reboot-num"></span> is executing the client request.
	<br> The session data was found on server ID <span id="source-server-id"></span>
	<br> Session: <span id="session-id"></span>
	<br> Version: <span id="session-version"></span>
	<br> Date: <span id="current-date"></span></p>

<h1 id="info"></h1>


<div class="alert alert-danger alert-dismissible" id="msg-error" role="alert">
	<button type="button" class="close" aria-label="Close">
	<span aria-hidden="true">&times;</span></button>
	<strong>Oops!</strong> Message can't be empty and can't contain '#' or '_' or whitespace
</div>

<div>
	<div>
		<input id="replace" class="btn btn-lg btn-primary request" name="Replace" value="Replace" type="button"/>
		<input id="message" class="form-control" name="Message" value="" type="text" maxlength="512"/>
	</div>
	<div>
		<input id="refresh" class="btn btn-lg btn-primary request" name="Refresh" value="Refresh" type="button"/>
	</div>
	<div>
		<input id="logout" class="btn btn-lg btn-primary request" name="Logout" value="Logout" type="button"/>
	</div>
</div>

<div>
	<p>Cookie: <span id="cookie-id"></span>
	<br>Cookie meta data: <span id="cookie-metadata"></span>
	<br>Cookie domain: <span id="cookie-domain"></span>
	<br>ExpirationTime: <span id="exp-time"></p>
</div>

<script>
	// get request and populate the info upon successful request
	function servletCall(params) {
		$.ajax({
			url: "/proj1b/proj1bServlet",
			type: "GET",
			datatype: "text",
			data: params,
			success: function(data) {
				console.log(data);

				$("#server-id").text(data.serverID);
				$("#reboot-num").text(data.rebootNum);
				$("#source-server-id").text(data.sourceServerID);

				$("#session-id").text(data.sessionID);
				$("#session-version").text(data.sessionVersion);
				$("#current-date").text(data.currentDate);
				$("#info").text(data.info);

				$("#cookie-id").text(data.cookieID);
				$("#cookie-metadata").text(data.cookieMetadata);
				$("#cookie-domain").text(data.cookieDomain);
				$("#exp-time").text(data.expTime);
				
			}
		});
	}

	// init get request to servlet
	$(document).ready(function() {
		$("#msg-error").hide();
		servletCall({ Init: "Init" });
	});

	// send get request for replace action, validate message before send
	$("#replace").click(function() {
		var msg = $("#message").val();
		if (msg == "" || $.trim(msg) == "") {
			$("#msg-error").show();
			return;
		}

		var invalidChars = ["_", "#", " "];
		invalidChars.forEach(function(c) {
			if (msg.indexOf(c) != -1) {
				$("#msg-error").show();
				return;
			}
		});

		servletCall({ Replace: "Replace", Message: msg });
	});

	// send get request for refresh action
	$("#refresh").click(function() {
		servletCall({ Refresh: "Refresh" });
	});

	// send get request for logout action
	$("#logout").click(function() {
		servletCall({ Logout: "Logout"});
		window.location.href = "logout.jsp";
	});

	// close the alert box
	$('.alert .close').on('click', function(e) {
		$(this).parent().hide();
	});

</script>
</body>
</html>