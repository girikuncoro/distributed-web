package proj1b.rpc;

import com.amazonaws.services.ec2.model.Instance;

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

	static int callID = 0;
	
	static boolean isValidID(int receivedCallID) {
		return callID == receivedCallID;
	}
	
	static String getServerID(String ipAddress) {
		Instance instance = new Instance();
		instance.withPublicIpAddress(ipAddress);
		Integer serverID = instance.getAmiLaunchIndex();
		return serverID.toString();
	}
}
