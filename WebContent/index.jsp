<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">

<head>

<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CS5300 - Project 1b</title>

<!-- CSS -->
<link rel="stylesheet"
	href="http://fonts.googleapis.com/css?family=Lobster">
<link rel='stylesheet'
	href='http://fonts.googleapis.com/css?family=Lato:400,700'>
<link rel="stylesheet" href="assets/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
	href="assets/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet" href="assets/css/style.css">
</head>

<body>

	<!-- Header -->
	<div class="container">
		<div class="row header">
			<div class="col-sm-4 logo">
				<h1>
					<a href="/proj1b">CS5300 Proj1b</a> <span>.</span>
				</h1>
			</div>
			<div class="col-sm-8 call-us">
				<p>
					Shibo Zang: <span>sz428</span> | Yihui Fu: <span>yf263</span> |
					Giri Kuncoro: <span>gk256</span>
				</p>
			</div>
		</div>
	</div>

	<!-- Main info -->
	<div class="coming-soon">
		<div class="inner-bg">
			<div class="container">
				<div class="row">
					<div class="col-sm-12">
						<h2>
							Session ID: <span id="session-id"></span>
						</h2>
						<p>
							Currently, Server ID <span class="server-id"></span> with reboot
							number <span class="reboot-num"></span> is executing the client
							request. The session data was found on server ID <span
								class="source-server-id"></span>.
						</p>
						<div class="session">
							<div class="session-wrapper">
								<span class="server-id session-data"></span> <br>serverID
							</div>
							<div class="session-wrapper">
								<span class="reboot-num session-data"></span> <br>rebootNum
							</div>
							<div class="session-wrapper">
								<span class="source-server-id session-data"></span> <br>svrIDFound
							</div>
							<div class="session-wrapper">
								<span class="session-version session-data"></span> <br>version
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- Content -->
	<div class="container">
		<div class="row">
			<div class="col-sm-12 subscribe">
				<h3 id="info"></h3>
				<p>
					<b>Cookie Value:</b> <span id="cookie-id"></span> | <b>Cookie
						metadata:</b> <span id="cookie-metadata"></span> | <b>Cookie-domain:</b>
					<span id="cookie-domain"></span>
				</p>
				<p>
					<b>Curr time: </b> <span id="curr-time"></span> | <b>Exp time:
					</b> <span id="exp-time"></span>
				</p>
				<form class="form-inline" role="form" method="post">
					<div class="form-group">
						<label class="sr-only" for="message">Session state message</label>
						<input type="text" name="Message"
							placeholder="Enter your message..."
							class="replace-msg form-control" id="message" maxlength="512">
					</div>
					<button id="replace" type="submit" class="btn">Replace</button>
				</form>
				<div class="error-message" id="msg-error" role="alert">
					<strong>Oops!</strong> Message can't be empty and can't contain '#'
					or '_' or whitespace
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-12 social">
				<a id="refresh" data-toggle="tooltip" data-placement="top"
					title="Refresh"><i class="fa fa-refresh"></i></a> <a id="logout"
					data-toggle="tooltip" data-placement="top" title="Logout"><i
					class="fa fa-sign-out"></i></a>
			</div>
		</div>
	</div>


	<!-- Javascript -->
	<script src="assets/js/jquery-1.11.1.min.js"></script>
	<script src="assets/bootstrap/js/bootstrap.min.js"></script>
	<script src="assets/js/jquery.backstretch.min.js"></script>
	<script src="assets/js/scripts.js"></script>
	<script>
		// get request and populate the info upon successful request
		function servletCall(params) {
			$.ajax({
				url : "/proj1b/proj1bServlet",
				type : "GET",
				datatype : "text",
				data : params,
				success : function(data) {
					if (data.status == "error") {
						window.location.href = "/proj1b/error.jsp";	
					}
					
					console.log(data);

					data.sessionID = data.sessionID.replace(/#/g, "_");
					data.cookieID = data.cookieID.replace(/#/g, "_");
					if (!data.hasOwnProperty("sourceServerID")) {
						data.sourceServerID = "N/A";
					}

					$(".server-id").text(data.serverID);
					$(".reboot-num").text(data.rebootNum);
					$(".source-server-id").text(data.sourceServerID);

					$("#session-id").text(data.sessionID);
					$(".session-version").text(data.sessionVersion);
					$("#current-date").text(data.currentDate);
					$("#info").text(data.info);

					$("#cookie-id").text(data.cookieID);
					$("#cookie-metadata").text(data.cookieMetadata);
					$("#cookie-domain").text(data.cookieDomain);
					$("#curr-time").text(data.currentDate);
					$("#exp-time").text(data.expTime);

				}
			});
		}

		// init get request to servlet
		$(document).ready(function() {
			$("#msg-error").hide();
			servletCall({
				Init : "Init"
			});
		});

		// send get request for replace action, validate message before send
		$("#replace").click(function() {
			var msg = $("#message").val();
			if (msg == "" || $.trim(msg) == "") {
				$("#msg-error").show();
				return;
			}

			var invalidRegex = ".*#|_+.*";
			if (msg.match(invalidRegex) != null) {
				$("#msg-error").show();
				return;
			}

			servletCall({
				Replace : "Replace",
				Message : msg
			});
			$("#msg-error").hide();
		});

		// send get request for refresh action
		$("#refresh").click(function() {
			servletCall({
				Refresh : "Refresh"
			});
			$("#msg-error").hide();
		});

		// send get request for logout action
		$("#logout").click(function() {
			servletCall({
				Logout : "Logout"
			});
			window.location.href = "logout.jsp";
		});
	</script>
</body>
</html>