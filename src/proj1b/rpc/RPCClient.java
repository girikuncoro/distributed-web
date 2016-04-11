package proj1b.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import proj1b.ssm.Session;
import proj1b.ssm.SessionInServer;
import proj1b.util.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;

import com.amazonaws.services.ec2.model.Instance;

public class RPCClient {
	private static final Logger LOGGER = Logger.getLogger("RPC client logger");

	public RPCClient() {
		super();
	}

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
	 * @param svrIDs
	 *            A list of server IDs which might contain the session
	 *            information.
	 * @return The session fetched from other nodes.
	 */
	public SessionInServer sessionRead(String sessionID, int versionNumber, List<String> svrIDs) {
		for (String svrID : svrIDs) {
			String svrIP = Utils.getSvrIPfromID(svrID);
			LOGGER.info("Trying to read from server: " + svrID + ", whose IP address is: " + svrIP);

			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();
				socket.setSoTimeout(RPCConfig.SOCKET_TIMEOUT);
				byte[] sendBytes = RPCStream
						.marshall(String.join(RPCConfig.RPC_DELIMITER, String.valueOf(RPCConfig.callID),
								String.valueOf(RPCConfig.READ_CODE), sessionID, String.valueOf(versionNumber)));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(svrIP),
						RPCConfig.SERVER_PORT);
				LOGGER.info("Packet ready to send.");
				socket.send(sendPkt);
				LOGGER.info("Packet sent to server.");

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				String receivedServerIP = null;
				try {
					do {
						recvPkt.setLength(inBuf.length);
						LOGGER.info("Ready to receive reply.");
						socket.receive(recvPkt);
						LOGGER.info("Reply received.");

						receivedServerIP = recvPkt.getAddress().getHostAddress();
						System.out.println("Getting host address: " + receivedServerIP);
						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
						LOGGER.info("Server response: " + RPCStream.unmarshall(recvPkt.getData()));
					} while (receivedServerIP.compareTo(svrIP) != 0 || !RPCConfig.isValidID(Integer.parseInt(recvInfo[0]))
							|| Integer.parseInt(recvInfo[1]) != RPCConfig.RPC_RESPONSE_OK);
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + svrID);
					recvPkt = null;
					continue;
				}

				RPCConfig.callID++;
				return new SessionInServer(Session.decode(recvInfo[2]), svrID);
			} catch (IOException e) {
				System.out.println("IOException in communicating with server ID: " + svrID);
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		RPCConfig.callID++;
		return null;
	}

	/**
	 * This function implements RPC sessionWrite by go over all the IP addresses
	 * in the list and send the RPC call sequentially. If we receive WQ replies
	 * message, then we are done. If not, there is not enough backups.
	 * 
	 * @param session
	 *            The session we need to write to other servers.
	 * @param svrIPs
	 *            A list of IP addresses which might contain the session
	 *            information.
	 */
	public List<String> sessionWrite(Session session, List<String> svrIDs) {
		List<String> backupList = new ArrayList<String>();

		for (String svrID : svrIDs) {
			String svrIP = Utils.getSvrIPfromID(svrID);
			System.out.println("Trying to write to : " + svrIP);
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();
				socket.setSoTimeout(RPCConfig.SOCKET_TIMEOUT);

				byte[] sendBytes = RPCStream.marshall(String.join(RPCConfig.RPC_DELIMITER,
						String.valueOf(RPCConfig.callID), String.valueOf(RPCConfig.WRITE_CODE), session.encode()));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(svrIP),
						RPCConfig.SERVER_PORT);

				LOGGER.info("Packet ready to send.");
				socket.send(sendPkt);
				LOGGER.info("Packet sent to server.");

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				String receivedServerIP = null;
				try {
					do {
						if(backupList.size() == Constants.WQ) break;
						
						recvPkt.setLength(inBuf.length);
						LOGGER.info("Ready to receive reply.");
						socket.receive(recvPkt);
						LOGGER.info("Reply received.");

						receivedServerIP = recvPkt.getAddress().getHostAddress();
						System.out.println("Server IP in session write " + receivedServerIP);
						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
						LOGGER.info("Server response: " + RPCStream.unmarshall(recvPkt.getData()));
					} while (receivedServerIP.compareTo(svrIP) != 0 || !RPCConfig.isValidID(Integer.parseInt(recvInfo[0]))
							|| addToList(backupList, svrID) < Constants.WQ);
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + svrIP);
					recvPkt = null;
					continue;
				}
			} catch (IOException e) {
				System.out.println("IOException in communicating with server ID: " + svrIP);
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		RPCConfig.callID++;
		System.out.println("!!!!!!!!BackupList is: " + backupList.toString());
		if(backupList.size() == Constants.WQ) return backupList;
		else {
			LOGGER.info("Not enough backup.");
			return null;
		}
	}

	private int addToList(List<String> list, String item) {
		list.add(item);
		System.out.println(list.toString());
		return list.size();
	}
}
