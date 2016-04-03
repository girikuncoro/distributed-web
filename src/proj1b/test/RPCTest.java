package proj1b.test;

import java.util.ArrayList;
import java.util.List;

import proj1b.rpc.RPCClient;
import proj1b.rpc.RPCServer;
import proj1b.ssm.Session;

public class RPCTest {

	public static void main(String[] args) {
		RPCServer server = new RPCServer();
		RPCClient client1 = new RPCClient();
		
		List<String> ipAddresses = new ArrayList<String>();
		ipAddresses.add("127.0.0.1");
		
		Thread serverThread = new Thread(server);
		serverThread.setName("server");
		serverThread.start();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Session session = client1.sessionRead("1", 1, ipAddresses);
		System.out.println(session.encode());
	}

}
