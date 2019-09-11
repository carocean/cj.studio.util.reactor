package cj.studio.util.reactor.disk.stream;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 在数据文件中用于标识数据块长度，位于数据块前面，是块的分隔节点
 */
public class DataNode extends Node {
    int dataLen;//数据长度

    @Override
    protected int getNodeSize() {
        return 0;
    }

    public DataNode(Pointer top) {
        super(top);
    }


    @Override
    public void load(RandomAccessFile file) {

    }

    @Override
    public void save(RandomAccessFile file) {

    }

    public void writeData(RandomAccessFile datafile, byte[] data) throws IOException {

    }


    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }
}
