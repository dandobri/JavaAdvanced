package info.kgeorgiy.ja.dobris.bank;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Server {
    private final static int DEFAULT_PORT = 8888;
    private final ConcurrentMap<Integer, Person> persons = new ConcurrentHashMap<>();

    /**
     * Main method wchi starting the server
     * @param args id of port
     */
    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        final Bank bank = new RemoteBank(port);
        try {
            UnicastRemoteObject.exportObject(bank, port);
            Naming.rebind("//localhost:8088/bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
