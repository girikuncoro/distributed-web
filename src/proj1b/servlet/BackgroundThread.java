package proj1b.servlet;

import java.util.logging.Logger;
import javax.servlet.*;

import proj1b.rpc.RPCServer;

public class BackgroundThread implements ServletContextListener {
	private static final Logger LOGGER = Logger.getLogger("Background thread Logger");
	private Thread t = null;

	public void contextInitialized(ServletContextEvent sce) {
		if ((t == null) || (!t.isAlive())) {
			t = new Thread(new RPCServer());
			t.start();
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		t.interrupt();
	}
}
