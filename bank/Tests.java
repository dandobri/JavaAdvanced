package info.kgeorgiy.ja.dobris.bank;
import org.junit.jupiter.api.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@DisplayName("Tests")
public class Tests {
    private static String name;
    private static String surname;
    private static String passportId;
    private final static int DEFAULT_PORT = 8888;
    private static Bank bank;
    private static void initFields(int i, int testNumber) {
        name = "name" + testNumber + i;
        surname = "surname" + testNumber + i;
        passportId = "passportId" + testNumber + i;
    }
    @BeforeAll
    static void beforeAll() {
        bank = new RemoteBank(DEFAULT_PORT);
        try {
            LocateRegistry.createRegistry(DEFAULT_PORT);
            UnicastRemoteObject.exportObject(bank, DEFAULT_PORT);
            Naming.rebind("//localhost:8888/bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
    @Test
    @DisplayName("Creating Test")
    void test1() throws RemoteException {
        int testNumber = 1;
        for(int i = 0; i < 30; i++) {
            initFields(i, testNumber);
            final Person person = bank.createPerson(name, surname, passportId);
            Assertions.assertNotNull(person);
            Assertions.assertEquals(name, person.getName());
            Assertions.assertEquals(surname, person.getSurname());
            Assertions.assertEquals(passportId, person.getPassportId());
            final Account account = bank.createAccount(passportId + ":" + i);
            Assertions.assertEquals(0, account.getAmount());
        }
    }

    @Test
    @DisplayName("Checking that we can cast to Local")
    void test2() throws RemoteException {
        int testNumber = 2;
        for(int i = 0; i < 30; i++) {
            initFields(i, testNumber);
            final Person person = bank.createPerson(name, surname, passportId);
            final LocalPerson localPerson = bank.getPerson(passportId, LocalPerson.class);
            Assertions.assertEquals(passportId, person.getPassportId());
            final Account account = bank.createAccount(passportId + ":" + 1);
            Assertions.assertEquals(passportId + ":" + 1, account.getId());
            Assertions.assertEquals(0, localPerson.getLocalAccountList().size());
        }
    }
    @Test
    @DisplayName("Checking that we can cast to Remote")
    void test3() throws RemoteException {
        int testNumber = 3;
        for(int i = 0; i < 30; i++) {
            initFields(i, testNumber);
            final Person person = bank.createPerson(name, surname, passportId);
            Assertions.assertNotNull(person);
            final RemotePerson remotePerson = bank.getPerson(passportId, RemotePerson.class);
            final Account account = bank.getAccount(remotePerson, remotePerson.getPassportId() + ":" + 1);
            Assertions.assertNull(account);
        }
    }
    @Test
    @DisplayName("Checking that the amount is changing right")
    void test4() throws RemoteException {
        int testNumber = 4;
        for(int i = 0; i < 30; i++) {
            initFields(i, testNumber);
            final Person person = bank.createPerson(name, surname, passportId);
            Account account = bank.createAccount(person.getPassportId() + ":" + i);
            Assertions.assertEquals(0, account.getAmount());
            account.setAmount(100);
            account = bank.getAccount(person, person.getPassportId() + ":" + i);
            Assertions.assertEquals(100, account.getAmount());
        }
    }
    @Test
    @DisplayName("Checking that the Remote Persons see all the changes on Bank Server")
    void test5() throws RemoteException {
        int testNumber = 5;
        for(int i = 0; i < 30; i++) {
            initFields(i, testNumber);
            final Person person = bank.createPerson(name, surname, passportId);
            final RemotePerson remotePerson = bank.getPerson(person.getPassportId(), RemotePerson.class);
            Account account = bank.createAccount(passportId + ":" + i);
            account.setAmount(200);
            final Account account1 = bank.getAccount(remotePerson, account.getId());
            account1.setAmount(1000);
            Assertions.assertEquals(1000, account.getAmount());
        }
    }

    @Test
    @DisplayName("Checking that the local Persons not downloading information from bank")
    void test6() throws RemoteException {
        int testNumber = 6;
        for(int i = 0; i < 30; i++) {
            initFields(i, testNumber);
            final Person person = bank.createPerson(name, surname, passportId);
            Assertions.assertNotNull(person);
            Account account = bank.createAccount(passportId + ":" + i);
            account.setAmount(200);
            final LocalPerson localPerson = bank.getPerson(person.getPassportId(), LocalPerson.class);
            account = bank.getAccount(localPerson, account.getId());
            Assertions.assertEquals(200, account.getAmount());
            final Account account1 = bank.getAccount(person, account.getId());
            account1.setAmount(1000);
            Assertions.assertEquals(200, account.getAmount());
        }
    }

    @Test
    @DisplayName("Concurrency test")
    void test7() throws RemoteException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        bank.createPerson("hello", "hi", "finalTest");
        final Account account = bank.createAccount("finalTest:1234");
        for(int i = 0; i < 11; i++) {
            service.submit(() -> {
                try {
                    synchronized (account) {
                        account.setAmount(account.getAmount() + 1);
                    }
                } catch (RemoteException e) {
                    System.err.println("You have a RemoteException " + e.getMessage());
                }
            });
        }
        service.shutdown();
        while(!service.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println("You have a problem in thread " + e.getMessage());
            }
        }
        Assertions.assertEquals(11, account.getAmount());
    }
}
