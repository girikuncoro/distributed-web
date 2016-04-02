package proj1b.rpc;

public class RPCConfig {
	public static final int PORT = 5300;
	public static final int MAX_PACKET_LENGTH = 512;
	public static final int NO_OP_CODE = 0;
	public static final int READ_CODE = 1;
	public static final int WRITE_CODE = 2;
	
	public static final String RPC_DELIMITER = "_";
}
