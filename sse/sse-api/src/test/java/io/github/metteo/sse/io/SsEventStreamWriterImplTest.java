package io.github.metteo.sse.io;

import static org.junit.Assert.*;
import io.github.metteo.sse.SsEvent;
import io.github.metteo.sse.io.SsEventStreamWriterImpl;

import java.io.StringWriter;

import org.junit.Test;

public class SsEventStreamWriterImplTest {

	@Test
	public void testWrite() throws Exception {
		StringWriter writer = new StringWriter();
		SsEventStreamWriterImpl sseWriter = new SsEventStreamWriterImpl(writer);
		
		SsEvent e1 = SsEvent.bldr().id("5").event("create").data("some\nmulti\r\nline\rdata").retry(5000).build();
		SsEvent e2 = SsEvent.bldr().data("simple").build();
		
		sseWriter.write(e1);
		sseWriter.write(e2);
		sseWriter.close();
		
		String output = writer.toString();

		String expected = "id: 5\n" + 
				"event: create\n" + 
				"data: some\n" + 
				"data: multi\n" + 
				"data: line\n" + 
				"data: data\n" + 
				"retry: 5000\n" + 
				"\n" + 
				"data: simple\n" + 
				"\n";
		
		assertEquals(expected, output);
	}
}
