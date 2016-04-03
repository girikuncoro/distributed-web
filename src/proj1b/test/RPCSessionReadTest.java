package proj1b.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import proj1b.rpc.RPCClient;
import proj1b.rpc.RPCConfig;
import proj1b.rpc.RPCServer;
import proj1b.ssm.Session;
import proj1b.ssm.SessionManager;

public class RPCSessionReadTest {
	
	private static final Logger LOGGER = Logger.getLogger("RPC Session read test logger");
	
	/**
	 * There are four cases:
	 * 1. Valid callID, session exists, version number is correct.
	 * 2. Valid callID, session exists, version number is incorrect.
	 * 3. Valid callID, session dosen't exist.
	 * 4. Invalid callID.
	 */

	public static void main(String[] args) {
		RPCServer server = new RPCServer();
		RPCClient client1 = new RPCClient();
		
		Set<String> ipAddresses = new HashSet<String>();
		ipAddresses.add("127.0.0.1");
		
		RPCConfig.initializeMap(ipAddresses);
		
		Thread serverThread = new Thread(server);
		serverThread.setName("server");
		serverThread.start();
		
		Session session1 = new Session("1", ipAddresses);
		Session session2 = new Session("1", ipAddresses);
		session2.update();
		
		SessionManager.addToTable(session1);
		SessionManager.addToTable(session2);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> l = new ArrayList<String>();
		l.addAll(ipAddresses);
		Session returnSession = client1.sessionRead("1", 1, l);
		LOGGER.info("Return serialized session object for case 1: " + returnSession.encode());
	}

}
