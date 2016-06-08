package io.github.metteo.sse.io;

import static org.junit.Assert.*;
import io.github.metteo.sse.SsEvent;
import io.github.metteo.sse.io.SsEventStreamReader;
import io.github.metteo.sse.io.SsEventStreamReaderImpl;
import io.github.metteo.sse.io.SsEventStreamWriter;
import io.github.metteo.sse.io.SsEventStreamWriterImpl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

public class SsEventStreamTest {
	
	@Test
	public void testEndToEnd() throws Exception {
		SsEvent e1 = SsEvent.bldr().id("5").event("create").data("some\nmulti\r\nline\rdata").retry(5000).build();
		SsEvent e2 = SsEvent.bldr().data("simple").build();
		
		StringWriter sWriter = new StringWriter();
		SsEventStreamWriter sseWriter = new SsEventStreamWriterImpl(sWriter);
		
		sseWriter.write(e1);
		sseWriter.write(e2);
		sseWriter.close();
		
		String stream = sWriter.toString();
		
		StringReader sReader = new StringReader(stream);
		BufferedReader bReader = new BufferedReader(sReader);
		SsEventStreamReader sseReader = new SsEventStreamReaderImpl(bReader);
		
		SsEvent e11 = sseReader.read();
		SsEvent e22 = sseReader.read();
		SsEvent e3 = sseReader.read();
		sseReader.close();
		
		assertEquals(SsEvent.bldr().id("5").event("create").data("some\nmulti\nline\ndata").retry(5000).build(), e11);
		assertEquals(SsEvent.bldr().id("5").data("simple").retry(5000).build(), e22);
		assertNull(e3);
	}
}
