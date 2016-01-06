package io.github.metteo.sse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SsEventSourceServlet extends HttpServlet {

	private static final long serialVersionUID = 2085187802346673647L;
	
	protected SsEventSourceSupport mSupport;
	
	@Override
	public void init() throws ServletException {
		mSupport = new SsEventSourceSupport();
		
		mSupport.setOpenListener(new SsEventSourceListener() {
			
			@Override
			public void onEvent(SsEventSource eventSource) {
				onOpen(eventSource);
			}
		});
		
		mSupport.setTimeoutListener(new SsEventSourceListener() {
			
			@Override
			public void onEvent(SsEventSource eventSource) {
				onTimeout(eventSource);
			}
		});
		
		mSupport.setCloseListener(new SsEventSourceListener() {
			
			@Override
			public void onEvent(SsEventSource eventSource) {
				onClose(eventSource);
			}
		});
		
		mSupport.setErrorListener(new SsEventSourceErrorListener() {
			
			@Override
			public void onEvent(SsEventSource eventSource, Throwable t) {
				onError(eventSource, t);
			}
		});
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		if(mSupport.isEventStream(req)) {
			mSupport.process(req, resp);
		} else {
			doRegularGet(req, resp);
		}
	}
	
	protected void doRegularGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
	}
	
	/**
	 * Called when client makes a connection
	 * @param eventSource
	 */
	protected void onOpen(SsEventSource eventSource) {
		
	}
	
	/**
	 * Called when async servlet request times out. Followed by onClose.
	 * @param eventSource
	 */
	protected void onTimeout(SsEventSource eventSource) {
		
	}
	
	/**
	 * Called when error occurred
	 * @param eventSource
	 * @param t
	 */
	protected void onError(SsEventSource eventSource, Throwable t) {
		
	}

	/**
	 * Called when connection was closed by the client or server
	 * @param eventSource
	 */
	protected void onClose(SsEventSource eventSource) {
		
	}
}
