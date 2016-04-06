package proj1b.ssm;

import java.util.*;

import proj1b.util.*;

public class Session{
	private String sessionID; //<ServerID, reboot_num, session id>
	private Integer versionNumber;
	private String message;
	private long expirationTime;
	private Set<String> locationData;
	
	public Session(Integer serID, Integer rebootNum, Integer sessID, Integer verNum, String msg, Set<String> locations){
		sessionID = serID.toString() + Constants.SESSION_DELIMITER + rebootNum.toString() + Constants.SESSION_DELIMITER + sessID.toString();
		versionNumber = verNum;
		message = msg;
		expirationTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000;
		locationData = locations;
	}
	
	// for testing
	public Session(String sessionID) {
		this(0, 0, Integer.parseInt(sessionID), 0, "", null);
	}
	
	/**
	 * Encode session information as cookie value. For example, "1_2_4_11_0_3" 
	 * represents an instance of the fourth session created at SvrID 1 on its 
	 * second reboot, session version number 11, currently stored at SvrIDs 0 and 3.
	 * @return a string representing the cookie value. 
	 */
	public String encode(){
		StringBuilder res = new StringBuilder();
		res.append(sessionID);
		res.append(Constants.SESSION_DELIMITER);
		res.append(String.valueOf(versionNumber));
		Iterator<String> iter = locationData.iterator();
		while (iter.hasNext()){
			res.append(Constants.SESSION_DELIMITER);
			res.append(iter.next());
		}
		return res.toString();
	}
	
	public static Session decode(String encodedSession){
		List<String> fields = Arrays.asList(encodedSession.split(Constants.SESSION_DELIMITER));
		return new Session(Integer.parseInt(fields.get(0)), Integer.parseInt(fields.get(1)), 
				Integer.parseInt(fields.get(2)), Integer.parseInt(fields.get(3)), fields.get(4),
				new HashSet<String>(fields.subList(5, fields.size())));
	}
	
	/**
	 * Redisplay the session message, with an updated session expiration time
	 */
	public void refresh(){
		versionNumber += 1;
		expirationTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000;
	}
	
	/**
	 * Replace the message with a new one (that the user typed into an HTML form field), and display the (new) message and expiration time;
	 */
	public void replace(String msg){
		message = msg;
		versionNumber += 1;
		expirationTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000;
	}
	
	public String getMessage(){
		return message;
	}

	public String getSessionID() {
		return sessionID;
	}
	
	public Integer getVersionNumber() {
		return versionNumber;
	}
	
	public long getExpirationTime(){
		return expirationTime;
	}
	
	public String getCookieValue(){
		return this.encode();
	}
	
	public Set<String> getLocationData(){
		return locationData;
	}

	public int addLocation(String location) {
		locationData.add(location);
		return locationData.size();
	}
	
	public int getLocationDataNumber() {
		return locationData.size();
	}
	
}