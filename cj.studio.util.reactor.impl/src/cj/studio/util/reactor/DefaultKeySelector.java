package cj.studio.util.reactor;

import cj.studio.ecm.ServiceCollection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class DefaultKeySelector implements IKeySelector, IServiceProvider {
    Map<String, ISelectionKey> keys;
    private IPipelineCombination combination;
    IServiceProvider parent;

    public DefaultKeySelector(IPipelineCombination combination, IServiceProvider parent) {
        keys = new ConcurrentHashMap<String, ISelectionKey>();
        this.combination = combination;
        this.parent = parent;
    }

    @Override
    public <T> T getService(String name) {
        int pos = name.indexOf("$.key.");
        if (pos == 0) {
            String key = name.substring("$.key.".length(), name.length());
            return (T) keys.get(key);
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
    public synchronized ISelectionKey select(String key, Object attachment) throws CombineException {
        ISelectionKey sk = keys.get(key);
        if (sk != null) {
            return sk;
        }
        Object attachment2 = assignAttachmentFor(key);
        if(attachment2!=null){
            attachment=attachment2;//覆盖方法传入的附件，而采用系统分配的附件
        }
        IPipeline pipeline = new DefaultPipeline(key, this, attachment);
        sk = new SelectionKey(key, pipeline);
        combination.combine(pipeline);
        keys.put(key, sk);
        return sk;
    }

    /**
     * 在新的pipeline创建之前，派生类可为该pipleline的key绑定附件<br>
     *
     *
     * @param key
     * @return
     */
    protected Object assignAttachmentFor(String key) {

        return null;
    }

}
