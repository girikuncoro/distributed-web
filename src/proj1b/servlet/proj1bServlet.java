package proj1b.servlet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import proj1b.rpc.*;
import proj1b.ssm.*;
import proj1b.util.*;

/**
 * Servlet implementation class proj1bServlet
 */
//@WebServlet("/proj1bServlet")
public class proj1bServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Integer localServerID; //TODO mapping
	private static Integer rebootNum; 	// TODO read reboot_num from file system
	private static Integer nextSessionID = 0;
//	private static SessionManager ssm = SessionManager.getInstance();
	private static Map<Integer, String> instancesIDtoIP = new ConcurrentHashMap<Integer, String>(); //<serverID, serverIP>
	private static Map<String, Integer> instancesIPtoID = new ConcurrentHashMap<String, Integer>(); //<serverIP, serverID>
	private static final Logger LOGGER = Logger.getLogger("Servlet Logger");
	private static RPCClient client = new RPCClient();
//	private static RPCServer server = new RPCServer();
//	private static Thread serverThread = new Thread(server);

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public proj1bServlet() {
        super();
        buildInstancesMap();
        getRebootNum();
        getLocalServerID();
//    	serverThread.setName("server");
//    	serverThread.start();
        LOGGER.info("Servlet instantialized");
    }
    
    public proj1bServlet(RPCClient rpcClient) {
    	super();
    	client = rpcClient;
    	buildInstancesMap();
        getRebootNum();
        getLocalServerID();
//    	serverThread.setName("server");
//    	serverThread.start();
        LOGGER.info("Servlet instantialized");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		//initialized variables
		Session session = null;
		Cookie cookie = null;
		Boolean logout = false;
		String sessionID = null;
		Integer serverID = null;
		int versionNumber = 0;
		List<String> bricks = null;
		List<String> IPs = null;
		
		//iterate over cookies and find the related one
		Cookie[] cookies = request.getCookies();
		if (cookies != null){
			for (Cookie co : cookies){
				if (co.getName().equals(Constants.COOKIE_NAME)){
					String[] cookieValues = co.getValue().split("\\" + Constants.SESSION_DELIMITER);
					System.out.println(Arrays.asList(cookieValues).toString());
					sessionID = cookieValues[0] + Constants.SESSION_DELIMITER + cookieValues[1] + Constants.SESSION_DELIMITER + cookieValues[2];
					versionNumber = Integer.parseInt(cookieValues[3]);
					bricks = Arrays.asList(cookieValues).subList(4, cookieValues.length);
					LOGGER.info("Found a cookie named CS5300PROJ1SESSION");
					System.out.println("Cookie value : " + Arrays.asList(cookieValues));
				}
			}
		}
		
		// create a new session if an arriving client request doesn't have a related cookie
		if (sessionID == null){
			session = new Session(localServerID, rebootNum, nextSessionID++, new ArrayList<String>());
			LOGGER.info("Couldn't find a cookie named CS5300PROJ1SESSION. Created a new session " + session.getCookieValue());
		}else{
			// convert server ID to server IP
			IPs = new ArrayList<String>(bricks.size());
			for (int i = 0; i < bricks.size(); i++){
				IPs.add(instancesIDtoIP.get(Integer.parseInt(bricks.get(i))));
			}
			
			
			System.out.println("Bricks : " + bricks.toString());
			System.out.println("Instances bricks: " + instancesIDtoIP.toString());
			System.out.println("List of IPs in the map: " + IPs.toString());
			
			// send read request to retrieve session state from WQ servers
			session = client.sessionRead(sessionID, versionNumber, IPs);
			
			// set attribute for source server
			if (session != null){
				request.setAttribute("sourceServerID", instancesIPtoID.get(session.getSourceServerIP()));
			}
			
			if (session == null || session.getExpirationTime() < System.currentTimeMillis()){
				// removed session or invalid session (timed out)
				session = new Session(localServerID, rebootNum, nextSessionID++, new ArrayList<String>());
				LOGGER.info("Retrieved session from peers or created a new session for removed/invalid session");
			}
		}
		
		// replace or refresh
		if (request.getParameter("Refresh") != null){
			session.refresh();
		} else if (request.getParameter("Replace") != null){
			String message = request.getParameter("Message");
			session.replace(message);
		} else if (request.getParameter("Logout") != null){
			logout = true;
			session.logout();
		}
		
		//update to at least WQ bricks
		//get target W bricks
		IPs = new ArrayList<String>(Constants.W);
		Iterator<String> iter = instancesIDtoIP.values().iterator();
		for(int count = 0; count < Constants.W; count++){
			if(iter.hasNext()){
				IPs.add(iter.next());
			}else{
				LOGGER.info("Less than W number of bricks for writing request.");
			}
		}
		System.out.println("IPs for write request " + IPs.toString());
		// send write request and get returned IPs where the session is updated
		List<String> locations = client.sessionWrite(session, IPs);
		// convert IPs back to server IDs
		bricks = new ArrayList<String>();
		if (locations != null){
			System.out.println("Server IPs from session write " + locations.toString());
			for (int i = 0; i < locations.size(); i++){
				serverID = instancesIPtoID.get(locations.get(i));
				if (serverID != null){
					bricks.add(serverID.toString());
				}
//				bricks.add(instancesIPtoID.get(locations.get(i)).toString());
			}
		}else{
			LOGGER.info("RPC Client returns Null from sessionWrite()");
		}
		session.resetLocation(bricks);
		
		// update cookie
		cookie = new Cookie(Constants.COOKIE_NAME, session.getCookieValue());
		if (logout){
			cookie.setMaxAge(0);
		}else{
			cookie.setMaxAge((int)Constants.MAX_AGE);
		}
		response.addCookie(cookie);
		LOGGER.info("add cookie is here");
		// TODO set cookie domain, see instruction P7
		
		// set output information
		request.setAttribute("serverID", localServerID);
		request.setAttribute("rebootNum", rebootNum);
		
		String outSessionID = logout? "Logged out. No session." : session.getSessionID();
		request.setAttribute("sessionID", outSessionID);
		
		int version = logout? 0 : session.getVersionNumber();
		request.setAttribute("sessionVersion", version);
		
		Date date = new Date(System.currentTimeMillis());
		request.setAttribute("currentDate", date.toString());
		
		String info = session.getMessage(); 
		request.setAttribute("info", info);
		
		String cookieValue = logout? "Logged out. No cookie value." : cookie.getValue();
		request.setAttribute("cookieID", cookieValue);
		
		String expTime = logout? "Logged out. No expiration time." : new Date(session.getExpirationTime()).toString();
		request.setAttribute("expTime", expTime);

		request.setAttribute("cookieMetadata", session.getLocations());
//		request.setAttribute("cookieDomain", cookie.getDomain()); // TODO wait for instructions on cookie domain
		
		// request forwarding
		RequestDispatcher dispatcher = request.getRequestDispatcher("content.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}	
	
	public void buildInstancesMap(){
		FileInputStream baseFile;
		BufferedReader reader;
		try{
			baseFile = new FileInputStream(Constants.InstancesDir);
			reader = new BufferedReader(new InputStreamReader(baseFile));
			LOGGER.info("Opened instances.txt and ready to read info");
			String line = reader.readLine();
			while (line != null){
				String[] pairs = line.split("\\s+");
				instancesIDtoIP.put(Integer.parseInt(pairs[1]), pairs[0]);
				instancesIPtoID.put(pairs[0],Integer.parseInt(pairs[1]));
				line = reader.readLine();
			}
			reader.close();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private void getRebootNum(){
		FileInputStream baseFile;
		BufferedReader reader;
		try{
			baseFile = new FileInputStream(Constants.rebootDir);
			reader = new BufferedReader(new InputStreamReader(baseFile));
			LOGGER.info("Opened rebootNum.txt and ready to read info");
			rebootNum = Integer.parseInt(reader.readLine());
			reader.close();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private void getLocalServerID(){
		FileInputStream baseFile;
		BufferedReader reader;
		try{
			baseFile = new FileInputStream(Constants.localIPDir);
			reader = new BufferedReader(new InputStreamReader(baseFile));
			LOGGER.info("Opened local-ipv4 and ready to read info");
			String ip = reader.readLine();
			localServerID = instancesIPtoID.get(ip);
			reader.close();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}
