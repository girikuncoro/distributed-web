package proj1b.ssm;

public class SessionInServer {
	private Session session;
	private String serverID;

	public SessionInServer(Session session, String serverID) {
		this.session = session;
		this.serverID = serverID;
	}

	public Session getSession() {
		return session;
	}

	public String getServerID() {
		return serverID;
	}
}
