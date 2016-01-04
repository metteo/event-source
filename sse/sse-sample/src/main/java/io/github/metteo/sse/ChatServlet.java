package io.github.metteo.sse;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/chat", asyncSupported = true)
public class ChatServlet extends SsEventSourceServlet {

	private static final long serialVersionUID = -3386433968220146874L;
	
	private static final Logger sLogger = Logger.getLogger("ChatServlet");
	private static Queue<SsEventSource> sQueue = new ConcurrentLinkedQueue<SsEventSource>();

	@Override
	protected void doRegularGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String msg = req.getParameter("message");
		
		if(msg == null || msg.isEmpty()) {
			return;
		}
		
        for (SsEventSource s : sQueue) {
        	try {
            	s.sendEvent(SsEvent.bldr().data(msg).build());
            	sLogger.log(Level.INFO, "Message: {0}", msg);
            
            } catch (Exception e) {
            	sLogger.log(Level.INFO, "Error while sending", e);
            }
        }
	}
	
	@Override
	protected void onOpen(SsEventSource eventSource) {
		sQueue.add(eventSource);
	}

	@Override
	protected void onClose(SsEventSource eventSource) {
		sQueue.remove(eventSource);
	}
	
	@Override
	protected void onError(SsEventSource eventSource, Throwable t) {
		sQueue.remove(eventSource);
	}
}
