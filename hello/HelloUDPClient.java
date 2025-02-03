package info.kgeorgiy.ja.dobris.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class which make the connection with Server
 */
public class HelloUDPClient implements HelloClient {
    /**
     * Main method which reading the command line and do the connection with server
     * @param args arguments of command line host of server, number of port, string prefix, count of threads, request
     */
    public static void main(String[] args) {
        Copyable.mainClient(args, new HelloUDPClient());
    }

    /**
     * Method which do the connection with server and make request
     * @param host server host
     * @param port server port
     * @param prefix request prefix
     * @param threads number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try(DatagramChannel channel = DatagramChannel.open()) {
            ExecutorService client = Executors.newFixedThreadPool(threads);
            InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);
            channel.connect(address);
            for(int i = 1; i < threads + 1; i++) {
                int finalI = i;
                client.submit(() -> {
                    synchronized (channel) {
                        try {
                            channel.socket().setSoTimeout(10);
                            int size = channel.socket().getReceiveBufferSize();
                            Data data = new Data(finalI, 1, requests, ByteBuffer.allocate(size));
                            while (!data.ended() && !Thread.currentThread().isInterrupted()) {
                                try {
                                    String sendData = data.getSendData(prefix);
                                    data.write(sendData, address, channel);
                                    String receivedData = data.read(channel);
                                    if (receivedData.contains(sendData)) {
                                        System.out.println(1);
                                        System.out.println(sendData);
                                        System.out.println(receivedData);
                                        data.update();
                                    }
                                } catch (IOException e) {
                                    System.err.println("Failed to send message " + e.getMessage());
                                }
                            }
                        } catch (SocketException e) {
                            System.err.println("Your socket could not be created " + e.getMessage());
                        }
                    }
                });
            }
            client.shutdown();
            while (!client.isTerminated()) {
                Thread.sleep(10);
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Your client was interrupted " + e.getMessage());
        } catch (IOException e) {
            System.err.println("E");
        }
    }
}