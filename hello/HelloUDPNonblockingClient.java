package info.kgeorgiy.ja.dobris.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
/**
 * Class which make the Non blocking connection with Server
 */
public class HelloUDPNonblockingClient implements HelloClient {
    /**
     * Main method which reading the command line and do the Non blocking connection with server
     * @param args arguments of command line host of server, number of port, string prefix, count of threads, request
     */
    public static void main(String[] args) {
        Copyable.mainClient(args, new HelloUDPNonblockingClient());
    }

    /**
     * Method which do the Non bloking connection with server and make request
     * @param host server host
     * @param port server port
     * @param prefix request prefix
     * @param threads number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try(Selector selector = Selector.open()) {
            final InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);
            for(int i = 1; i < threads + 1; i++) {
                try {
                    final DatagramChannel channel = DatagramChannel.open();
                    channel.configureBlocking(false);
                    channel.connect(address);
                    channel.register(selector, SelectionKey.OP_WRITE
                            , new Data(i, 1, requests
                                    , ByteBuffer.allocate(channel.socket().getReceiveBufferSize())));
                } catch (final ClosedChannelException e) {
                    System.err.println("This channel is closed: " + e.getMessage());
                } catch (final SocketException e) {
                    System.err.println("This socket is invalid: " + e.getMessage());;
                } catch (final IOException e) {
                    System.err.println("You have an IO exception: " + e.getMessage());;
                }
            }
            while(!selector.keys().isEmpty() && !Thread.interrupted()) {
                selector.select(100);
                if(selector.selectedKeys().isEmpty()) {
                    for(final SelectionKey key: selector.keys()) {
                        final Data data = (Data) key.attachment();
                        if(System.currentTimeMillis() - data.getLastSendTime() >= 10) {
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                    }
                }
                for(final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                    final SelectionKey key = i.next();
                    try {
                        final Data attach = (Data) key.attachment();
                        final DatagramChannel channel = (DatagramChannel) key.channel();
                        final String sendData = attach.getSendData(prefix);
                        if (key.isWritable()) {
                            attach.write(sendData, address, channel);
                            key.interestOps(SelectionKey.OP_READ);
                        }
                        if (key.isReadable()) {
                            final String receivedData = attach.read(channel);
                            if (receivedData.contains(sendData)) {
                                System.out.println(sendData);
                                System.out.println(receivedData);
                                attach.update();
                            }
                            if (attach.ended()) {
                                key.channel().close();
                            } else {
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                        }
                    } catch (final IOException e) {
                        System.err.println("This channel is already closed: " + e.getMessage());
                    } finally {
                        i.remove();
                    }
                }
            }
        } catch (final UnknownHostException e) {
            System.err.println("Your host is invalid " + e.getMessage());
        } catch (final ClosedSelectorException e) {
            System.err.println("Your selector already closed " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Your channel is invalid: " + e.getMessage());
        }
    }
}
