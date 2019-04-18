package cj.studio.util.reactor;

 class EventLooper implements IEventLooper {
	IKeySelector selector;

	public EventLooper(IKeySelector selector) {
		this.selector = selector;
	}

	@Override
	public Event call() throws Exception {
		while (!Thread.interrupted()) {
			ISelectionKey key = selector.select();
			synchronized (key.pipeline().key()) {//让同一个管道的事件按序执行
				while (!key.isEventEmpty()) {
					key.pipeline().input(key.event());
				}
			}
		}
		return null;
	}

}
