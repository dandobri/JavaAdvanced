package info.kgeorgiy.ja.dobris.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.NewHelloServer;

public class Copyable {
    public static void mainClient(String[] args, HelloClient clazz) {
        if(args == null || args.length != 5) {
            System.err.println("You must pass 5 arguments");
            return;
        }
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            clazz.run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("Your input can not be cast" + e.getMessage());
        }
    }
    public static void mainServer(String[] args, NewHelloServer clazz) {
        if(args == null || args.length != 2) {
            System.err.println("You must pass 2 arguments");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            clazz.start(port, threads);
            clazz.close();
        } catch (NumberFormatException e) {
            System.err.println("Your input can not be parsed as an integer" + e.getMessage());
        }
    }
}
