package cj.studio.util.reactor.disk.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReader {
    public static synchronized void readfully(RandomAccessFile file, byte[] all) throws IOException {
        int theRead = 0;
        int totalReads = 0;
        while (theRead > -1 && totalReads < all.length) {
            theRead = file.read(all, 0, all.length);
            totalReads += theRead;
        }
    }
}
