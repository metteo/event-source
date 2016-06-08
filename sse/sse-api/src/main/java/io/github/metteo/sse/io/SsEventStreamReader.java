package io.github.metteo.sse.io;

import io.github.metteo.sse.SsEvent;

import java.io.Closeable;
import java.io.IOException;

public interface SsEventStreamReader extends Closeable {

	String getLastEventId();
	
	int getRetry();
	
	/**
	 * Blocks until read enough bytes to return an event
	 * @return
	 */
	SsEvent read() throws IOException;
}
