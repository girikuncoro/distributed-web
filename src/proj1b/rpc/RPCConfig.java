package proj1b.rpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.ec2.model.Instance;

public class RPCConfig {
	public static final int PORT = 5300;
	public static final int MAX_PACKET_LENGTH = 512;
	public static final int NO_OP_CODE = 0;
	public static final int READ_CODE = 1;
	public static final int WRITE_CODE = 2;

	public static final String RPC_DELIMITER = "_";
	public static final int RPC_RESPONSE_OK = 200;
	public static final int RPC_RESPONSE_INVALID_OPCODE = 300;
	public static final int RPC_RESPONSE_NOT_FOUND = 400;
	public static final int RPC_RESPONSE_INVALID_CALLID = 401;

	private static Map<String, Integer> svrIDcallIDMap = new HashMap<String, Integer>();

	public static void initializeMap(List<String> svrIDs) {
		for (String svrID : svrIDs)
			svrIDcallIDMap.put(svrID, 0);
	}

	public static int getCallID(String svrID) {
		return svrIDcallIDMap.get(svrID);
	}

	public static void setCallID(String svrID, int callID) {
		svrIDcallIDMap.put(svrID, callID);
	}

	public static boolean isValidID(String svrID, int receivedCallID) {
		int callIdInMap = svrIDcallIDMap.get(svrID);

		if (receivedCallID < callIdInMap) // Delayed packet
			return false;

		svrIDcallIDMap.put(svrID, receivedCallID);
		return true;
	}
	
	public static String getServerID(String ipAddress) {
		Instance instance = new Instance();
		instance.withPublicIpAddress(ipAddress);
		Integer serverID = instance.getAmiLaunchIndex();
		return serverID.toString();
	}
}
