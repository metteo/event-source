package io.github.metteo.sse;

import java.io.Closeable;
import java.io.IOException;


public interface SsEventStream extends Closeable {

	/**
	 * Blocks until read enough bytes to return an event
	 * @return
	 */
	SsEvent read() throws IOException;
}
