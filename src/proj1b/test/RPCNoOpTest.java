package proj1b.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import proj1b.rpc.RPCClient;
import proj1b.rpc.RPCConfig;
import proj1b.rpc.RPCServer;
import proj1b.ssm.Session;

public class RPCNoOpTest {

	public static void main(String[] args) {
		RPCServer server = new RPCServer();
		RPCClient client1 = new RPCClient();
		
		Set<String> ipAddresses = new HashSet<String>();
		ipAddresses.add("127.0.0.1");
		
		RPCConfig.initializeMap(ipAddresses);
		
		Thread serverThread = new Thread(server);
		serverThread.setName("server");
		serverThread.start();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> l = new ArrayList<String>();
		l.addAll(ipAddresses);
		Session returnSession = client1.sessionNoOp("1", 1, l);
	}

}
