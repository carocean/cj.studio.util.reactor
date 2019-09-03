package cj.studio.util.reactor;

import consistenthash.Node;

public class RemoteServiceNode implements Node {
    String key;
    String host;
    Object extra;
    public RemoteServiceNode(String key, String host) {
        this.key = key;
        this.host = host;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
