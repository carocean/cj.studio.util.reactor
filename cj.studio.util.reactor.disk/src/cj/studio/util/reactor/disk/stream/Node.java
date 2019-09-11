package cj.studio.util.reactor.disk.stream;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class Node {
    protected byte header;//节点头作为保留位
    protected transient Pointer top;//节点开始位，不需要存储

    public Node(Pointer top) {
        this.top = top;
    }
    protected  int getNodeSize(){
        return 1;
    }
    public final byte getHeader() {
        return header;
    }

    public final void setHeader(byte header) {
        this.header = header;
    }

    public abstract void load(RandomAccessFile file) throws IOException;

    public abstract void save(RandomAccessFile file) throws IOException;




}
