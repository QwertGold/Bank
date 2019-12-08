package groenbaek.bank.memory;

import com.google.common.base.Strings;
import com.google.common.math.DoubleMath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;

import javax.annotation.concurrent.GuardedBy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Account {

    public static final double PENNY_TOLERANCE = 0.005;
    private final Customer customer;
    private final String accountNumber;
    @GuardedBy("$lock")
    private List<JournalEntry> entries = new ArrayList<>();

    @Synchronized
    public List<JournalEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public void deposit(String text, double amount) {
        checkText(text);
        checkAmount(amount, "You cant deposit negative amounts.");
        addJournalEntry(Instant.now(), text, amount);
    }

    public void withdraw(String text, double amount) {
        checkText(text);
        checkAmount(amount, "You cant withdraw negative amounts.");
        addJournalEntry(Instant.now(), text, -amount);
    }

    public double calculateBalance() {
        return getEntries().stream().mapToDouble(JournalEntry::getAmount).sum();
    }

    @Synchronized
    private void addJournalEntry(Instant entryTime, String title, double amount) {
        if (DoubleMath.fuzzyEquals(amount, 0, 0.005)){
            throw new IllegalArgumentException("Journal entries have to have an amount larger than one cent.");
        }
        String id = getNextJournalId();
        entries.add(new JournalEntry(this, id, entryTime, title, amount));
    }

    private String getNextJournalId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void checkAmount(double amount, String errorMessage) {
        if (DoubleMath.fuzzyCompare(amount, 0, PENNY_TOLERANCE) < 0){
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void checkText(String text) {
        if (Strings.isNullOrEmpty(text)) {
            throw new IllegalArgumentException("text is required");
        }
    }

}
