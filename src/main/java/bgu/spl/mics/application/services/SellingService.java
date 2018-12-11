package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.CheckAvailabilityEvent;
import bgu.spl.mics.application.messages.OrderBookEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;
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

	public SellingService(String name) {
		super(name);
		moneyRegister = MoneyRegister.getInstance();
		onGoingAcquireVehicleEvent = new LinkedList<>();
		onGoingCheckAvailabilityEventQueue = new LinkedList<>();
	}

	@Override
	protected void initialize() {

		subscribeEvent(OrderBookEvent.class, OrderBookEventCallback -> {
			OrderBookEventCallback.setProccessTick(CurrentTime);
			boolean orderVehicle;

			CheckAvailabilityEvent checkAvailabilityEvent = new CheckAvailabilityEvent(OrderBookEventCallback.getBookName());
			Future<Integer> checkAvailabilityEventFuture = sendEvent(checkAvailabilityEvent);

			if (checkAvailabilityEventFuture.isDone()) { // CheckAvailabilityEvent is done;
				OrderBookEventCallback.setBookPrice(CheckAvailabilityEventIsDone(OrderBookEventCallback.getCustomer(), checkAvailabilityEventFuture.get()));
				orderVehicle = OrderBookEventCallback.getBookPrice() >= 0;
				if (!orderVehicle)
					complete(OrderBookEventCallback, null);
				else { // need to order a vehicle.
					/* do:
					0. send an acquire vehicle event
					1. push AcquireVehicleEvent to onGoingAcquireVehicleEvent
					2. check for all elements in onGoingDeliveryEvent if they're done.
						a. if so - send to AcquireVehicleEventIsDone(...) - this function completes the OrderBookEvent.
					 */

					AcquireVehicleEvent acquireVehicleEvent = new AcquireVehicleEvent(OrderBookEventCallback.getCustomer().getAddress(), OrderBookEventCallback.getCustomer().getDistance());
					Future<Boolean> acquireVehicleEventFuture = sendEvent(acquireVehicleEvent);
					WaitingEvent acquireVechileWaitingEvent = new WaitingEvent(acquireVehicleEventFuture, acquireVehicleEvent,OrderBookEventCallback);
					onGoingAcquireVehicleEvent.add(acquireVechileWaitingEvent);
					CompleteIfAcquireVehicleEventIsDone();
				}
			} else { // CheckAvailabilityEvent !isDone();
				/* do:
				1. 1. push waiting CheckInventoryEvent to onGoingCheckAvailabilityEvent
				2. go through all elements in onGoingCheckAvailabilityEvent and send them with the customer to CheckIfDone
					a. if done and book is available:
						i. send AcquireVehicleEvent
						ii. push AcquireVehicleEvent to onGoingAcquireDeliveryEvent
				 */
				WaitingEvent checkAvailabilityWaitingEvent = new WaitingEvent(checkAvailabilityEventFuture,checkAvailabilityEvent,OrderBookEventCallback);
				onGoingCheckAvailabilityEventQueue.add(checkAvailabilityWaitingEvent);
				sendAcquireVehicleEventIfCheckAvailabilityEventIsDone();
			}

			CompleteIfAcquireVehicleEventIsDone();

		});

		subscribeBroadcast(TickBroadcast.class, TickBroadcastCallback -> {
			this.CurrentTime = TickBroadcastCallback.getCurrentTime();
			if (TickBroadcastCallback.getCurrentTime() == TickBroadcastCallback.getDuration()) terminate();
		});
		
	}

	private int CheckAvailabilityEventIsDone(Customer c, int bookPrice) {

		/* do:
		0. check if available or not.
			a. if not - send -1
		1. check if customer has enough money
			a. if so - reserve that money and return bookPrice;
			b. if not - send -1
		 */

		if (bookPrice == -1)
			return bookPrice;

		if (c.reserveAmount(bookPrice)) {
			moneyRegister.chargeCreditCard(c,bookPrice);
			return bookPrice;
		}

		return -1;
	}

	private void sendAcquireVehicleEventIfCheckAvailabilityEventIsDone() {
		/* do:
		1. go through onGoingCheckAvailabilityEventQueue
			a. if done - send to CheckAvailabilityEventIsDone to see if book is in stock and to reserve money if possible
			b. if the conditions above apply, send an AcquireVehicleEvent and push event to onGoingAcquireVehicleEvent.
		 */
		boolean orderVehicle;
		for (WaitingEvent tmp: onGoingCheckAvailabilityEventQueue) {
			OrderBookEvent orderBookEvent = tmp.getOrderBookEvent();
			if (tmp.getFuture().isDone()) {
				orderBookEvent.setBookPrice(CheckAvailabilityEventIsDone(orderBookEvent.getCustomer(), (int)tmp.getFuture().get()));
				orderVehicle = orderBookEvent.getBookPrice() >= 0;
				if (orderVehicle) {
					AcquireVehicleEvent acquireVehicleEvent = new AcquireVehicleEvent(tmp.getOrderBookEvent().getCustomer().getAddress(), tmp.getOrderBookEvent().getCustomer().getDistance());
					Future<Boolean> acquireVehicleEventFuture = sendEvent(acquireVehicleEvent);
					WaitingEvent acquireVehicleWaitingEvent = new WaitingEvent(acquireVehicleEventFuture,acquireVehicleEvent, orderBookEvent);
					onGoingAcquireVehicleEvent.add(acquireVehicleWaitingEvent);
				}
			}
		}
	}

	private void CompleteIfAcquireVehicleEventIsDone() {
		/* notice, that if isDone() == true, then the book was already delivered.
		do:
		1. create a receipt
		2. complete order book event.
		*/

		for (WaitingEvent tmp: onGoingAcquireVehicleEvent) {
			if (tmp.getFuture().isDone()) {
				OrderBookEvent orderBookEvent = tmp.getOrderBookEvent();
				OrderReceipt orderReceipt = new OrderReceipt(0,getName(),
															orderBookEvent.getCustomer().getId(),
															orderBookEvent.getBookName(),
															orderBookEvent.getBookPrice(),
															orderBookEvent.getOrderTick(),
															orderBookEvent.getProccessTick(),
															CurrentTime);
				complete(orderBookEvent,orderReceipt);
			}
		}
	}


	public static class WaitingEvent {

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
