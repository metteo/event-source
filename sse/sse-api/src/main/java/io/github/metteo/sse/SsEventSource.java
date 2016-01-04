package io.github.metteo.sse;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public interface SsEventSource extends Closeable {
	
	String getTag();
	
	void setTag(String tag);
	
	boolean isOpen();
	
	boolean isSecure();
	
	void close() throws IOException;
	
	Map<String,String[]> getPathParameters();
	
	String getQueryString();
	
	URI getRequestUri();
	
	Set<SsEventSource> getOpenConnections();
	
	void sendEvent(SsEvent event);
	
	void sendComment(String comment);
	
	/**
	 * Reconnect from client
	 * @param retryMs
	 */
	void setRetry(int retryMs);
	
	/**
	 * Heartbeat to keep the connection
	 * @param heartbeatMs
	 */
	void setHeartbeat(int heartbeatMs);
	
	/**
	 * Sets Servlet async op timeout, 0 means no timeout
	 * @param timeoutMs
	 */
	void setTimeout(long timeoutMs);
}
