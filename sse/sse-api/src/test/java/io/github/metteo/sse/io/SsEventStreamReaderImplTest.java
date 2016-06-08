package io.github.metteo.sse.io;

import static org.junit.Assert.*;
import io.github.metteo.sse.SsEvent;
import io.github.metteo.sse.io.SsEventStreamReader;
import io.github.metteo.sse.io.SsEventStreamReaderImpl;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

public class SsEventStreamReaderImplTest {

	@Before
	public void setUp() throws Exception {
	}

	private SsEventStreamReader getStream(String data) {
		StringReader reader = new StringReader(data);
		BufferedReader bufferedReader = new BufferedReader(reader);

		return new SsEventStreamReaderImpl(bufferedReader);
	}

	@Test
	public void testReadEmptyStream() throws Exception {
		SsEventStreamReader stream = getStream("");

		SsEvent event = stream.read();

		assertNull(event);
	}

	@Test
	public void testReadEmptyLine() throws Exception {
		SsEventStreamReader stream = getStream("\n");

		SsEvent event = stream.read();

		assertNull(event);
	}

	@Test
	public void testReadComment() throws Exception {
		SsEventStreamReader stream = getStream(":some comment\n" + "\n");

		SsEvent event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadRetry() throws Exception {
		SsEventStreamReader stream = getStream("retry: 5\n" + "\n");

		SsEvent event = stream.read();

		assertNull(event);
		assertEquals(5, stream.getRetry());
	}
	
	@Test
	public void testReadCustomEvent() throws Exception {
		SsEventStreamReader stream = getStream(
			"event: remove\n" + 
			"data: objectId\n" + 
			"\n"
		);

		SsEvent event = stream.read();

		assertEquals("remove", event.getEvent());
		assertEquals("objectId", event.getData());
		assertEquals("", event.getId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());

		event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testFullEvents() throws Exception {
		SsEventStreamReader stream = getStream(
			"id: 3\n" + 
			"event: remove\n" + 
			"data: objectId\n" + 
			"retry: 200\n" + 
			"\n" +
			"id: \n" + 
			"event: add\n" + 
			"data: null\n" + 
			"retry: 500\n" + 
			"\n" +
			"data: :)\n" + 
			"\n"
		);

		SsEvent event = stream.read();

		assertEquals("3", event.getId());
		assertEquals("remove", event.getEvent());
		assertEquals("objectId", event.getData());
		assertEquals(200, (int)event.getRetry());

		event = stream.read();
		
		assertEquals("", event.getId());
		assertEquals("add", event.getEvent());
		assertEquals("null", event.getData());
		assertEquals(500, (int)event.getRetry());
		assertEquals("", stream.getLastEventId());
		assertEquals(500, stream.getRetry());

		event = stream.read();
		
		assertEquals("", event.getId());
		assertEquals("message", event.getEvent());
		assertEquals(":)", event.getData());
		assertEquals(500, (int)event.getRetry());

		event = stream.read();

		assertNull(event);
	}

	@Test
	public void testReadExample1() throws Exception {
		SsEventStreamReader stream = getStream(
			"data: YHOO\n" + 
			"data: +2\n" + 
			"data: 10\n" + 
			"\n"
		);

		SsEvent event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("YHOO\n+2\n10", event.getData());
		assertEquals("", event.getId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());

		event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadExample2() throws Exception {
		SsEventStreamReader stream = getStream(
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
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());
		assertEquals("1", stream.getLastEventId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, stream.getRetry());
		
		event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("second event", event.getData());
		assertEquals("", event.getId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());
		
		event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals(" third event", event.getData());
		assertEquals("", event.getId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());

		event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadExample3() throws Exception {
		SsEventStreamReader stream = getStream(
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
		assertEquals("", event.getId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());
		
		event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("\n", event.getData());
		assertEquals("", event.getId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());
		
		event = stream.read();

		assertNull(event);
	}
	
	@Test
	public void testReadExample4() throws Exception {
		SsEventStreamReader stream = getStream(
			"data:test\n" + 
			"\n" + 
			"data: test\n" + 
			"\n"
		);

		SsEvent event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("test", event.getData());
		assertEquals("", event.getId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());
		
		event = stream.read();

		assertEquals("message", event.getEvent());
		assertEquals("test", event.getData());
		assertEquals("", event.getId());
		assertEquals(SsEventStreamReaderImpl.DEFAULT_RETRY, (int)event.getRetry());
		
		event = stream.read();

		assertNull(event);
	}
}
