package info.kgeorgiy.ja.dobris.bank;
import java.io.Serializable;
import java.util.Map;

public class LocalPerson extends ExtendPerson implements Serializable {

    private final Map<String, Account> localAccountList;

    public LocalPerson(String name, String surname, String passport, Map<String, Account> accountList) {
        super(name, surname, passport);
        this.localAccountList = accountList;
    }

    /**
     * Method which getting localAccountList;
     * @return localAccountList
     */
    public Map<String, Account> getLocalAccountList() {
        return localAccountList;
    }

    /**
     * Method which getting element in localAccountList by id
     * @param id
     * @return account in localAccountList by id
     */
    public Account getLocalAccount(String id) {
        return localAccountList.get(id);
    }
}
