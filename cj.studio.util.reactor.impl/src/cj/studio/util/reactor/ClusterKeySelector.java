package cj.studio.util.reactor;

import cj.studio.util.reactor.disk.jdbm3.DBMaker;

public class ClusterKeySelector extends DefaultKeySelector {
    IRemoteServiceNodeRouter remoteServiceNodeRouter;
    IOrientor orientor;

    public ClusterKeySelector(IRemoteServiceNodeRouter remoteServiceNodeRouter,IOrientor orientor, IPipelineCombination combination, IServiceProvider parent) {
        super(combination, parent);
        this.remoteServiceNodeRouter = remoteServiceNodeRouter;
        this.orientor=orientor;
    }

    @Override
    protected Object assignAttachmentFor(String key) {
        if (orientor == null) {
            return remoteServiceNodeRouter.routeNode(key);
        }
        RemoteServiceNode node = orientor.get(key);
        if (node != null) return node;
        node = remoteServiceNodeRouter.routeNode(key);
        orientor.set(key, node);
        return node;
    }
}
