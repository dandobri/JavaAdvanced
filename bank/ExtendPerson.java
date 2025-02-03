package info.kgeorgiy.ja.dobris.bank;

public class ExtendPerson implements Person {
    private final String name;
    private final String surname;
    private final String passport;
    public ExtendPerson(String name, String surname, String passport) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassportId() {
        return passport;
    }
}
