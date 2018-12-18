package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link //ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link //MoneyRegister}, {@link //Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

/* IS RESOURCE-HOLDER THREAD SAFE?
deals with getting the car to deliver the book.
 */
public class ResourceService extends MicroService{

	private ResourcesHolder resourcesHolder;
	//private Queue<WaitingDelivery> onGoingDeliveryQueue;
	private int CurrentTime;
	public ResourceService(String name) {
		super(name);
		resourcesHolder = ResourcesHolder.getInstance();
		//onGoingDeliveryQueue = new LinkedList<>();
	}


	@Override
	protected void initialize() {


		subscribeBroadcast(TickBroadcast.class, TickBroadcastCallback -> {
			this.CurrentTime = TickBroadcastCallback.getCurrentTime();
			if (TickBroadcastCallback.getCurrentTime() == TickBroadcastCallback.getDuration()) {
				System.out.println(getName()+" is being terminated");
				terminate();
			}
		});

		subscribeEvent(AcquireVehicleEvent.class, AcquireVehicleEventCallback -> {
			Future<DeliveryVehicle> deliveryVehicleFuture = resourcesHolder.acquireVehicle();
			while(!deliveryVehicleFuture.isDone());
			DeliveryVehicle deliveryVehicle = deliveryVehicleFuture.get();
			System.out.println(getName()+": the vehicle supplied is: " + deliveryVehicle.toString());
			complete(AcquireVehicleEventCallback, deliveryVehicle);
			System.out.println(getName()+" acquired a vehicle");
		});

		subscribeEvent(ReleaseVehicleEvent.class, ReleaseVehicleEventCallback -> {
			DeliveryVehicle deliveryVehicle = ReleaseVehicleEventCallback.getDeliveryVehicle();
			if (deliveryVehicle == null) {
				throw new NullPointerException("delivery vehicle is null!!!!!!1");
			}
			resourcesHolder.releaseVehicle(deliveryVehicle);
			System.out.println(getName()+" released a vehicle: "+ deliveryVehicle.toString());
			complete(ReleaseVehicleEventCallback, null);
		});

		/*
		subscribeEvent(AcquireVehicleEvent.class, AcquireVehicleEventCallBack -> {
			/*
			1. try to acquire a vehicle
				a. successful?
					i. send a delivery event
					ii. push delivery event to onGoingDeliveryQueue
					iii. perform the function releaseVehicles once. - if onGoing... .size() == 0 then return true.
				b. unsuccessful?
					i. perform releaseVehicles until it returns "true" (e.g. released one or more vehicles.
					ii. return to (1).
			 */

		/*
			Future<DeliveryVehicle> deliveryVehicleFuture = resourcesHolder.acquireVehicle();
			boolean successful = false;
			while (!successful) {
				if (deliveryVehicleFuture.isDone()) { // a. successful
					DeliveryVehicle myDeliveryVehicle = deliveryVehicleFuture.get();
					DeliveryEvent deliveryEvent = new DeliveryEvent(myDeliveryVehicle,
							AcquireVehicleEventCallBack.getAddress(),
							AcquireVehicleEventCallBack.getDistance());
					Future<Boolean> deliveryEventFuture = sendEvent(deliveryEvent); // i. send a delivery event
					onGoingDeliveryQueue.add(new WaitingDelivery(deliveryEventFuture, myDeliveryVehicle, AcquireVehicleEventCallBack)); // ii. push delivery event to onGoingDeliveryQueue
					releaseVehicles(); // iii. perform the function releaseVehicles once
					successful = true;
				} else { // b. unsuccessful
					while (!releaseVehicles()) ;
					/* explanation:
					if releaseVehicles == true, then there are free vehicles, and we return to 1 (e.g., the beginning of the while loop)
					if releaseVehicles == false, then there are no free vehicles, and we'll call this function again.
					 */
				}
			}
/*
			for (WaitingDelivery waitingDelivery: onGoingDeliveryQueue) {
				if (waitingDelivery.getFuture().isDone()) {
					complete(waitingDelivery.getAcquireVehicleEvent(), true);
					onGoingDeliveryQueue.remove(waitingDelivery);
					System.out.println(getName()+" just completed a DeliveryEvent");
				}
			}
		});
	}
	*/
/*
	private static class WaitingDelivery {
		private Future<Boolean> future;
		private DeliveryVehicle deliveryVehicle;
		private AcquireVehicleEvent acquireVehicleEvent;

		public WaitingDelivery(Future<Boolean> future, DeliveryVehicle deliveryVehicle, AcquireVehicleEvent acquireVehicleEvent) {
			this.future = future;
			this.deliveryVehicle = deliveryVehicle;
			this.acquireVehicleEvent = acquireVehicleEvent;
		}

		public Future<Boolean> getFuture() {
			return future;
		}

		public DeliveryVehicle getDeliveryVehicle() {
			return deliveryVehicle;
		}

		public AcquireVehicleEvent getAcquireVehicleEvent() {
			return acquireVehicleEvent;
		}
	}
/*
	private boolean releaseVehicles() {
		boolean isReleased = false;
		for (WaitingDelivery waitingDelivery : onGoingDeliveryQueue) {
			if (waitingDelivery.getFuture().isDone()) {
				resourcesHolder.releaseVehicle(waitingDelivery.getDeliveryVehicle());
				complete(waitingDelivery.getAcquireVehicleEvent(), true);
				isReleased = true;
			}
		}
		if (onGoingDeliveryQueue.size() == 0)
			return true;
		else
			return isReleased;
	}

}

/* NAIVE IMPLEMENTATION:

		1. should acquire the car
		2. send a DeliveryEvent, and wait until it is resolved
		3. return the car to the ResourcesHolder.
		4. complete the action by sending isDone = true;

		subscribeEvent(AcquireVehicleEvent.class, AcquireVehicleEventCallback -> {
			//1. acquiring a car
			Future<DeliveryVehicle> deliveryVehicleFuture = resourcesHolder.acquireVehicle();
			while (!deliveryVehicleFuture.isDone()) {
				System.out.println("I'm stuck in ResourceService, waiting for a car to be available");
			}

			DeliveryVehicle ourDeliveryVehicle = deliveryVehicleFuture.get();
			deliveryVehicleFuture.resolve(ourDeliveryVehicle); // putting the vehicle as the result of our future object.

			// 2. i. sending a delivery event
			DeliveryEvent deliveryEvent = new DeliveryEvent(ourDeliveryVehicle,
															AcquireVehicleEventCallback.getAddress(),
															AcquireVehicleEventCallback.getDistance());
			Future<Boolean> deliveryEventFuture = sendEvent(deliveryEvent);

			// 2. ii. wait till it's resolved
			while (!deliveryEventFuture.isDone()) {
				System.out.println("Im stuck again in ResourceService, waiting for the shipping to end");
			}

			// 3. return the car to the resourceHolder
			resourcesHolder.releaseVehicle(ourDeliveryVehicle);

			// 4. complete the action by sending true
			complete(AcquireVehicleEventCallback, true);
		});

*/

