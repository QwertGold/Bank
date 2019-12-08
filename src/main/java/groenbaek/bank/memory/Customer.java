package groenbaek.bank.memory;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class Customer {

    @GuardedBy("$lock")
    private static int nextAccountNumber = 1;

    @Setter
    private volatile String name;
    private final String cpr;
    @GuardedBy("$lock")
    private final List<Account> accounts = new ArrayList<>();

    public Customer(String name, String cpr) {
        this.name = name;
        this.cpr = cpr;
    }

    @Synchronized
    public Account addAccount() {
        String accountNumber = String.format("%09d", nextAccountNumber++);
        Account account = new Account(this, accountNumber);
        accounts.add(account);
        return account;
    }

    @Synchronized
    public Optional<Account> findAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return Optional.of(account);
            }
        }
        return Optional.empty();
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }
}
