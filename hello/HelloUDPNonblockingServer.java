package info.kgeorgiy.ja.dobris.hello;

import info.kgeorgiy.java.advanced.hello.NewHelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Class which make the Non blocking connection with Client
 */
public class HelloUDPNonblockingServer implements NewHelloServer {
    private Selector selector;
    private final Map<Integer, DatagramChannel> datagramChannel = new HashMap<>();
    private ExecutorService executor;
    /**
     * Main method which reading the command line and start service
     * @param args args of command line number of port and count of threads
     */
    public static void main(String[] args) {
        Copyable.mainServer(args, new HelloUDPNonblockingServer());
    }
    /**
     * Method which do the Non blocking connection with map
     * @param threads number of working threads.
     * @param ports port no to response format mapping.
     */
    @Override
    public void start(int threads, Map<Integer, String> ports) {
        if(ports == null || ports.isEmpty()) {
            System.err.println("ports is null or empty");
            return;
        }
        try {
            selector = Selector.open();
            executor = Executors.newSingleThreadExecutor();
            for(Map.Entry<Integer, String> entry : ports.entrySet()) {
                final DatagramChannel channel = DatagramChannel.open();
                final InetSocketAddress address = new InetSocketAddress(entry.getKey());
                channel.configureBlocking(false);
                channel.bind(address);
                channel.register(selector, SelectionKey.OP_READ, new Buffer(
                        ByteBuffer.allocate(channel.socket().getReceiveBufferSize()), null, entry.getValue()));
                datagramChannel.put(entry.getKey(), channel);
            }
            executor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        selector.select(100);
                    } catch (final IOException e) {
                        System.err.println("Error in selection " + e.getMessage());
                    }
                    for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                        final SelectionKey key = i.next();
                        try {
                            final DatagramChannel datagramChannel = (DatagramChannel) key.channel();
                            final Buffer attach = (Buffer) key.attachment();
                            if (key.isReadable()) {
                                attach.buffer.clear();
                                try {
                                    final InetSocketAddress addr = (InetSocketAddress) datagramChannel.receive(attach.buffer);
                                    attach.buffer.flip();
                                    final String replace = new String(attach.buffer.array(), 0, attach.buffer.limit());
                                    final String sendingData = attach.value.replaceAll("\\$", replace);
                                    attach.read(sendingData, addr);
                                    key.interestOps(SelectionKey.OP_WRITE);
                                } catch (IOException e) {
                                    System.err.println("You have an error in received data " + e.getMessage());
                                }
                            }
                            if (key.isWritable()) {
                                try {
                                    datagramChannel.send(attach.buffer, attach.address);
                                    key.interestOps(SelectionKey.OP_READ);
                                } catch (final IOException e) {
                                    System.err.println("You have an error in sending data " + e.getMessage());
                                }
                            }
                        } finally {
                            i.remove();
                        }
                    }
                }
            });
        } catch (final IOException e) {
            System.err.println("Error opening selector " + e.getMessage());
        }
    }
    /**
     * @{inheritDoc}
     * Method which close all the threads and sockets
     */
    @Override
    public void close() {
        if(executor != null) {
            executor.shutdown();
        }
        if(selector != null) {
            try {
                selector.close();
            } catch (final IOException e) {
                System.err.println("Error closing selector " + e.getMessage());
            }
        }
        datagramChannel.forEach((a, b) -> {
            try {
                b.close();
            } catch (final IOException e) {
                System.err.println("Error closing channel " + e.getMessage());
            }
        });
    }
}
