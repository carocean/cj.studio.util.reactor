package cj.studio.util.reactor;

import cj.ultimate.IDisposable;

/**
 * 远程节点管理器
 */
public interface IRemoteServiceNodeRouter extends IDisposable {
    /**
     * 初始化
     * @param vNodeCount 虚拟节点是物理节点的副本，每个物理节点均可以有0-n个副本，正是以副本进行均衡的而不是以物理节点，因此此值为0表示无节点，为1表示每个物理节点有一个副本进行均衡
     */
    void init(int vNodeCount);

    int getExistingReplicas(RemoteServiceNode remoteServiceNode);

    void removeNode(RemoteServiceNode remoteServiceNode);

    void addNode(RemoteServiceNode remoteServiceNode);

    boolean isInit();

    RemoteServiceNode routeNode(String key);
}
