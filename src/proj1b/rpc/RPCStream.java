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
	 * Marshall the data from RPC client stub to server stub
	 * @param String data: input data from client
	 * @return byte[] res: marshalled data
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
}
