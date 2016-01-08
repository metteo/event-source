package io.github.metteo.sse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class SsEventSourceSupportTest {

	@Test
	public void testIsEventStream() throws Exception {
		SsEventSourceSupport ess = new SsEventSourceSupport();

		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletRequest req2 = mock(HttpServletRequest.class);
		
		//prepare
		when(req.getHeader("Accept")).thenReturn("text/event-stream");
		when(req2.getHeader("Accept")).thenReturn("text/plain");
		
		//do & verify
		assertTrue(ess.isEventStream(req));
		assertFalse(ess.isEventStream(req2));
	}
	
	@Test
	public void testProcess() throws Exception {
		SsEventSourceSupport ess = new SsEventSourceSupport();
		
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		
		//prepare
		when(req.getHeader("Accept")).thenReturn("text/plain");
		
		//do
		SsEventSource es = ess.process(req, resp);
		
		//verify
		assertEquals(null, es);
		verify(req).getHeader("Accept");
	}
	
	@Test(expected=ServletException.class)
	public void testProcessWithIoEx() throws Exception {
		SsEventSourceSupport ess = new SsEventSourceSupport();
		
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		
		//prepare
		when(req.getHeader("Accept")).thenReturn("text/event-stream");
		doThrow(IOException.class).when(resp).flushBuffer();
		
		//do
		ess.process(req, resp);
	}
}
