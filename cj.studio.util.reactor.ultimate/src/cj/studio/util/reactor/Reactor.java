package cj.studio.util.reactor;

import java.util.HashMap;
import java.util.Map;

public abstract class Reactor implements IReactor, IServiceProvider {

	private boolean isOpened;
	IServiceProvider parent;
	Map<String, Object> cached;

	@Override
	public boolean isOpened() {
		return isOpened;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService(String name) {
		if (cached.containsKey(name)) {
			return (T) cached.get(name);
		}
		if (parent != null) {
			T service= parent.getService(name);
			cached.put(name, service);
			return service;
		}
		return null;
	}

	@Override
	public final void input(Event e) {
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
			reactor.cached = new HashMap<String, Object>();
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

	protected abstract void onclose();
}
