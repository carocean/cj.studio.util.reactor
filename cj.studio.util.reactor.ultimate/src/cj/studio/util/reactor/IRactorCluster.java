package cj.studio.util.reactor;

public interface IRactorCluster {
    void addRemoteNode(RemoteServiceNode node);
    void removeRemoteNode(String key);
    int nodeCount();
    void input(Event event);

    void close();
}
