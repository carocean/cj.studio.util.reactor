package cj.studio.util.reactor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

 class DefaultEventQueue implements IEventQueue {
	private BlockingQueue<Event> queue;

	public DefaultEventQueue(int capacity) {
		this.queue = new LinkedBlockingQueue<Event>(capacity);
	}

	@Override
	public synchronized List<Event> select() {
		List<Event> list = new ArrayList<Event>();
		while (!queue.isEmpty()) {
			try {
				list.add(queue.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		return list;
	}
	@Override
	public Event selectOne() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public synchronized void addEvent(Event e) {
		try {
			queue.put(e);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	 @Override
	 public int count() {
		 return queue.size();
	 }
 }
