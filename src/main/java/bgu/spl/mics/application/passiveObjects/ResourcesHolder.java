package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import javafx.util.Pair;

import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
//Amir
//There is a fixed amount of cars-supplied in the json file input.
public class ResourcesHolder {
	private BlockingQueue<DeliveryVehicle> listOfCars;


private static class HolderOfResourceHolder{
	private static ResourcesHolder List=new ResourcesHolder();
}
	private ResourcesHolder(){
		//listOfCars=inputFromJsonFile
	}
	/**
     * Retrieves the single instance of this class.
     */
	public static ResourcesHolder getInstance() {
		return HolderOfResourceHolder.List;
	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
			DeliveryVehicle result=null;
			try {
				result = listOfCars.take();//get a vehicle from queue
			}
			catch (InterruptedException e){}
			Future<DeliveryVehicle> ans=new Future<>();
			ans.resolve(result);
			return ans;

	}
	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		listOfCars.add(vehicle);
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		for (DeliveryVehicle v:vehicles){
			listOfCars.add(v);
	}}

}
