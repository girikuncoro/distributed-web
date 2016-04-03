package proj1b.rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import proj1b.ssm.Session;
import proj1b.ssm.SessionManager;

public class RPCServer implements Runnable {
	DatagramSocket rpcSocket;
	
	public RPCServer() throws SocketException {
		rpcSocket = new DatagramSocket(RPCConfig.PORT);
	}
	
	@Override
	public void run() {
		while (true) {
			byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
			byte[] outBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
			
			DatagramPacket recvPacket = new DatagramPacket(inBuf, inBuf.length);
			
			try {
				rpcSocket.receive(recvPacket);
				InetAddress returnAddr = recvPacket.getAddress();
				int returnPort = recvPacket.getPort();
				
				// inBuf contains callID and operationCode
				String data = RPCStream.unmarshall(recvPacket.getData());
				RPCStream.Data request = RPCStream.extract(data);
				
				String response = request.getCallID();
				Session session;
				
				switch (request.getOperationCode()) {
					// NoOp: expected response format: callID_responseCode
					case RPCConfig.NO_OP_CODE:  
						response += RPCConfig.RPC_DELIMITER + RPCConfig.NO_OP_CODE;
						break;  
					// sessionRead: expected response format: callID_responseCode_encodedSessionData
					case RPCConfig.READ_CODE:  
						session = SessionManager.getSession(request.getSessionID());
						
						// session found and valid
						if (session != null && session.getVersionNumber() == request.getSessionVersion()) {
							response += RPCConfig.RPC_DELIMITER + session.encode();
						} else {
							response += RPCConfig.RPC_DELIMITER + RPCConfig.RPC_RESPONSE_NOT_FOUND;
						}
						break;
					// sessionWrite: expected response format: callID_responseCode_encodedSessionData
					case RPCConfig.WRITE_CODE:  
						// TODO: sessionWrite
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			DatagramPacket sendPacket = new DatagramPacket(outBuf, outBuf.length);
			
			
		}
	}
	
	
}
