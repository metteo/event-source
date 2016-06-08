package io.github.metteo.sse;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * Represents a single event used for pushing data to client
 *
 */
@AutoValue
public abstract class SsEvent implements Serializable {

	private static final long serialVersionUID = 5642453586141359781L;

	@AutoValue.Builder
	public abstract static class Builder {
		public abstract Builder id(String id);

		public abstract Builder event(String event);

		public abstract Builder data(String data);

		public abstract Builder retry(Integer retryMs);

		abstract SsEvent autoBuild();

		public SsEvent build() {
			SsEvent sse = autoBuild();
			
			String event = sse.getEvent();
			if (event != null && event.isEmpty()) {
				throw new IllegalStateException("Event can't be empty");
			}
			
			Integer retry = sse.getRetry();
			if (retry != null && retry <= 0) {
				throw new IllegalStateException("Retry must be >0 ms");
			}

			return sse;
		}
	}

	public static Builder bldr() {
		// default event type is 'message'
		return new AutoValue_SsEvent.Builder().event("message");
	}

	public static SsEvent newSsEvent(String id, String event, String data,
			Integer retryMs) {
		return bldr().id(id).event(event).data(data).retry(retryMs).build();
	}

	SsEvent() {
		//recommended in auto value docs
	}
	
	/**
	 * Id of the event. Available as 'lastEventId' on the client
	 * @return
	 */
	@Nullable
	public abstract String getId();

	/**
	 * Type of the event. Default is 'message'
	 * @return
	 */
	@Nullable
	public abstract String getEvent();

	/**
	 * Payload of the event
	 * @return
	 */
	@Nullable
	public abstract String getData();

	/**
	 * Delay in 'ms' before retrying connection after it was closed / broken
	 * @return
	 */
	@Nullable
	public abstract Integer getRetry();
	
	/**
	 * Builder instance initialized with values from this event
	 * @return
	 */
	abstract Builder toBuilder();
	
	/**
	 * Exact copy of this instance. To customize it, use {@link #toBuilder()}
	 * @return
	 */
	public SsEvent copy() {
		return toBuilder().build();
	}
}
