package proj1b.ssm;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import proj1b.util.*;

public class SessionManager {
	private static SessionManager instance = new SessionManager();
	private static Map<String, Session> sessionDataTable = new ConcurrentHashMap<String, Session>();

	private static ScheduledExecutorService executor;

	private SessionManager() {
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new SessionCleaner(), Constants.SESSION_CLEANER_INTERVAL,
				Constants.SESSION_CLEANER_INTERVAL, TimeUnit.SECONDS);
	}

	public static SessionManager getInstance() {
		return instance;
	}

	public void addSession(Session session) {
		String key = session.getSessionID() + Constants.SESSION_DELIMITER + session.getVersionNumber();
		sessionDataTable.put(key, session);
	}

	public void removeFromTable(Session session) {
		String key = session.getSessionID() + Constants.SESSION_DELIMITER + session.getVersionNumber();
		if (sessionDataTable.containsKey(key)) {
			sessionDataTable.remove(key);
		}
	}

	public void removeFromTable(String key) {
		if (sessionDataTable.containsKey(key)) {
			sessionDataTable.remove(key);
		}
	}

	public Session getSession(String sessionName, int versionNumber) {
		String key = sessionName + Constants.SESSION_DELIMITER + versionNumber;
		if (!sessionDataTable.containsKey(key))
			return null;
		return sessionDataTable.get(key);
	}

	public Collection<Session> getTableValues() {
		return sessionDataTable.values();
	}

	public Set<Entry<String, Session>> getEntrySet() {
		return sessionDataTable.entrySet();
	}
}