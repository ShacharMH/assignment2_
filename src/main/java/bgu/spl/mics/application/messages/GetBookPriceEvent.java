package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class GetBookPriceEvent implements Event {

    private String bookName;

    public GetBookPriceEvent(String bookName) {
        this.bookName = bookName;
    }

    public String getBookName() {
        return bookName;
    }

}
