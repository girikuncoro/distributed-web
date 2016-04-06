package proj1b.ssm;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import proj1b.util.*;

public class SessionManager{
	private static SessionManager instance = new SessionManager();
	private static Integer serverID; //TODO mapping
	private static Integer rebootNum; 	// TODO read reboot_num from file system
	private static Integer nextSessionID = 0;
	private static Map<String, Session> sessionDataTable = new ConcurrentHashMap<String, Session>();
	
	private SessionManager(){
		
	}
	
	public static SessionManager getInstance(){
		return instance;
	}
	
	// TODO garbage collection
	
	
	// From Shibo, for testing RPC
	public static void addToTable(Session session) {
		String key = session.getSessionID() + Constants.SESSION_DELIMITER + session.getVersionNumber();
		sessionDataTable.put(key, session);
	}
	
	public static void removeFromTable(Session session) {
		try {
			sessionDataTable.remove(session.getSessionID());
		}
		catch(Exception e) {
			System.out.println("Session: " + session.getSessionID() + " has already been removed.");
		}
	}
	
	public static Session getSession(String sessionName, int versionNumber) {
		String key = sessionName + Constants.SESSION_DELIMITER + versionNumber;
		if(!sessionDataTable.containsKey(key)) return null;
		return sessionDataTable.get(key);
	}
	
	public static Collection<Session> getTableValues() {
		return sessionDataTable.values();
	}
}