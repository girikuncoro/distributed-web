package proj1b.rpc;

public class RPCConfig {
	public static final int PORT = 5300;
	public static final int MAX_PACKET_LENGTH = 512;
	public static final int NO_OP_CODE = 0;
	public static final int READ_CODE = 1;
	public static final int WRITE_CODE = 2;
	
	public static final String RPC_DELIMITER = "_";
	public static final int RPC_RESPONSE_OK = 200;
	public static final int RPC_RESPONSE_NOT_FOUND = 400;
	public static final int RPC_RESPONSE_INVALID_OPCODE = 300;
}
