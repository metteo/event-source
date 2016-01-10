package io.github.metteo.sse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SsEventSourceSupport {
	
	private static final Logger sLogger = Logger.getLogger("SsEventSourceSupport");

	protected Set<SsEventSource> openConnections = Collections
			.synchronizedSet(new HashSet<SsEventSource>());
	
	private Timer mTimer = new Timer();
	
	private SsEventSourceListener mOpenListener;
	private SsEventSourceListener mTimeoutListener;
	private SsEventSourceListener mCloseListener;
	private SsEventSourceErrorListener mErrorListener;

	// 204 No content - stops client from reconnecting
	// 301 and 307 redirects are followed by clients
	// 5xx should be returned in case of capacity problems

	// event structure:
	// id: <id>\n
	// event: <type>\n //defaults to 'message'
	// data: <actual data>\n
	// retry: <number>\n //reconnection time
	// : <comment>\n - useful for proxies killing connection (send empty comment
	// every 15 secs)
	// \n - end of block

	// header Last-Event-ID is sent on reconnect - it allows to catch up after
	// connection was lost

	public SsEventSource process(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException {
		try {
			return process0(req, resp);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	private SsEventSource process0(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		
		boolean eventStream = isEventStream(req);

		if (eventStream) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("text/event-stream");
			resp.setHeader("Cache-Control", "no-cache,no-store");
			resp.addHeader("Connection", "keep-alive");
			resp.flushBuffer();

			AsyncContext context = req.startAsync();
			SsEventSourceImpl es = new SsEventSourceImpl();
			es.open(this, context);
			
			return es;
		}
		
		return null;
	}

	public boolean isEventStream(HttpServletRequest req) {
		String accept = req.getHeader("Accept");
		boolean eventStream = "text/event-stream".equals(accept);
		return eventStream;
	}
	
	protected Heartbeat setHeartbeat(SsEventSource es, long heartbeatMs) {
		Heartbeat hb = new Heartbeat(es);
		mTimer.scheduleAtFixedRate(hb, heartbeatMs, heartbeatMs);
		return hb;
	}

	protected void doOpen(SsEventSource eventSource) {
		if(mOpenListener != null) mOpenListener.onEvent(eventSource);
	}
	
	public void setOpenListener(SsEventSourceListener l) {
		mOpenListener = l;
	}

	protected void doTimeout(SsEventSource eventSource) {
		if(mTimeoutListener != null) mTimeoutListener.onEvent(eventSource);
	}
	
	public void setTimeoutListener(SsEventSourceListener l) {
		mTimeoutListener = l;
	}

	protected void doError(SsEventSource eventSource, Throwable t) {
		if(mErrorListener != null) mErrorListener.onEvent(eventSource, t);
	}
	
	public void setErrorListener(SsEventSourceErrorListener l) {
		mErrorListener = l;
	}

	protected void doClose(SsEventSource eventSource) {
		openConnections.remove(eventSource);
		if(mCloseListener != null) mCloseListener.onEvent(eventSource);
	}
	
	public void setCloseListener(SsEventSourceListener l) {
		mCloseListener = l;
	}
	
	static class Heartbeat extends TimerTask {
		
		private SsEventSource mEventSource;
		
		public Heartbeat(SsEventSource es) {
			mEventSource = es;
		}
		
		@Override
		public void run() {
			if(mEventSource.isOpen()) {
				try {
					mEventSource.sendComment("hb");
				} catch (IOException e) {
					//probably closed connection
					sLogger.log(Level.FINEST, "Unable to sendComment", e);
				}
			}
		}
	}
}
