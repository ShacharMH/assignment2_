package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import org.junit.Before;

import static org.junit.Assert.*;

public class InventoryTest {
    private Inventory inv;

    @Before
    public void setUp() throws Exception {
        this.inv = createInventory();
    }

    private Future<Integer> createInventory() {
        return Inventory.getInstance();
    }




}