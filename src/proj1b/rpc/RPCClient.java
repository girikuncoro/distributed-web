package proj1b.rpc;

import java.util.List;

import proj1b.ssm.Session;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class RPCClient {
	private static int callID = 0;

	public Session sessionRead(String sessionID, int versionNumber, List<String> ipAddresses) {
		for (String address : ipAddresses) {
			try {
				DatagramSocket socket = new DatagramSocket(RPCConfig.PORT, InetAddress.getByName(address));

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
					socket.close();
					continue;
				}

				socket.close();
				return Session.decode(recvInfo[2]);
			} catch (IOException e) {
				System.out.println("IOException in communicating with address: " + address);
				e.printStackTrace();
			}
		}

		callID++;
		return null;
	}
}
