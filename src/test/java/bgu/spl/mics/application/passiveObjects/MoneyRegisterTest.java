package bgu.spl.mics.application.passiveObjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
public class MoneyRegisterTest {

    MoneyRegister instance;
    OrderReceipt order1a;
    OrderReceipt order1b;
    OrderReceipt order1c;
    OrderReceipt order1d;
    Customer SlumberWhich_HaveSomeStuff;

    @Before
    public void setUp() throws Exception {
        order1a = new OrderReceipt(1, "seller1", 123456, "book1", 10, -1, -1, -1);
        order1b = new OrderReceipt(2, "seller1", 123456, "book2", 10, -1, -1, -1);
        order1c = new OrderReceipt(3, "seller1", 123456, "book3", 10, -1, -1, -1);
        order1d = new OrderReceipt(4, "seller1", 123456, "book3", 20, -1, -1, -1);


        List<OrderReceipt> orderReceipts = new ArrayList<>();
        orderReceipts.add(order1a);
        orderReceipts.add(order1b);
        orderReceipts.add(order1c);
        SlumberWhich_HaveSomeStuff = new Customer("SlumberWhich HaveSomeStuff", 123456, "somewhere", 3, 20, 123, orderReceipts);

        instance = MoneyRegister.getInstance();
    }

    @After
    public void tearDown() throws Exception {

        instance.total = 0;
        instance.issuedReceipts.clear();
        instance = null;

        order1a = null;
        order1b = null;
        order1c = null;

        SlumberWhich_HaveSomeStuff = null;
    }


    // for some reason it doesn't pass :(
    @Test
    public void chargeCreditCard() {

        assertEquals(0, instance.total);
        instance.chargeCreditCard(SlumberWhich_HaveSomeStuff, order1a.getPrice());
        assertTrue(SlumberWhich_HaveSomeStuff.getAvailableCreditAmount() == 10);
        assertEquals(10, instance.getTotalEarnings());
        instance.chargeCreditCard(SlumberWhich_HaveSomeStuff, order1a.getPrice());
        assertEquals(0, SlumberWhich_HaveSomeStuff.getAvailableCreditAmount());
        assertEquals(20,instance.getTotalEarnings());
        try {
            instance.chargeCreditCard(SlumberWhich_HaveSomeStuff, order1c.getPrice());
        } catch (Exception e) {
            System.out.println("Exception was successfully thrown");
        }
    }

    @Test
    public void getInstance() {

        assertTrue(instance!=null);
    }

    /*
    @Test
    public void file() {

        instance.file(order1a);
        assertFalse(instance.issuedReceipts.isEmpty());
        instance.file(order1b);
        assertEquals(instance.issuedReceipts.size(),2);

    }
    */


    @Test
    public void getTotalEarnings() {
        assertTrue(instance.getTotalEarnings() == 0);
        instance.chargeCreditCard(SlumberWhich_HaveSomeStuff, 10);
        assertEquals(instance.getTotalEarnings(), 10);
    }



    @Test
    public void printOrderReceipts() {

        instance.file(order1a);
        instance.file(order1b);
        instance.file(order1c);
        instance.printOrderReceipts("test for printing receipts");
    }

    /* now, we'll check if MoneyRegister is thread-safe */
    /*
    @Test
    public void file_concurrent() {
        instance.issuedReceipts.clear();
        Runnable runnable1 = () ->
                instance.file(order1c);
        Runnable runnable2 = () ->
                instance.file(order1b);

        Thread t1 = new Thread(runnable1);
        Thread t2 = new Thread(runnable2);
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }

        assertEquals(2, instance.issuedReceipts.size());
    }
    */

    /*
    @Test
    public void file_concurrentSameOrderId() {
        instance.issuedReceipts.clear();
        Runnable runnable1 = () ->
                instance.file(order1b);
        Runnable runnable2 = () ->
                instance.file(order1b);

        Thread t1 = new Thread(runnable1);
        Thread t2 = new Thread(runnable2);

        try {
            t1.start();
            t2.start();
        } catch (Exception e) {
            System.out.println("Exception successfully thrown");
        }

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }

        assertEquals(1, instance.issuedReceipts.size());

    }
    */

    @Test
    public void getTotalEarnings_concurrent() {
        Runnable runnable1 = () ->
                instance.chargeCreditCard(SlumberWhich_HaveSomeStuff,10);
        Runnable runnable2 = () -> {
            System.out.println("first time checking total earnings: " + instance.getTotalEarnings());
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("second time checking total earnings: " + instance.getTotalEarnings());
        };
        Thread t1 = new Thread(runnable1);
        Thread t2 = new Thread(runnable2);
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }

        assertEquals(10, instance.getTotalEarnings());
    }

    /*
    @Test
    public void chargeCreditCard_concurrent() {
        instance.issuedReceipts.clear();
        Runnable runnable1 = () -> {
            instance.chargeCreditCard(SlumberWhich_HaveSomeStuff, 20);
            instance.file(order1d);
        };

        Runnable runnable2 = () -> {
            instance.chargeCreditCard(SlumberWhich_HaveSomeStuff, 10);
            instance.file(order1b);
        };

        Thread t1 = new Thread(runnable1);
        Thread t2 = new Thread(runnable2);
        try {
            t1.start();
            t2.start();
        } catch (Exception e) {
            System.out.println("Exception successfully thrown");
        }

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }

        assertTrue((20 == instance.getTotalEarnings()) || (10 == instance.getTotalEarnings()));
        assertEquals(1, instance.issuedReceipts.size());
    }
    */

}