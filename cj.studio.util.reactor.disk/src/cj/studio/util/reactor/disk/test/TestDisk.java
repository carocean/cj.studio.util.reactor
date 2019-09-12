package cj.studio.util.reactor.disk.test;

import cj.studio.util.reactor.disk.stream.Disk;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TestDisk {

    static Disk disk;
    static String diskDir;

    static {
        diskDir = "/Users/caroceanjofers/studio/github/cj.studio.util.reactor/cj.studio.util.reactor.disk/data";
        empty();
        long dataFileLength = 2 * 1024 * 1024;
        try {
            disk = new Disk(diskDir, dataFileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws IOException, InterruptedException {
        //多线程有冲突，必须对节点上锁
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    testWrite();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(5000L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    testRead();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private static void testRead() throws IOException {
        long v=System.currentTimeMillis();
        while (true) {
            byte[] data = disk.read();//如果为空
            if (data == null) {
                System.out.println("------结束");
                break;
            }
            System.out.println("------" + new String(data));
        }
        v=System.currentTimeMillis()-v;
        System.out.println("*****读耗时***"+(v/1000.000)+"S");
    }

    private static void testWrite() throws IOException {
        long size=0;
        long v=System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            byte[] b = String.format("%s-%s-%s-香港记者协会及香港摄影记者协会9月12日召开记者会表示，至今已收到50多件涉及警员无理阻碍采访和攻击的投诉，形容记者的工作环境变得史无前例的恶劣。", i, UUID.randomUUID(), new Object().hashCode()).getBytes();
            disk.write(b);
            size+=b.length;
        }
        v=System.currentTimeMillis()-v;
        System.out.println("*****写耗时***"+(v/1000.000)+"S-大小-"+(size/1024.00/1024.00)+"M");
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
