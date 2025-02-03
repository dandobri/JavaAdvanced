package info.kgeorgiy.ja.dobris.hello;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class Buffer {
    public ByteBuffer buffer;
    public InetSocketAddress address;
    public String value;
    public Buffer(ByteBuffer buffer, InetSocketAddress address, String value) {
        this.buffer = buffer;
        this.address = address;
        this.value = value;
    }
    public void read(String sendingData, InetSocketAddress addr) {
        buffer.clear();
        buffer.put(sendingData.getBytes());
        buffer.flip();
        address = addr;
    }
}
