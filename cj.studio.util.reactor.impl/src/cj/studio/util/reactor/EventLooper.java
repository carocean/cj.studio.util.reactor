package cj.studio.util.reactor;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

import java.io.PrintWriter;
import java.io.StringWriter;

class EventLooper implements IEventLooper {
    IKeySelector selector;
    IEventQueue queue;

    public EventLooper(IKeySelector selector, IEventQueue queue) {
        this.selector = selector;
        this.queue = queue;
    }

    @Override
    public Event call() throws Exception {
        while (!Thread.interrupted()) {
            Event event = queue.selectOne();
            ISelectionKey key = selector.select(event.getKey());
            synchronized (key.key()) {// 让同一个管道的事件按序执行
                try {
                    key.pipeline().input(event);
                    if (event.isMustCancelKey()) {
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
                        if (event.isMustCancelKey()) {
                            selector.removeKey(key.key());
                        }
                    }
                }
            }

        }

        return null;
    }


}
