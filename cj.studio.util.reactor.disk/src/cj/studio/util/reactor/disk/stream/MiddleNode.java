package cj.studio.util.reactor.disk.stream;

import cj.studio.util.reactor.disk.util.FileReader;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 指向中间的节点<br>
 * 如果读指针与写指针相等则说明节点已满
 */
public class MiddleNode extends Node {
    //读指针在数据索引文件中的位置
    Pointer readerPointer;//读指针为8字节
    //写指针在数据索引文件中的位置
    Pointer writerPointer;//写指针为8字节
    Pointer nextPointer;//指向下一个中间节点，表明顺序读取完当前数据节点文件节点后应该往指定的下一个中间节点指向擞据节点索引文件中按顺序读

    public MiddleNode(Pointer top) {
        super(top);
       reset();
    }

    public boolean isEmpty() {
        return (writerPointer.compareTo(0)==0)&&(readerPointer.compareTo(writerPointer) == 0);
    }

    @Override
    protected int getNodeSize() {
        return super.getNodeSize() + 8+8+8;
    }

    @Override
    public Pointer load(RandomAccessFile file) throws IOException {
        file.seek(top.getPosition());
        byte[] dest = new byte[getNodeSize()];
        FileReader.readfully(file,dest);
        header=dest[0];
        byte[] rp=new byte[8];
        byte[] wp=new byte[8];
        byte[] np=new byte[8];
        System.arraycopy(dest,1,rp,0,rp.length);
        System.arraycopy(dest,1+rp.length,wp,0,wp.length);
        System.arraycopy(dest,1+rp.length+wp.length,np,0,np.length);
        readerPointer.loadBytes(rp);
        writerPointer.loadBytes(wp);
        nextPointer.loadBytes(np);
        return new Pointer(dest.length);
    }

    @Override
    public Pointer save(RandomAccessFile file) throws IOException {
        byte[] rp = readerPointer.toBytes();
        byte[] wp = writerPointer.toBytes();
        byte[] np = nextPointer.toBytes();
        byte[] dest = new byte[getNodeSize()];
        dest[0] = header;
        System.arraycopy(rp, 0, dest, 1, rp.length);
        System.arraycopy(wp, 0, dest, 1 + rp.length, wp.length);
        System.arraycopy(np, 0, dest, 1 + rp.length+wp.length, np.length);
        file.seek(0);
        file.write(dest, 0, dest.length);
        return top.plus(getNodeSize());
    }

    public void flushReaderPointer(RandomAccessFile file) throws IOException {
        byte[] rp = readerPointer.toBytes();
        file.seek(top.getPosition()+1);
        file.write(rp,0,rp.length);
    }

    public void flushWriterPointer(RandomAccessFile file) throws IOException {
        byte[] wp = writerPointer.toBytes();
        file.seek(top.getPosition()+1+8);
        file.write(wp,0,wp.length);
    }

    public void flushNextPointer(RandomAccessFile file) throws IOException {
        byte[] np = nextPointer.toBytes();
        file.seek(top.getPosition()+1+8+8);
        file.write(np);
    }

    public Pointer getWriterPointer() {
        return writerPointer;
    }

    public Pointer getReaderPointer() {
        return readerPointer;
    }

    public Pointer getNextPointer() {
        return nextPointer;
    }

    public void setWriterPointer(Pointer writerPointer) {
        this.writerPointer = writerPointer;
    }

    public void setReaderPointer(Pointer readerPointer) {
        this.readerPointer = readerPointer;
    }

    public void setNextPointer(Pointer nextPointer) {
        this.nextPointer = nextPointer;
    }

    public boolean canWriteable(long dataFileLength) {
        return writerPointer.compareTo(dataFileLength)<0;
    }

    public long getNumber(int headerNodeSize) {
        long v=top.getPosition()-headerNodeSize;
        return v/getNodeSize();
    }

    public boolean isReadToWritePointer() {
        return readerPointer.compareTo(writerPointer)>=0;
    }

    public void reset() {
        this.header=0;
        this.readerPointer=new Pointer(0);
        this.writerPointer=new Pointer(0);
        this.nextPointer=new Pointer(0);
    }
}
