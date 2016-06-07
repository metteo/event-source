package io.github.metteo.sse;

import java.io.BufferedReader;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class SsEventStreamImpl implements SsEventStream {

	private BufferedReader mReader;
	
	private String mIdBuffer;
	private String mEventBuffer;
	private StringBuilder mDataBuffer;
	private Integer mRetryBuffer;

	public SsEventStreamImpl(BufferedReader reader) {
		mReader = reader;

		mDataBuffer = new StringBuilder();

		initBuffers();
	}

	private void initBuffers() {
		mIdBuffer = null;
		mEventBuffer = null;
		mDataBuffer.setLength(0);
		mRetryBuffer = null;
	}

	@Override
	public SsEvent read() throws IOException {
		while (true) {
			String line = mReader.readLine();

			if (line == null) { // end of stream
				// return null even though there might be sth in buffer
				return null;
			}

			if (line.isEmpty()) { // event separator, return event
				SsEvent.Builder bldr = null;
				
				int dataLength = mDataBuffer.length();
				if(dataLength > 0) {
					bldr = SsEvent.bldr();
					
					if(mDataBuffer.charAt(dataLength - 1) == '\n') {
						mDataBuffer.deleteCharAt(dataLength - 1);
					}
					
					bldr.data(mDataBuffer.toString());
					
					if(mEventBuffer != null) {
						bldr.event(mEventBuffer);
					}
				}
				
				if(mIdBuffer != null) {
					if(bldr == null) { bldr = SsEvent.bldr(); }
					
					bldr.id(mIdBuffer);
				}
				
				if(mRetryBuffer != null) {
					if(bldr == null) { bldr = SsEvent.bldr(); }
					
					bldr.retry(mRetryBuffer);
				}
				
				initBuffers();
				
				if(bldr != null) {
					return bldr.build();
				}
				
				continue;
			}

			if (line.startsWith(":")) { // comment
				continue;
			}

			int semicolonIdx = line.indexOf(':');
			if (semicolonIdx > 0) {
				String field = line.substring(0, semicolonIdx);

				// ignore possible space after semicolon
				if (semicolonIdx < line.length() - 1 && line.charAt(semicolonIdx + 1) == ' ') {
					semicolonIdx++;
				}
				String value = line.substring(semicolonIdx + 1, line.length());

				processField(field, value);
			}

			processField(line, "");
		}
	}

	private void processField(String field, String value) {
		switch (field) {
		case "event":
			mEventBuffer = value;
			break;
		case "data":
			mDataBuffer.append(value).append("\n");
			break;
		case "id":
			mIdBuffer = value;
			break;
		case "retry":
			try {
				mRetryBuffer = Integer.valueOf(value);
			} catch (NumberFormatException e) {
				// noop, ignore the field
			}
			break;
		default:
			// ignore the field
			break;
		}
	}

	@Override
	public void close() throws IOException {
		mReader.close();
	}

}
