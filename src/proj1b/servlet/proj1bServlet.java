package proj1b.servlet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
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

import proj1b.ssm.*;
import proj1b.util.*;

/**
 * Servlet implementation class proj1bServlet
 */
@WebServlet("/proj1bServlet")
public class proj1bServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static SessionManager ssm = SessionManager.getInstance();
	private static Map<Integer, String> instances = new ConcurrentHashMap<Integer, String>();
	private static final Logger LOGGER = Logger.getLogger("Servlet Logger");
       
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public proj1bServlet() {
        super();
        buildInstancesMap();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		//initialized variables
		Session session = null;
		Cookie cookie = null;
		Boolean logout = false;
		
		//iterate over cookies and find the related one
		String sessionKey = null;
		String sessionID = null;
		List<String> bricks = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null){
			for (Cookie co : cookies){
				if (co.getName().equals(Constants.COOKIE_NAME)){
					String[] cookieValues = co.getValue().split("\\" + Constants.SESSION_DELIMITER);
					sessionID = cookieValues[0];
					sessionKey = cookieValues[0] + Constants.SESSION_DELIMITER + cookieValues[1];
					bricks = Arrays.asList(cookieValues).subList(2, cookieValues.length);
				}
			}
		}
		
		//if logout, remove this cookie
		if (request.getParameter("Logout") != null){
			logout = true;
			if (sessionKey != null){
				ssm.removeFromTable(sessionKey);
			}
		}else{
			//else, if no such cookie, or expired (removed or not removed), create a new session
			if (sessionKey == null || !ssm.isInTheTable(sessionKey) 
					|| ssm.isExpired(sessionKey)){
				session = new Session(UUID.randomUUID(),System.currentTimeMillis());
				//public Session(Integer serID, Integer rebootNum, Integer sessID, Set<String> locations)
				sessionKey = session.getSessionID();
			}else{
				session = ssm.getSession(sessionKey);
			}
			
			// then replace or refresh
			if (request.getParameter("Refresh") != null){
				session.refresh();
			}else if (request.getParameter("Replace") != null){
				String message = request.getParameter("Message");
				session.replace(message);
			}
			
			// update existing session and cookie
			ssm.addSession(session);
			cookie = new Cookie(Constants.COOKIE_NAME, session.getCookieValue());
			cookie.setMaxAge((int)Constants.MAX_AGE);
			response.addCookie(cookie);
		}
		
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
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private void buildInstancesMap(){
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
}
