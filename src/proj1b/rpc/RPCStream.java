package proj1b.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import proj1b.ssm.Session;

// Ref: http://stackoverflow.com/questions/5837698/converting-any-object-to-a-byte-array-in-java
public class RPCStream {
	/**
	 * Marshall the data from RPC client stub to server stub
	 * 
	 * @param String
	 *            data: input data from client
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
	 * 
	 * @param byte[]
	 *            data: input data from client
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
		String callID;
		int operationCode;
	}

	public static class DataRead extends Data {
		String sessionID;
		int sessionVersion;

		public DataRead() {
			super();
		}
	}

	public static class DataWrite extends Data {
		Session session;

		public DataWrite() {
			super();
		}
	}

	/**
	 * Extract the string data to get callID and operationCode
	 * 
	 * @param String
	 *            RPCdata
	 * @return Data extracted data
	 */
	public static Data extract(String RPCData) {
		// Expected format: callID_operationCode_***

		String[] req = RPCData.split(RPCConfig.RPC_DELIMITER);
		DataRead res = new DataRead();

		res.callID = req[0];
		res.operationCode = Integer.parseInt(req[1]);
		return res;
	}

	/**
	 * Extract the string data to get callID, operationCode, sessionID, and
	 * sessionVersion from current RPC call
	 * 
	 * @param String
	 *            RPCdata
	 * @return DataRead extracted data
	 */
	public static DataRead extractRead(String RPCData) {
		// Expected format: callID_operationCode_sessionID_sessionVersion

		String[] req = RPCData.split(RPCConfig.RPC_DELIMITER);
		DataRead res = new DataRead();

		res.callID = req[0];
		res.operationCode = Integer.parseInt(req[1]);
		res.sessionID = req[2];
		res.sessionVersion = Integer.parseInt(req[3]);
		return res;
	}

	/**
	 * Extract the string data to get callID, operationCode, sessionData from
	 * current RPC call
	 * 
	 * @param String
	 *            RPCdata
	 * @return DataWrite extracted data
	 */
	public static DataWrite extractWrite(String RPCData) {
		// Expected format: callID_operationCode_encodedSessionData

		String[] req = RPCData.split(RPCConfig.RPC_DELIMITER);
		DataWrite res = new DataWrite();

		System.out.println("extract write : " + Arrays.asList(req).toString());

		res.callID = req[0];
		res.operationCode = Integer.parseInt(req[1]);
		res.session = Session.decode(req[2]);
		return res;
	}
}
