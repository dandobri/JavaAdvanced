package info.kgeorgiy.ja.dobris.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    <T extends Person> Account getAccount(final T person, final String id) throws RemoteException;

    /**
     * Method which getting person
     * @param passport id of passport
     * @param type type which we cast the result of getting
     * @return person, or null if it is not exists
     * @param <T> type which inherited from Person
     * @throws RemoteException exception if the getting was false
     */
    <T extends Person> T getPerson(final String passport, Class<T> type) throws RemoteException;

    /**
     * Method which creates person
     * @param name name of person
     * @param surname surnmae of person
     * @param passport passportId of person
     * @return create the person if the person is not exists or return already created person
     * @throws RemoteException exception if was error
     */
    Person createPerson(final String name, final String surname, final String passport) throws RemoteException;
}
