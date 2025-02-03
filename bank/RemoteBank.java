package info.kgeorgiy.ja.dobris.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Person, List<Account>> personAccounts = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        if (id == null || id.isEmpty()) {
            System.err.println("id is null or empty");
            return null;
        }
        String[] ids = id.split(":");
        if (ids.length != 2 || ids[0].isEmpty() || ids[1].isEmpty()) {
            System.err.println("Your id must be passportId:subId, for example 1:2");
            return null;
        }
        String passportId = ids[0];
        String subId = ids[1];
        System.out.println("Creating account " + subId + " for " + passportId);
        final Person person = persons.get(passportId);
        if (person == null) {
            System.err.println("Your person " + passportId + " does not exist");
            return null;
        }
        final Account account = new RemoteAccount(id, 0);
        if (accounts.putIfAbsent(id, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            personAccounts.get(person).add(account);
            return account;
        }
        System.out.println("Your account " + id + " already exists");
        return accounts.get(id);
    }

    @Override
    public <T extends Person> Account getAccount(final T person, final String id) {
        if (person.getClass().equals(LocalPerson.class)) {
            final LocalPerson localPerson = (LocalPerson) person;
            return localPerson.getLocalAccount(id);
        }
        if (!accounts.containsKey(id)) {
            System.err.println("Account " + id + " does not exist");
            return null;
        }
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public <T extends Person> T getPerson(final String passport, Class<T> type) throws RemoteException {
        Person person = persons.get(passport);
        if (person == null) {
            return null;
        }
        if (type.equals(LocalPerson.class)) {
            Map<String, Account> map = new HashMap<>();
            personAccounts.get(person).forEach(value -> {
                try {
                    map.put(value.getId(), new RemoteAccount(value.getId(), value.getAmount()));
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
            person = new LocalPerson(person.getName(), person.getSurname(), passport, map);
        }
        return type.cast(person);
    }

    @Override
    public Person createPerson(final String name, final String surname, final String passport) throws RemoteException {
        if (persons.containsKey(passport)) {
            System.err.println("Your person " + passport + " already exists");
            return null;
        }
        final Person person = new RemotePerson(name, surname, passport);
        persons.put(passport, person);
        personAccounts.put(person, new ArrayList<>());
        UnicastRemoteObject.exportObject(person, port);
        return person;
    }
}
