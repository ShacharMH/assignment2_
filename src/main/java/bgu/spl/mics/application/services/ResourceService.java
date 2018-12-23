package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.concurrent.CountDownLatch;

//
/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link //ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link //MoneyRegister}, {@link //Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class ResourceService extends MicroService{
	private CountDownLatch countDownLatch;
	private ResourcesHolder resourcesHolder;
	private int CurrentTime;
	public ResourceService(String name,CountDownLatch countDownLatch) {
		super(name);
		resourcesHolder = ResourcesHolder.getInstance();
		this.countDownLatch=countDownLatch;
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

		countDownLatch.countDown();
				}
			}