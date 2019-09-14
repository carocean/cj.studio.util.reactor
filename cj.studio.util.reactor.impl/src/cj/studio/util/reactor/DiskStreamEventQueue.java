package cj.studio.util.reactor;

import cj.studio.ecm.EcmException;
import cj.studio.util.reactor.disk.stream.DiskStream;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DiskStreamEventQueue implements IEventQueue {
    DiskStream diskStream;
    String dbHome;
    long dataFileLength;
    ReentrantLock lock;
    Condition readToWPointerCondition;

    public DiskStreamEventQueue(String dbHome) {
        this.dbHome = dbHome;
    }

    @Override
    public void init(int capacity) {
        lock = new ReentrantLock();
        readToWPointerCondition = lock.newCondition();
        this.dataFileLength = capacity;
        try {
            diskStream = new DiskStream(dbHome, dataFileLength);
        } catch (IOException e) {
            throw new EcmException(e);
        }
    }

    @Override
    public Event selectOne() {
        byte[] b = null;
        try {
            b = diskStream.read();
        } catch (IOException e) {
            throw new EcmException(e);
        }
        if (b == null) {
            try {
                lock.lock();
                readToWPointerCondition.await();
                return selectOne();
            } catch (InterruptedException e) {
                throw new EcmException(e);
            } finally {
                lock.unlock();
            }
        }
        Event e = new Gson().fromJson(new String(b), Event.class);
        return e;

    }

    @Override
    public void addEvent(Event e) {
        try {
            diskStream.write(new Gson().toJson(e).getBytes());
        } catch (IOException ex) {
            throw new EcmException(ex);
        }
        try {
            lock.lock();
            readToWPointerCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        try {
            this.diskStream.close();
        } catch (IOException e) {
            throw new EcmException(e);
        }
    }

    @Override
    public int count() {
        throw new EcmException("不支持该方法");
    }

}
