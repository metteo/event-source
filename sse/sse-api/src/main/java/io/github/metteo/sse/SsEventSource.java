package io.github.metteo.sse;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public interface SsEventSource extends Closeable {
	
	String getTag();
	
	void setTag(String tag);
	
	/**
	 * If not null represents last event id received by the browser
	 * Allows server to re-send data which was not delivered because of
	 * connection was lost. Will be null if server doesn't set event ids
	 */
	String getLastEventId();
	
	boolean isOpen();
	
	boolean isSecure();
	
	void close() throws IOException;
	
	Map<String,String[]> getPathParameters();
	
	String getQueryString();
	
	URI getRequestUri();
	
	Set<SsEventSource> getOpenConnections();
	
	void sendEvent(SsEvent event) throws IOException;
	
	void sendComment(String comment) throws IOException;
	
	/**
	 * Reconnect from client
	 * @param retryMs
	 */
	void setRetry(int retryMs) throws IOException;
	
	/**
	 * Heartbeat to keep the connection
	 * @param heartbeatMs
	 */
	void setHeartbeat(long heartbeatMs);
	
	/**
	 * Sets Servlet async op timeout, 0 means no timeout
	 * If set the connection will be closed after specified time and possibly
	 * re-estabilished by the browser
	 * @param timeoutMs
	 */
	void setTimeout(long timeoutMs);
}
