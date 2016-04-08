package proj1b.servlet;

import java.io.*;
import java.util.logging.Logger;

import javax.servlet.http.*;

import proj1b.rpc.RPCServer;

import javax.servlet.*;

public class BackgroundThread implements ServletContextListener {
	private static final Logger LOGGER = Logger.getLogger("Background thread Logger");
    private Thread t = null;
    public void contextInitialized(ServletContextEvent sce) {
        if ((t == null) || (!t.isAlive())) {
           t = new Thread(new RPCServer()); 
//                public void run() {
//                	LOGGER.info("Starting Background Thread");
//                    try {
////                        new FileOutputStream("/tmp/cs5300proj1b-tmp").close();
//                    	new RPCServer();
//                    } catch (Exception e) {
//                    	LOGGER.info("Exception in background thread");
//                    	e.printStackTrace();
//                    }
//                }
//            };
            t.start();
        }
        
    }
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            t.interrupt();
        } catch (Exception ex) {
        }
    }
}
