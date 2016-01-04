package io.github.metteo.sse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class EventSourceServlet extends HttpServlet {

	private static final long serialVersionUID = 2085187802346673647L;
	
	protected Set<SsEventSource> openConnections = Collections.synchronizedSet(new HashSet<SsEventSource>());

	//204 No content - stops client from reconnecting
	//301 and 307 redirects are followed by clients
	//5xx should be returned in case of capacity problems
	
	//event structure:
	//id: <id>\n
	//event: <type>\n //defaults to 'message'
	//data: <actual data>\n
	//retry: <number>\n //reconnection time
	//: <comment>\n - useful for proxies killing connection (send empty comment every 15 secs)
	//\n - end of block
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String accept = req.getHeader("Accept");
		
		if("text/event-stream".equals(accept)) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("text/event-stream");
			resp.setHeader("Cache-Control", "no-cache,no-store");
	        resp.addHeader("Connection", "keep-alive");
	        resp.flushBuffer();
			
	        AsyncContext context = req.startAsync();
			@SuppressWarnings("resource")
			EventSourceImpl es = new EventSourceImpl();
			es.open(this, context);
		} else {
			doRegularGet(req, resp);
		}
	}
	
	protected void doRegularGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
	}
	
	protected void doOpen(SsEventSource eventSource) {
		onOpen(eventSource);
	}
	
	/**
	 * Called when client makes a connection
	 * @param eventSource
	 */
	protected void onOpen(SsEventSource eventSource) {
		
	}
	
	protected void doTimeout(SsEventSource eventSource) {
		onTimeout(eventSource);
	}
	
	/**
	 * Called when async servlet request times out. Followed by onClose.
	 * @param eventSource
	 */
	protected void onTimeout(SsEventSource eventSource) {
		
	}
	
	protected void doError(SsEventSource eventSource, Throwable t) {
		onError(eventSource, t);
	}
	
	/**
	 * Called when error occurred
	 * @param eventSource
	 * @param t
	 */
	protected void onError(SsEventSource eventSource, Throwable t) {
		
	}
	
	protected void doClose(SsEventSource eventSource) {
		openConnections.remove(eventSource);
		onClose(eventSource);
	}
	/**
	 * Called when connection was closed by the client or server
	 * @param eventSource
	 */
	protected void onClose(SsEventSource eventSource) {
		
	}
}
