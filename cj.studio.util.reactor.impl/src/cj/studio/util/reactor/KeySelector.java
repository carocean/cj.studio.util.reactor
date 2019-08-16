package cj.studio.util.reactor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class KeySelector implements IKeySelector {
    Map<String, ISelectionKey> keys;
    private IPipelineCombination combination;
    IServiceProvider parent;

    public KeySelector(IPipelineCombination combination, IServiceProvider parent) {
        keys = new ConcurrentHashMap<String, ISelectionKey>();
        this.combination = combination;
        this.parent = parent;
    }

    @Override
    public int keyCount() {
        return keys.size();
    }

    @Override
    public synchronized void removeKey(String key) {
        ISelectionKey k=keys.get(key);
        keys.remove(key);
    }

    @Override
    public synchronized ISelectionKey select(String key) {
        try {
            ISelectionKey sk = keys.get(key);
            if (sk != null) {
                return sk;
            }
            IPipeline pipeline = new DefaultPipeline(key, parent);
            sk = new SelectionKey(key, pipeline);
            combination.combine(pipeline);
            keys.put(key, sk);
            return sk;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        } finally {
        }

    }

}
