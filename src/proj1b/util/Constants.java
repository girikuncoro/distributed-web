package proj1b.util;

public class Constants {
	/*
	 * System wide
	 */

	public static int N;
	public static int F;
	public static int R;
	public static int WQ;
	public static int W;

	public static final String SESSION_DELIMITER = "#";
	public static final String InstancesDir = "/var/tmp/instances.txt";
	public static final String rebootDir = "/var/tmp/rebootNum.txt";
	public static final String localIDDir = "/var/tmp/ami-launch-index";
	public static final String localIPDir = "/var/tmp/local-ipv4";
	public static final String FNDir = "/var/tmp/config.txt";

	/*
	 * Cookie
	 */
	public static final String COOKIE_NAME = "CS5300PROJ1SESSION";
	public static final int MAX_AGE = 600; // 10 minutes

	/*
	 * Session
	 */
	public static final long SESSION_TIMEOUT = 600; // 10 minutes
	public static final long SESSION_CLEANER_INTERVAL = 60; // 1 minute
	public static final long SESSION_TIMEOUT_DELTA = 100; // 100ms
	public static final String DEFAULT_MESSAGE = "Hello, User!";
}
