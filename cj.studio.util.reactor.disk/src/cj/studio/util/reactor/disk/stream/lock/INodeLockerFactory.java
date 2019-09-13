package cj.studio.util.reactor.disk.stream.lock;

import cj.studio.util.reactor.disk.stream.Node;

/**
 * 节点锁工厂
 */
public interface INodeLockerFactory {
    /**
     * 尝试对节点加锁,如果得不到锁就堵塞
     * @param node 以node的top位置作为节点是否相同的判断依据，因为同一节点的实例可能不同
     */
    ILocker tryLock(Node node, boolean readLocker, boolean writeLockker);

    /**
     * 释放锁
     * @param locker
     */
    void unlock(ILocker locker);
}
