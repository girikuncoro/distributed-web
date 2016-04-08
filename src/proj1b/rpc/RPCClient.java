package proj1b.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import proj1b.ssm.Session;
import proj1b.util.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.amazonaws.services.ec2.model.Instance;

public class RPCClient {
	// private static int callID = 0;

	private static final Logger LOGGER = Logger.getLogger("RPC client logger");
	
	public RPCClient(){
		super();
	}

	public Session sessionNoOp(String sessionID, int versionNumber, List<String> svrIPs) {
		for (String svrIP : svrIPs) {
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();
				socket.setSoTimeout(RPCConfig.SOCKET_TIMEOUT);
				byte[] sendBytes = RPCStream
						.marshall(
								String.join(RPCConfig.RPC_DELIMITER,
										new String[] { String.valueOf(RPCConfig.callID),
												String.valueOf(RPCConfig.NO_OP_CODE), sessionID,
												String.valueOf(versionNumber) }));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(svrIP),
						RPCConfig.SERVER_PORT);
				LOGGER.info("Packet ready to send.");
				socket.send(sendPkt);
				LOGGER.info("Packet sent to server.");

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				// String serverIP =
				// RPCConfig.getserverIP(recvPkt.getAddress().getHostAddress());
				String serverIP = null;
				try {
					do {
						recvPkt.setLength(inBuf.length);
						LOGGER.info("Ready to receive reply.");
						socket.receive(recvPkt);
						LOGGER.info("Reply received.");

						serverIP = recvPkt.getAddress().getHostAddress();
						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
						LOGGER.info("Server response: " + RPCStream.unmarshall(recvPkt.getData()));
					} while (serverIP.compareTo(svrIP) != 0 || !RPCConfig.isValidID(Integer.parseInt(recvInfo[0]))
							|| Integer.parseInt(recvInfo[1]) != RPCConfig.RPC_RESPONSE_OK);
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + svrIP);
					recvPkt = null;
					continue;
				}

				RPCConfig.callID++;
				// return Session.decode(recvInfo[2]);
				return null;
			} catch (IOException e) {
				System.out.println("IOException in communicating with server ID: " + svrIP);
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		RPCConfig.callID++;
		return null;
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
	 * @param svrIPs
	 *            A list of IP addresses which might contain the session
	 *            information.
	 * @return The session fetched from other nodes.
	 */
	public Session sessionRead(String sessionID, int versionNumber, List<String> svrIPs) {
		for (String svrIP : svrIPs) {
			System.out.println("Trying to read from: " + svrIP);
			
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();
				socket.setSoTimeout(RPCConfig.SOCKET_TIMEOUT);
				byte[] sendBytes = RPCStream
						.marshall(
								String.join(RPCConfig.RPC_DELIMITER,
										new String[] { String.valueOf(RPCConfig.callID),
												String.valueOf(RPCConfig.READ_CODE), sessionID,
												String.valueOf(versionNumber) }));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(svrIP),
						RPCConfig.SERVER_PORT);
				LOGGER.info("Packet ready to send.");
				socket.send(sendPkt);
				LOGGER.info("Packet sent to server.");

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				// String serverIP =
				// RPCConfig.getserverIP(recvPkt.getAddress().getHostAddress());
				String serverIP = null;
				try {
					do {
						recvPkt.setLength(inBuf.length);
						LOGGER.info("Ready to receive reply.");
						socket.receive(recvPkt);
						LOGGER.info("Reply received.");

						serverIP = recvPkt.getAddress().getHostAddress();
						System.out.println("Getting host address: " + serverIP);
						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
						LOGGER.info("Server response: " + RPCStream.unmarshall(recvPkt.getData()));
					} while (serverIP.compareTo(svrIP) != 0 || !RPCConfig.isValidID(Integer.parseInt(recvInfo[0]))
							|| Integer.parseInt(recvInfo[1]) != RPCConfig.RPC_RESPONSE_OK);
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + svrIP);
					recvPkt = null;
					continue;
				}

				RPCConfig.callID++;
				Session result = Session.decode(recvInfo[2]);
				result.setSourceServerIP(serverIP);
				return result;
//				return Session.decode(recvInfo[2]);
			} catch (IOException e) {
				System.out.println("IOException in communicating with server ID: " + svrIP);
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
	public List<String> sessionWrite(Session session, List<String> svrIPs) {
		List<String> backupList = new ArrayList<String>();
		String localAddress = null;
		
		
		
		for (String svrIP : svrIPs) {
			System.out.println("Trying to write to : " + svrIP);
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();
				localAddress = socket.getLocalAddress().getHostAddress();
				socket.setSoTimeout(RPCConfig.SOCKET_TIMEOUT);
				
				byte[] sendBytes = RPCStream.marshall(String.join(RPCConfig.RPC_DELIMITER, new String[] {
						String.valueOf(RPCConfig.callID), String.valueOf(RPCConfig.WRITE_CODE), session.encode() }));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(svrIP),
						RPCConfig.SERVER_PORT);
				
				LOGGER.info("Packet ready to send.");
				socket.send(sendPkt);
				LOGGER.info("Packet sent to server.");

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				String serverIP = null;
				try {
					do {
						recvPkt.setLength(inBuf.length);
						LOGGER.info("Ready to receive reply.");
						socket.receive(recvPkt);
						LOGGER.info("Reply received.");

						serverIP = recvPkt.getAddress().getHostAddress();
						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
						LOGGER.info("Server response: " + RPCStream.unmarshall(recvPkt.getData()));
					} while (serverIP.compareTo(svrIP) != 0
							|| RPCConfig.isValidID(Integer.parseInt(recvInfo[0])) && addToList(backupList, serverIP) < Constants.WQ);
					// 2 Should be WQ.[Changed 2 to WQ]
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
		if (backupList.size() < Constants.WQ) { // Again, 2 should be WQ. [Changed]
			LOGGER.info("Not enough backup.");
			return null;
		}
		else {
			backupList.add(localAddress); // Add myself
			return backupList;
		}
	}
	
	private int addToList(List<String> list, String item) {
		list.add(item);
		return list.size();
	}
	
	//
	// public void sessionUpdate(Session session, List<String> svrIPs) {
	// for (String svrIP : svrIPs) {
	// DatagramSocket socket = null;
	// try {
	// socket = new DatagramSocket();
	// byte[] sendBytes =
	// RPCStream.marshall(String.join(RPCConfig.RPC_DELIMITER, new String[] {
	// String.valueOf(RPCConfig.callID), String.valueOf(RPCConfig.WRITE_CODE),
	// session.encode() }));
	// DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length,
	// InetAddress.getByName(svrIP),
	// RPCConfig.SERVER_PORT);
	// socket.send(sendPkt);
	// } catch (IOException e) {
	// System.out.println("IOException in communicating with server ID: " +
	// svrIP);
	// e.printStackTrace();
	// } finally {
	// socket.close();
	// }
	// }
	//
	// RPCConfig.callID++;
	// }
}
