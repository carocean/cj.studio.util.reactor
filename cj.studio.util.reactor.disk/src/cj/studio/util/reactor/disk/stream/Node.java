package cj.studio.util.reactor.disk.stream;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class Node {
    protected byte header;//节点头作为保留位,目前仅用最低2位用于读写锁，第0位为1为读已锁，第1位为1为写已锁，两位均为1表示块被锁
    protected transient Pointer top;//节点开始位，不需要存储

    public Node(Pointer top) {
        this.top = top;
    }

    public Pointer getTop() {
        return top;
    }

    protected int getNodeSize() {
        return 1;
    }

    public final byte getHeader() {
        return header;
    }

    public final void setHeader(byte header) {
        this.header = header;
    }

    public abstract Pointer load(RandomAccessFile file) throws IOException;

    public abstract Pointer save(RandomAccessFile file) throws IOException;


}
