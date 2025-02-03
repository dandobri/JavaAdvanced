package info.kgeorgiy.ja.dobris.bank;

import java.rmi.*;

public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money in the account. */
    double getAmount() throws RemoteException;

    /** Sets amount of money in the account. */
    void setAmount(double amount) throws RemoteException;
}
