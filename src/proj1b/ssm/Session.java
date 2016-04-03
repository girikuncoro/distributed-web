package proj1b.ssm;

import java.sql.Timestamp;
import java.util.*;

public class Session {
	private String sessionID;
	private int versionNumber;
	private String message;
	private Timestamp creationTime;
	private Timestamp expirationTime;
	private List<String> locationData;

	public Session(String sessionID, int versionNumber, String message, Timestamp creationTime,
			Timestamp expirationTime, List<String> locationData) {
		this.sessionID = sessionID;
		this.versionNumber = versionNumber;
		this.message = message;
		this.creationTime = creationTime;
		this.expirationTime = expirationTime;
		this.locationData = locationData;
	}

	public Session(String sessionID, List<String> locationData) {
		this(sessionID, 0, "", null, null, locationData);

		Date date = new Date();
		this.creationTime = new Timestamp(date.getTime());
		this.expirationTime = new Timestamp(creationTime.getTime() + (SessionManager.getTimeToLive() * 1000));
	}

	public Session(String sessionID, List<String> locationData, String message) {
		this(sessionID, locationData);
		this.message = message;
	}

	public String encode() {
		String locationDataString = String.join(SessionManager.SESSION_DELIMITER, locationData);
		return String.join(SessionManager.SESSION_DELIMITER, sessionID, String.valueOf(versionNumber), message,
				creationTime.toString(), expirationTime.toString(), locationDataString);
	}

	public static Session decode(String encodedSession) {
		List<String> fields = Arrays.asList(encodedSession.split(SessionManager.SESSION_DELIMITER));
		List<String> locationData = new ArrayList<String>();
		locationData.addAll(fields.subList(5, fields.size()));

		return new Session(fields.get(0), Integer.parseInt(fields.get(1)), fields.get(2),
				Timestamp.valueOf(fields.get(3)), Timestamp.valueOf(fields.get(4)), locationData);
	}
	
	public String getSessionID() {
		return sessionID;
	}
	
	public Integer getVersionNumber() {
		return versionNumber;
	}
}
