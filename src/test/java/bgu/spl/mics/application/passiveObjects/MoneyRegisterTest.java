package bgu.spl.mics.application.passiveObjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
//notice, that checking if a customer has enough to purchase is done by sellingService.
public class MoneyRegisterTest {

    @Before
    public void setUp() throws Exception {
        OrderReceipt order1a = new OrderReceipt(1, "seller1", 123456, "Santana", 10, -1, -1, -1);
        OrderReceipt order1b = new OrderReceipt(2, "seller1", 123456, "Santana", 10, -1, -1, -1);
        OrderReceipt order1c = new OrderReceipt(3, "seller1", 123456, "Santana", 10, -1, -1, -1);


/*
        Customer Slumberwhich = new Customer("Jona Hill", 123456, "probably somewhere in Hollywood",
                                        5, 20, 12345, orderRceiptslistHavesomescoth);
  */
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getInstance() {
    }

    @Test
    public void file() {
    }

    @Test
    public void getTotalEarnings() {
    }

    @Test
    public void chargeCreditCard() {
    }

    @Test
    public void printOrderReceipts() {
    }
}