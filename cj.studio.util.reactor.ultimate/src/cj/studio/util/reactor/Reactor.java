package cj.studio.util.reactor;

import java.util.HashMap;
import java.util.Map;

import cj.studio.ecm.ServiceCollection;

public abstract class Reactor implements IReactor, IServiceProvider {

	private boolean isOpened;
	IServiceProvider parent;
	Map<Object, Object> cached;

	@Override
	public boolean isOpened() {
		return isOpened;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService(String name) {
		synchronized (cached) {
			if (cached.containsKey(name)) {
				return (T) cached.get(name);
			}
			if (parent != null) {
				T service = parent.getService(name);
				cached.put(name, service);
				return service;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ServiceCollection<T> getServices(Class<T> clazz) {
		synchronized (cached) {
			if (cached.containsKey(clazz)) {
				return (ServiceCollection<T>) cached.get(clazz);
			}

			if (parent != null) {
				ServiceCollection<T> col = parent.getServices(clazz);
				cached.put(clazz, col);
				return col;
			}
		}

		return null;
	}

	@Override
	public  void input(Event e) {
		if (!isOpened)
			return;
		getQueue().addEvent(e);
	}

	protected abstract IEventQueue getQueue();

	public final static IReactor open(Class<? extends Reactor> reactorClazz, int workTreadCount, int capacity,
			IPipelineCombination combination, IServiceProvider parent) {
		Reactor reactor;
		try {
			reactor = (Reactor) reactorClazz.newInstance();
			reactor.cached = new HashMap<>();
			reactor.onopen(workTreadCount, capacity, combination);
			reactor.isOpened = true;
			reactor.parent = parent;
			return reactor;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	public final static IReactor open(Class<? extends Reactor> reactorClazz, int workTreadCount, int capacity,
			IPipelineCombination combination) {
		return open(reactorClazz, workTreadCount, capacity, combination, null);
	}

	protected abstract void onopen(int workTreadCount, int capacity, IPipelineCombination combination);

	@Override
	public final void close() {
		if (!isOpened)
			return;
		onclose();
		cached.clear();
		isOpened = false;
	}

	@Override
	public final int queueCount() {
		IEventQueue queue=getQueue();
		return queue==null?0:queue.count();
	}

	protected abstract void onclose();
}
