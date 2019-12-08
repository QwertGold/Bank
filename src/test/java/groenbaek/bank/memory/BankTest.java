package groenbaek.bank.memory;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static groenbaek.bank.memory.Account.PENNY_TOLERANCE;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

// Java 8 does not have orElseThrow() with no argument so get() is used without isPresent() check
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class BankTest {

    @Test
    public void addCustomer() {
        Bank b = new Bank();
        String cpr = "0123456789";
        b.addCustomer("Klaus", cpr);
        Optional<Customer> optional  = b.findCustomer(cpr);
        assertTrue(optional.isPresent());
        Customer customer = optional.get();
        assertEquals("Klaus", customer.getName());
        assertEquals("0123456789", customer.getCpr());

        customer.setName("Peter");
        assertEquals("Peter", customer.getName());

        // error scenarios
        assertFalse(b.findCustomer("1234567890").isPresent());

        Assertions.assertThatThrownBy(() -> b.addCustomer("Same CPR", cpr))
                .isExactlyInstanceOf(IllegalStateException.class);

        Assertions.assertThatThrownBy(() -> b.findCustomer("not a cpr"))
                .isExactlyInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void addAccount() {
        Bank b = new Bank();
        String cpr = "0123456789";
        b.addCustomer("Klaus", cpr);

        Customer customer = b.findCustomer(cpr).get();
        Account account = customer.addAccount();
        assertNotNull(account.getAccountNumber());
        Assertions.assertThat(account.getEntries()).hasSize(0);

        Account found = customer.findAccount(account.getAccountNumber()).get();
        assertEquals(account.getAccountNumber(), found.getAccountNumber());

        assertFalse(customer.findAccount("non existing account").isPresent());
    }

    @Test
    public void depositAndWithdraw() {
        Bank b = new Bank();
        String cpr = "0123456789";
        b.addCustomer("Klaus", cpr);

        Customer customer = b.findCustomer(cpr).get();
        Account account = customer.addAccount();

        account.deposit("first deposit", 100);
        account.withdraw("first withdrawal", 80);

        Assertions.assertThat(account.getEntries()).hasSize(2);
        JournalEntry firstEntry = account.getEntries().get(0);
        assertEquals(100, firstEntry.getAmount(), PENNY_TOLERANCE);
        assertEquals("first deposit", firstEntry.getText());
        assertNotNull(firstEntry.getId());

        JournalEntry secondEntry = account.getEntries().get(1);
        assertEquals(-80, secondEntry.getAmount(), PENNY_TOLERANCE);
        assertEquals("first withdrawal", secondEntry.getText());
        assertNotNull(secondEntry.getId());

        assertEquals(20, account.calculateBalance(), PENNY_TOLERANCE);
    }

    @Test
    public void illegalDepositAndWithdraws() {
        Bank b = new Bank();
        Account account = b.addCustomer("Klaus", "0123456789").addAccount();

        Assertions.assertThatThrownBy(() -> account.deposit(null, 5))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> account.deposit("text", -5))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> account.withdraw(null, 5))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> account.withdraw("text", -5))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testMoneyLaundering() throws InterruptedException {
        Instant start = Instant.now();
        // computers are so fast that we need to insert a 1ms sleep to ensure that we can measure that time passes
        Thread.sleep(1);

        Bank b = new Bank();
        Account klausAccount = b.addCustomer("Klaus", "0123456789").addAccount();
        Account peterAccount = b.addCustomer("peter", "0012345678").addAccount();

        klausAccount.deposit("below 10K", 100.0);
        klausAccount.deposit("exactly 10K", 10000.0);
        klausAccount.deposit("above 10K", 10000.01);
        klausAccount.withdraw("above 10K", 10010.0);

        peterAccount.deposit("below 10K", 50);

        Map<Customer, List<JournalEntry>> customerToEntry = b.findPotentialMoneyLaundryCustomers(Instant.now());
        Assertions.assertThat(customerToEntry).hasSize(1);
        List<JournalEntry> klausEntries = customerToEntry.get(klausAccount.getCustomer());
        Assertions.assertThat(klausEntries).hasSize(1);
        JournalEntry foundEntry = klausEntries.get(0);
        assertEquals("above 10K", foundEntry.getText());
        assertEquals(10000.01, foundEntry.getAmount(), PENNY_TOLERANCE);
        Assertions.assertThat(foundEntry.getEntryTime()).isAfter(start);
    }

}
