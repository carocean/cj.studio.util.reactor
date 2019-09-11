package cj.studio.util.reactor.disk.stream;

import cj.studio.util.reactor.disk.util.FileReader;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 头节点
 */
public class HeaderNode extends Node {
    //读指针在主索引文件中的块位编号
    Pointer readerPointer;//读指针为8字节
    //写指针在主索引文件中的块位编号
    Pointer writerPointer;//写指针为8字节

    public HeaderNode(Pointer top) {
        super(top);
    }

    @Override
    protected int getNodeSize() {
        return super.getNodeSize()+readerPointer.pointerSize()+writerPointer.pointerSize();
    }

    @Override
    public void save(RandomAccessFile file) throws IOException {
        byte[] rp = readerPointer.toBytes();
        byte[] wp = writerPointer.toBytes();
        byte[] dest = new byte[getNodeSize()];
        dest[0] = header;
        System.arraycopy(rp, 0, dest, 1, rp.length);
        System.arraycopy(wp, 0, dest, 1 + rp.length, wp.length);
        file.seek(0);
        file.write(dest, 0, dest.length);
    }

    @Override
    public void load(RandomAccessFile file) throws IOException {
        if (file.length() == 0) {
            this.readerPointer=new Pointer(0);
            this.writerPointer=new Pointer(0);
            return;
        }
        byte[] dest = new byte[getNodeSize()];
        FileReader.readfully(file,dest);
        header=dest[0];
        byte[] rp=new byte[8];
        byte[] wp=new byte[8];
        System.arraycopy(dest,1,rp,0,rp.length);
        System.arraycopy(dest,1+rp.length,wp,0,wp.length);
        readerPointer.loadBytes(rp);
        writerPointer.loadBytes(wp);
    }

    public Pointer getWriterPointer() {
        return writerPointer;
    }

    public Pointer getReaderPointer() {
        return readerPointer;
    }

    public void setReaderPointer(Pointer readerPointer) {
        this.readerPointer = readerPointer;
    }

    public void setWriterPointer(Pointer writerPointer) {
        this.writerPointer = writerPointer;
    }

    public void flushWriterPointer() {

    }

    public void flushReaderPointer() {

    }
}
