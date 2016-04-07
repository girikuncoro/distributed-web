package proj1b.rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;

import proj1b.ssm.Session;
import proj1b.ssm.SessionManager;

public class RPCServer implements Runnable {
	DatagramSocket rpcSocket;
	
	private static final Logger LOGGER = Logger.getLogger("RPC server logger");
	SessionManager ssm = SessionManager.getInstance();
	
	public RPCServer() {
		try {
			LOGGER.info("Ready to build socket.");
			rpcSocket = new DatagramSocket(RPCConfig.SERVER_PORT);
			LOGGER.info("Socket built.");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This function runs a loop that serve RPC server in separate thread.
	 */
	@Override
	public void run() {
		LOGGER.info("Server is running.");
		while (true) {
			byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
			byte[] outBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
			
			DatagramPacket recvPacket = new DatagramPacket(inBuf, inBuf.length);
			
			try {
				LOGGER.info("Trying to receive packet.");
				rpcSocket.receive(recvPacket);
				InetAddress returnAddr = recvPacket.getAddress();
				int returnPort = recvPacket.getPort();
				LOGGER.info("Packet received from: " + returnAddr.toString() + ", " + returnPort);
				
				// inBuf contains callID and operationCode
				String data = RPCStream.unmarshall(recvPacket.getData());
				LOGGER.info("data is: " + data);
				RPCStream.Data request = RPCStream.extract(data);  // write has less split length
				
				String response = request.callID;
				Session session;
				
				// TODO: Validate callID?
				switch (request.operationCode) {
					
					// NoOp: expected response format: callID_responseCode
					case RPCConfig.NO_OP_CODE:  
						response += RPCConfig.RPC_DELIMITER + RPCConfig.RPC_RESPONSE_OK;
						break;  
					
					// sessionRead: expected response format: callID_responseCode_encodedSessionData
					case RPCConfig.READ_CODE:  
						RPCStream.DataRead read = RPCStream.extractRead(data);
						session = ssm.getSession(read.sessionID, read.sessionVersion);
//						String returnServerID = RPCConfig.getServerID(returnAddr.toString());
						String returnServerID = "127.0.0.1";
						
						// session found and valid
						if (session != null && session.getVersionNumber() == read.sessionVersion && 
								RPCConfig.isValidID(Integer.parseInt(request.callID))) {
							response += RPCConfig.RPC_DELIMITER + RPCConfig.RPC_RESPONSE_OK + RPCConfig.RPC_DELIMITER + session.encode();
						} 
						else if (!RPCConfig.isValidID(Integer.parseInt(request.callID))){
							response += RPCConfig.RPC_DELIMITER + RPCConfig.RPC_RESPONSE_INVALID_CALLID;
						} 
						else {
							response += RPCConfig.RPC_DELIMITER + RPCConfig.RPC_RESPONSE_NOT_FOUND;
						}
						break;
					
					// sessionWrite: expected response format: callID_responseCode_encodedSessionData
					case RPCConfig.WRITE_CODE:  
						// no need to check session exist, we need to keep all versions
						// TODO: validate callID
						// TODO: checking the old sessionID exist and keep if less than discardTime
						// TODO: should we check currentTime > discardedTime?
						// TODO: for heavy delayed call, what if existing session already cleaned up?
						// TODO: sessionID + ipAdress must be handled in servlet to maintain globally unique sessionID
						
						RPCStream.DataWrite write = RPCStream.extractWrite(data);
						String serverIpAddress = rpcSocket.getLocalAddress().toString();
//						request.session.addLocation(RPCConfig.getServerID(serverIpAddress));
						ssm.addSession(write.session);
						response += RPCConfig.RPC_DELIMITER + RPCConfig.RPC_RESPONSE_OK;
						break;
					
					// invalidOpCode: expected response format: callID_responseCode
					default:  
						response += RPCConfig.RPC_DELIMITER + RPCConfig.RPC_RESPONSE_INVALID_OPCODE;
						break;
				}
				
				// outBuf should contain callID and response data
				outBuf = RPCStream.marshall(response);
				DatagramPacket sendPacket = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
				rpcSocket.send(sendPacket);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
