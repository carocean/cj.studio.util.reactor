package cj.studio.util.reactor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多路复用响应器，该响应器一般用于处理具体业务逻辑
 */
public final class DefaultReactor extends Reactor {
	private IEventQueue queue;
	private ExecutorService exe;
	IKeySelector selector;

	@Override
	protected IEventQueue getQueue() {
		return queue;
	}

	@Override
	protected void onopen(int workTreadCount, int capacity, IPipelineCombination assembly) {
		this.queue = new DefaultEventQueue(capacity);
		this.exe = Executors.newFixedThreadPool(workTreadCount);
		selector = new KeySelector(assembly,this);
		for (int i = 0; i < workTreadCount; i++) {
			IEventLooper looper = new EventLooper(selector,queue);
			exe.submit(looper);
		}
	}
	@Override
	public void removeKey(String key) {
		selector.removeKey(key);
	}
	@Override
	public int pipelineCount() {
		return selector.keyCount();
	}
	@Override
	protected void onclose() {
		exe.shutdownNow();
		exe = null;
		queue = null;
		selector=null;
	}

}
