package cj.studio.util.reactor.disk.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReader {
    public static void readfully(RandomAccessFile file,byte[] all) throws IOException {
        int read = 0;
        int pre = 0;
        for (; (read = file.read(all, pre, all.length - pre)) > -1; ) {

        }
    }
}
