package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.concurrent.CountDownLatch;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link /ResourcesHolder}, {@link /MoneyRegister}, {@link /Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class LogisticsService extends MicroService {
	private int CurrentTime;
	private CountDownLatch countDownLatch;

	public LogisticsService(String name,CountDownLatch countDownLatch) {
		super(name);
		this.countDownLatch=countDownLatch;
	}

	@Override
	protected void initialize() {
		subscribeEvent(DeliveryEvent.class, DeliveryEventCallback -> {
			// we should just wait the amount of time it takes to deliver the book to the customer.
			String address = DeliveryEventCallback.getAddress();
			int distance = DeliveryEventCallback.getDistance();
			DeliveryEventCallback.getDeliveryVehicle().deliver(address,distance);
			complete(DeliveryEventCallback, true);
			//System.out.println(getName()+" completed a delivery");
		});

		subscribeBroadcast(TickBroadcast.class, TickBroadcastCallback -> {
			this.CurrentTime = TickBroadcastCallback.getCurrentTime();
			if (TickBroadcastCallback.getCurrentTime() == TickBroadcastCallback.getDuration()) {
				//System.out.println(getName()+" is being terminated");
				terminate();
			}
		});
		countDownLatch.countDown();
	}

}
