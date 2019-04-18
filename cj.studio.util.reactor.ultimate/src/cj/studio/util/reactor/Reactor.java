package cj.studio.util.reactor;

public abstract class Reactor implements IReactor {

	private boolean isOpened;

	@Override
	public boolean isOpened() {
		return isOpened;
	}

	@Override
	public final void input(Event e) {
		if (!isOpened)
			return;
		getQueue().addEvent(e);
	}

	protected abstract IEventQueue getQueue();

	public final static IReactor open(Class<? extends Reactor> reactorClazz, int workTreadCount, int capacity,
			IPipelineCombination combination) {
		Reactor reactor;
		try {
			reactor = (Reactor) reactorClazz.newInstance();
			reactor.onopen(workTreadCount, capacity, combination);
			reactor.isOpened = true;
			return reactor;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	protected abstract void onopen(int workTreadCount, int capacity, IPipelineCombination combination);

	@Override
	public final void close() {
		if (!isOpened)
			return;
		onclose();
		isOpened = false;
	}

	protected abstract void onclose();
}
