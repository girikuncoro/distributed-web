package proj1b.util;

public class Constants {
	/*
	 * System wide
	 */
	
	// TODO: removing final for testing purpose, this params should be configurable from somewhere else
	public static int N = 3; // number of total servers
	public static int F = 1; // F-resilient website
	public static int R = F + 1; // number of data bricks to send read requests
	public static int WQ = F + 1; // number of data bricks that should contain a session
	public static int W = 2 * F + 1; // number of data bricks to send write requests
	
//	public static final int N = 3; // number of total servers
//	public static final int F = 1; // F-resilient website
//	public static final int R = F + 1; // number of data bricks to send read requests
//	public static final int WQ = F + 1; // number of data bricks that should contain a session
//	public static final int W = 2 * F + 1; // number of data bricks to send write requests
	public static final String SESSION_DELIMITER = "_";
	public static final String InstancesDir = System.getProperty("user.home") + "/instances.txt";
	public static final String rebootDir = System.getProperty("user.home") + "/rebootNum.txt";
	/*
	 * Cookie
	 */
	public static final String COOKIE_NAME = "CS5300PROJ1SESSION";
	public static final long MAX_AGE = 600; // 10 minutes
	/*
	 * Session
	 */
	public static final long SESSION_TIMEOUT = 600; // 10 minutes
	public static final long SESSION_TIMEOUT_DELTA = 100;
}
