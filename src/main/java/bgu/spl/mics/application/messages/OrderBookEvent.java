package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class OrderBookEvent implements Event {
    private int OrderTick;
    private String BookName;

    public OrderBookEvent(String BookName,int CurrentTime){
        this.BookName=BookName;
        this.OrderTick=CurrentTime;//tick when book was ordered
    }

    public String getBookName(){
        return BookName;
    }

    public int getOrderTick(){
        return OrderTick;
    }

}
