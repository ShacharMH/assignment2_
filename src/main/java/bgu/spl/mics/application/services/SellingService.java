package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.*;
import java.util.LinkedList;
import java.util.Queue;

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
	private Queue<WaitingEvent> onGoingCheckAvailabilityEventQueue;
	private Queue<WaitingEvent> onGoingAcquireVehicleEvent;
	private Queue<WaitingEvent> onGoingGetBookPriceEvent;

	public SellingService(String name) {
		super(name);
		moneyRegister = MoneyRegister.getInstance();
		onGoingAcquireVehicleEvent = new LinkedList<>();
		onGoingCheckAvailabilityEventQueue = new LinkedList<>();
		onGoingGetBookPriceEvent = new LinkedList<>();
	}

	@Override
	protected void initialize() {

		subscribeEvent(OrderBookEvent.class, OrderBookEventCallback -> {

			OrderBookEventCallback.setProccessTick(CurrentTime);

			GetBookPriceEvent getBookPriceEvent = new GetBookPriceEvent(OrderBookEventCallback.getBookName());
			Future<Integer> getBookPriceEventFuture = sendEvent(getBookPriceEvent);
			System.out.println(getName() + " sent a GetBookPriceEvent!");
			WaitingEvent getBookPriceWaitingEvent = new WaitingEvent(getBookPriceEventFuture, getBookPriceEvent, OrderBookEventCallback);
			onGoingGetBookPriceEvent.add(getBookPriceWaitingEvent);

			CheckIfGetBookPriceEventIsDoneAndSendCheckAvailabilityEventIfConditionsApply();
			CheckIfCheckAvailabilityEventIsDoneAndAcquireVehicleIfConditionsApply();
			CheckIfAcquireVehicleEventIsDoneAndCompleteOrderBookEventIfConditionsApply();

		});

		subscribeBroadcast(TickBroadcast.class, TickBroadcastCallback -> {
			this.CurrentTime = TickBroadcastCallback.getCurrentTime();
			if (TickBroadcastCallback.getCurrentTime() == TickBroadcastCallback.getDuration()) terminate();
		});
	}

	/* do:
        2. for each element in Queue:
            a. if customer has enough money & result =! -1 -> reserve amount and call to CheckAvailabilityEvent + push to queue
            b. if result == -1 -> complete orderBookEvent with null.
            c. if customer's creditAmount+reservedAmount >= book.getPrice(), then keep it in loop until termination
               (because maybe reserved amount will be released).
         */
	private void CheckIfGetBookPriceEventIsDoneAndSendCheckAvailabilityEventIfConditionsApply() {

		for (WaitingEvent waitingEvent: onGoingGetBookPriceEvent) {
			System.out.println(getName()+" iterates over onGoingGetBookPriceEvent");

			if (waitingEvent.getFuture().isDone()) {
				int price = (int)waitingEvent.future.get();
				Customer customer = waitingEvent.getOrderBookEvent().getCustomer();
				int availableAmount = customer.getAvailableCreditAmount();
				String bookName = waitingEvent.getOrderBookEvent().getBookName();

				if (price != -1 && availableAmount >= price) {
					waitingEvent.getOrderBookEvent().setBookPrice(price); // setting book price
					customer.reserveAmount(price);
					// Here I double check that that credit amount left is not negative:
					availableAmount = customer.getAvailableCreditAmount();
					if (availableAmount < 0) {
						customer.releaseAmount(price);
						complete(waitingEvent.getOrderBookEvent(), null);
						System.out.println(getName() + " finished OrderBookEvent with null :(");
						onGoingGetBookPriceEvent.remove(waitingEvent);
					}
					CheckAvailabilityEvent checkAvailabilityEvent = new CheckAvailabilityEvent(bookName);
					Future<Integer> checkAvailabilityEventFuture = sendEvent(checkAvailabilityEvent);
					System.out.println(getName() + " sent a CheckAvailabilityEvent for book: " + bookName);
					onGoingGetBookPriceEvent.remove(waitingEvent);
					WaitingEvent availabilityWaitingEvent = new WaitingEvent(checkAvailabilityEventFuture,checkAvailabilityEvent,waitingEvent.getOrderBookEvent());
					onGoingCheckAvailabilityEventQueue.add(availabilityWaitingEvent);
					System.out.println(getName()+" added a new availabilityWaitingEvent!");
				}
				if (price == -1 || customer.getAvailableCreditAmount()+customer.getReservedAmount() < price) {
					complete(waitingEvent.orderBookEvent, null);
					onGoingGetBookPriceEvent.remove(waitingEvent);
				}
			}
		}
	}

	/* for each element on onGoingCheckAvailabilityEvent:
	1. if isDone()
		a. check if future.get() != -1. if it does' it means that the book is on stock. if so-
			i. pay
			ii. order a vehicle.
		b. if not -
			i. release amount
			ii. complete order book event with null.
	 */
	private void CheckIfCheckAvailabilityEventIsDoneAndAcquireVehicleIfConditionsApply() {
		//boolean atLeastOneEnded = false;
		//while(!atLeastOneEnded) {
			for (WaitingEvent waitingEvent : onGoingCheckAvailabilityEventQueue) {

				System.out.println(getName() + " iterates over onGoingCheckAvailabilityEventQueue ");
				System.out.println("is the WaitingEvent we are  currently working on done? " + waitingEvent.getFuture().isDone());


				Customer customer = waitingEvent.getOrderBookEvent().getCustomer();
				int bookPrice = waitingEvent.getOrderBookEvent().getBookPrice();
				String bookName = waitingEvent.getOrderBookEvent().getBookName();
				String address = customer.getAddress();
				int distance = customer.getDistance();

				if (waitingEvent.getFuture().isDone()) {
					//atLeastOneEnded = true;
					System.out.println(getName() + ": the Future object of CheckAvailabilityEvent of book " + bookName + " is done!");
					if ((int) waitingEvent.getFuture().get() == -1) { // so book is not in stock
						customer.releaseAmount(bookPrice);
						complete(waitingEvent.getOrderBookEvent(), null);
						System.out.println(getName() + " just finished an OrderBookEvent with null :(");
						onGoingCheckAvailabilityEventQueue.remove(waitingEvent);
					} else { // book is in stock!
						moneyRegister.chargeCreditCard(customer, bookPrice);
						System.out.println(getName() + " just charged the credit card of " + customer.getName() + " for book " + bookName);
						onGoingCheckAvailabilityEventQueue.remove(waitingEvent);
						AcquireVehicleEvent acquireVehicleEvent = new AcquireVehicleEvent(address, distance);
						Future<Boolean> acquireVehicleEventFuture = sendEvent(acquireVehicleEvent);
						System.out.println(getName() + " sent an AcquireVehicleEvent!");
						WaitingEvent acquireVehicleWaitingEvent = new WaitingEvent(acquireVehicleEventFuture, acquireVehicleEvent, waitingEvent.getOrderBookEvent());
						onGoingAcquireVehicleEvent.add(acquireVehicleWaitingEvent);
					}
				}
			}
		//}
	}

	/* do:
	1. for each element in onGoingAcquireVehicleEvent:
		a. check if done. if so:
			i. remove from queue
			ii. create a receipt in another function
			iii. call complete on waitingEvent.GetOrderBookEvent();
	 */
	private void CheckIfAcquireVehicleEventIsDoneAndCompleteOrderBookEventIfConditionsApply() {


		for(WaitingEvent waitingEvent: onGoingAcquireVehicleEvent) {

			System.out.println(getName() + " iterates over onGoingAcquireVehicleEvent ");

			if (waitingEvent.getFuture().isDone()) {
				if ((Boolean)waitingEvent.getFuture().get() != true)
					throw new RuntimeException("AcquireVehicleEvent returned false. what on earth??!!");
				onGoingAcquireVehicleEvent.remove(waitingEvent);
				OrderReceipt orderReceipt = createAndFileReceipt(waitingEvent);
				System.out.println(getName() + " created a Receipt!");
				complete(waitingEvent.getOrderBookEvent(), orderReceipt);
				System.out.println(getName() + " *********completed a OrderBookEvent successfully!*************");
			}
		}
	}

	private OrderReceipt createAndFileReceipt(WaitingEvent waitingEvent) {
		Customer customer = waitingEvent.getOrderBookEvent().getCustomer();
		String bookName = waitingEvent.getOrderBookEvent().getBookName();
		int price = waitingEvent.getOrderBookEvent().getBookPrice();

		OrderReceipt orderReceipt = new OrderReceipt((CurrentTime*customer.getId())/17,getName(),customer.getId(),
													bookName,price,waitingEvent.getOrderBookEvent().getOrderTick(),
													waitingEvent.getOrderBookEvent().getProccessTick(),CurrentTime);
		moneyRegister.file(orderReceipt);
		return orderReceipt;
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
