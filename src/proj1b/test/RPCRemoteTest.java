package proj1b.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import proj1b.rpc.RPCClient;
import proj1b.rpc.RPCServer;
import proj1b.ssm.Session;
import proj1b.ssm.SessionManager;

public class RPCRemoteTest {
	
	private static final Logger LOGGER = Logger.getLogger("RPC Session read test logger");

	public static void main(String[] args) {
		RPCServer server = new RPCServer();
		RPCClient client1 = new RPCClient();
		
		Thread serverThread = new Thread(server);
		serverThread.setName("server");
		serverThread.start();
		
		Session session1 = new Session("1");
		Session session2 = new Session("1");
		session2.update();
		
		SessionManager.addToTable(session1);
		SessionManager.addToTable(session2);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Set<String> ipAddresses = new HashSet<String>();
		ipAddresses.add("10.148.7.106");
		List<String> l = new ArrayList<String>();
		l.addAll(ipAddresses);
		
		Session newSession = new Session("2");
		client1.sessionWrite(newSession, l);
		
		Session returnSession;
		returnSession = client1.sessionRead("1", 1, l);
		if(returnSession == null) LOGGER.info("Return session is null for the case 1.");
		else LOGGER.info("Return serialized session object for case 1: " + returnSession.encode());
		
		returnSession = client1.sessionRead("1", 0, l);
		if(returnSession == null) LOGGER.info("Return session is null for the case 2.");
		else LOGGER.info("Return serialized session object for case 2: " + returnSession.encode());
		
		returnSession = client1.sessionRead("1", 2, l);
		if(returnSession == null) LOGGER.info("Return session is null for the case 3.");
		else LOGGER.info("Return serialized session object for case 3: " + returnSession.encode());
		
		returnSession = client1.sessionRead("2", 0, l);
		if(returnSession == null) LOGGER.info("Return session is null for the case 4.");
		else LOGGER.info("Return serialized session object for case 4: " + returnSession.encode());
	}

}
