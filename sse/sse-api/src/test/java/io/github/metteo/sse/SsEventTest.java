package io.github.metteo.sse;

import static org.junit.Assert.*;
import static io.github.metteo.sse.SsEvent.*;

import org.junit.Test;

public class SsEventTest {

	@Test
	public void testNewSsEvent() {
		SsEvent e = newSsEvent("id", "event", "data", 123);
		assertEquals("id", e.getId());
		assertEquals("event", e.getEvent());
		assertEquals("data", e.getData());
		assertEquals(123, (int) e.getRetry());
	}

}
