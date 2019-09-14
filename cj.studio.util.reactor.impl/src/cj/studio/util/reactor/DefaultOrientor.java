package cj.studio.util.reactor;

import cj.studio.util.reactor.disk.jdbm3.DB;
import cj.studio.util.reactor.disk.jdbm3.DBMaker;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.util.concurrent.ConcurrentMap;

public class DefaultOrientor implements IOrientor {
    ConcurrentMap<String, String> map;
    DB db;

    public DefaultOrientor(String file) {
        db = DBMaker.openFile(file)
                .closeOnExit()
//                .enableEncryption("password", false)
                .make();
        map =db.getHashMap("nodes");
        if(map==null) {
            map = db.createHashMap("nodes");
        }
    }

    @Override
    public RemoteServiceNode get(String key) {
        String json = map.get(key);
        return new Gson().fromJson(json, RemoteServiceNode.class);
    }

    @Override
    public void set(String key, RemoteServiceNode node) {
        String text = new Gson().toJson(node);
        map.put(key, text);
        db.commit();
    }


}
