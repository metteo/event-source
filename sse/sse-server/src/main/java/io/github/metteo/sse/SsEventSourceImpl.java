package io.github.metteo.sse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class SsEventSourceImpl implements SsEventSource, AsyncListener {

	private String mTag;
	private SsEventSourceSupport mSupport;
	private AsyncContext mContext;

	private String mLastEventId;

	void open(SsEventSourceSupport support, AsyncContext context)
			throws IOException {
		mSupport = support;
		mContext = context;
		mContext.addListener(this);
		
		mLastEventId = getRequest().getHeader("Last-Event-ID");

		mSupport.openConnections.add(this);
		mSupport.doOpen(this);
	}

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
			try {
				close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private PrintWriter getWriter() throws IOException {
		HttpServletResponse resp = getResponse();
		PrintWriter writer = resp.getWriter();
		return writer;
	}

	@Override
	public void sendEvent(SsEvent event) {
		if(!isOpen()) {
			throw new IllegalStateException("Connection is closed");
		}
		
		try {
			PrintWriter writer = getWriter();
			writer.print(event.toText());
			writer.print("\n");
			flush();
		} catch (IOException e) {
			//TODO: what to do with exception
			e.printStackTrace();
		}
	}

	@Override
	public void sendComment(String comment) {
		if(!isOpen()) {
			throw new IllegalStateException("Connection is closed");
		}
		
		try {
			PrintWriter writer = getWriter();
			writer.print(": " + comment + "\n");
			writer.print("\n");
			flush();
		} catch (IOException e) {
			//TODO: what to do with exception
			e.printStackTrace();
		}
	}

	@Override
	public void setRetry(int retry) {
		sendEvent(SsEvent.bldr().retry(retry).build());
	}

	@Override
	public void setHeartbeat(int ms) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void setTimeout(long timeoutMs) {
		mContext.setTimeout(timeoutMs);
	}

	@Override
	public void onComplete(AsyncEvent ae) {
		mSupport.doClose(this);

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
