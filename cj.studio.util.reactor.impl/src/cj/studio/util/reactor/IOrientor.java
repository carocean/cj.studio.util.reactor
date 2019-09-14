package cj.studio.util.reactor;

/**
 * 定向器<br>
 *     用于记忆路由路径，结合ClusterRactor，一旦选取了路径，之后便将请求永完定向到该路径
 */
public interface IOrientor {
    RemoteServiceNode get(String key);

    void set(String key, RemoteServiceNode node);

}
