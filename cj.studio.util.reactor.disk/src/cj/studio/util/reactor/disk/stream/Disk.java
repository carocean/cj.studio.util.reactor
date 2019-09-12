package cj.studio.util.reactor.disk.stream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
设计原理：
- 采用单链表结构
- 读指针永远指向链表头
- 写指针永远指向链表尾
- 节点类型分为：头节点和数据节点，数据节点是指向真实数据文件的节点，头节点是指向数据节点的节点。它是第0个节点
-- 头节点存有读指针和写指针，分别指向读或写的索引节点
-- 当读完一个数据文件则重置读写指针为0
-- 中间节点有三个部分：读在数据文件中的位置及写在数据文件中的位置
- 每次写时从索引文件第一个节点开始扫描，如果发现空块（未用过或读等于写位置）则作为上一个写节点的后继节点。
- 读按链表后续往后读，当到达写的位置等待
- 两类文件：主索引文件（a.m)，数据文件（以.d作为扩展名）,数据文件中的数据块以数据节点来分隔。
- 数据结构为三类节点和数据块构成，三类节点分别是头节点、中间节点、数据节点
 */
public class Disk {
    ReentrantLock lock;
    Condition readToWPointerCondition;
    long dataFileLength;
    RandomAccessFile mainIndexFile;
    Map<Long, RandomAccessFile> dataIndexFiles;
    HeaderNode header;
    String homeDir;
    public Disk(String dir, long dataFileLength) throws IOException {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        dataIndexFiles = new HashMap<>();
        this.homeDir=f.getPath();
        if (!this.homeDir.endsWith("/")) {
            this.homeDir = this.homeDir + "/";
        }
        File ifile = new File(String.format("%sa.m", this.homeDir));

        if (!ifile.exists()) {
            ifile.createNewFile();
        }
        this.dataFileLength = dataFileLength;
        this.header = new HeaderNode(new Pointer(0));
        this.mainIndexFile = new RandomAccessFile(ifile, "rw");
        this.lock=new ReentrantLock();
        this.readToWPointerCondition=lock.newCondition();
        header.load(mainIndexFile);
    }

    //每次读一个数据块
    public byte[] read() throws IOException {
        Pointer readerPointer = header.readerPointer;
        //开始读一个中间节点
        MiddleNode middleNode = new MiddleNode(readerPointer);
        middleNode.load(mainIndexFile);
        if (middleNode.isReadToWritePointer()) {//如果读到写指针则到尾，返回null
            try {
                lock.lock();
                this.readToWPointerCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
//            return null;
        }

        long num = middleNode.getNumber(header.getNodeSize());
        RandomAccessFile dfile = this.dataIndexFiles.get(num);
        if (dfile == null) {
            dfile = openOrCreateDataFile(num);
            this.dataIndexFiles.put(num, dfile);
        }
        DataNode dataNode = new DataNode(middleNode.getReaderPointer());
        Pointer readerNewPos = dataNode.load(dfile);
        if (readerNewPos.compareTo(dataFileLength) >= 0) {//到了文件尾，跳
            //将下一指针给头节点，重置当前中间节点的读写下指针为0
            header.setReaderPointer(middleNode.getNextPointer());
            header.flushReaderPointer(mainIndexFile);
            middleNode.reset();
            middleNode.save(mainIndexFile);
        } else {//仅更新下次读的位置
            middleNode.setReaderPointer(readerNewPos);
            middleNode.flushReaderPointer(mainIndexFile);
        }

        return dataNode.getData();
    }

    public void write(byte[] data) throws IOException {
        if (data.length + 5 > dataFileLength) {//5是数据节点的头头大小
            throw new IOException(String.format("数据大小超过数据文件大小,dataLength+5>dataFileLength: %s>%s", data.length+5, dataFileLength));
        }
        Pointer writerPointer = header.writerPointer;
        MiddleNode middleNode = new MiddleNode(writerPointer);
        middleNode.load(mainIndexFile);
        MiddleNode next = null;
        if (!middleNode.canWriteable(this.dataFileLength)) {//如果此中间节点不能写了
            long begin = header.getNodeSize();
            next = scanForEmpty(new Pointer(begin));//从头开始扫中间节点
            if (next == null) {//如果在现有块中未发现，则开辟新块
                long length = mainIndexFile.length() + middleNode.getNodeSize() * 32;//扩展
                mainIndexFile.setLength(length);
                next = new MiddleNode(new Pointer(mainIndexFile.length() + middleNode.getNodeSize()));
            }
            middleNode.setNextPointer(next.top);
            middleNode.flushNextPointer(mainIndexFile);
            middleNode = next;//下移了中间节点
            header.setWriterPointer(next.top);//头指向新节点
            header.flushWriterPointer(mainIndexFile);
        }
        Pointer wp = middleNode.getWriterPointer();
        long num = middleNode.getNumber(header.getNodeSize());
        RandomAccessFile dfile = this.dataIndexFiles.get(num);
        if (dfile == null) {
            dfile = openOrCreateDataFile(num);
            dfile.setLength(dataFileLength);
            this.dataIndexFiles.put(num, dfile);
        }
        DataNode dataNode = new DataNode(wp, data);
        Pointer wpointer = dataNode.save(dfile);
        middleNode.setWriterPointer(wpointer);
        middleNode.flushWriterPointer(mainIndexFile);
        try{
            lock.lock();
            readToWPointerCondition.signal();
        }finally {
            lock.unlock();
        }
    }

    //从头到主索引文件尾扫描
    private MiddleNode scanForEmpty(Pointer begin) throws IOException {
        MiddleNode node = new MiddleNode(begin);
        node.load(mainIndexFile);
        if (node.isEmpty()) {
            return node;
        }
        return scanForEmpty(begin.plus(node.getNodeSize()));
    }

    private RandomAccessFile openOrCreateDataFile(long num) throws IOException {
        String fn = String.format("%s%s.d",homeDir, num);
        File f = new File(fn);
        if (!f.exists()) {
            f.createNewFile();
        }
        RandomAccessFile file = new RandomAccessFile(f, "rw");
        return file;
    }



}
