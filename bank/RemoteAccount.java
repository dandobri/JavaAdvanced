package info.kgeorgiy.ja.dobris.bank;

public class RemoteAccount implements Account {
    private final String id;
    private double amount;

    public RemoteAccount(final String id, double amount) {
        this.id = id;
        this.amount = amount;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized double getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final double amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
