package proj1b.ssm;

import java.util.*;
import java.util.logging.Logger;

import proj1b.util.*;

public class Session {
	private String sessionID; // <ServerID, reboot_num, session id>
	private Integer versionNumber; // default to 0
	private String message; // default "Hello, User!"
	private long expirationTime; // creation time + time out time
	private static final Logger LOGGER = Logger.getLogger("Session Logger");

	public Session(String svrID, Integer rebootNum, Integer sessID, Integer versionNumber, String message, long expirationTime) {
		sessionID = String.join(Constants.SESSION_DELIMITER, svrID, rebootNum.toString(), sessID.toString());
		this.versionNumber = versionNumber;
		this.message = message;
		this.expirationTime = expirationTime;
		LOGGER.info("Created a new session: " + sessionID);
	}

	public Session(String svrID, Integer rebootNum, Integer sessID, Integer versionNumber) {
		this(svrID, rebootNum, sessID, versionNumber, Constants.DEFAULT_MESSAGE,
				System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000);
	}

	public Session(String serID, Integer rebootNum, Integer sessID) {
		this(serID, rebootNum, sessID, 0);
	}

	/**
	 * Encode session information as cookie value. For example, "1_2_4_11_0_3"
	 * represents an instance of the fourth session created at SvrID 1 on its
	 * second reboot, session version number 11, currently stored at SvrIDs 0
	 * and 3.
	 * 
	 * @return a string representing the cookie value.
	 */
	public String encode() {
		return String.join(Constants.SESSION_DELIMITER, sessionID, String.valueOf(versionNumber), message,
				String.valueOf(expirationTime));
	}

	public static Session decode(String encodedSession) {
		System.out.println("Encoded session in Session object : " + encodedSession);
		List<String> fields = Arrays.asList(encodedSession.split("\\" + Constants.SESSION_DELIMITER));
		System.out.println("Fields in Session object : " + fields.toString());
		return new Session(fields.get(0), Integer.parseInt(fields.get(1)), Integer.parseInt(fields.get(2)),
				Integer.parseInt(fields.get(3)), fields.get(4), Long.parseLong(fields.get(5)));
	}

	/**
	 * Redisplay the session message, with an updated session expiration time
	 */
	public void refresh() {
		versionNumber += 1;
		expirationTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000
				+ Constants.SESSION_TIMEOUT_DELTA;
		LOGGER.info("Refreshed a session");
	}

	/**
	 * Replace the message with a new one (that the user typed into an HTML form
	 * field), and display the (new) message and expiration time;
	 */
	public void replace(String msg) {
		message = msg;
		versionNumber += 1;
		expirationTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000
				+ Constants.SESSION_TIMEOUT_DELTA;
		LOGGER.info("Replaced a session state with message " + msg);
	}

	public void logout() {
		message = "You have logged out.";
		this.expirationTime = System.currentTimeMillis();
		LOGGER.info("Invalidated a session due to logging out");
	}

	public String getMessage() {
		return message;
	}

	public String getSessionID() {
		return sessionID;
	}

	public Integer getVersionNumber() {
		return versionNumber;
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	public String getCookieValue(List<String> locationData) {
		StringBuilder sb = new StringBuilder();
		sb.append(sessionID).append(Constants.SESSION_DELIMITER).append(versionNumber);

		for (String location : locationData)
			sb.append(Constants.SESSION_DELIMITER).append(location);

		return sb.toString();
	}
}