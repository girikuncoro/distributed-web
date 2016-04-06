package proj1b.util;

public class Constants {
	/*
	 * System wide
	 */
	public static final int F = 1; // F-resilient website
	public static final int R = F + 1; // number of data bricks to send read requests
	public static final int WQ = F + 1; // number of data bricks that should contain a session
	public static final int W = 2 * F + 1; // number of data bricks to send write requests
	public static final String SESSION_DELIMITER = "#";
	/*
	 * Cookie
	 */
	public static final String COOKIE_NAME = "CS5300PROJ1SESSION";
	public static final long MAX_AGE = 600; // 10 minutes
	/*
	 * Session
	 */
	public static final long SESSION_TIMEOUT = 600; // 10 minutes

}
