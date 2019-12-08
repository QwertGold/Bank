package groenbaek.bank.memory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class JournalEntry {

    private final Account account;
    private final String id;
    private final Instant entryTime;
    private final String text;
    private final double amount;
}
