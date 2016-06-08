package io.github.metteo.sse.io;

import io.github.metteo.sse.SsEvent;
import io.github.metteo.sse.SsEvent.Builder;

import java.io.BufferedReader;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * 
 * @author metteo
 * @see <a href="https://html.spec.whatwg.org/multipage/comms.html#event-stream-interpretation">Spec</a>
 */
@NotThreadSafe
public class SsEventStreamReaderImpl implements SsEventStreamReader {

	public static final int DEFAULT_RETRY = 2_000; //ms
	
	private BufferedReader mReader;
	
	private String mLastEventId;
	private int mCurrentRetry;
	
	private String mEventBuffer;
	private StringBuilder mDataBuffer;

	public SsEventStreamReaderImpl(BufferedReader reader) {
		mReader = reader;

		mLastEventId = "";
		mCurrentRetry = DEFAULT_RETRY;
		
		mDataBuffer = new StringBuilder();

		initBuffers();
	}

	private void initBuffers() {
		mEventBuffer = "";
		mDataBuffer.setLength(0);
	}

	@Override
	public SsEvent read() throws IOException {
		while (true) {
			String line = mReader.readLine();

			if (line == null) { // end of stream
				// return null even though there might be sth in buffer
				return null;
			}

			if (line.isEmpty()) { // event separator, dispatch event
				SsEvent event = dispatchEvent();
				
				if(event != null) {
					return event;
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
			} else {
				processField(line, "");
			}
		}
	}

	private SsEvent dispatchEvent() {
		 SsEvent event = null;
		
		int dataLength = mDataBuffer.length();
		if(dataLength > 0) {
			SsEvent.Builder bldr = SsEvent.bldr();
			
			bldr.id(mLastEventId);
			
			if(!mEventBuffer.isEmpty()) {
				bldr.event(mEventBuffer);
			}
			
			//remove last new line if present
			if(mDataBuffer.charAt(dataLength - 1) == '\n') {
				mDataBuffer.deleteCharAt(dataLength - 1);
			}
			
			bldr.data(mDataBuffer.toString());
			
			bldr.retry(mCurrentRetry);
			
			event = bldr.build();
		}
		
		initBuffers();
		
		return event;
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
			mLastEventId = value;
			break;
		case "retry":
			try {
				Integer retry = Integer.valueOf(value);
				if (retry > 0) { mCurrentRetry = retry; }
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
	public String getLastEventId() {
		return mLastEventId;
	}
	
	@Override
	public int getRetry() {
		return mCurrentRetry;
	}

	@Override
	public void close() throws IOException {
		mReader.close();
	}

}
