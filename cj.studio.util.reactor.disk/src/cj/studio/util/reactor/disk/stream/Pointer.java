package cj.studio.util.reactor.disk.stream;

import cj.studio.util.reactor.disk.util.BytesUtil;

public class Pointer {
    private long position;


    public Pointer(long position) {
        this.position = position;
    }
    public int pointerSize(){
        return 8;
    }
    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public byte[] toBytes() {
        return BytesUtil.longToBytes(position);
    }

    public void loadBytes(byte[] b) {
        this.position = BytesUtil.bytesToLong(b);
    }

    public int compareTo(long l) {
        if(position==l)return 0;
        return position>l?1:-1;
    }

    public int compareTo(Pointer pointer) {
        return compareTo(pointer.position);
    }
    public long plusOffset(long offset){
        return position+offset;
    }
    public Pointer plus(int size) {
        return new Pointer(size+position);
    }

    public Pointer subtract(int size) {
        return new Pointer(position-size);
    }

    public Pointer divide(int size) {
        return new Pointer(position/size);
    }
}
