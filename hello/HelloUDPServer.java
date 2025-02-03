package info.kgeorgiy.ja.dobris.hello;

import info.kgeorgiy.java.advanced.hello.NewHelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class which make the connection with Client
 */
public class HelloUDPServer implements NewHelloServer {
    private ExecutorService server;
    private final Map<Integer, DatagramSocket> sockets = new HashMap<>();
    private final Map<Integer, ExecutorService> clients = new HashMap<>();

    /**
     * Main method which reading the command line and start service
     * @param args args of command line number of port and count of threads
     */
    public static void main(String[] args) {
        Copyable.mainServer(args, new HelloUDPServer());
    }

    /**
     * Method which do the connection with map
     * @param threads number of working threads.
     * @param ports port no to response format mapping.
     */
    @Override
    public void start(int threads, Map<Integer, String> ports) {
        if(ports == null || ports.isEmpty()) {
            return;
        }
        server = Executors.newFixedThreadPool(ports.size());
        ports.forEach((port, reg) -> hello(port, threads, reg));
    }

    /**
     * Method doing the connection with server by socket, do request and get the data from server
     * @param key number of port
     * @param threads count of threads in which we do the connection
     * @param value value which we will change in request
     */
    public void hello(Integer key, int threads, String value){
        try {
            DatagramSocket socket = new DatagramSocket(key);
            sockets.put(key, socket);
            ExecutorService client = Executors.newFixedThreadPool(threads);
            clients.put(key, client);
            int size = socket.getReceiveBufferSize();
            server.submit(() -> {
                try {
                    while (!socket.isClosed()) {
                        DatagramPacket packet = new DatagramPacket(new byte[size], size);
                        socket.receive(packet);
                        client.submit(() -> {
                            String replace = new String(packet.getData(), 0
                                    , packet.getLength(), StandardCharsets.UTF_8);
                            String sendingData = value.replaceAll("\\$", replace);
                            byte[] sendingDataBuffer = sendingData.getBytes();
                            try {
                                socket.send(new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length
                                        , packet.getAddress(), packet.getPort()));
                            } catch (IOException e) {
                                System.err.println("Failed to send packet: " + e.getMessage());
                            }
                        });
                    }
                } catch (IOException e) {
                    System.err.println("You can not receive data " + e.getMessage());
                }
            });
        } catch (SocketException e) {
            System.err.println("Your socket is invalid " + e.getMessage());
        }
    }

    /**
     * @{inheritDoc}
     * Method which close all the threads and sockets
     */
    @Override
    public void close() {
        if(server != null) {
            server.shutdown();
        }
        clients.values().forEach(ExecutorService::shutdown);
        sockets.values().forEach(DatagramSocket::close);
    }
}