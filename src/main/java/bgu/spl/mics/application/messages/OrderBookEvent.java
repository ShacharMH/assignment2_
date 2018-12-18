package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;

public class OrderBookEvent implements Event {
    private int OrderTick;
    private int proccessTick = -1;
    private String BookName;
    private Customer c;
    int bookPrice = -2;
    int receiptId;

    public OrderBookEvent(String BookName,int CurrentTime, Customer c, int receiptId){
        this.BookName=BookName;
        this.OrderTick=CurrentTime;//tick when book was ordered
        this.c = c;
        this.receiptId = receiptId;
    }

    public String getBookName(){
        return BookName;
    }

    public int getOrderTick(){
        return OrderTick;
    }

    public Customer getCustomer() {
        return c;
    }

    public void setProccessTick(int proccessTick) {
        this.proccessTick = proccessTick;
    }

    public int getProccessTick() {
        return proccessTick;
    }

    public void setBookPrice(int bookPrice) {
        this.bookPrice = bookPrice;
    }

    public int getBookPrice() {
        return bookPrice;
    }

    public int getReceiptId() {
        return receiptId;
    }
}
