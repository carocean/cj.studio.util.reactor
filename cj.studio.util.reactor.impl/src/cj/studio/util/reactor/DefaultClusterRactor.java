package cj.studio.util.reactor;

import cj.studio.ecm.EcmException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 负载均衡响应器
 */
public final class DefaultClusterRactor extends Reactor {


    IEventQueue queue;
    private ExecutorService exe;
    IKeySelector selector;
    IRemoteServiceNodeRouter remoteServiceNodeRouter;

    @Override
    protected IEventQueue getQueue() {
        return queue;
    }

    @Override
    public <T> T getService(String name) {
        if ("$.remote.nodes.router".equals(name)) {
            return (T) remoteServiceNodeRouter;
        }
        return super.getService(name);
    }

    @Override
    protected void onopen(int workTreadCount, int capacity, IPipelineCombination combination) {
        remoteServiceNodeRouter = new DefaultRemoteServiceNodeRouter();
        this.queue = new DefaultEventQueue(capacity);
        this.exe = Executors.newFixedThreadPool(workTreadCount);
        selector = new KeySelector(combination, this);
        for (int i = 0; i < workTreadCount; i++) {
            IEventLooper looper = new ClusterEventLooper(selector, queue,remoteServiceNodeRouter);
            exe.submit(looper);
        }
    }

    @Override
    public void input(Event e) {
        if (!remoteServiceNodeRouter.isInit()) {
            throw new EcmException("服务：$.remote.nodes.manager 未初始化");
        }
        super.input(e);
    }

    @Override
    protected void onclose() {
        exe.shutdownNow();
        exe = null;
        queue = null;
        selector.dispose();
        selector = null;
        remoteServiceNodeRouter.dispose();
        remoteServiceNodeRouter = null;
    }

    @Override
    public void removeKey(String key) {
        selector.removeKey(key);
    }

    @Override
    public int pipelineCount() {
        return selector.keyCount();
    }

}
