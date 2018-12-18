package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CheckAvailabilityEvent;
import bgu.spl.mics.application.messages.GetBookPriceEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
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
	private int CurrentTime;

	public InventoryService(String name) {
		super(name);
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
			OrderResult orderResult = inventory.take(bookName);

			if (orderResult == OrderResult.SUCCESSFULLY_TAKEN && price != -1) {
				complete(CheckAvailabilityCallback, price);
				System.out.println(getName()+" finished CheckInventoryEvent regarding book " + bookName+ " with result "+ price);
			} else {
				complete(CheckAvailabilityCallback, -1);
				System.out.println(getName()+" finished CheckInventoryEvent regarding book " + bookName+ " with result -1");
			}
		});

		subscribeEvent(GetBookPriceEvent.class, getBookPriceEventCallback -> {
			// this event returns -1 if book is not in stock, else returns its price
			int result = inventory.checkAvailabiltyAndGetPrice(getBookPriceEventCallback.getBookName());
			complete(getBookPriceEventCallback, result);
			System.out.println("**************** "+getName()+" finished a GetBookPriceEvent for book " + getBookPriceEventCallback.getBookName() + " with result: "+ result);
		});

		subscribeBroadcast(TickBroadcast.class, TickBroadcastCallback -> {
			this.CurrentTime = TickBroadcastCallback.getCurrentTime();
			if (TickBroadcastCallback.getCurrentTime() == TickBroadcastCallback.getDuration()) {
				System.out.println(getName()+" is being terminated");
				terminate();
			}
		});
	}

}
