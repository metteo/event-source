package io.github.metteo.sse;

import java.util.EventListener;

//@FunctionalInterface
public interface SsEventSourceListener extends EventListener {
	void onEvent(SsEventSource eventSource);
}
