package cj.studio.util.reactor.disk.test;

import cj.studio.util.reactor.disk.stream.Disk;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class TestDisk {

    static Disk disk;

    static {
        String diskDir = "/Users/cj/studio/cj.studio.util.reactor/data";
        long dataFileLength = 640;
        try {
            disk = new Disk(diskDir, dataFileLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws IOException {
        emptyDir();
        testWrite();
        testRead();
    }

    private static void testRead() throws IOException {

        while (true) {
            byte[] data = disk.read();//如果为空
            if (data == null) {
                System.out.println("------结束");
                break;
            }
            System.out.println("------" + new String(data));
        }
    }

    private static void testWrite() throws IOException {
        for (int i = 0; i < 100; i++) {
            byte[] b = String.format("%s-%s-%s-香港记者协会及香港摄影记者协会9月12日召开记者会表示，至今已收到50多件涉及警员无理阻碍采访和攻击的投诉，形容记者的工作环境变得史无前例的恶劣。", i, UUID.randomUUID(), new Object().hashCode()).getBytes();
            disk.write(b);
        }
    }

    private static void emptyDir() {
        disk.empty();

    }
}
