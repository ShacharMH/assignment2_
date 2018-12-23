package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.*;
import jdk.nashorn.internal.ir.BlockLexicalContext;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link //MoneyRegister} singleton of the store.
 * Handles {@link //BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link //ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{

	private MoneyRegister moneyRegister;
	private int CurrentTime;
	private CountDownLatch countDownLatch;
	int nextReceiptId = 0;
	private Queue<WaitingEvent> onGoingCheckAvailabilityEventQueue;
	private Queue<WaitingEvent> onGoingAcquireVehicleEvent;
	private Queue<WaitingEvent> onGoingGetBookPriceEvent;

	public SellingService(String name,CountDownLatch countDownLatch) {
		super(name);
		moneyRegister = MoneyRegister.getInstance();
		onGoingAcquireVehicleEvent = new LinkedList<>();
		onGoingCheckAvailabilityEventQueue = new LinkedList<>();
		onGoingGetBookPriceEvent = new LinkedList<>();
		this.countDownLatch=countDownLatch;
	}

	@Override
	protected void initialize() {

		subscribeEvent(OrderBookEvent.class, OrderBookEventCallback -> {
			OrderBookEventCallback.setProccessTick(CurrentTime);
			GetBookPriceEvent getBookPriceEvent = new GetBookPriceEvent(OrderBookEventCallback.getBookName());
			Future<Integer> getBookPriceEventFuture = sendEvent(getBookPriceEvent);
			while(!getBookPriceEventFuture.isDone());
			int price = getBookPriceEventFuture.get();
			if (price == -1) { // book is not available
				System.out.println(getName() + " completed an OrderBookEvent regarding book " + OrderBookEventCallback.getBookName()+" with null because book is not available");
				complete(OrderBookEventCallback, null);
			}
			else { // book is available
				if (OrderBookEventCallback.getCustomer().reserveAmount(price)) { // customer has enough money
					CheckAvailabilityEvent checkAvailabilityEvent = new CheckAvailabilityEvent(OrderBookEventCallback.getBookName());
					Future<Integer> checkAvailabilityEventFuture = sendEvent(checkAvailabilityEvent);
					System.out.println(getName()+" sent a CheckAvailabilityEvent");
					while (!checkAvailabilityEventFuture.isDone()) ;
					System.out.println(getName()+" says: checkAvailabilityEvent is done");
					boolean isAvailable = checkAvailabilityEventFuture.get() >= 0;
					System.out.println(getName()+" isAvailable?: " + isAvailable);
					if (isAvailable) { // book is in stock & customer has enough money
						moneyRegister.chargeCreditCard(OrderBookEventCallback.getCustomer(), price);
						AcquireVehicleEvent acquireVehicleEvent = new AcquireVehicleEvent(OrderBookEventCallback.getCustomer().getAddress(), OrderBookEventCallback.getCustomer().getDistance());
						System.out.println(getName() + " ordered a vehicle for book " + OrderBookEventCallback.getBookName());
						Future<DeliveryVehicle> acquireVehicleEventFuture = sendEvent(acquireVehicleEvent);
						while (!acquireVehicleEventFuture.isDone());
						System.out.println(getName()+" acquireVehicleEventFuture is done");
						DeliveryVehicle deliveryVehicle = acquireVehicleEventFuture.get();
						DeliveryEvent deliveryEvent = new DeliveryEvent(deliveryVehicle,OrderBookEventCallback.getCustomer().getAddress(),OrderBookEventCallback.getCustomer().getDistance());
						Future<Boolean> deliveryEventFuture = sendEvent(deliveryEvent);
						while (!deliveryEventFuture.isDone());
						ReleaseVehicleEvent releaseVehicleEvent = new ReleaseVehicleEvent(deliveryVehicle);
						sendEvent(releaseVehicleEvent);
						OrderReceipt orderReceipt = new OrderReceipt(OrderBookEventCallback.getReceiptId(), getName(), OrderBookEventCallback.getCustomer().getId(), OrderBookEventCallback.getBookName(), price, OrderBookEventCallback.getOrderTick(), OrderBookEventCallback.getProccessTick(), 0);
						moneyRegister.file(orderReceipt);
						System.out.println(getName() + " completed an OrderBookEvent regarding book " + OrderBookEventCallback.getBookName()+" with a purchase!");
						complete(OrderBookEventCallback, orderReceipt);
					} else { // book not is stock
						OrderBookEventCallback.getCustomer().releaseAmount(price);
						complete(OrderBookEventCallback, null);
						System.out.println(getName() + " completed an OrderBookEvent regarding book " + OrderBookEventCallback.getBookName()+" with null because book is not available");
					}
				} else { // customer doesn't have enough money
					complete(OrderBookEventCallback, null);
					System.out.println(getName()+" completed an OrderBookEvent with null because customer didn't have enough money");
				}
			}
		});

		subscribeBroadcast(TickBroadcast.class, TickBroadcastCallback -> {
			this.CurrentTime = TickBroadcastCallback.getCurrentTime();
			if (TickBroadcastCallback.getCurrentTime() == TickBroadcastCallback.getDuration()) {
				System.out.println(getName()+" is being terminated");
				terminate();
			}
		});
		countDownLatch.countDown();
	}




	/* just a simple class to hold the future object, the event and the bookOrderEvent together. */
	private static class WaitingEvent {

		private Future future;
		private Event event;
		private OrderBookEvent orderBookEvent;


		public WaitingEvent(Future future, Event event, OrderBookEvent orderBookEvent) {
			this.future = future;
			this.event = event;
			this.orderBookEvent = orderBookEvent;
		}

		public Event getEvent() {
			return event;
		}

		public Future getFuture() {
			return future;
		}

		public OrderBookEvent getOrderBookEvent() {
			return orderBookEvent;
		}
	}


}
