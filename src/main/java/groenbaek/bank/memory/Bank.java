package groenbaek.bank.memory;

import com.google.common.math.DoubleMath;
import lombok.Synchronized;

import javax.annotation.concurrent.GuardedBy;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static groenbaek.bank.memory.Account.*;

public class Bank {
    private static Pattern cprPattern = Pattern.compile("\\d{10}");
    private static double MONEY_LAUNDERING_LIMIT = 10000;

    @GuardedBy("$lock")
    private final Map<String, Customer> cprToCustomer = new HashMap<>();

    @Synchronized
    public Customer addCustomer(String name, String cpr) {
        validateCPR(cpr);
        if (cprToCustomer.containsKey(cpr)) {
            throw new IllegalStateException("Customer already exist.");
        }
        Customer customer = new Customer(name, cpr);
        cprToCustomer.put(cpr, customer);
        return customer;
    }

    @Synchronized
    public Optional<Customer> findCustomer(String cpr) {
        validateCPR(cpr);
        return Optional.ofNullable(cprToCustomer.get(cpr));
    }

    /**
     * Find all deposits above 10K since the given data, these entries needs to be checked for potential money laundering  activity
     */
    @Synchronized
    public Map<Customer, List<JournalEntry>> findPotentialMoneyLaundryCustomers(Instant since) {
        return cprToCustomer.values().stream()
                .flatMap(customer->customer.getAccounts().stream())
                .flatMap(account->account.getEntries().stream())
                .filter(journalEntry -> !journalEntry.getEntryTime().isBefore(since) )
                .filter(journalEntry-> DoubleMath.fuzzyCompare(journalEntry.getAmount(), MONEY_LAUNDERING_LIMIT, PENNY_TOLERANCE) > 0)
                .collect(Collectors.groupingBy(journalEntry -> journalEntry.getAccount().getCustomer()));
    }

    private void validateCPR(String cpr) {
        if (!cprPattern.matcher(cpr).matches()) {
            throw new IllegalArgumentException(cpr + " is not a valid CPR number.");
        }
    }

}
