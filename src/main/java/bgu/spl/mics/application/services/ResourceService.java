package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

/* IS RESOURCE-HOLDER THREAD SAFE?
deals with getting the car to deliver the book.
1. should acquire the car
2. send a DeliveryEvent, and wait until it is resolved
3. return the car to the ResourcesHolder.
4. complete the action by sending isDone = true;
 */
public class ResourceService extends MicroService{

	private ResourcesHolder resourcesHolder;

	public ResourceService() {
		super("ResourceService");
		resourcesHolder = ResourcesHolder.getInstance();
	}

	@Override
	protected void initialize() {

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
		
	}

}
