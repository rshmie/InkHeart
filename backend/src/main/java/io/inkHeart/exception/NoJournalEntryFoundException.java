package io.inkHeart.exception;

public class NoJournalEntryFoundException extends RuntimeException{
    public NoJournalEntryFoundException(Long id) {
        super("No journal entry found for the id - " + id);
    }

}
