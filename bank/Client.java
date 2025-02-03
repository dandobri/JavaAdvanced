package info.kgeorgiy.ja.dobris.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {
    /** Utility class. */
    private Client() {}

    /**
     * Main method which start the bank and creating one person with account
     * @param args 5 args of command line name, surname, passportId, accountId, amount
     * @throws RemoteException exception if error
     */
    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost:8088/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        if (args == null || args.length != 5) {
            System.err.println("Your input must exists 5 arguments: name, surname, passportId, accountId, amount");
            return;
        }

        try {
            final String name = args[0];
            final String surname = args[1];
            final String passportId = args[2];
            final String accountId = args[3];
            final double amount = Double.parseDouble(args[4]);
            Person person = bank.getPerson(passportId, RemotePerson.class);
            if(person == null) {
                person = bank.createPerson(name, surname, passportId);
            } else {
                if(!(person.getName().equals(name) && person.getSurname().equals(surname))) {
                    System.err.println("Name and Surname do not match");
                    return;
                }
            }
            Account account = bank.getAccount(person, person.getPassportId() + ":" + accountId);
            if (account == null) {
                System.out.println("Creating account");
                account = bank.createAccount(accountId);
            } else {
                System.out.println("Account already exists");
            }
            System.out.println("Account id: " + account.getId());
            System.out.println("Money: " + account.getAmount());
            System.out.println("Adding money");
            account.setAmount(account.getAmount() + amount);
            System.out.println("Money: " + account.getAmount());
        } catch (NumberFormatException e) {
            System.err.println("Your passportId and amount must be integer");
        }
    }
}
