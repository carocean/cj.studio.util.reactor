package cj.studio.util.reactor;

import cj.studio.ecm.ServiceCollection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class KeySelector implements IKeySelector, IServiceProvider {
    Map<String, ISelectionKey> keys;
    private IPipelineCombination combination;
    IServiceProvider parent;

    public KeySelector(IPipelineCombination combination, IServiceProvider parent) {
        keys = new ConcurrentHashMap<String, ISelectionKey>();
        this.combination = combination;
        this.parent = parent;
    }

    @Override
    public <T> T getService(String name) {
        int pos=name.indexOf("$.key.");
        if(pos==0){
            String key=name.substring("$.key.".length(),name.length());
            return (T)keys.get(key);
        }
        if (parent != null) {
            return parent.getService(name);
        }
        return null;
    }

    @Override
    public <T> ServiceCollection<T> getServices(Class<T> clazz) {
        if (parent != null) {
            return parent.getServices(clazz);
        }
        return null;
    }

    @Override
    public void dispose() {
        parent = null;
        keys.clear();
        combination = null;
    }

    @Override
    public int keyCount() {
        return keys.size();
    }

    @Override
    public synchronized void removeKey(String key) {
        ISelectionKey k = keys.get(key);
        keys.remove(key);
        combination.demolish(k.pipeline());
    }

    @Override
    public synchronized ISelectionKey select(String key, Object attachment) {
        try {
            ISelectionKey sk = keys.get(key);
            if (sk != null) {
                return sk;
            }
            IPipeline pipeline = new DefaultPipeline(key, this, attachment);
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
