package io.github.metteo.sse;

import java.util.EventListener;

//@FunctionalInterface
public interface SsEventSourceErrorListener extends EventListener {
	void onEvent(SsEventSource eventSource, Throwable t);
}
