package bgu.spl.mics.application.passiveObjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InventoryTest {

    private BookInventoryInfo[] bookInventoryInfos;
    private Inventory inventory;
    private String santana = "Santana";
    private String queen = "Queen";

    private Thread thread1;
    private Thread thread2;
    private Thread thread3;

    @Before
    public void setUp() throws Exception {
        inventory = Inventory.getInstance();
        //initializing the books array for the inventory testing
        String Santana = this.santana;
        int santanaAmount = 1;
        int santanaPrice = 10;
        String Queen = this.queen;
        int queenAmount = 2;
        int queenPrice = 5;
        BookInventoryInfo santana = new BookInventoryInfo(Santana,santanaPrice,santanaAmount);
        BookInventoryInfo queen = new BookInventoryInfo(Queen,queenPrice,queenAmount);
        bookInventoryInfos = new BookInventoryInfo[]{santana,queen};

        inventory.load(bookInventoryInfos);
    }

    @After
    public void tearDown() throws Exception {
        bookInventoryInfos = null;
        inventory = null;
        thread3 = null;
        thread2 = null;
        thread1 = null;
    }

    @Test
    public void getInstance() {
        // I already called _getInstance_ in the class setUp.
        assertNotNull(inventory);

    }

    @Test
    public void load() {
        inventory.load(bookInventoryInfos);
        assertEquals(5, inventory.checkAvailabiltyAndGetPrice(this.queen));
        assertEquals(10, inventory.checkAvailabiltyAndGetPrice(this.santana));
    }

    @Test
    public void take() {
        assertEquals(OrderResult.SUCCESSFULLY_TAKEN, inventory.take(this.queen));
        assertEquals(OrderResult.SUCCESSFULLY_TAKEN, inventory.take(this.santana));
        assertEquals(OrderResult.NOT_IN_STOCK, inventory.take(this.santana));
    }

    @Test
    public void checkAvailabiltyAndGetPrice() {
        inventory.take(this.santana);
        inventory.take(this.queen);
        assertEquals(-1,inventory.checkAvailabiltyAndGetPrice(this.santana));
        assertEquals(5, inventory.checkAvailabiltyAndGetPrice(this.queen));

        inventory.take(this.queen);
        assertEquals(-1, inventory.checkAvailabiltyAndGetPrice(this.queen));

    }

    @Test
    public void printInventoryToFile() {
        inventory.printInventoryToFile("testPrintInventoryToFile");
        // I manually checked the that it is printed to the file correctly.

    }

    @Test
    public void take2Threads() {

        Runnable runnable1 = () -> {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            System.out.println("thread 1 runs quite fine");
            System.out.println("thread 1 says: I tried to take Santa: " + inventory.take(this.santana));
            System.out.println("thread 1 says: I tried to take Queen: " + inventory.take(this.queen));
        };
        Runnable runnable2 = () -> {
            System.out.println("thread 2 runs nice too");
            System.out.println("thread 2 says: I tried to take Santa: " + inventory.take(this.santana));
            System.out.println("thread 2 says: I tried to take Queen: " + inventory.take(this.queen));
        };
        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(-1,inventory.checkAvailabiltyAndGetPrice(this.santana));
        assertEquals(-1, inventory.checkAvailabiltyAndGetPrice(this.queen));
    }


    @Test
    public void checkAvailabiltyAndGetPrice2Threads() {
        Runnable runnable1 = () -> {
            System.out.println(inventory.checkAvailabiltyAndGetPrice(this.santana));
            System.out.println(inventory.take(this.santana));
        };


        Runnable runnable2 = () -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(inventory.checkAvailabiltyAndGetPrice(this.santana));
        };

        Runnable runnable3 =  () ->
                System.out.println(inventory.take(this.santana));

        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(-1, inventory.checkAvailabiltyAndGetPrice(this.santana));
    }

}