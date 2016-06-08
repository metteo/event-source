package io.github.metteo.sse;

import io.github.metteo.sse.SsEventSourceSupport.Heartbeat;
import io.github.metteo.sse.io.SsEventStreamWriter;
import io.github.metteo.sse.io.SsEventStreamWriterImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class SsEventSourceImpl implements SsEventSource, AsyncListener {
	
	private static final Logger sLogger = Logger.getLogger("SsEventSourceSupport");

	private String mTag;
	private SsEventSourceSupport mSupport;
	private SsEventStreamWriter mWriter;
	private AsyncContext mContext;
	private Heartbeat mHeartbeat;

	private String mLastEventId;

	void open(SsEventSourceSupport support, AsyncContext context)
			throws IOException {
		mSupport = support;
		mContext = context;
		mContext.addListener(this);
		
		mLastEventId = getRequest().getHeader("Last-Event-ID");

		mSupport.openConnections.add(this);
		mSupport.doOpen(this);
		
		mWriter = new SsEventStreamWriterImpl();
	}

	//instead of tagging make use of HttpSession since sse are already stateful
	@Override
	public String getTag() {
		return mTag;
	}

	@Override
	public void setTag(String tag) {
		mTag = tag;
	}
	
	@Override
	public String getLastEventId() {
		return mLastEventId;
	}

	@Override
	public boolean isOpen() {
		if(mContext == null) {
			return false;
		}
		
		if(mContext.getResponse() == null) {
			//browser disconnected, but we didn't get onComplete
			onComplete(null);
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isSecure() {
		return mContext.getRequest().isSecure();
	}

	@Override
	public void close() throws IOException {
		mContext.complete();
	}

	@Override
	public Map<String, String[]> getPathParameters() {
		return mContext.getRequest().getParameterMap();
	}

	@Override
	public String getQueryString() {
		return getRequest().getQueryString();
	}

	@Override
	public URI getRequestUri() {
		return URI.create(getRequest().getRequestURI());
	}

	@Override
	public Set<SsEventSource> getOpenConnections() {
		return Collections.unmodifiableSet(mSupport.openConnections);
	}

	private HttpServletRequest getRequest() {
		return (HttpServletRequest) mContext.getRequest();
	}

	private HttpServletResponse getResponse() {
		return (HttpServletResponse) mContext.getResponse();
	}

	private void flush() {
		try {
			getResponse().flushBuffer();
		} catch (IOException e) {
			// probably closed connection
			sLogger.log(Level.FINEST, "Unable to flush", e);
			onComplete(null);
		}
	}
	
	private PrintWriter getWriter() throws IOException {
		HttpServletResponse resp = getResponse();
		PrintWriter writer = resp.getWriter();
		return writer;
	}

	//TODO: implement queue of events, and this just adds event to that queue?
	@Override
	public void sendEvent(SsEvent event) throws IOException {
		if(!isOpen()) {
			throw new IllegalStateException("Connection is closed");
		}

		PrintWriter writer = getWriter();
		mWriter.setWriter(writer);
		mWriter.write(event);
		mWriter.setWriter(null); //do not hold on to the reference
		flush();
	}

	@Override
	public void sendComment(String comment) throws IOException {
		if(!isOpen()) {
			throw new IllegalStateException("Connection is closed");
		}

		PrintWriter writer = getWriter();
		writer.print(": " + comment + "\n");
		writer.print("\n");
		flush();
	}

	@Override
	public void setRetry(int retry) throws IOException {
		sendEvent(SsEvent.bldr().retry(retry).build());
	}

	@Override
	public void setHeartbeat(long ms) {
		if(mHeartbeat != null) {
			mHeartbeat.cancel();
			mHeartbeat = null;
		}
		
		if(ms > 0) {
			mHeartbeat = mSupport.setHeartbeat(this, ms);
		}
	}
	
	@Override
	public void setTimeout(long timeoutMs) {
		mContext.setTimeout(timeoutMs);
	}

	@Override
	public void onComplete(AsyncEvent ae) {
		mSupport.doClose(this);
		if(mHeartbeat != null) {
			mHeartbeat.cancel();
			mHeartbeat = null;
		}

		mContext = null;
		mSupport = null;
	}

	@Override
	public void onTimeout(AsyncEvent ae) throws IOException {
		mSupport.doTimeout(this);
	}

	@Override
	public void onError(AsyncEvent ae) throws IOException {
		mSupport.doError(this, ae.getThrowable());
	}

	@Override
	public void onStartAsync(AsyncEvent event) throws IOException {
	}
}
