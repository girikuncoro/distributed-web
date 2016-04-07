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

import proj1b.rpc.RPCClient;
import proj1b.ssm.*;
import proj1b.util.*;

/**
 * Servlet implementation class proj1bServlet
 */
@WebServlet("/proj1bServlet")
public class proj1bServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Integer localServerID = 1; //TODO mapping
	private static Integer rebootNum; 	// TODO read reboot_num from file system
	private static Integer nextSessionID = 0;
//	private static SessionManager ssm = SessionManager.getInstance();
	private static Map<Integer, String> instances = new ConcurrentHashMap<Integer, String>();
	private static final Logger LOGGER = Logger.getLogger("Servlet Logger");
	private static RPCClient client = new RPCClient();
//	RequestDispatcher dispatcher;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public proj1bServlet() {
        super();
        buildInstancesMap();
        getRebootNum();
        LOGGER.info("Servlet instantialized");
    }
    
    public proj1bServlet(RPCClient rpcClient) {
    	super();
    	client = rpcClient;
    	buildInstancesMap();
        getRebootNum();
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
		
		//iterate over cookies and find the related one
		String sessionID = null;
		int versionNumber = 0;
		List<String> bricks = null;
		List<String> IPs = null;
		
		Cookie[] cookies = request.getCookies();
		if (cookies != null){
			for (Cookie co : cookies){
				if (co.getName().equals(Constants.COOKIE_NAME)){
					String[] cookieValues = co.getValue().split("\\" + Constants.SESSION_DELIMITER);
					sessionID = cookieValues[0];
					versionNumber = Integer.parseInt(cookieValues[1]);
					bricks = Arrays.asList(cookieValues).subList(2, cookieValues.length);
					LOGGER.info("Found a cookie named CS5300PROJ1SESSION");
				}
			}
		}
		
		// create a new session if an arriving client request doesn't have a related cookie
		if (sessionID == null){
			session = new Session(localServerID, rebootNum, nextSessionID++, new ArrayList<String>());
			LOGGER.info("Couldn't find a cookie named CS5300PROJ1SESSION. Created a new session");
		}else{
			// convert server ID to server IP
			IPs = new ArrayList<String>(bricks.size());
			for (int i = 0; i < bricks.size(); i++){
				IPs.add(instances.get(bricks.get(i)));
			}
			
			// send read request to retrieve session state from WQ servers
			// TODO sessionRead call will return null if all servers are timed out. Thus, even though
			session = client.sessionRead(sessionID, versionNumber, IPs);
			if (session == null || session.getExpirationTime() < System.currentTimeMillis()){
				// removed session or invalid session (timed out)
				session = new Session(localServerID, rebootNum, nextSessionID++, new ArrayList<String>());
			}
			LOGGER.info("Retrieved session from peers or created a new session for removed/invalid session");
				
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
		}
		
		//update to at least WQ bricks
		//get target W bricks
		IPs = new ArrayList<String>(Constants.W);
		Iterator<String> iter = instances.values().iterator();
		for(int count = 0; count <= Constants.W; count++){
			if(iter.hasNext()){
				IPs.add(iter.next());
			}else{
				LOGGER.info("Less than W number of bricks for writing request.");
			}
		}
		// send write request
		List<String> locations = client.sessionWrite(session, IPs);
		if (locations == null){
			session.addLocation(instances.get(localServerID));
			LOGGER.info("Running with only one server instance.");
		}else{
			session.resetLocation(locations);
			LOGGER.info("Updated session location data");
		}
		
		// update cookie
		cookie = new Cookie(Constants.COOKIE_NAME, session.getCookieValue());
		cookie.setMaxAge((int)Constants.MAX_AGE);
		response.addCookie(cookie);
		LOGGER.info("addcookie is here");
		// TODO set cookie domain, see instruction P7
		
		// set output information
		String info = logout ? "You have logged out. So long!" : session.getMessage(); 
		String cookieValue = logout? "Logged out. No cookie value." : cookie.getValue();
		String expTime = logout? "Logged out. No expiration time." : new Date(session.getExpirationTime()).toString();
		int version = logout? 0 : session.getVersionNumber();
		String outSessionID = logout? "Logged out. No session." : session.getSessionID();
		Date date = new Date(System.currentTimeMillis());

		request.setAttribute("sessionID", outSessionID);
		request.setAttribute("currentDate", date.toString());
		request.setAttribute("info", info);
		request.setAttribute("cookieID", cookieValue);
		request.setAttribute("expTime", expTime);
		request.setAttribute("sessionVersion", version);
		
		// request forwarding
		RequestDispatcher dispatcher = request.getRequestDispatcher("content.jsp");
		dispatcher.forward(request, response);
//		dispatcher = request.getRequestDispatcher("content.jsp");
//		dispatcher.forward(request, response);
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
				instances.put(Integer.parseInt(pairs[1]), pairs[0]);
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
}
