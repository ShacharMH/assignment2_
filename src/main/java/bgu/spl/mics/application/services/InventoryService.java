package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CheckAvailabilityEvent;
import bgu.spl.mics.application.passiveObjects.Inventory;

import bgu.spl.mics.application.passiveObjects.*;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store. // which is initialized by BookStoreRunner
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
//SHACHAR
	// 1. for each event/broadcast I subscribe to, I need to create a matching callback.
public class InventoryService extends MicroService{

	private Inventory inventory;

	public InventoryService() {
		super("InventoryService");
		inventory = Inventory.getInstance();
	}

	@Override
	/* Things to notice:
	1. the registration of the ms happens in the class MicroService which InventoryService extends.
	2. registration to the CheckAvailabilityEvent. that's it.
	 */
	protected void initialize() {

		subscribeEvent(CheckAvailabilityEvent.class, CheckAvailabilityCallback -> {

			String bookName = CheckAvailabilityCallback.getBookName();
			int price = inventory.checkAvailabiltyAndGetPrice(bookName);

			if (price != -1) { // there is a copy in stock!
				OrderResult orderResult = inventory.take(bookName); // we try to take it
				if (orderResult == OrderResult.SUCCESSFULLY_TAKEN)
					complete(CheckAvailabilityCallback, price); // we successfully took it!
				else // book not in stock anymore
					complete(CheckAvailabilityCallback,-1);
				}
		});
	}

}
