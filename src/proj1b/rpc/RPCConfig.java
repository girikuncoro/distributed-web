package proj1b.rpc;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.ec2.model.Instance;

import proj1b.util.Utils;

public class RPCConfig {
	static final int SERVER_PORT = 5300;
	static final int MAX_PACKET_LENGTH = 512;
	static final int NO_OP_CODE = 0;
	static final int READ_CODE = 1;
	static final int WRITE_CODE = 2;

	static final String RPC_DELIMITER = "_";
	static final int RPC_RESPONSE_OK = 200;
	static final int RPC_RESPONSE_INVALID_OPCODE = 300;
	static final int RPC_RESPONSE_NOT_FOUND = 400;
	static final int RPC_RESPONSE_INVALID_CALLID = 401;

	static final int SOCKET_TIMEOUT = 1000;

	static Map<String, Integer> callIDMap = new HashMap<String, Integer>();

	static boolean isValidID(String receivedSvrID, int receivedCallID) {
		if (!callIDMap.containsKey(receivedCallID)) {
			callIDMap.put(receivedSvrID, receivedCallID);
			return true;
		}

		if (callIDMap.get(receivedSvrID) > receivedCallID)
			return false;

		callIDMap.put(receivedSvrID, receivedCallID);
		return true;
	}

	static int getLocalCallID() {
		String localServerID = Utils.getLocalServerID();
		if (!callIDMap.containsKey(localServerID))
			callIDMap.put(localServerID, 0);
		return callIDMap.get(localServerID);
	}

	static void incrementLocalCallID() {
		String localServerID = Utils.getLocalServerID();
		if (!callIDMap.containsKey(localServerID))
			System.out.println("Error in increment local call ID. No such key.");

		callIDMap.put(localServerID, callIDMap.get(localServerID) + 1);
	}

	static String getServerID(String ipAddress) {
		Instance instance = new Instance();
		instance.withPublicIpAddress(ipAddress);
		Integer serverID = instance.getAmiLaunchIndex();
		return serverID.toString();
	}
}
