package cj.studio.util.reactor;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

class ClusterEventLooper implements IEventLooper {
    IKeySelector selector;
    IEventQueue queue;
    IRemoteServiceNodeRouter remoteServiceNodeRouter;

    public ClusterEventLooper(IKeySelector selector, IEventQueue queue, IRemoteServiceNodeRouter remoteServiceNodeRouter) {
        this.selector = selector;
        this.queue = queue;
        this.remoteServiceNodeRouter = remoteServiceNodeRouter;
    }

    @Override
    public Event call() throws Exception {
        while (!Thread.interrupted()) {
            Event event = queue.selectOne();
            RemoteServiceNode node = remoteServiceNodeRouter.routeNode(event.getKey());
            ISelectionKey key = selector.select(node.getKey(), node);//按远程节点负载
            synchronized (key.key()) {// 让同一个管道的事件按序执行
                try {
                    key.pipeline().input(event);
                    if (key.pipeline().isDemandDemolish() || event.isMustCancelKey()) {
                        selector.removeKey(key.key());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    CircuitException ce = CircuitException.search(e);
                    if (ce != null) {
                        e = ce;
                    }
                    try {
                        key.pipeline().error(event, e);
                    } catch (Throwable e2) {
                        CJSystem.logging().error(getClass(), e2);
                    } finally {
                        if (key.pipeline().isDemandDemolish() || event.isMustCancelKey()) {
                            selector.removeKey(key.key());
                        }
                    }
                }
            }

        }

        return null;
    }
}
