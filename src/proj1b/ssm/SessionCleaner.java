package proj1b.ssm;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SessionCleaner implements Runnable {
	@Override
	public void run() {
		try {
			for (Iterator<Entry<String, Session>> iter = SessionManager.getInstance().getEntrySet().iterator(); iter.hasNext();) {
				Map.Entry<String, Session> entry = iter.next();
				if (entry.getValue().getExpirationTime() < System.currentTimeMillis())
					iter.remove();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
