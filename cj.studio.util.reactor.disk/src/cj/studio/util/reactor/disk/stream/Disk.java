package cj.studio.util.reactor.disk.stream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/*
设计原理：
- 采用单链表结构
- 读指针永远指向链表头
- 写指针永远指向链表尾
- 节点类型分为：头节点和数据节点，数据节点是指向真实数据文件的节点，头节点是指向数据节点的节点。它是第0个节点
-- 头节点存有读指针和写指针，分别指向读或写的索引节点
-- 中间节点有三个部分：读在数据文件中的位置及写在数据文件中的位置
- 每次写时从索引文件第一个节点开始扫描，如果发现空块（未用过或读等于写位置）则作为上一个写节点的后继节点。
- 读按链表后续往后读，当到达写的位置等待
- 两类文件：主索引文件（a.m)，数据文件（以.d作为扩展名）,数据文件中的数据块以数据节点来分隔。
- 数据结构为三类节点和数据块构成，三类节点分别是头节点、中间节点、数据节点
 */
public class Disk {

    long dataFileLength;
    RandomAccessFile mainIndexFile;
    List<RandomAccessFile> dataIndexFiles;

    public Disk(String dir, long dataFileLength) throws IOException {
        dataIndexFiles = new ArrayList<>();
        File f = new File(dir);
        if (!f.isDirectory()) {
            throw new IOException("不是目录");
        }
        if (!f.exists()) {
            f.mkdirs();
        }
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        File ifile = new File(String.format("%sa.m", dir));

        if (!ifile.exists()) {
            ifile.createNewFile();
        }
        this.dataFileLength = dataFileLength;
        this.mainIndexFile = new RandomAccessFile(ifile, "rw");

    }

    public void write(byte[] data) throws IOException {
        HeaderNode header = new HeaderNode(new Pointer(0));
        header.load(mainIndexFile);
        Pointer readerPointer = header.getReaderPointer();
        Pointer postion = readerPointer.plus(header.getNodeSize());
        MiddleNode middleNode = new MiddleNode(postion);
        middleNode.load(mainIndexFile);
        if (middleNode.isFree()) {
            Pointer num = postion.subtract(header.getNodeSize()).divide(middleNode.getNodeSize());
            RandomAccessFile dindexfile = dataIndexFiles.get((int) num.getPosition());
            if (dindexfile == null) {
                dindexfile = new RandomAccessFile(String.format("/Users/cj/studio/cj.studio.util.reactor/data/%s.i", num), "rw");
                dataIndexFiles.add(dindexfile);
            }
            DataNode dataNode = new DataNode(middleNode.getWriterPointer());
            dataNode.load(dindexfile);
            RandomAccessFile datafile =new RandomAccessFile(String.format("/Users/cj/studio/cj.studio.util.reactor/data/%s.d", num),"rw");
            dataNode.writeData(datafile,data);
        }
    }

    public static void main(String... args) throws IOException {
        Disk disk = new Disk("/Users/cj/studio/cj.studio.util.reactor/data", 1000L);
        byte[] b = "fuck".getBytes();
        disk.write(b);

    }

}
