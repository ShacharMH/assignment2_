package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class CheckAvailabilityEvent implements Event {

    private String bookName;

    public CheckAvailabilityEvent(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }
}
