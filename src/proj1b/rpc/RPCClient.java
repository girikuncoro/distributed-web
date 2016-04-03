package proj1b.rpc;

import java.util.List;
import java.sql.Timestamp;

import proj1b.ssm.Session;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class RPCClient {
	private static int callID = 0;

	/**
	 * This function implements RPC sessionRead by go over all the IP addresses
	 * in the list and send the RPC call sequentially. If we receive one reply
	 * message, then return the session by decoding the stream bytes. If we
	 * didn't receive the message for all addresses, return null.
	 * 
	 * @param sessionID
	 *            Session ID to be included in the read RPC.
	 * @param versionNumber
	 *            Version Number to be included in the read RPC.
	 * @param ipAddresses
	 *            A list of IP addresses which might contain the session
	 *            information.
	 * @return The session fetched from other nodes.
	 */
	public Session sessionRead(String sessionID, int versionNumber, List<String> ipAddresses) {
		for (String address : ipAddresses) {
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket(RPCConfig.PORT, InetAddress.getByName(address));
				byte[] sendBytes = RPCStream
						.marshall(String.join(RPCConfig.RPC_DELIMITER, new String[] { String.valueOf(callID),
								String.valueOf(RPCConfig.READ_CODE), sessionID, String.valueOf(versionNumber) }));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length);
				socket.send(sendPkt);

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				try {
					do {
						recvPkt.setLength(inBuf.length);
						socket.receive(recvPkt);

						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
					} while (recvPkt.getAddress().getHostAddress().compareTo(address) != 0
							|| Integer.parseInt(recvInfo[0]) != callID || Integer.parseInt(recvInfo[1]) != 200);
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + address);
					recvPkt = null;
					continue;
				}

				callID++;
				return Session.decode(recvInfo[2]);
			} catch (IOException e) {
				System.out.println("IOException in communicating with address: " + address);
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		callID++;
		return null;
	}

	/**
	 * This function implements RPC sessionWrite by go over all the IP addresses
	 * in the list and send the RPC call sequentially. If we receive WQ replies
	 * message, then we are done. If not, there is not enough backups.
	 * 
	 * @param session
	 *            The session we need to write to other servers.
	 * @param ipAddresses
	 *            A list of IP addresses which might contain the session
	 *            information.
	 */
	public void sessionWrite(Session session, List<String> ipAddresses) {
		for (String address : ipAddresses) {
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket(RPCConfig.PORT, InetAddress.getByName(address));
				byte[] sendBytes = RPCStream.marshall(String.join(RPCConfig.RPC_DELIMITER, new String[] {
						String.valueOf(callID), String.valueOf(RPCConfig.READ_CODE), session.encode() }));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length);
				socket.send(sendPkt);

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				try {
					do {
						recvPkt.setLength(inBuf.length);
						socket.receive(recvPkt);

						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
					} while (recvPkt.getAddress().getHostAddress().compareTo(address) == 0
							&& Integer.parseInt(recvInfo[0]) == callID && Integer.parseInt(recvInfo[1]) == 200
							&& session.addLocation(address) <= 2); // 2 Should be WQ
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + address);
					recvPkt = null;
					continue;
				}
			} catch (IOException e) {
				System.out.println("IOException in communicating with address: " + address);
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		callID++;
		if (session.getLocationDataNumber() <= 2) // Again, 2 should be WQ
			System.out.println("Not enough backup.");
	}
}
