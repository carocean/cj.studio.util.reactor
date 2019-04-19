package cj.studio.util.reactor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

 class KeySelector implements IKeySelector {
	Map<String, ISelectionKey> keys;
	private IPipelineCombination combination;
	IEventQueue queue;
	IServiceProvider parent;
	public KeySelector(IEventQueue queue, IPipelineCombination combination,IServiceProvider parent) {
		keys = new ConcurrentHashMap<String, ISelectionKey>();
		this.combination = combination;
		this.queue = queue;
		this.parent=parent;
	}

	@Override
	public int keyCount() {
		return keys.size();
	}
	@Override
	public void removeKey(String key) {
		keys.remove(key);
	}
	@Override
	public synchronized ISelectionKey select() {
		Event e = null;
		try {
			e = queue.selectOne();
			String key = e.getKey();
			ISelectionKey sk = keys.get(key);
			if (sk == null) {
				IPipeline pipeline = new DefaultPipeline(key,parent);
				sk = new SelectionKey(key, pipeline);
				sk.addEvent(e);
				combination.combine(pipeline);
				keys.put(key, sk);
			} else {
				sk.addEvent(e);
			}
			return sk;
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		} finally {
		}

	}

}
