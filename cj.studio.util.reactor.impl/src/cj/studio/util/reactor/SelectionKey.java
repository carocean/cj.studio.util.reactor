package cj.studio.util.reactor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

 class SelectionKey implements ISelectionKey {
	String key;
	IPipeline pipeline;
	private Queue<Event> events;

	public SelectionKey(String key, IPipeline pipeline) {
		this.key = key;
		this.pipeline = pipeline;
		events=new ConcurrentLinkedQueue <>();
	}

	@Override
	public IPipeline pipeline() {
		return pipeline;
	}

	@Override
	public String key() {
		return key;
	}

	@Override
	public void addEvent(Event e) {
		events.offer(e);
	}

	@Override
	public Event event() {
		return events.poll();
	}

	@Override
	public boolean isEventEmpty() {
		return events.isEmpty();
	}
	
	@Override
	public int eventCount() {
		return events.size();
	}
}
