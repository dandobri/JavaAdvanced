package info.kgeorgiy.ja.dobris.hello;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Data {
    private final int thread;
    private int countRequest;
    private final int requests;
    private final ByteBuffer buffer;
    private long lastSendTime;
    public Data(final int thread, final int countRequest, final int requests, final ByteBuffer buffer) {
        this.thread = thread;
        this.countRequest = countRequest;
        this.requests = requests;
        this.buffer = buffer;
        this.lastSendTime = System.currentTimeMillis();
    }
    public boolean ended() {
        return countRequest > requests;
    }
    public void update() {
        countRequest++;
    }
    public void write(String sendData, InetSocketAddress address, DatagramChannel channel) throws IOException {
        buffer.clear();
        buffer.put(sendData.getBytes());
        buffer.flip();
        channel.send(buffer, address);
        lastSendTime = System.currentTimeMillis();
    }
    public long getLastSendTime() {
        return lastSendTime;
    }
    public String getSendData(String prefix) {
        return prefix + thread + "_" + countRequest;
    }
    public String read(DatagramChannel channel) throws IOException {
        buffer.clear();
        channel.receive(buffer);
        buffer.flip();
        return new String(buffer.array(), 0, buffer.limit());
    }
}
