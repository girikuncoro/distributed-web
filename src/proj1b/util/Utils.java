package proj1b.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Utils {
	private static Map<String, String> instancesIDtoIP = new ConcurrentHashMap<String, String>(); //<serverID, serverIP>
	private static Map<String, String> instancesIPtoID = new ConcurrentHashMap<String, String>(); //<serverIP, serverID>
	private static Integer rebootNum;
	private static String localServerID;
	
	private static final Logger LOGGER = Logger.getLogger("Utils Logger");
	
	public static void init() {
		try{
			// Build the instance map
			FileInputStream baseFile = new FileInputStream(Constants.InstancesDir);
			BufferedReader reader = new BufferedReader(new InputStreamReader(baseFile));
			LOGGER.info("Opened instances.txt and ready to read info");
			String line = reader.readLine();
			while (line != null){
				String[] pairs = line.split("\\s+");
				instancesIDtoIP.put(pairs[1], pairs[0]);
				instancesIPtoID.put(pairs[0], pairs[1]);
				line = reader.readLine();
			}
			reader.close();
			
			// Retrieve the reboot number
			baseFile = new FileInputStream(Constants.rebootDir);
			reader = new BufferedReader(new InputStreamReader(baseFile));
			LOGGER.info("Opened rebootNum.txt and ready to read info");
			rebootNum = Integer.parseInt(reader.readLine());
			reader.close();
			
			// Retrieve the local server ID
			baseFile = new FileInputStream(Constants.localIPDir);
			reader = new BufferedReader(new InputStreamReader(baseFile));
			LOGGER.info("Opened local-ipv4 and ready to read info");
			localServerID = reader.readLine();
//			localServerID = instancesIPtoID.get(ip);
			reader.close();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public static String getLocalServerID() {
		return localServerID;
	}
	
	public static int getRebootNum() {
		return rebootNum;
	}
	
	public static String getSvrIPfromID(String svrID) {
		if(instancesIDtoIP.containsKey(svrID)) return instancesIDtoIP.get(svrID);
		LOGGER.warning("No svrID: " + svrID);
		return null;
	}
	
	public static Set<String> getSvrIDs() {
		return instancesIDtoIP.keySet();
	}
}
