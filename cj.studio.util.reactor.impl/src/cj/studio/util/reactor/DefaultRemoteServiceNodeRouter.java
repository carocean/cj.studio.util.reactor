package cj.studio.util.reactor;

import consistenthash.ConsistentHashRouter;

 class DefaultRemoteServiceNodeRouter implements IRemoteServiceNodeRouter {
    ConsistentHashRouter<RemoteServiceNode> router;
    boolean isInit;
    int vNodeCount;
    @Override
    public void init(int vNodeCount) {
        this.vNodeCount=vNodeCount;
        router = new ConsistentHashRouter<RemoteServiceNode>(null, vNodeCount);
        isInit=true;
    }

    @Override
    public int getExistingReplicas(RemoteServiceNode remoteServiceNode) {
        return router.getExistingReplicas( remoteServiceNode);
    }
    @Override
    public void removeNode(RemoteServiceNode remoteServiceNode) {
         router.removeNode( remoteServiceNode);
    }
    @Override
    public void addNode(RemoteServiceNode remoteServiceNode){
        router.addNode(remoteServiceNode,vNodeCount);
    }

    @Override
    public boolean isInit() {
        return isInit;
    }

    @Override
    public void dispose() {
        router=null;
        isInit=false;
    }

    @Override
    public RemoteServiceNode routeNode(String key) {
        return router.routeNode(key);
    }
}
