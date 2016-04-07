package proj1b.test;

import static org.junit.Assert.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import proj1b.rpc.RPCClient;
import proj1b.servlet.proj1bServlet;
import proj1b.ssm.Session;
import proj1b.util.Constants;

/**
 * Test cases for N=1 and F=0 (same as proj1a)
 */
public class ServletTestN1F0 {
	private proj1bServlet servlet;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private RPCClient client;
	private Session session;
	private List<String> locationData;
	private RequestDispatcher rd;
	
	@Before
	public void setUp() {
		
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		rd = Mockito.mock(RequestDispatcher.class);
		client = Mockito.mock(RPCClient.class);
		
		Mockito.when(request.getRequestDispatcher("content.jsp")).thenReturn(rd);
		Mockito.when(request.getParameter("Replace")).thenReturn("Replace");
		Mockito.when(request.getParameter("Refresh")).thenReturn("Refresh");
		Mockito.when(request.getParameter("Message")).thenReturn("Some message");
		Mockito.when(request.getParameter("Logout")).thenReturn("Logout");
		
		PrintWriter writerIns = null;
		PrintWriter writerReb = null;
		try {
			writerIns = new PrintWriter(Constants.InstancesDir, "UTF-8");
			writerReb = new PrintWriter(Constants.rebootDir, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		writerIns.println("1.1.1.1    2");  // ipAddress-SvrID pairs, assume localIP is 1.1.1.1
		writerIns.println("127.31.19.148   1");
		writerIns.close();
		writerReb.println("3");  // rebooot number
		writerReb.close();
		
		locationData = new ArrayList<String>();
		locationData.add("1.1.1.1");

		Constants.N = 1;
		Constants.R = 1;
		Constants.W = 1;
		Constants.F = 1;
		
		servlet = new proj1bServlet(client);
	}

	@Test
	public void testCreateNewSession() {
		Mockito.when(client.sessionWrite(Mockito.any(Session.class), Mockito.any(List.class))).thenReturn(locationData);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = new Cookie("INVALID_NAME", "FOO");
		Mockito.when(request.getCookies()).thenReturn(cookies);
		
		try {
			servlet.doGet(request, response);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		
		ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
		Mockito.verify(response).addCookie(cookie.capture());
		
		assertEquals("CS5300PROJ1SESSION", cookie.getValue().getName());
		
		// expectedCookieFormat : SessID_version_svrIDs
		// expectedSessionID : svrID_rebootNum_sessNum
		assertEquals("2_3_0_0_2", cookie.getValue().getValue());
		assertEquals(Constants.MAX_AGE, cookie.getValue().getMaxAge());
	}
	
	@Test
	public void testUpdateSession() {
		session = new Session(2, 3, 4, new ArrayList<String>(2));
		Mockito.when(client.sessionRead(session.getSessionID(), session.getVersionNumber(), session.getLocationData())).thenReturn(session);
		
		Cookie[] cookies = new Cookie[1];
		cookies[0] = new Cookie("CS5300PROJ1SESSION", session.getCookieValue());
		System.out.println("Curr cookie value : " + session.getCookieValue());
		Mockito.when(request.getCookies()).thenReturn(cookies);
		
		try {
			servlet.doGet(request, response);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		
		ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
		Mockito.verify(response).addCookie(cookie.capture());
		
		assertEquals("CS5300PROJ1SESSION", cookie.getValue().getName());
		assertEquals("2_3_", cookie.getValue().getValue());
	}
}
