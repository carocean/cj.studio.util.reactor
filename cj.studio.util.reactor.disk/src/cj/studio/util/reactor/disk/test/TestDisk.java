package cj.studio.util.reactor.disk.test;

import cj.studio.util.reactor.disk.stream.DiskStream;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TestDisk {

    static DiskStream disk;
    static String diskDir;
    static ReentrantLock lock;
    static Condition readToWPointerCondition;
    static {
        lock=new ReentrantLock();
        readToWPointerCondition=lock.newCondition();
        diskDir = "/Users/cj/studio/cj.studio.util.reactor/cj.studio.util.reactor.disk/data";
//        empty();
        long dataFileLength = 2 * 1024 * 1024;
        try {
            disk = new DiskStream(diskDir, dataFileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws IOException, InterruptedException {
//        testWrite();
//        disk.close();
//        System.out.println("完");
//        testRead();
//        System.out.println("完");
        //多线程有冲突，必须对节点上锁
        for(int i=0;i<100;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        testRead();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        for(int i=0;i<100;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        testWrite();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            Thread.sleep(1000);
        }


    }

    private static void testRead() throws IOException, InterruptedException {
//        long v=System.currentTimeMillis();
        while (true) {
            byte[] data = disk.read();//如果为空
            if (data == null) {
//                System.out.println("------结束");
                try {
                    lock.lock();
                    readToWPointerCondition.await();
                    continue;
                }finally {
                    lock.unlock();
                }
//                break;
            }
            System.out.println("------" + new String(data));
            Thread.sleep(100);
        }
//        v=System.currentTimeMillis()-v;
//        System.out.println("*****读耗时***"+(v/1000.000)+"S");
    }

    private static void testWrite() throws IOException, InterruptedException {
//        long size=0;
//        long v=System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            byte[] b = String.format("%s-%s-%s-香港记者协会及香港摄影记者协会9月12日召开记者会表示，至今已收到50多件涉及警员无理阻碍采访和攻击的投诉，形容记者的工作环境变得史无前例的恶劣。", i, UUID.randomUUID(), new Object().hashCode()).getBytes();
            disk.write(b);
//            size+=b.length;
            try {
                lock.lock();
                readToWPointerCondition.signal();
            }finally {
                lock.unlock();
            }
            Thread.sleep(10);
        }
//        v=System.currentTimeMillis()-v;
//        System.out.println("*****写耗时***"+(v/1000.000)+"S-大小-"+(size/1024.00/1024.00)+"M");
    }

    public static void empty() {
        File dir = new File(diskDir);
        if (!dir.exists()) return;

        for (File f : dir.listFiles()) {
            f.delete();
        }
        dir.delete();
    }
}
