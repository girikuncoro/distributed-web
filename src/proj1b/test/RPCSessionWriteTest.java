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

public class RPCSessionWriteTest {
	
	private static final Logger LOGGER = Logger.getLogger("RPC Session read test logger");

	public static void main(String[] args) {
		RPCServer server = new RPCServer();
		RPCClient client1 = new RPCClient();
		SessionManager ssm = SessionManager.getInstance();
		
		Set<String> ipAddresses = new HashSet<String>();
		ipAddresses.add("127.0.0.1");
		
		Thread serverThread = new Thread(server);
		serverThread.setName("server");
		serverThread.start();
		
		Session session1 = new Session("1");
		Session session2 = new Session("1");
		session2.refresh();
		
		ssm.addSession(session1);
		ssm.addSession(session2);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> l = new ArrayList<String>();
		l.addAll(ipAddresses);
		
		Session newSession = new Session("2");
		client1.sessionWrite(newSession, l);
		for(Session s : ssm.getTableValues())
			LOGGER.info(s.encode());
	}

}
