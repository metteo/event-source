package io.github.metteo.sse.io;

import io.github.metteo.sse.SsEvent;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

public interface SsEventStreamWriter extends Closeable {

	public void write(SsEvent event) throws IOException;
	
	/**
	 * Replace writer associated with this instance
	 * @param writer
	 */
	public void setWriter(Writer writer);
}
