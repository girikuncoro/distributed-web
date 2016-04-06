package proj1b.ssm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import proj1b.util.*;

public class SessionManager{
	private static SessionManager instance = new SessionManager();
	private static Map<String, Session> sessionDataTable = new ConcurrentHashMap<String, Session>();
	// garbage collection
	private static Thread cleanUp = new Thread(new Runnable(){
		@Override
		public void run() {
			while (true){
				Iterator<Map.Entry<String, Session>> iter = sessionDataTable.entrySet().iterator();
				Session element = null;
				while (iter.hasNext()){
					element = iter.next().getValue();
					if (element.getExpirationTime() < System.currentTimeMillis()){
						iter.remove();
					}
				}
				try{
					Thread.sleep(3 * 60 * 1000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	});
	
	private SessionManager(){
        cleanUp.start();
	}
	
	public static SessionManager getInstance(){
		return instance;
	}

	public void addSession(Session session) {
		String key = session.getSessionID() + Constants.SESSION_DELIMITER + session.getVersionNumber();
		sessionDataTable.put(key, session);
	}
	
	public void removeFromTable(Session session) {
		String key = session.getSessionID() + Constants.SESSION_DELIMITER + session.getVersionNumber();
		if (sessionDataTable.containsKey(key)){
			sessionDataTable.remove(key);
		}
		
//		try {
//			sessionDataTable.remove(session.getSessionID());
//		}
//		catch(Exception e) {
//			System.out.println("Session: " + session.getSessionID() + " has already been removed.");
//		}
	}
	
	public void removeFromTable(String key){
		if (sessionDataTable.containsKey(key)){
			sessionDataTable.remove(key);
		}
	}
	
	public Session getSession(String sessionName, int versionNumber) {
		String key = sessionName + Constants.SESSION_DELIMITER + versionNumber;
		if(!sessionDataTable.containsKey(key)) return null;
		return sessionDataTable.get(key);
	}
	
	public Session getSession(String key){
		return sessionDataTable.get(key);
	}

	public boolean isInTheTable(String key){
		return sessionDataTable.containsKey(key);
	}
	
	public boolean isExpired(String key){
		if (sessionDataTable.containsKey(key)){
			if (sessionDataTable.get(key).getExpirationTime() < System.currentTimeMillis()){
				return true;
			}
		}
		return false;
	}
	
	public Collection<Session> getTableValues() {
		return sessionDataTable.values();
	}
}