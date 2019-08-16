package cj.studio.util.reactor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class SelectionKey implements ISelectionKey {
    String key;
    IPipeline pipeline;

    public SelectionKey(String key, IPipeline pipeline) {
        this.key = key;
        this.pipeline = pipeline;
    }

    @Override
    public IPipeline pipeline() {
        return pipeline;
    }

    @Override
    public String key() {
        return key;
    }


}
