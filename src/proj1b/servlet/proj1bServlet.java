package proj1b.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import proj1b.rpc.*;
import proj1b.ssm.*;
import proj1b.util.*;

/**
 * Servlet implementation class proj1bServlet
 */
// @WebServlet("/proj1bServlet")
public class proj1bServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Integer nextSessionID = 0;
	private static RPCClient client = new RPCClient();

	private static final Logger LOGGER = Logger.getLogger("Servlet Logger");

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public proj1bServlet() {
		super();
		Utils.init();
		Constants.init();
		LOGGER.info("Servlet instantialized");
	}

	public proj1bServlet(RPCClient rpcClient) {
		super();
		client = rpcClient;
		Utils.init();
		Constants.init();
		LOGGER.info("Servlet instantialized");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// initialized variables
		Session session = null;
		Boolean logout = false;
		String sessionID = null;
		int versionNumber = 0;
		List<String> svrIDs = null;
		String sourceServerID = null;

		// iterate over cookies and find the related one
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie co : cookies) {
				if (co.getName().equals(Constants.COOKIE_NAME)) {
					String[] cookieValues = co.getValue().split("\\" + Constants.SESSION_DELIMITER);

					sessionID = String.join(Constants.SESSION_DELIMITER, cookieValues[0], cookieValues[1],
							cookieValues[2]);
					versionNumber = Integer.parseInt(cookieValues[3]);
					svrIDs = Arrays.asList(cookieValues).subList(4, cookieValues.length);
					LOGGER.info("Found a cookie named CS5300PROJ1SESSION");
					System.out.println("Cookie value : " + Arrays.asList(cookieValues));
				}
			}
		}

		// create a new session if an arriving client request doesn't have a related cookie
		if (sessionID == null) {
			session = new Session(Utils.getLocalServerID(), Utils.getRebootNum(), nextSessionID++);
			LOGGER.info("Couldn't find a cookie named CS5300PROJ1SESSION. Created a new session "
					+ session.getCookieValue(new ArrayList<String>()));
		} else {
			// send read request to retrieve session state from R out of WQ
			Set<Integer> randomNumbers = generateRandomNumbers(Constants.R, Constants.WQ);
			List<String> svrIDs_R = new ArrayList<String>(Constants.R);
			for (int n : randomNumbers)
				svrIDs_R.add(svrIDs.get(n));

			LOGGER.info("Before session read, sessionID: " + sessionID);
			LOGGER.info("Before session read, versionNumber: " + String.valueOf(versionNumber));
			LOGGER.info("Before session read, svrIDs_R: " + svrIDs_R.toString());
			SessionInServer sessionInServer = client.sessionRead(sessionID, versionNumber, svrIDs_R);

			if (sessionInServer == null) {
				LOGGER.info("Session read returns null.");
				session = new Session(Utils.getLocalServerID(), Utils.getRebootNum(), nextSessionID++);
			} else {
				LOGGER.info("Session read returns not null.");
				session = sessionInServer.getSession();
				sourceServerID = sessionInServer.getServerID();
			}

			if (session == null || session.getExpirationTime() < System.currentTimeMillis()) {
				// removed session or invalid session (timed out)
				session = new Session(Utils.getLocalServerID(), Utils.getRebootNum(), nextSessionID++);
				LOGGER.info("Retrieved session from peers or created a new session for removed/invalid session");
			}
		}

		// replace or refresh
		if (request.getParameter("Refresh") != null) {
			session.refresh();
		} else if (request.getParameter("Replace") != null) {
			String message = request.getParameter("Message");
			session.replace(message);
		} else if (request.getParameter("Logout") != null) {
			logout = true;
			session.logout();
		}

		// update to at least WQ bricks to get target W bricks
		List<String> svrIDs_W = new ArrayList<String>(Constants.W);
		Iterator<String> iter = Utils.getSvrIDs().iterator();
		while (svrIDs_W.size() < Constants.W) {
			if (iter.hasNext())
				svrIDs_W.add(iter.next());
			else
				LOGGER.info("Less than W number of bricks for writing request.");
		}
		System.out.println("Servers for write request: " + svrIDs_W.toString());

		List<String> locations = client.sessionWrite(session, svrIDs_W);

		if (locations == null) {
			LOGGER.info("RPC Client returns Null from sessionWrite()");	
			RequestDispatcher dispatcher = request.getRequestDispatcher("error.jsp");
			dispatcher.forward(request, response);
			return;
		}

		// update cookie
		Cookie cookie = new Cookie(Constants.COOKIE_NAME, session.getCookieValue(locations));
		if (logout)
			cookie.setMaxAge(0);
		else
			cookie.setMaxAge(Constants.MAX_AGE);
		
		// Set cookie domain
		cookie.setDomain(".sz428.bigdata.systems");
		
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		response.addCookie(cookie);
		PrintWriter out = response.getWriter();
		
		JSONObject json = new JSONObject();
		
		json.put("serverID", Utils.getLocalServerID());
		json.put("rebootNum", Utils.getRebootNum());
		json.put("sourceServerID", sourceServerID);
		
		String outSessionID = logout ? "Logged out. No session." : session.getSessionID();
		json.put("sessionID", outSessionID);
		
		int version = logout ? 0 : session.getVersionNumber();
		json.put("sessionVersion", version);
		
		Date date = new Date(System.currentTimeMillis());
		json.put("currentDate", date.toString());
		
		String info = session.getMessage();
		json.put("info", info);
		
		String cookieValue = logout ? "Logged out. No cookie value." : cookie.getValue();
		json.put("cookieID", cookieValue);
		
		String expTime = logout ? "Logged out. No expiration time." : new Date(session.getExpirationTime()).toString();
		json.put("expTime", expTime);
		
		json.put("cookieMetadata", locations);
		
		json.put("cookieDomain", ".sz428.bigdata.systems");
		
		out.print(json.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Generate a set of m distinct numbers out of n numbers
	 * 
	 * @param m
	 *            The number of numbers to be generated
	 * @param n
	 *            The total number of numbers
	 * @return A set of random generated integers
	 */
	private Set<Integer> generateRandomNumbers(int m, int n) {
		Random random = new Random();
		Set<Integer> generated = new HashSet<Integer>();
		while (generated.size() < m)
			generated.add(random.nextInt(n));
		return generated;
	}
}
