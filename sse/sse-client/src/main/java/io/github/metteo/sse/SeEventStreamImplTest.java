package io.github.metteo.sse;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

public class SeEventStreamImplTest {

	@Before
	public void setUp() throws Exception {
	}

	private SsEventStream getStream(String data) {
		StringReader reader = new StringReader(data);
		BufferedReader bufferedReader = new BufferedReader(reader);

		return new SsEventStreamImpl(bufferedReader);
	}

	@Test
	public void testReadEmptyStream() throws Exception {
		SsEventStream stream = getStream("");

		SsEvent event = stream.read();

		assertNull(event);
	}

	@Test
	public void testReadEmptyLine() throws Exception {
		SsEventStream stream = getStream("\n");

		SsEvent event = stream.read();

		assertNull(event);
	}

	@Test
	public void testReadComment() throws Exception {
		SsEventStream stream = getStream(":some comment\n" + "\n");

		SsEvent event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadRetry() throws Exception {
		SsEventStream stream = getStream("retry: 5\n" + "\n");

		SsEvent event = stream.read();

		assertEquals(5, (int) event.getRetry());

		event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadCustomEvent() throws Exception {
		SsEventStream stream = getStream(
			"event: remove\n" + 
			"data: objectId\n" + 
			"\n"
		);

		SsEvent event = stream.read();

		assertEquals("remove", event.getEvent());
		assertEquals("objectId", event.getData());
		assertEquals(null, event.getId());
		assertNull(event.getRetry());

		event = stream.read();

		assertNull(event);
	}

	@Test
	public void testReadExample1() throws Exception {
		SsEventStream stream = getStream(
			"data: YHOO\n" + 
			"data: +2\n" + 
			"data: 10\n" + 
			"\n"
		);

		SsEvent event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("YHOO\n+2\n10", event.getData());
		assertEquals(null, event.getId());
		assertNull(event.getRetry());

		event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadExample2() throws Exception {
		SsEventStream stream = getStream(
			": test stream\n" + 
			"\n" + 
			"data: first event\n" + 
			"id: 1\n" + 
			"\n" + 
			"data:second event\n" + 
			"id\n" + 
			"\n" + 
			"data:  third event\n" + 
			"\n"
		);

		SsEvent event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("first event", event.getData());
		assertEquals("1", event.getId());
		assertNull(event.getRetry());
		
		event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("second event", event.getData());
		assertEquals("", event.getId());
		assertNull(event.getRetry());
		
		event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals(" third event", event.getData());
		assertEquals(null, event.getId());
		assertNull(event.getRetry());

		event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadExample3() throws Exception {
		SsEventStream stream = getStream(
			"data\n" + 
			"\n" + 
			"data\n" + 
			"data\n" + 
			"\n" + 
			//ignored because not followed by '\n'
			"data:\n" 
		);

		SsEvent event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("", event.getData());
		assertEquals(null, event.getId());
		assertNull(event.getRetry());
		
		event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("\n", event.getData());
		assertEquals(null, event.getId());
		assertNull(event.getRetry());
		
		event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadExample4() throws Exception {
		SsEventStream stream = getStream(
			"data:test\n" + 
			"\n" + 
			"data: test\n" + 
			"\n"
		);

		SsEvent event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("test", event.getData());
		assertEquals(null, event.getId());
		assertNull(event.getRetry());
		
		event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("test", event.getData());
		assertEquals(null, event.getId());
		assertNull(event.getRetry());
		
		event = stream.read();

		assertNull(event);
	}
}
