package io.github.metteo.sse.io;

import io.github.metteo.sse.SsEvent;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

public class SsEventStreamWriterImpl implements SsEventStreamWriter {
	
	private static final Pattern sNewLineSplit = Pattern.compile("\\r\\n|[\\n\\r]");

	private Writer mWriter;
	
	public SsEventStreamWriterImpl() {}
	
	public SsEventStreamWriterImpl(Writer writer) {
		mWriter = writer;
	}
	
	@Override
	public void write(SsEvent event) throws IOException {
		ensureWriterPresent();
		
		String id = event.getId();
		if(id != null) {
			mWriter.write("id: " + id + "\n");
		}
		
		String eventType = event.getEvent();
		if(eventType != null && !"message".equals(eventType)) {
			mWriter.write("event: " + eventType + "\n");
		}
		
		String data = event.getData();
		if(data != null) {
			String[] lines = sNewLineSplit.split(data);
			for(String line : lines) {
				mWriter.write("data: " + line + "\n");
			}
		}
		
		Integer retry = event.getRetry();
		if(retry != null) {
			mWriter.write("retry: " + retry + "\n");
		}
		
		mWriter.write("\n"); //dispatch event
	}
	
	private void ensureWriterPresent() {
		if(mWriter == null) {
			throw new IllegalStateException("writer is null");
		}
	}
	
	@Override
	public void setWriter(Writer writer) {
		mWriter = writer;
	}

	@Override
	public void close() throws IOException {
		mWriter.close();
	}

}
