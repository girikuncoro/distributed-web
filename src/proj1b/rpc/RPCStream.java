package proj1b.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Ref: http://stackoverflow.com/questions/5837698/converting-any-object-to-a-byte-array-in-java
public class RPCStream {
	/**
	 * Marshall the data from RPC client stub to server stub
	 * @param String data: input data from client
	 * @return byte[] res: marshalled data
	 */
	public static byte[] marshall(String data) {
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
		byte[] res = null;
		
		try {
			ObjectOutputStream output = new ObjectOutputStream(outBuf);
			output.writeObject(data);
			res = outBuf.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return res;
	}
	
	/**
	 * Unmarshall the data from RPC client stub
	 * @param byte[] data: input data from client
	 * @return String res: unmarshalled data
	 */
	public static String unmarshall(byte[] data) {
		ByteArrayInputStream inBuf = new ByteArrayInputStream(data);
		String res = null;
		
		try {
			ObjectInputStream input = new ObjectInputStream(inBuf);
			res = (String) input.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * Inner class for the extracted data from string data
	 */
	public static class Data {
		private String callID;
		private Integer operationCode;
		private String sessionID;
		private Integer sessionVersion;
		
		public String getCallID() {
			return this.callID;
		} 
		
		public Integer getOperationCode() {
			return this.operationCode;
		}
		
		public String getSessionID() {
			return this.sessionID;
		}
		
		public Integer getSessionVersion() {
			return this.sessionVersion;
		}
	}
	
	/**
	 * Extract the string data to get callID, operationCode, sessionID, and sessionVersion from current RPC call
	 * @param String RPCdata
	 * @return Data extracted data
	 */
	public static Data extract(String RPCData) {
		// Expected format: callID_operationCode_sessionID_sessionVersion
		
		String[] req = RPCData.split(RPCConfig.RPC_DELIMITER);
		Data res = new Data();
		
		res.callID = req[0];
		res.operationCode = Integer.parseInt(req[1]);
		res.sessionID = req[2];
		res.sessionVersion = Integer.parseInt(req[3]);
		return res;
	}
}
