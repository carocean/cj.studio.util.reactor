package cj.studio.util.reactor.disk.stream;

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
    }

    public boolean isFree() {
        return readerPointer.compareTo(writerPointer) == 0;
    }

    @Override
    protected int getNodeSize() {
        return super.getNodeSize() + readerPointer.pointerSize() + writerPointer.pointerSize() + nextPointer.pointerSize();
    }

    @Override
    public void load(RandomAccessFile file) {

    }

    @Override
    public void save(RandomAccessFile file) {

    }

    public void flushReaderPointer() {

    }

    public void flushWriterPointer() {

    }

    public void flushNextPointer() {

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
}
