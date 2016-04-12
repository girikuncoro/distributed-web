package proj1b.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Logger;

public class Constants {
	/*
	 * System wide
	 */
	
//	public static int N = 1; // number of total servers
//	public static int F = 1; // F-resilient website
//	public static int R = 1; // number of data bricks to send read requests
//	public static int WQ = 1; // number of data bricks that should contain a session
//	public static int W = 1; // number of data bricks to send write requests
	
//	public static int N = 3; // number of total servers
//	public static int F = 1; // F-resilient website
//	public static int R = F + 1; // number of data bricks to send read requests
//	public static int WQ = F + 1; // number of data bricks that should contain a session
//	public static int W = 2 * F + 1; // number of data bricks to send write requests
	
	public static int N;
	public static int F;
	public static int R;
	public static int WQ;
	public static int W;
	private static final Logger LOGGER = Logger.getLogger("Constants Logger");
	
	public static void init(){
		// Read in the value of F and N, then initialize the values of R, WQ, W
		FileInputStream baseFile;
		try {
			baseFile = new FileInputStream(Constants.FNDir);
			BufferedReader reader = new BufferedReader(new InputStreamReader(baseFile));
			LOGGER.info("Opened config.txt and ready to read info");
			String line = reader.readLine();
			while (line != null){
				String[] pairs = line.split("\\s+");
				if (pairs[0].equals("N")){
					N = Integer.parseInt(pairs[1]);
				}else if (pairs[0].equals("F")){
					F = Integer.parseInt(pairs[1]);
				}
				line = reader.readLine();
			}
			reader.close();
			
			R = F + 1;
			WQ = F + 1;
			W = 2 * F + 1;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
//	public static final int N = 3; // number of total servers
//	public static final int F = 1; // F-resilient website
//	public static final int R = F + 1; // number of data bricks to send read requests
//	public static final int WQ = F + 1; // number of data bricks that should contain a session
//	public static final int W = 2 * F + 1; // number of data bricks to send write requests
	
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
