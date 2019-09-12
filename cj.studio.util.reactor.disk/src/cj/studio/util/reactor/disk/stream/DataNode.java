package cj.studio.util.reactor.disk.stream;

import cj.studio.util.reactor.disk.util.BytesUtil;
import cj.studio.util.reactor.disk.util.FileReader;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 在数据文件中用于标识数据块长度，位于数据块前面，是块的分隔节点
 */
public class DataNode extends Node {
    int dataLen;//数据长度
    byte[] data;


    @Override
    protected int getNodeSize() {
        return getNodeSize() + 4 + dataLen;
    }

    public DataNode(Pointer top) {
        super(top);
    }

    public DataNode(Pointer top, byte[] data) {
        super(top);
        setData(data);
    }

    public void setData(byte[] data) {
        dataLen = data.length;
        this.data = data;
    }

    @Override
    public Pointer load(RandomAccessFile file) throws IOException {
        file.seek(top.getPosition());
        byte[] head = new byte[super.getNodeSize() + 4];//节点头
        FileReader.readfully(file, head);
        byte[] b = new byte[4];//dataLen
        System.arraycopy(head, 1, b, 0, b.length);
        this.header = head[0];
        this.dataLen = BytesUtil.bytesToInt(b);
        //读数据
        byte[] data = new byte[dataLen];
        file.seek(top.getPosition() + head.length);
        FileReader.readfully(file, data);
        this.data = data;
        return top.plus(head.length + dataLen);
    }

    @Override
    public Pointer save(RandomAccessFile datafile) throws IOException {
//        if (datafile.length() <= top.getPosition()) {
//            long len = top.getPosition() + data.length * 1024;//向后面扩展n个数据块大小小
//            datafile.setLength(len);
//        }
        datafile.seek(top.getPosition());
        byte[] box = toBytes();
        datafile.write(box, 0, box.length);
        Pointer movedTo = top.plus(box.length);
        return movedTo;
    }

    private byte[] toBytes() {
        byte[] dataLenArr = BytesUtil.intToBytes(this.dataLen);
        byte[] box = new byte[1 + dataLenArr.length + data.length];
        box[0] = header;
        System.arraycopy(dataLenArr, 0, box, 1, dataLenArr.length);
        System.arraycopy(data, 0, box, 1 + dataLenArr.length, data.length);
        return box;
    }


    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public byte[] getData() {
        return data;
    }
}
